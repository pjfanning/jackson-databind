package tools.jackson.databind.introspect;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.annotation.*;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests to verify functioning of {@link PropertyNamingStrategy}.
 */
@SuppressWarnings("serial")
public class TestNamingStrategyCustom extends DatabindTestUtil
{
    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

	static class PrefixStrategy extends PropertyNamingStrategy
    {
        @Override
        public String nameForField(MapperConfig<?> config,
                AnnotatedField field, String defaultName)
        {
            return "Field-"+defaultName;
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config,
                AnnotatedMethod method, String defaultName)
        {
            return "Get-"+defaultName;
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config,
                AnnotatedMethod method, String defaultName)
        {
            return "Set-"+defaultName;
        }
    }

    static class CStyleStrategy extends PropertyNamingStrategy
    {
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
        {
            return convert(defaultName);
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
        {
            return convert(defaultName);
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
        {
            return convert(defaultName);
        }

        private String convert(String input)
        {
            // easy: replace capital letters with underscore, lower-cases equivalent
            StringBuilder result = new StringBuilder();
            for (int i = 0, len = input.length(); i < len; ++i) {
                char c = input.charAt(i);
                if (Character.isUpperCase(c)) {
                    result.append('_');
                    c = Character.toLowerCase(c);
                }
                result.append(c);
            }
            return result.toString();
        }
    }

    static class GetterBean {
        public int getKey() { return 123; }
    }

    static class SetterBean {
        protected int value;

        public void setKey(int v) {
            value = v;
        }
    }

    static class FieldBean {
        public int key;

        public FieldBean() { this(0); }
        public FieldBean(int v) { key = v; }
    }

    @JsonPropertyOrder({"firstName", "lastName", "age"})
    static class PersonBean {
        public String firstName;
        public String lastName;
        public int age;

        public PersonBean() { this(null, null, 0); }
        public PersonBean(String f, String l, int a)
        {
            firstName = f;
            lastName = l;
            age = a;
        }
    }

    static class Value {
        public int intValue;

        public Value() { this(0); }
        public Value(int v) { intValue = v; }
    }

    static class SetterlessWithValue
    {
        protected ArrayList<Value> values = new ArrayList<Value>();

        public List<Value> getValueList() { return values; }

        public SetterlessWithValue add(int v) {
            values.add(new Value(v));
            return this;
        }
    }

    static class LcStrategy extends PropertyNamingStrategies.NamingBase
    {
        @Override
        public String translate(String propertyName) {
            return propertyName.toLowerCase();
        }
    }

    static class RenamedCollectionBean
    {
//        @JsonDeserialize
        @JsonProperty
        private List<String> theValues = Collections.emptyList();

        // intentionally odd name, to be renamed by naming strategy
        public List<String> getTheValues() { return theValues; }
    }

    // [Issue#45]: Support @JsonNaming
    @JsonNaming(PrefixStrategy.class)
    static class BeanWithPrefixNames
    {
        protected int a = 3;

        public int getA() { return a; }
        public void setA(int value) { a = value; }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    @Test
    public void testSimpleGetters() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new PrefixStrategy())
                .build();
        assertEquals("{\"Get-key\":123}", mapper.writeValueAsString(new GetterBean()));
    }

    @Test
    public void testSimpleSetters() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new PrefixStrategy())
                .build();
        SetterBean bean = mapper.readValue("{\"Set-key\":13}", SetterBean.class);
        assertEquals(13, bean.value);
    }

    @Test
    public void testSimpleFields() throws Exception
    {
        // First serialize
        ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new PrefixStrategy())
                .build();
        String json = mapper.writeValueAsString(new FieldBean(999));
        assertEquals("{\"Field-key\":999}", json);

        // then deserialize
        FieldBean result = mapper.readValue(json, FieldBean.class);
        assertEquals(999, result.key);
    }

    @Test
    public void testCStyleNaming() throws Exception
    {
        // First serialize
        ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new CStyleStrategy())
                .build();
        String json = mapper.writeValueAsString(new PersonBean("Joe", "Sixpack", 42));
        assertEquals("{\"first_name\":\"Joe\",\"last_name\":\"Sixpack\",\"age\":42}", json);

        // then deserialize
        PersonBean result = mapper.readValue(json, PersonBean.class);
        assertEquals("Joe", result.firstName);
        assertEquals("Sixpack", result.lastName);
        assertEquals(42, result.age);
    }

    @Test
    public void testWithGetterAsSetter() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .enable(MapperFeature.USE_GETTERS_AS_SETTERS)
                .propertyNamingStrategy(new CStyleStrategy())
                .build();
        SetterlessWithValue input = new SetterlessWithValue().add(3);
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"value_list\":[{\"int_value\":3}]}", json);

        SetterlessWithValue result = mapper.readValue(json, SetterlessWithValue.class);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals(3, result.values.get(0).intValue);
    }

    @Test
    public void testLowerCase() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new LcStrategy())
                .build();
//        mapper.disable(DeserializationConfig.DeserializationFeature.USE_GETTERS_AS_SETTERS);
        RenamedCollectionBean result = mapper.readValue("{\"thevalues\":[\"a\"]}",
                RenamedCollectionBean.class);
        assertNotNull(result.getTheValues());
        assertEquals(1, result.getTheValues().size());
        assertEquals("a", result.getTheValues().get(0));
    }

    // @JsonNaming / [databind#45]
    @Test
    public void testPerClassAnnotation() throws Exception
    {
        final ObjectMapper mapper = jsonMapperBuilder()
                .propertyNamingStrategy(new LcStrategy())
                .build();
        BeanWithPrefixNames input = new BeanWithPrefixNames();
        String json = mapper.writeValueAsString(input);
        assertEquals("{\"Get-a\":3}", json);

        BeanWithPrefixNames output = mapper.readValue("{\"Set-a\":7}",
                BeanWithPrefixNames.class);
        assertEquals(7, output.a);
    }
}

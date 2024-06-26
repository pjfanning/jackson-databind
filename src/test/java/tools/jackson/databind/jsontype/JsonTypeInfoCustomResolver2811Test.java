package tools.jackson.databind.jsontype;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTypeInfoCustomResolver2811Test extends DatabindTestUtil
{
    interface Vehicle { }

    static class Car implements Vehicle {
        public int wheels;
        public String color;
    }

    static class Bicycle implements Vehicle {
        public int wheels;
        public String bicycleType;
    }

    static class Person<T extends Vehicle> {
        public String name;
        public VehicleType vehicleType;

        @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "vehicleType", defaultImpl = Car.class
        )
        @JsonTypeIdResolver(VehicleTypeResolver.class)
        public T vehicle;
    }

    public enum VehicleType {
        CAR(Car.class),
        BICYCLE(Bicycle.class);

        public final Class<? extends Vehicle> vehicleClass;

        VehicleType(Class<? extends Vehicle> vehicleClass) {
            this.vehicleClass = vehicleClass;
        }
    }

    static class VehicleTypeResolver extends TypeIdResolverBase {
        private static final long serialVersionUID = 1L;

        JavaType superType;

        @Override
        public void init(JavaType bt) {
            this.superType = bt;
        }

        @Override
        public String idFromValue(DatabindContext ctxt, Object value) {
            return idFromValueAndType(ctxt, value, value.getClass());
        }

        @Override
        public String idFromValueAndType(DatabindContext ctxt,
                Object value, Class<?> suggestedType) {
            // only to be called for default type but...
            return suggestedType.getSimpleName().toUpperCase();
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            Class<? extends Vehicle> vehicleClass;
            try {
                vehicleClass = VehicleType.valueOf(id).vehicleClass;
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return context.constructSpecializedType(superType, vehicleClass);
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.NAME;
        }
    }

    private final ObjectMapper MAPPER = jsonMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
            .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
            .build();

    // [databind#2811]
    @Test
    public void testTypeInfoWithCustomResolver2811NoTypeId() throws Exception
    {
        String json = "{ \"name\": \"kamil\", \"vehicle\": {\"wheels\": 4, \"color\": \"red\"}}";
        Person<?> person = MAPPER.readValue(json, Person.class);
        assertEquals("kamil", person.name);
        assertNull(person.vehicleType);
        assertNotNull(person.vehicle);
        assertEquals(Car.class, person.vehicle.getClass());
    }

    // Passing case for comparison
    /*
    @Test
    public void testTypeInfoWithCustomResolver2811WithTypeId() throws Exception
    {
        String json = "{\n" +
                "  \"name\": \"kamil\",\n" +
                "  \"vehicleType\": \"CAR\",\n" +
                "  \"vehicle\": {\n" +
                "    \"wheels\": 4,\n" +
                "    \"color\": \"red\"\n" +
                "  }\n" +
                "}"
                ;
        Person<?> person = MAPPER.readValue(json, Person.class);
        assertEquals("kamil", person.name);
        assertEquals(VehicleType.CAR, person.vehicleType);
        assertNotNull(person.vehicle);
    }
    */
}

package tools.jackson.databind.mixins;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.introspect.MixInHandler;
import tools.jackson.databind.introspect.MixInResolver;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class TestMixinSerForMethods
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */

    // base class: just one visible property ('b')
    @SuppressWarnings("unused")
    static class BaseClass
    {
        private String a;
        private String b;

        protected BaseClass() { }

        public BaseClass(String a, String b) {
            this.a = a;
            this.b = b;
        }

        @JsonProperty("b")
        public String takeB() { return b; }
    }

    /* extends, just for fun; and to show possible benefit of being
     * able to declare that a method is overridden (compile-time check
     * that our intended mix-in override will match a method)
     */
    abstract static class MixIn
        extends BaseClass
    {
        // let's make 'a' visible
        @JsonProperty String a;

        @Override
        @JsonProperty("b2")
        public abstract String takeB();

        // also: just for fun; add a "red herring"... unmatched method
        @JsonProperty abstract String getFoobar();
    }

    static class LeafClass
        extends BaseClass
    {
        public LeafClass(String a, String b) { super(a, b); }

        @Override
        @JsonIgnore
        public String takeB() { return null; }
    }

    static class EmptyBean { }

    static class SimpleBean extends EmptyBean
    {
        int x() { return 42; }
    }

    /**
     * This mix-in is to be attached to EmptyBean, but really modify
     * methods that its subclass, SimpleBean, has.
     */
    abstract class MixInForSimple
    {
        // This should apply to sub-class
        @JsonProperty("x") abstract int x();

        // and this matches nothing, should be ignored
        @JsonProperty("notreally") public int xxx() { return 3; }

        // nor this
        public abstract int getIt();
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    /**
     * Unit test for verifying that leaf-level mix-ins work ok;
     * that is, any annotations added properly override all annotations
     * that masked methods (fields etc) have.
     */
    @Test
    public void testLeafMixin() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("a1", "b2");

        // first: with no mix-ins:
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("b2", result.get("b"));

        // then with leaf-level mix-in
        mapper = jsonMapperBuilder()
                .addMixIn(BaseClass.class, MixIn.class)
                .build();
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("b2", result.get("b2"));
        assertEquals("a1", result.get("a"));
    }

    /**
     * Unit test for verifying that having a mix-in "between" classes
     * (overriding annotations of a base class, but being overridden
     * further by a sub-class) works as expected
     */
    @Test
    public void testIntermediateMixin() throws IOException
    {
        Map<String,Object> result;
        LeafClass bean = new LeafClass("XXX", "b2");

        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(BaseClass.class, MixIn.class)
                .build();
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("XXX", result.get("a"));
    }

    /**
     * Another intermediate mix-in, to verify that annotations
     * properly "trickle up"
     */
    @Test
    public void testIntermediateMixin2() throws IOException
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(EmptyBean.class, MixInForSimple.class)
                .build();
        Map<String,Object> result = writeAndMap(mapper, new SimpleBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("x"));
    }

    @Test
    public void testSimpleMixInResolverHasMixins() {
        MixInHandler simple = new MixInHandler(null);
        assertFalse(simple.hasMixIns());
        simple.addLocalDefinition(String.class, Number.class);
        assertTrue(simple.hasMixIns());
    }

    // [databind#688]
    @Test
    public void testCustomResolver() throws IOException
    {
        final MixInResolver res = new MixInResolver() {
            @Override
            public Class<?> findMixInClassFor(Class<?> target) {
                if (target == EmptyBean.class) {
                    return MixInForSimple.class;
                }
                return null;
            }

            @Override
            public MixInResolver snapshot() {
                return this;
            }

            @Override
            public boolean hasMixIns() {
                return true;
            }
        };

        ObjectMapper mapper = jsonMapperBuilder()
                .mixInOverrides(res)
                .build();
        Map<String,Object> result = writeAndMap(mapper, new SimpleBean());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("x"));

        MixInHandler simple = new MixInHandler(res);
        assertTrue(simple.hasMixIns());
    }
}

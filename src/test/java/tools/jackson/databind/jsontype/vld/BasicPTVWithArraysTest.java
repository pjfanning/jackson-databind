package tools.jackson.databind.jsontype.vld;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests related to [databind#2534], support for configuring
 * {@link BasicPolymorphicTypeValidator} to allow all Array-valued
 * polymorphic values.
 */
public class BasicPTVWithArraysTest extends DatabindTestUtil
{
    static abstract class Base2534 {
        public int x = 3;
    }

    static class Good2534 extends Base2534 {
        protected Good2534() { }
        public Good2534(int x) {
            super();
            this.x = x;
        }
    }

    static class Bad2534 extends Base2534 {
        protected Bad2534() { }
        public Bad2534(int x) {
            super();
            this.x = x;
        }
    }

    static final class ObjectWrapper {
        public Object value;

        protected ObjectWrapper() { }
        public ObjectWrapper(Object v) { value = v; }
    }

    /*
    /**********************************************************************
    /* Test methods: structured types
    /**********************************************************************
     */

    // [databind#2534]: handle Java array-types appropriately wrt validation
    @Test
    public void testAllowBySubClassInArray() throws Exception {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Good2534.class)
                .build();
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                .build();

        final String json = mapper.writeValueAsString(new ObjectWrapper(new Base2534[] { new Good2534(42) }));

        // First test blocked case:
        try {
            mapper.readValue(json, ObjectWrapper.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "Could not resolve type id '[Ltools.jackson.");
            verifyException(e, "as a subtype of");
        }

        // and then accepted:
        ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubTypeIsArray() // key addition
                .allowIfSubType(Good2534.class)
                .build();
        mapper = jsonMapperBuilder()
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                .build();

        ObjectWrapper w = mapper.readValue(json, ObjectWrapper.class);
        assertNotNull(w);
        assertNotNull(w.value);
        assertEquals(Base2534[].class, w.value.getClass());
        Base2534[] arrayOut = (Base2534[]) w.value;
        assertEquals(1, arrayOut.length);
        assertEquals(42, arrayOut[0].x);

        // but ensure array-acceptance does NOT allow non-validated element types!
        final String badJson = mapper.writeValueAsString(new ObjectWrapper(new Base2534[] { new Bad2534(13) }));
        try {
            mapper.readValue(badJson, ObjectWrapper.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "Could not resolve type id 'tools.jackson.");
            verifyException(e, "$Bad2534");
            verifyException(e, "as a subtype of");
        }
    }
}

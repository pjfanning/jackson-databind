package tools.jackson.databind.jsontype;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.*;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.fail;

// for [databind#1735]:
public class GenericTypeId1735Test extends DatabindTestUtil
{
    static class Wrapper1735 {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
        public Payload1735 w;
    }

    static class Payload1735 {
        public void setValue(String str) { }
    }

    static class Nefarious1735 {
        public Nefarious1735() {
            throw new Error("Never call this constructor");
        }

        public void setValue(String str) {
            throw new Error("Never call this setter");
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newJsonMapper();

    private final static String NEF_CLASS = Nefarious1735.class.getName();

    // Existing checks should kick in fine
    @Test
    public void testSimpleTypeCheck1735() throws Exception
    {
        try {
            MAPPER.readValue(a2q(
"{'w':{'type':'"+NEF_CLASS+"'}}"),
                    Wrapper1735.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "could not resolve type id");
            verifyException(e, "not a subtype");
        }
    }

    // but this was not being verified early enough
    @Test
    public void testNestedTypeCheck1735() throws Exception
    {
        try {
            MAPPER.readValue(a2q(
"{'w':{'type':'java.util.HashMap<java.lang.String,java.lang.String>'}}"),
                    Wrapper1735.class);
            fail("Should not pass");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "could not resolve type id");
            verifyException(e, "not a subtype");
        }
    }
}

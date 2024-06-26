package tools.jackson.databind.convert;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.type.LogicalType;

import static org.junit.jupiter.api.Assertions.*;

import static tools.jackson.databind.testutil.DatabindTestUtil.*;

// [databind#3013] / PR #3608
public class CoerceIntToStringTest
{
    private final ObjectMapper DEFAULT_MAPPER = newJsonMapper();

    private final ObjectMapper MAPPER_TO_FAIL = jsonMapperBuilder()
            .withCoercionConfig(LogicalType.Textual, cfg ->
                    cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail))
            .build();

    private final ObjectMapper MAPPER_TRY_CONVERT = jsonMapperBuilder()
            .withCoercionConfig(LogicalType.Textual, cfg ->
                    cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.TryConvert))
            .build();

    private final ObjectMapper MAPPER_TO_NULL = jsonMapperBuilder()
            .withCoercionConfig(LogicalType.Textual, cfg ->
                    cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.AsNull))
            .build();

    private final ObjectMapper MAPPER_TO_EMPTY = jsonMapperBuilder()
            .withCoercionConfig(LogicalType.Textual, cfg ->
                    cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.AsEmpty))
            .build();

    @Test
    public void testDefaultIntToStringCoercion() throws Exception
    {
        assertSuccessfulIntToStringCoercionWith(DEFAULT_MAPPER);
    }

    @Test
    public void testCoerceConfigToConvert() throws Exception
    {
        assertSuccessfulIntToStringCoercionWith(MAPPER_TRY_CONVERT);
    }

    @Test
    public void testCoerceConfigToNull() throws Exception
    {
        assertNull(MAPPER_TO_NULL.readValue("1", String.class));
        StringWrapper w = MAPPER_TO_NULL.readValue("{\"str\": -5}", StringWrapper.class);
        assertNull(w.str);
        String[] arr = MAPPER_TO_NULL.readValue("[ 2 ]", String[].class);
        assertEquals(1, arr.length);
        assertNull(arr[0]);
    }

    @Test
    public void testCoerceConfigToEmpty() throws Exception
    {
        assertEquals("", MAPPER_TO_EMPTY.readValue("3", String.class));
        StringWrapper w = MAPPER_TO_EMPTY.readValue("{\"str\": -5}", StringWrapper.class);
        assertEquals("", w.str);
        String[] arr = MAPPER_TO_EMPTY.readValue("[ 2 ]", String[].class);
        assertEquals(1, arr.length);
        assertEquals("", arr[0]);
    }

    @Test
    public void testCoerceConfigToFail() throws Exception
    {
        _verifyCoerceFail(MAPPER_TO_FAIL, String.class, "3");
        _verifyCoerceFail(MAPPER_TO_FAIL, StringWrapper.class, "{\"str\": -5}", "string");
        _verifyCoerceFail(MAPPER_TO_FAIL, String[].class, "[ 2 ]", "to `java.lang.String` value");
    }

    /*
    /********************************************************
    /* Helper methods
    /********************************************************
     */

    private void assertSuccessfulIntToStringCoercionWith(ObjectMapper objectMapper)
    {
        assertEquals("3", objectMapper.readValue("3", String.class));
        assertEquals("-2", objectMapper.readValue("-2", String.class));
        {
            StringWrapper w = objectMapper.readValue("{\"str\": -5}", StringWrapper.class);
            assertEquals("-5", w.str);
            String[] arr = objectMapper.readValue("[ 2 ]", String[].class);
            assertEquals("2", arr[0]);
        }
    }

    private void _verifyCoerceFail(ObjectMapper m, Class<?> targetType,
                                   String doc)
    {
        _verifyCoerceFail(m.reader(), targetType, doc, targetType.getName());
    }

    private void _verifyCoerceFail(ObjectMapper m, Class<?> targetType,
                                   String doc, String targetTypeDesc)
    {
        _verifyCoerceFail(m.reader(), targetType, doc, targetTypeDesc);
    }

    private void _verifyCoerceFail(ObjectReader r, Class<?> targetType,
                                   String doc, String targetTypeDesc)
    {
        try {
            r.forType(targetType).readValue(doc);
            fail("Should not accept Integer for "+targetType.getName()+" when configured to");
        } catch (MismatchedInputException e) {
            verifyException(e, "Cannot coerce Integer");
            verifyException(e, targetTypeDesc);
        }
    }
}

package tools.jackson.databind.deser.dos;

import org.junit.jupiter.api.Test;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.fail;

import static tools.jackson.databind.testutil.DatabindTestUtil.ABC;
import static tools.jackson.databind.testutil.DatabindTestUtil.verifyException;

// for [databind#2157]
public class HugeIntegerCoerceTest
{
    private final static int BIG_NUM_LEN = 199999;
    private final static String BIG_POS_INTEGER;
    static {
        StringBuilder sb = new StringBuilder(BIG_NUM_LEN);
        for (int i = 0; i < BIG_NUM_LEN; ++i) {
            sb.append('9');
        }
        BIG_POS_INTEGER = sb.toString();
    }

    @Test
    public void testMaliciousLongForEnum() throws Exception
    {
        JsonFactory f = JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNumberLength(BIG_NUM_LEN + 10).build())
            .build();
        final ObjectMapper mapper = new JsonMapper(f);

        // Note: due to [jackson-core#488], fix verified with streaming over multiple
        // parser types. Here we focus on databind-level

        try {
            /*ABC value =*/ mapper.readValue(BIG_POS_INTEGER, ABC.class);
            fail("Should not pass");
        } catch (InputCoercionException e) {
            verifyException(e, "out of range of `int`");
            verifyException(e, "Integer with "+BIG_NUM_LEN+" digits");
        }
    }
}

package tools.jackson.databind.deser.creators;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.NoCheckSubTypeValidator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static tools.jackson.databind.testutil.DatabindTestUtil.jsonMapperBuilder;

// for [databind#1392] (regression in 2.7 due to separation of array-delegating creator)
public class ArrayDelegatorCreatorForCollectionTest
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    abstract static class UnmodifiableSetMixin {

        @JsonCreator
        public UnmodifiableSetMixin(Set<?> s) {}
    }

    @Test
    public void testUnmodifiable() throws Exception
    {
        Class<?> unmodSetType = Collections.unmodifiableSet(Collections.<String>emptySet()).getClass();
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .addMixIn(unmodSetType, UnmodifiableSetMixin.class)
                .build();
        final String EXPECTED_JSON = "[\""+unmodSetType.getName()+"\",[]]";
        Set<?> foo = mapper.readValue(EXPECTED_JSON, Set.class);
        assertTrue(foo.isEmpty());
    }
}

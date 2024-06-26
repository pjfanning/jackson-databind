package tools.jackson.databind.jsontype.deftyping;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonTypeResolver;
import tools.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultTypeResolverForLong2753Test extends DatabindTestUtil
{
    static class Data {
        private Long key;

        @JsonCreator
        Data(@JsonProperty("key") Long key) {
            this.key = key;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        @JsonTypeResolver(MyTypeResolverBuilder.class)
        public long key() {
            return key;
        }
    }

    static class MyTypeResolverBuilder extends StdTypeResolverBuilder {
        @Override
        protected boolean allowPrimitiveTypes(DatabindContext ctxt,
                JavaType baseType) {
            return true;
        }
    }

    @Test
    public void testDefaultTypingWithLong() throws Exception
    {
        Data data = new Data(1L);
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);

        // Configure Jackson to preserve types
//        StdTypeResolverBuilder resolver = new MyTypeResolverBuilder();
//        resolver.init(JsonTypeInfo.Id.CLASS, null);
//        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
//        resolver.typeProperty("__t");
        ObjectMapper mapper = jsonMapperBuilder()
//                .setDefaultTyping(resolver)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();

        // Serialize
        String json = mapper.writeValueAsString(mapData);
//System.err.println("JSON:\n"+json);
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}

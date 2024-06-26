package tools.jackson.databind.struct;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class PojoAsArray646Test extends DatabindTestUtil
{
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(alphabetic = true)
    static class Outer {
        protected Map<String, TheItem> attributes;

        public Outer() {
            attributes = new HashMap<String, TheItem>();
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
        public Map<String, TheItem> getAttributes() {
            return attributes;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(alphabetic = true)
    static class TheItem {

        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        @JsonPropertyOrder(alphabetic = true)
        public static class NestedItem {
            public String nestedStrValue;

            @JsonCreator
            public NestedItem(@JsonProperty("nestedStrValue") String nestedStrValue) {
                this.nestedStrValue = nestedStrValue;
            }
        }

        private String strValue;
        private boolean boolValue;
        private List<NestedItem> nestedItems;

        @JsonCreator
        public TheItem(@JsonProperty("strValue") String strValue, @JsonProperty("boolValue") boolean boolValue, @JsonProperty("nestedItems") List<NestedItem> nestedItems) {
            this.strValue = strValue;
            this.boolValue = boolValue;
            this.nestedItems = nestedItems;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }

        public boolean isBoolValue() {
            return boolValue;
        }

        public void setBoolValue(boolean boolValue) {
            this.boolValue = boolValue;
        }

        public List<NestedItem> getNestedItems() {
            return nestedItems;
        }

        public void setNestedItems(List<NestedItem> nestedItems) {
            this.nestedItems = nestedItems;
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newJsonMapper();

    @Test
    public void testWithCustomTypeId() throws Exception {

        List<TheItem.NestedItem> nestedList = new ArrayList<TheItem.NestedItem>();
        nestedList.add(new TheItem.NestedItem("foo1"));
        nestedList.add(new TheItem.NestedItem("foo2"));
        TheItem item = new TheItem("first", false, nestedList);
        Outer outer = new Outer();
        outer.getAttributes().put("entry1", item);

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(outer);

        Outer result = MAPPER.readValue(json, Outer.class);
        assertNotNull(result);
        assertNotNull(result.attributes);
        assertEquals(1, result.attributes.size());
    }
}

package tools.jackson.databind.views;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.testutil.DatabindTestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class JsonViewExternalTypeIdBypassTest extends DatabindTestUtil {
    public static class PublicView {}
    public static class AdminView extends PublicView {}

    public static abstract class Asset { public String name; }
    public static class PublicAsset extends Asset {}
    public static class AdminAsset extends Asset { public String secret; }

    public static class Container {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "kind")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = PublicAsset.class, name = "pub"),
                @JsonSubTypes.Type(value = AdminAsset.class,  name = "admin")
        })
        @JsonView(AdminView.class)
        public Asset asset;

        public String label;

        @JsonCreator
        public Container(
                @JsonProperty("label") String label,
                @JsonProperty("asset") @JsonView(AdminView.class) Asset asset) {
            this.label = label;
            this.asset = asset;
        }
    }

    public static class Wrapper {
        @JsonView(PublicView.class)
        public Container data;
    }

    @Test
    void testJsonViewExternalTypeIdBypass() throws Exception {
        // Admin-only "asset" should be blocked when reading with PublicView
        String json = a2q("{'data':{'label':'hello','kind':'admin',"
                + "'asset':{'name':'foo','secret':'LEAKED'}}}");

        ObjectMapper om = sharedMapper();
        Wrapper r = om.readerWithView(PublicView.class)
                .forType(Wrapper.class)
                .readValue(json);

        PublicAsset asset = assertInstanceOf(PublicAsset.class, r.data.asset);
        assertEquals("foo", asset.name);
    }
}

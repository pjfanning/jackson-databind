package tools.jackson.databind.deser.std;

import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.jsontype.TypeDeserializer;

/**
 * Convenience deserializer that may be used to deserialize values given an
 * intermediate tree representation ({@link JsonNode}).
 * Note that this is a slightly simplified alternative to {@link StdConvertingDeserializer}).
 *
 * @param <T> Target type of this deserializer; that is, type of values that
 *   input data is deserialized into.
 */
public abstract class StdNodeBasedDeserializer<T>
    extends StdDeserializer<T>
{
    protected ValueDeserializer<Object> _treeDeserializer;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected StdNodeBasedDeserializer(JavaType targetType) {
        super(targetType);
    }

    protected StdNodeBasedDeserializer(Class<T> targetType) {
        super(targetType);
    }

    /**
     * "Copy-constructor" used when creating a modified copies, most often
     * if sub-class overrides {@link tools.jackson.databind.ValueDeserializer#createContextual}.
     */
    protected StdNodeBasedDeserializer(StdNodeBasedDeserializer<?> src) {
        super(src);
        _treeDeserializer = src._treeDeserializer;
    }

    @Override
    public void resolve(DeserializationContext ctxt) {
        _treeDeserializer = ctxt.findRootValueDeserializer(ctxt.constructType(JsonNode.class));
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes
    /**********************************************************************
     */

    public abstract T convert(JsonNode root, DeserializationContext ctxt)
        throws JacksonException;

    /**
     * Facilitates usage with {@link ObjectMapper#readerForUpdating(Object)} and {@link #deserialize(JsonParser, DeserializationContext, Object)}
     * by eliminating the need to manually convert the value to a {@link JsonNode}.
     *
     * If this method is not overridden, it falls back to the behavior of {@link #convert(JsonNode, DeserializationContext)}.
     *
     * @since 2.15
     */
    public T convert(JsonNode root, DeserializationContext ctxt, T newValue) throws JacksonException {
        ctxt.handleBadMerge(this);
        return convert(root, ctxt);
    }

    /*
    /**********************************************************************
    /* ValueDeserializer impl
    /**********************************************************************
     */

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode n = (JsonNode) _treeDeserializer.deserialize(p, ctxt);
        return convert(n, ctxt);
    }

    /**
     *
     * Added to support {@link #convert(JsonNode, DeserializationContext, Object)}
     *
     * @since 2.15
     */
    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt, T newValue) throws JacksonException {
        JsonNode n = (JsonNode) _treeDeserializer.deserialize(jp, ctxt);
        return convert(n, ctxt, newValue);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt,
            TypeDeserializer td)
        throws JacksonException
    {
        // 19-Nov-2014, tatu: Quite likely we'd have some issues but... let's
        //   try, just in case.
        JsonNode n = (JsonNode) _treeDeserializer.deserializeWithType(p, ctxt, td);
        return convert(n, ctxt);
    }
}

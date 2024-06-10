package com.fasterxml.jackson.failing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TestSetterlessProperty
    extends DatabindTestUtil
{
    static class ImmutableId {
        private final int id;

        public ImmutableId(int id) { this.id = id; }

        public int getId() {
            return id;
        }
    }

    static class ImmutableIdWithEmptyConstuctor {
        private final int id;

        public ImmutableIdWithEmptyConstuctor() { this(-1); }

        public ImmutableIdWithEmptyConstuctor(int id) { this.id = id; }

        public int getId() {
            return id;
        }
    }

    static class ImmutableIdWithJsonCreatorAnnotation {
        private final int id;

        @JsonCreator
        public ImmutableIdWithJsonCreatorAnnotation(int id) { this.id = id; }

        public int getId() {
            return id;
        }
    }

    static class ImmutableIdWithJsonPropertyFieldAnnotation {
        @JsonProperty("id") private final int id;

        public ImmutableIdWithJsonPropertyFieldAnnotation(int id) { this.id = id; }

        public int getId() {
            return id;
        }
    }

    static class ImmutableIdWithJsonPropertyConstructorAnnotation {
        private final int id;

        public ImmutableIdWithJsonPropertyConstructorAnnotation(@JsonProperty("id") int id) { this.id = id; }

        public int getId() {
            return id;
        }
    }

    static class MyParamIntrospector extends JacksonAnnotationIntrospector
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String findImplicitPropertyName(AnnotatedMember param) {
            return "id";
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    @Test
    public void testSetterlessProperty() throws Exception
    {
        ImmutableId input = new ImmutableId(13);
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new MyParamIntrospector());
        String json = m.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        ImmutableId output = m.readValue(json, ImmutableId.class);
        assertNotNull(output);

        assertEquals(input.id, output.id);
    }

    // this passes - but with an extra (messy) constructor
    @Test
    public void testSetterlessPropertyWithEmptyConstructor() throws Exception
    {
        ImmutableIdWithEmptyConstuctor input = new ImmutableIdWithEmptyConstuctor(13);
        ObjectMapper m = new ObjectMapper();
        String json = m.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        ImmutableIdWithEmptyConstuctor output = m.readValue(json, ImmutableIdWithEmptyConstuctor.class);
        assertNotNull(output);

        assertEquals(input.id, output.id);
    }

    // this only passes with MyParamIntrospector
    // - the JsonCreator annotation only seems to work with MyParamIntrospector
    // - or presumably with jackson-module-parameter-names registered
    @Test
    public void testSetterlessPropertyWithJsonCreator() throws Exception
    {
        ImmutableIdWithJsonCreatorAnnotation input = new ImmutableIdWithJsonCreatorAnnotation(13);
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new MyParamIntrospector());
        String json = m.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        ImmutableIdWithJsonCreatorAnnotation output =
                m.readValue(json, ImmutableIdWithJsonCreatorAnnotation.class);
        assertNotNull(output);

        assertEquals(input.id, output.id);
    }

    // this passes - but needs an untidy JsonProperty annotation
    @Test
    public void testSetterlessPropertyWithJsonPropertyField() throws Exception
    {
        ImmutableIdWithJsonPropertyConstructorAnnotation input = new ImmutableIdWithJsonPropertyConstructorAnnotation(13);
        ObjectMapper m = new ObjectMapper();
        String json = m.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        ImmutableIdWithJsonPropertyConstructorAnnotation output =
                m.readValue(json, ImmutableIdWithJsonPropertyConstructorAnnotation.class);
        assertNotNull(output);

        assertEquals(input.id, output.id);
    }

    // this still fails - despite the JsonProperty annotation
    @Test
    public void testSetterlessPropertyWithJsonPropertyConstructor() throws Exception
    {
        ImmutableIdWithJsonPropertyFieldAnnotation input = new ImmutableIdWithJsonPropertyFieldAnnotation(13);
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new MyParamIntrospector());
        String json = m.writerWithDefaultPrettyPrinter().writeValueAsString(input);

        ImmutableIdWithJsonPropertyFieldAnnotation output =
                m.readValue(json, ImmutableIdWithJsonPropertyFieldAnnotation.class);
        assertNotNull(output);

        assertEquals(input.id, output.id);
    }
}

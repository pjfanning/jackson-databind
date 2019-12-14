package com.fasterxml.jackson.databind.deser.vavr;

import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vavr.control.Either;
import org.junit.Test;

public class TestVavrEither extends BaseMapTest {

    @Test
    public void testDeser() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Either<?, ?>.class, new EitherDeserializer());
        mapper.registerModule();
    }
}

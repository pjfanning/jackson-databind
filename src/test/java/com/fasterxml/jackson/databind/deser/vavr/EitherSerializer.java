/*  __    __  __  __    __  ___
 * \  \  /  /    \  \  /  /  __/
 *  \  \/  /  /\  \  \/  /  /
 *   \____/__/  \__\____/__/
 *
 * Copyright 2014-2017 Vavr, http://vavr.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fasterxml.jackson.databind.deser.vavr;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.vavr.control.Either;

import java.io.IOException;

class EitherSerializer extends HListSerializer<Either<?, ?>> {

    private static final long serialVersionUID = 1L;

    EitherSerializer(JavaType type) {
        super(type);
    }

    @Override
    public void serialize(Either<?, ?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        if (value.isLeft()) {
            gen.writeString("left");
            write(value.getLeft(), 0, gen, provider);
        } else {
            gen.writeString("right");
            write(value.get(), 1, gen, provider);
        }
        gen.writeEndArray();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Either<?, ?> value) {
        return value.isEmpty();
    }
}

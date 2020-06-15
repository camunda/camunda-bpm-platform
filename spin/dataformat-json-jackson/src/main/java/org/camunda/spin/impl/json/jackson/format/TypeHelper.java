/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.spin.impl.json.jackson.format;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

/**
 * Collection of helper methods to construct types.
 */
public class TypeHelper {

    /**
     * Checks if the erased type has the correct number of type bindings.
     *
     * @param erasedType                  class of the type.
     * @param expectedTypeParametersCount expected number of bindings.
     *
     * @return true if the number of type binding matches expected value.
     */
    static boolean bindingsArePresent(Class<?> erasedType, int expectedTypeParametersCount) {
        if (erasedType == null) {
            return false;
        }
        TypeVariable<? extends Class<?>>[] typeParameters = erasedType.getTypeParameters();
        if (typeParameters.length == 0) {
            return false;
        }
        if (typeParameters.length != expectedTypeParametersCount) {
            throw new IllegalArgumentException("Cannot create TypeBindings for class " + 
                erasedType.getName() + " with " + expectedTypeParametersCount + 
                " type parameter: class expects " + typeParameters.length + " type parameters.");
        }
        return true;
    }

    /**
     * Constructs Java type based on the content values.
     *
     * @param value value with values.
     *
     * @return Java type.
     */
    static JavaType constructType(Object value) {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        if (value instanceof Collection<?> && !((Collection<?>) value).isEmpty()) {
            Object firstElement = ((Collection<?>) value).iterator().next();
            if (bindingsArePresent(value.getClass(), 1)) {
                JavaType elementType = constructType(firstElement);
                return typeFactory.constructCollectionType(guessCollectionType(value), elementType);
            }
        } else if (value instanceof Map<?, ?> && !((Map<?, ?>) value).isEmpty()) {
            Map.Entry<?, ?> firstEntry = ((Map<?, ?>) value).entrySet().iterator().next();
            if (bindingsArePresent(firstEntry.getClass(), 2)) {
                JavaType keyType = constructType(firstEntry.getKey());
                JavaType valueType = constructType(firstEntry.getValue());
                return typeFactory.constructMapType(Map.class, keyType, valueType);
            }
        }
        return typeFactory.constructType(value.getClass());
    }

    /**
     * Guess collection class.
     *
     * @param value collection.
     *
     * @return class of th collection implementation.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Class<? extends Collection> guessCollectionType(Object value) {
        if (value instanceof Collection<?>) {
            return (Class<? extends Collection>) value.getClass();
        } else {
            throw new IllegalArgumentException("Could not detect class for " + value
                + " of type " + value.getClass().getName());
        }
    }

}

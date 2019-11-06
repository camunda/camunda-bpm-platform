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

import java.lang.reflect.TypeVariable;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MapJacksonJsonTypeDetector extends AbstractJacksonJsonTypeDetector {

  public boolean canHandle(Object object) {
    return object instanceof Map;
  }

  public String detectType(Object object) {
    return constructType(object).toCanonical();
  }

  protected JavaType constructType(Object object) {
    TypeFactory typeFactory = TypeFactory.defaultInstance();

    if (object instanceof Map && !((Map<?, ?>) object).isEmpty()) {
      Map<?, ?> map = (Map<?, ?>) object;
      Object keyElement = map.keySet().iterator().next();
      Object valueElement = map.get(keyElement);
      if (bindingsArePresent(map.getClass())) {
        final JavaType keyType = constructType(keyElement);
        final JavaType valueType = constructType(valueElement);
        return typeFactory.constructMapType(map.getClass(), keyType, valueType);
      }
    }
    return typeFactory.constructType(object.getClass());
  }

  private boolean bindingsArePresent(Class<?> erasedType) {
    TypeVariable<?>[] vars = erasedType.getTypeParameters();
    int varLen = (vars == null) ? 0 : vars.length;
    if (varLen == 0) {
      return false;
    }
    if (varLen != 2) {
      throw new IllegalArgumentException("Cannot create TypeBindings for class " + erasedType.getName() + " with 2 type parameter: class expects " + varLen);
    }
    return true;
  }

}

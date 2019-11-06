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
import java.util.Collection;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CollectionJacksonJsonTypeDetector extends AbstractJacksonJsonTypeDetector {

  public boolean canHandle(Object object) {
    return object instanceof Collection;
  }

  public String detectType(Object object) {
    return constructType(object).toCanonical();
  }

  protected JavaType constructType(Object object) {
    TypeFactory typeFactory = TypeFactory.defaultInstance();

    if (object instanceof Collection && !((Collection<?>) object).isEmpty()) {
      Collection<?> collection = (Collection<?>) object;
      Object firstElement = collection.iterator().next();
      if (bindingsArePresent(collection.getClass())) {
        final JavaType elementType = constructType(firstElement);
        return typeFactory.constructCollectionType(collection.getClass(), elementType);
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
    if (varLen != 1) {
      throw new IllegalArgumentException("Cannot create TypeBindings for class " + erasedType.getName() + " with 1 type parameter: class expects " + varLen);
    }
    return true;
  }

}

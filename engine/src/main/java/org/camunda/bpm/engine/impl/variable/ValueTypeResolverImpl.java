/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.variable;

import static org.camunda.bpm.engine.variable.type.ValueType.BOOLEAN;
import static org.camunda.bpm.engine.variable.type.ValueType.BYTES;
import static org.camunda.bpm.engine.variable.type.ValueType.DATE;
import static org.camunda.bpm.engine.variable.type.ValueType.DOUBLE;
import static org.camunda.bpm.engine.variable.type.ValueType.FILE;
import static org.camunda.bpm.engine.variable.type.ValueType.INTEGER;
import static org.camunda.bpm.engine.variable.type.ValueType.LONG;
import static org.camunda.bpm.engine.variable.type.ValueType.NULL;
import static org.camunda.bpm.engine.variable.type.ValueType.NUMBER;
import static org.camunda.bpm.engine.variable.type.ValueType.OBJECT;
import static org.camunda.bpm.engine.variable.type.ValueType.SHORT;
import static org.camunda.bpm.engine.variable.type.ValueType.STRING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;

/**
 * Resolves ValueType by name.
 *
 * @author Daniel Meyer
 *
 */
public class ValueTypeResolverImpl implements ValueTypeResolver {

  protected Map<String, ValueType> knownTypes = new HashMap<String, ValueType>();

  public ValueTypeResolverImpl() {
    addType(BOOLEAN);
    addType(BYTES);
    addType(DATE);
    addType(DOUBLE);
    addType(INTEGER);
    addType(LONG);
    addType(NULL);
    addType(SHORT);
    addType(STRING);
    addType(OBJECT);
    addType(NUMBER);
    addType(FILE);
  }

  public void addType(ValueType type) {
    knownTypes.put(type.getName(), type);
  }

  public ValueType typeForName(String typeName) {
    return knownTypes.get(typeName);
  }

  public Collection<ValueType> getSubTypes(ValueType type) {
    List<ValueType> types = new ArrayList<ValueType>();

    Set<ValueType> validParents = new HashSet<ValueType>();
    validParents.add(type);

    for (ValueType knownType : knownTypes.values()) {
      if (validParents.contains(knownType.getParent())) {
        validParents.add(knownType);

        if (!knownType.isAbstract()) {
          types.add(knownType);
        }
      }
    }

    return types;
  }

}

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
package org.camunda.bpm.engine.impl.variable.serializer;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class DeserializedObject {

  protected AbstractObjectValueSerializer serializer;
  Object deserializedObject;
  byte[] originalBytes;
  VariableInstanceEntity variableInstanceEntity;

  public DeserializedObject(AbstractObjectValueSerializer serializer, Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    this.serializer = serializer;
    this.deserializedObject = deserializedObject;
    this.originalBytes = serializedBytes;
    this.variableInstanceEntity = variableInstanceEntity;
  }

  public void flush() {
    // this first check verifies if the variable value was not overwritten with another object
    TypedValue cachedValue = variableInstanceEntity.getCachedValue();
    if (cachedValue != null && deserializedObject == cachedValue.getValue()) {
      try {
        byte[] bytes = serializer.serializeToByteArray(deserializedObject);
        if(!Arrays.equals(originalBytes, bytes)) {
          variableInstanceEntity
            .getByteArrayValue()
            .setBytes(bytes);
        }
      }
      catch (Exception e) {
        throw new ProcessEngineException("Exception while serializing object: "+e.getMessage(), e);
      }
    }
  }

}

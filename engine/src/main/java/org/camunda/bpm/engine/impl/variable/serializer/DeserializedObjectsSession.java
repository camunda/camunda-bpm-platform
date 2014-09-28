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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Daniel Meyer
 *
 */
public class DeserializedObjectsSession implements Session {

  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();

  protected void addDeserializedObject(DeserializedObject deserializedObject) {
    deserializedObjects.add(deserializedObject);
  }

  public void addDeserializedObject(AbstractObjectValueSerializer serializer, Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    addDeserializedObject(new DeserializedObject(serializer, deserializedObject, serializedBytes, variableInstanceEntity));
  }

  public void flush() {
    for (DeserializedObject deserializedObject : deserializedObjects) {
      deserializedObject.flush();
    }
  }

  public void close() {
    deserializedObjects = null;
  }

}

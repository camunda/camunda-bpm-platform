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
package org.camunda.bpm.engine.impl.core.variable;

import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.delegate.SerializedVariableValueBuilder;

/**
 * Default Implementation of SerializedVariableValueBuilder
 *
 * @author Daniel Meyer
 *
 */
public class SerializedVariableValueBuilderImpl extends SerializedVariableValueBuilder {

  protected SerializedVariableValueImpl serializedValue;

  public SerializedVariableValueBuilderImpl() {
    serializedValue = new SerializedVariableValueImpl();
  }

  public SerializedVariableValueBuilder value(Object value) {
    this.serializedValue.setValue(value);
    return this;
  }

  public SerializedVariableValueBuilder configValue(String key, Object value) {
    this.serializedValue.setConfigValue(key, value);
    return this;
  }

  public SerializedVariableValueBuilder config(Map<String, Object> cfg) {
    if(cfg != null) {
      this.serializedValue.setConfig(cfg);
    }
    return this;
  }

  public SerializedVariableValue done() {
    return serializedValue;
  }

}

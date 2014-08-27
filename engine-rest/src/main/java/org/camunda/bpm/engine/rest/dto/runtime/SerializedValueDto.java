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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class SerializedValueDto {

  protected Object value;
  protected Map<String, Object> configuration;

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }

  public static SerializedValueDto fromSerializedVariableValue(SerializedVariableValue serializedValue) {
    SerializedValueDto dto = new SerializedValueDto();
    dto.value = serializedValue.getValue();
    dto.configuration = serializedValue.getConfig();
    return dto;
  }
}

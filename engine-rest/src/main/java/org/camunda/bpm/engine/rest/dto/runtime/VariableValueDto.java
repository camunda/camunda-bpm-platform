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

import org.camunda.bpm.engine.rest.util.DtoUtil;

/**
 * @author: drobisch
 */
public class VariableValueDto {

  protected Object value;
  protected String type;
  protected String variableType;
  protected Map<String, Object> serializationConfig;

  public VariableValueDto() {
  }

  public VariableValueDto(Object value, String type) {
    this.value = value;
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVariableType() {
    return variableType;
  }

  public void setVariableType(String variableType) {
    this.variableType = variableType;
  }

  public Map<String, Object> getSerializationConfig() {
    return serializationConfig;
  }

  public void setSerializationConfig(Map<String, Object> serializationConfig) {
    this.serializationConfig = serializationConfig;
  }

  /**
   * Decide whether the variable update represented by this object
   * is a primitive value update (i.e. can be handled by setVariable(..) methods)
   * or whether it is an update from a serialized value (i.e. should be handled by
   * setVariableFromSerialized(..) methods);
   *
   * We have a primitive update if either the value type or the variable type
   * match the primitive variable types, or if both or null (which is a fallback
   * for backwards compatibility)
   */
  public boolean isPrimitiveVariableUpdate() {
    return DtoUtil.handledByPrimitivePlainTextType(type) ||
        DtoUtil.handledByPrimitivePlainTextType(variableType) ||
        (type == null && variableType == null);
  }

  public boolean isSerializedVariableUpdate() {
    return !DtoUtil.handledByPrimitivePlainTextType(type) &&
        !DtoUtil.handledByPrimitivePlainTextType(variableType) &&
        variableType != null;
  }
}

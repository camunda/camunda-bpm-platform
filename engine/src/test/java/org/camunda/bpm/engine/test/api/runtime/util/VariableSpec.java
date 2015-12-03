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
package org.camunda.bpm.engine.test.api.runtime.util;

import java.util.Map;

public class VariableSpec {

  protected String name;
  protected Object value;
  protected String variableTypeName;
  protected String valueTypeName;
  protected Object serializedValue;
  protected Boolean storesCustomObjects;
  protected Map<String, Object> configuration;

  public String getName() {
    return name;
  }
  public VariableSpec name(String name) {
    this.name = name;
    return this;
  }
  public Object getValue() {
    return value;
  }
  public VariableSpec value(Object value) {
    this.value = value;
    return this;
  }
  public String getVariableTypeName() {
    return variableTypeName;
  }
  public VariableSpec variableTypeName(String variableTypeName) {
    this.variableTypeName = variableTypeName;
    return this;
  }
  public String getValueTypeName() {
    return valueTypeName;
  }
  public VariableSpec valueTypeName(String valueTypeName) {
    this.valueTypeName = valueTypeName;
    return this;
  }
  public Object getSerializedValue() {
    return serializedValue;
  }
  public VariableSpec serializedValue(Object serializedValue) {
    this.serializedValue = serializedValue;
    return this;
  }
  public Map<String, Object> getConfiguration() {
    return configuration;
  }
  public VariableSpec configuration(Map<String, Object> configuration) {
    this.configuration = configuration;
    return this;
  }
  public boolean getStoresCustomObjects() {
    return storesCustomObjects;
  }
  public VariableSpec storesCustomObjects(boolean storesCustomObjects) {
    this.storesCustomObjects = storesCustomObjects;
    return this;
  }


}

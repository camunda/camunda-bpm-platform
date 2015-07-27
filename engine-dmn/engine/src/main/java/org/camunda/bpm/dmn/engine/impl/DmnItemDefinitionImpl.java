/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnTypeDefinition;

public class DmnItemDefinitionImpl extends DmnElementImpl implements DmnItemDefinition {

  protected DmnTypeDefinition typeDefinition;
  protected List<DmnExpression> allowedValues = new ArrayList<DmnExpression>();

  public DmnTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public void setTypeDefinition(DmnTypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  public List<DmnExpression> getAllowedValues() {
    return allowedValues;
  }

  public void setAllowedValues(List<DmnExpression> allowedValues) {
    this.allowedValues = allowedValues;
  }

  public void addAllowedValue(DmnExpression allowedValue) {
    allowedValues.add(allowedValue);
  }

  public String toString() {
    return "DmnItemDefinitionImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", typeDefinition=" + typeDefinition +
      ", allowedValues=" + allowedValues +
      '}';
  }

}

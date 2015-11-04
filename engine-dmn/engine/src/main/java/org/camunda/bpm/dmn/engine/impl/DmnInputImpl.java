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

import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnTypeDefinition;

public class DmnInputImpl extends DmnElementImpl implements DmnInput {

  public static final String DEFAULT_INPUT_VARIABLE_NAME = "cellInput";

  protected DmnExpression inputExpression;
  protected String inputVariable;

  protected DmnTypeDefinition typeDefinition = new DefaultTypeDefinition();

  public DmnExpression getInputExpression() {
    return inputExpression;
  }

  public void setInputExpression(DmnExpression inputExpression) {
    this.inputExpression = inputExpression;
  }

  public String getInputVariable() {
    return inputVariable;
  }

  public void setInputVariable(String inputVariable) {
    this.inputVariable = inputVariable;
  }

  public DmnTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public void setTypeDefinition(DmnTypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  @Override
  public String toString() {
    return "DmnInputImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", inputExpression=" + inputExpression +
      ", inputVariable='" + inputVariable + '\'' +
      ", typeDefinition=" + typeDefinition +
      '}';
  }

}

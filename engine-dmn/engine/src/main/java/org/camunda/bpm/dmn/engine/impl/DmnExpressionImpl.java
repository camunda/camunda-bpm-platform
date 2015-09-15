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
import org.camunda.bpm.dmn.engine.DmnItemDefinition;

public class DmnExpressionImpl extends DmnElementImpl implements DmnExpression {

  protected String expressionLanguage;
  protected String expression;
  protected DmnItemDefinition itemDefinition;

  public DmnExpressionImpl() {
  }

  @Override
  public void setKey(String id) {
    this.key = id;
  }

  @Override
  public String getKey() {
    return key;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpressionLanguage(String expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  public String getExpressionLanguage() {
    return expressionLanguage;
  }

  public DmnItemDefinition getItemDefinition() {
    return itemDefinition;
  }

  public void setItemDefinition(DmnItemDefinition itemDefinition) {
    this.itemDefinition = itemDefinition;
  }

  @Override
  public String toString() {
    return "DmnExpressionImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", expressionLanguage='" + expressionLanguage + '\'' +
      ", expression='" + expression + '\'' +
      ", itemDefinition='" + itemDefinition + '\'' +
      '}';
  }

}

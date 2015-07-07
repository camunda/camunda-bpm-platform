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

package org.camunda.dmn.engine.impl.transform;

import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnTransformContext;

public class DmnLiteralExpressionHandler implements DmnElementHandler<LiteralExpression, DmnExpression> {

  public DmnExpression handleElement(DmnTransformContext context, LiteralExpression expression) {
    DmnExpressionImpl dmnExpression = new DmnExpressionImpl();
    dmnExpression.setId(expression.getId());
    String expressionLanguage = expression.getExpressionLanguage();
    if (expressionLanguage != null) {
      dmnExpression.setExpressionLanguage(expressionLanguage.trim());
    }
    Text text = expression.getText();
    if (text != null) {
      String textContent = text.getTextContent();
      if (textContent != null) {
        dmnExpression.setExpression(textContent.trim());
      }
    }
    return dmnExpression;
  }

}

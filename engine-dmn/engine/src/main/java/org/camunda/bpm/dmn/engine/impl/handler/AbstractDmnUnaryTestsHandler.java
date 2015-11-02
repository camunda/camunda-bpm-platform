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

package org.camunda.bpm.dmn.engine.impl.handler;

import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.bpm.model.dmn.instance.UnaryTests;

public abstract class AbstractDmnUnaryTestsHandler<U extends UnaryTests, I extends DmnExpressionImpl> extends AbstractDmnElementHandler<U, I> {

  @Override
  @SuppressWarnings("unchecked")
  protected I createElement(DmnElementHandlerContext context, U unaryTests) {
    return (I) new DmnExpressionImpl();
  }

  @Override
  protected void initElement(DmnElementHandlerContext context, U unaryTests, I dmnExpression) {
    super.initElement(context, unaryTests, dmnExpression);
    initExpressionLanguage(context, unaryTests, dmnExpression);
    initExpression(context, unaryTests, dmnExpression);
  }

  protected void initExpressionLanguage(DmnElementHandlerContext context, U unaryTests, DmnExpressionImpl dmnExpression) {
    String expressionLanguage = unaryTests.getExpressionLanguage();
    String globalExpressionLanguage = context.getDecisionModel().getExpressionLanguage();
    if (expressionLanguage != null) {
      dmnExpression.setExpressionLanguage(expressionLanguage.trim());
    }
    else if (globalExpressionLanguage != null) {
      dmnExpression.setExpressionLanguage(globalExpressionLanguage);
    }
  }

  protected void initExpression(DmnElementHandlerContext context, U unaryTests, DmnExpressionImpl dmnExpression) {
    Text text = unaryTests.getText();
    if (text != null) {
      String textContent = text.getTextContent();
      if (textContent != null && !textContent.trim().isEmpty()) {
        dmnExpression.setExpression(textContent.trim());
      }
    }
    postProcessExpressionText(context, unaryTests, dmnExpression);
  }

  protected void postProcessExpressionText(DmnElementHandlerContext context, U unaryTests, DmnExpressionImpl dmnExpression) {
    // do nothing
  }

}

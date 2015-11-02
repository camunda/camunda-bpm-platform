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

package org.camunda.bpm.dmn.feel.impl;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.camunda.bpm.dmn.feel.FeelConvertException;
import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.dmn.feel.FeelMethodInvocationException;
import org.camunda.bpm.dmn.feel.FeelMissingFunctionException;
import org.camunda.bpm.dmn.feel.FeelMissingVariableException;
import org.camunda.bpm.dmn.feel.impl.el.ElContextFactory;
import org.camunda.bpm.dmn.feel.impl.transform.FeelToJuelTransform;
import org.camunda.bpm.engine.variable.context.VariableContext;

public class FeelEngineImpl implements FeelEngine {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected FeelToJuelTransform transform;
  protected ExpressionFactory expressionFactory;
  protected ElContextFactory elContextFactory;

  public FeelEngineImpl(FeelToJuelTransform transform, ExpressionFactory expressionFactory, ElContextFactory elContextFactory) {
    this.transform = transform;
    this.expressionFactory = expressionFactory;
    this.elContextFactory = elContextFactory;
  }

  public <T> T evaluateSimpleExpression(String simpleExpression, VariableContext variableContext) {
    throw LOG.simpleExpressionNotSupported();
  }

  public boolean evaluateSimpleUnaryTests(String simpleUnaryTests, String inputName, VariableContext varCtx) {
    try {
      ELContext elContext = createContext(varCtx);
      ValueExpression valueExpression = transformSimpleUnaryTests(simpleUnaryTests, inputName, elContext);
      return (Boolean) valueExpression.getValue(elContext);
    }
    catch (FeelMissingFunctionException e) {
      throw LOG.unknownFunction(simpleUnaryTests, e);
    }
    catch (FeelMissingVariableException e) {
      if (inputName.equals(e.getVariable())) {
        throw LOG.unableToEvaluateExpressionAsNotInputIsSet(simpleUnaryTests, e);
      }
      else {
        throw LOG.unknownVariable(simpleUnaryTests, e);
      }
    }
    catch (FeelConvertException e) {
      throw LOG.unableToConvertValue(simpleUnaryTests, e);
    }
    catch (ELException e) {
      if (e.getCause() instanceof FeelMethodInvocationException) {
        throw LOG.unableToInvokeMethod(simpleUnaryTests, (FeelMethodInvocationException) e.getCause());
      }
      else {
        throw LOG.unableToEvaluateExpression(simpleUnaryTests, e);
      }
    }
  }

  protected ELContext createContext(VariableContext varCtx) {
    return elContextFactory.createContext(expressionFactory, varCtx);
  }

  protected ValueExpression transformSimpleUnaryTests(String simpleUnaryTests, String inputName, ELContext elContext) {
    String juelExpression = transform.transformSimpleUnaryTests(simpleUnaryTests, inputName);
    try {
      return expressionFactory.createValueExpression(elContext, juelExpression, Object.class);
    }
    catch (ELException e) {
      throw LOG.invalidExpression(simpleUnaryTests, e);
    }
  }

}

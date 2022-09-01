/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.dmn.feel.impl.juel;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;

import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.juel.el.ElContextFactory;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransform;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.commons.utils.cache.Cache;

public class FeelEngineImpl implements FeelEngine {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected FeelToJuelTransform transform;
  protected ExpressionFactory expressionFactory;
  protected ElContextFactory elContextFactory;
  protected Cache<TransformExpressionCacheKey, String> transformExpressionCache;

  public FeelEngineImpl(FeelToJuelTransform transform, ExpressionFactory expressionFactory, ElContextFactory elContextFactory,
      Cache<TransformExpressionCacheKey, String> transformExpressionCache) {
    this.transform = transform;
    this.expressionFactory = expressionFactory;
    this.elContextFactory = elContextFactory;
    this.transformExpressionCache = transformExpressionCache;
  }

  public <T> T evaluateSimpleExpression(String simpleExpression, VariableContext variableContext) {
    throw LOG.simpleExpressionNotSupported();
  }

  public boolean evaluateSimpleUnaryTests(String simpleUnaryTests, String inputName, VariableContext variableContext) {
    try {
      ELContext elContext = createContext(variableContext);
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

  protected ELContext createContext(VariableContext variableContext) {
    return elContextFactory.createContext(expressionFactory, variableContext);
  }

  protected ValueExpression transformSimpleUnaryTests(String simpleUnaryTests, String inputName, ELContext elContext) {

    String juelExpression = transformToJuelExpression(simpleUnaryTests, inputName);

    try {
      return expressionFactory.createValueExpression(elContext, juelExpression, Object.class);
    }
    catch (ELException e) {
      throw LOG.invalidExpression(simpleUnaryTests, e);
    }
  }

  protected String transformToJuelExpression(String simpleUnaryTests, String inputName) {

    TransformExpressionCacheKey cacheKey = new TransformExpressionCacheKey(simpleUnaryTests, inputName);
    String juelExpression = transformExpressionCache.get(cacheKey);

    if (juelExpression == null) {
      juelExpression = transform.transformSimpleUnaryTests(simpleUnaryTests, inputName);
      transformExpressionCache.put(cacheKey, juelExpression);
    }
    return juelExpression;
  }

}

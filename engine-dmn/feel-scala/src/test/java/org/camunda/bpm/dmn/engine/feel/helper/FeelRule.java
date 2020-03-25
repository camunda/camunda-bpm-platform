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
package org.camunda.bpm.dmn.engine.feel.helper;

import org.camunda.bpm.dmn.engine.feel.function.helper.FunctionProvider;
import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelEngine;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.Collections;
import java.util.List;

public class FeelRule extends TestWatcher {

  protected FunctionProvider functionProvider;
  protected ScalaFeelEngine feelEngine;

  protected FeelRule(FunctionProvider functionProvider) {
    this.functionProvider = functionProvider;
  }

  protected FeelRule() {
    feelEngine = new ScalaFeelEngine(null);
  }

  public static FeelRule buildWithFunctionProvider() {
    FunctionProvider functionProvider = new FunctionProvider();
    return new FeelRule(functionProvider);
  }

  public static FeelRule build() {
    return new FeelRule();
  }

  @Override
  protected void finished(Description description) {
    super.finished(description);

    if (functionProvider != null) {
      functionProvider.clear();
    }
  }

  public <T> T evaluateExpression(String expression) {
    return evaluateExpression(expression, null);
  }

  public <T> T evaluateExpression(String expression, Object value) {
    if (functionProvider != null) {
      List<FeelCustomFunctionProvider> functionProviders =
        Collections.singletonList(functionProvider);

      feelEngine = new ScalaFeelEngine(functionProviders);
    }

    VariableContext variableCtx = Variables.putValue("variable", value).asVariableContext();
    return feelEngine.evaluateSimpleExpression(expression, variableCtx);
  }

  public FunctionProvider getFunctionProvider() {
    return functionProvider;
  }

}

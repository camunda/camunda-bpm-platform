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
package org.camunda.bpm.engine.impl.cmmn.handler;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessOrCaseTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.core.model.CallableElementParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ProcessOrCaseTaskItemHandler extends CallingTaskItemHandler {

  protected CallableElement createCallableElement() {
    return new CallableElement();
  }

  protected void initializeCallableElement(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    super.initializeCallableElement(element, activity, context);

    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    // inputs
    initializeInputParameter(element, activity, context, callableElement);

    // outputs
    initializeOutputParameter(element, activity, context, callableElement);
  }

  protected void initializeInputParameter(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();

    List<CamundaIn> inputs = getInputs(element);

    for (CamundaIn input : inputs) {

      // businessKey
      String businessKey = input.getCamundaBusinessKey();
      if (businessKey != null && !businessKey.isEmpty()) {
        ParameterValueProvider businessKeyValueProvider = createParameterValueProvider(businessKey, expressionManager);
        callableElement.setBusinessKeyValueProvider(businessKeyValueProvider);

      } else {
        // create new parameter
        CallableElementParameter parameter = new CallableElementParameter();
        callableElement.addInput(parameter);

        if (input.getCamundaLocal()) {
          parameter.setReadLocal(true);
        }

        // all variables
        String variables = input.getCamundaVariables();
        if ("all".equals(variables)) {
          parameter.setAllVariables(true);
          continue;
        }

        // source/sourceExpression
        String source = input.getCamundaSource();
        if (source == null || source.isEmpty()) {
          source = input.getCamundaSourceExpression();
        }

        ParameterValueProvider sourceValueProvider = createParameterValueProvider(source, expressionManager);
        parameter.setSourceValueProvider(sourceValueProvider);

        // target
        String target = input.getCamundaTarget();
        parameter.setTarget(target);
      }
    }
  }

  protected void initializeOutputParameter(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();

    List<CamundaOut> outputs = getOutputs(element);

    for (CamundaOut output : outputs) {

      // create new parameter
      CallableElementParameter parameter = new CallableElementParameter();
      callableElement.addOutput(parameter);

      // all variables
      String variables = output.getCamundaVariables();
      if ("all".equals(variables)) {
        parameter.setAllVariables(true);
        continue;
      }

      // source/sourceExpression
      String source = output.getCamundaSource();
      if (source == null || source.isEmpty()) {
        source = output.getCamundaSourceExpression();
      }

      ParameterValueProvider sourceValueProvider = createParameterValueProvider(source, expressionManager);
      parameter.setSourceValueProvider(sourceValueProvider);

      // target
      String target = output.getCamundaTarget();
      parameter.setTarget(target);

    }
  }

  protected List<CamundaIn> getInputs(CmmnElement element) {
    PlanItemDefinition definition = getDefinition(element);
    return queryExtensionElementsByClass(definition, CamundaIn.class);
  }

  protected List<CamundaOut> getOutputs(CmmnElement element) {
    PlanItemDefinition definition = getDefinition(element);
    return queryExtensionElementsByClass(definition, CamundaOut.class);
  }
}

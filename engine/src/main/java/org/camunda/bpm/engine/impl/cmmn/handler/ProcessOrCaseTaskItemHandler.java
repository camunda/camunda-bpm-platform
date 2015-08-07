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

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessOrCaseTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.core.model.CallableElementParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.NullValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ProcessOrCaseTaskItemHandler extends TaskItemHandler {

  protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    super.initializeActivity(element, activity, context);

    initializeCallableElement(element, activity, context);
  }

  protected void initializeCallableElement(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    Deployment deployment = context.getDeployment();
    String deploymentId = null;
    if (deployment != null) {
      deploymentId = deployment.getId();
    }

    CallableElement callableElement = new CallableElement();
    callableElement.setDeploymentId(deploymentId);

    // set callableElement on behavior
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    behavior.setCallableElement(callableElement);

    // definition key
    initializeDefinitionKey(element, activity, context, callableElement);

    // binding
    initializeBinding(element, activity, context, callableElement);

    // version
    initializeVersion(element, activity, context, callableElement);

    // inputs
    initializeInputParameter(element, activity, context);

    // outputs
    initializeOutputParameter(element, activity, context);
  }

  protected void initializeDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String definitionKey = getDefinitionKey(element, activity, context);
    ParameterValueProvider definitionKeyProvider = createParameterValueProvider(definitionKey, expressionManager);
    callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);
  }

  protected void initializeBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    String binding = getBinding(element, activity, context);

    if (CallableElementBinding.DEPLOYMENT.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.DEPLOYMENT);
    } else if (CallableElementBinding.LATEST.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.LATEST);
    } else if (CallableElementBinding.VERSION.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.VERSION);
    }
  }

  protected void initializeVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String version = getVersion(element, activity, context);
    ParameterValueProvider versionProvider = createParameterValueProvider(version, expressionManager);
    callableElement.setVersionValueProvider(versionProvider);
  }

  protected void initializeInputParameter(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

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

  protected void initializeOutputParameter(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

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

  protected ParameterValueProvider createParameterValueProvider(String value, ExpressionManager expressionManager) {
    if (value == null) {
      return new NullValueProvider();

    } else if (StringUtil.isExpression(value)) {
      Expression expression = expressionManager.createExpression(value);
      return new ElValueProvider(expression);

    } else {
      return new ConstantValueProvider(value);
    }
  }

  protected abstract String getDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

}

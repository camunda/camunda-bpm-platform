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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElementParameter;
import org.camunda.bpm.engine.impl.cmmn.behavior.ProcessOrCaseTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.mapping.value.NullValueProvider;
import org.camunda.bpm.engine.impl.core.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.model.cmmn.Query;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ProcessOrCaseTaskPlanItemHandler extends TaskPlanItemHandler {

  protected void initializeActivity(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    super.initializeActivity(planItem, activity, context);

    initializeCallableElement(planItem, activity, context);
  }

  protected void initializeCallableElement(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    CallableElement callableElement = new CallableElement();

    // set callableElement on behavior
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    behavior.setCallableElement(callableElement);

    // definition key
    initializeDefinitionKey(planItem, activity, context, callableElement);

    // binding
    initializeBinding(planItem, activity, context, callableElement);

    // version
    initializeVersion(planItem, activity, context, callableElement);

    // inputs
    initializeInputParameter(planItem, activity, context);

    // outputs
    initializeOutputParameter(planItem, activity, context);
  }

  protected void initializeDefinitionKey(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String definitionKey = getDefinitionKey(planItem, activity, context);
    ParameterValueProvider definitionKeyProvider = createParameterValueProvider(definitionKey, expressionManager);
    callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);
  }

  protected void initializeBinding(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    String binding = getBinding(planItem, activity, context);

    if (CallableElementBinding.DEPLOYMENT.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.DEPLOYMENT);
    } else if (CallableElementBinding.LATEST.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.LATEST);
    } else if (CallableElementBinding.VERSION.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.VERSION);
    }
  }

  protected void initializeVersion(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context, CallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String version = getVersion(planItem, activity, context);
    ParameterValueProvider versionProvider = createParameterValueProvider(version, expressionManager);
    callableElement.setVersionValueProvider(versionProvider);
  }

  protected void initializeInputParameter(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ExpressionManager expressionManager = context.getExpressionManager();

    List<CamundaIn> inputs = getInputs(planItem);

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

  protected void initializeOutputParameter(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    ProcessOrCaseTaskActivityBehavior behavior = (ProcessOrCaseTaskActivityBehavior) activity.getActivityBehavior();
    CallableElement callableElement = behavior.getCallableElement();

    ExpressionManager expressionManager = context.getExpressionManager();

    List<CamundaOut> outputs = getOutputs(planItem);

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

  protected List<CamundaIn> getInputs(PlanItem planItem) {
    return getParameter(planItem, CamundaIn.class);
  }

  protected List<CamundaOut> getOutputs(PlanItem planItem) {
    return getParameter(planItem, CamundaOut.class);
  }

  protected <T extends ModelElementInstance> List<T> getParameter(PlanItem planItem, Class<T> cls) {
    PlanItemDefinition definition = getDefinition(planItem);
    ExtensionElements extensionElements = definition.getExtensionElements();

    if (extensionElements != null) {
      Query<ModelElementInstance> query = extensionElements.getElementsQuery();
      return query.filterByType(cls).list();

    } else {
      return new ArrayList<T>();
    }
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

  protected abstract String getDefinitionKey(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getBinding(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getVersion(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context);

}

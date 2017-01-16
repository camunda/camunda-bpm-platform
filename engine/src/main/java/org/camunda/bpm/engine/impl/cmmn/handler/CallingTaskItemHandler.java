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

import org.camunda.bpm.engine.impl.cmmn.behavior.CallingTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.DefaultCallableElementTenantIdProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.NullValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

import static org.camunda.bpm.engine.impl.util.StringUtil.isCompositeExpression;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CallingTaskItemHandler extends TaskItemHandler {

  @Override
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

    BaseCallableElement callableElement = createCallableElement();
    callableElement.setDeploymentId(deploymentId);

    // set callableElement on behavior
    CallingTaskActivityBehavior behavior = (CallingTaskActivityBehavior) activity.getActivityBehavior();
    behavior.setCallableElement(callableElement);

    // definition key
    initializeDefinitionKey(element, activity, context, callableElement);

    // binding
    initializeBinding(element, activity, context, callableElement);

    // version
    initializeVersion(element, activity, context, callableElement);

    // tenant-id
    initializeTenantId(element, activity, context, callableElement);
  }

  protected void initializeDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, BaseCallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String definitionKey = getDefinitionKey(element, activity, context);
    ParameterValueProvider definitionKeyProvider = createParameterValueProvider(definitionKey, expressionManager);
    callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);
  }

  protected void initializeBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, BaseCallableElement callableElement) {
    String binding = getBinding(element, activity, context);

    if (CallableElementBinding.DEPLOYMENT.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.DEPLOYMENT);
    } else if (CallableElementBinding.LATEST.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.LATEST);
    } else if (CallableElementBinding.VERSION.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.VERSION);
    }
  }

  protected void initializeVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, BaseCallableElement callableElement) {
    ExpressionManager expressionManager = context.getExpressionManager();
    String version = getVersion(element, activity, context);
    ParameterValueProvider versionProvider = createParameterValueProvider(version, expressionManager);
    callableElement.setVersionValueProvider(versionProvider);
  }

  protected void initializeTenantId(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, BaseCallableElement callableElement) {
    ParameterValueProvider tenantIdProvider;

    ExpressionManager expressionManager = context.getExpressionManager();
    String tenantId = getTenantId(element, activity, context);
    if (tenantId != null && tenantId.length() > 0) {
      tenantIdProvider = createParameterValueProvider(tenantId, expressionManager);
    } else {
      tenantIdProvider = new DefaultCallableElementTenantIdProvider();
    }

    callableElement.setTenantIdProvider(tenantIdProvider);
  }

  protected ParameterValueProvider createParameterValueProvider(String value, ExpressionManager expressionManager) {
    if (value == null) {
      return new NullValueProvider();

    } else if (isCompositeExpression(value, expressionManager)) {
      Expression expression = expressionManager.createExpression(value);
      return new ElValueProvider(expression);

    } else {
      return new ConstantValueProvider(value);
    }
  }

  protected abstract BaseCallableElement createCallableElement();

  protected abstract String getDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

  protected abstract String getTenantId(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context);

}

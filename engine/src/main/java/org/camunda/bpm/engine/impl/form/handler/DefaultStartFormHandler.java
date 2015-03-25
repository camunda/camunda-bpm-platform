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

package org.camunda.bpm.engine.impl.form.handler;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.form.StartFormDataImpl;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.variable.VariableMap;


/**
 * @author Tom Baeyens
 */
public class DefaultStartFormHandler extends DefaultFormHandler implements StartFormHandler {

  protected Expression formKey;

  @Override
  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    super.parseConfiguration(activityElement, deployment, processDefinition, bpmnParse);

    ExpressionManager expressionManager = Context
        .getProcessEngineConfiguration()
        .getExpressionManager();

    String formKeyAttribute = activityElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formKey");

    if (formKeyAttribute != null) {
      this.formKey = expressionManager.createExpression(formKeyAttribute);
    }

    if (formKey != null) {
      processDefinition.setStartFormKey(true);
    }
  }

  public StartFormData createStartFormData(final ProcessDefinitionEntity processDefinition) {

    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication(processDefinition.getDeploymentId());

    if(targetProcessApplication != null) {

      return Context.executeWithinProcessApplication(new Callable<StartFormData>() {

        public StartFormData call() throws Exception {
          return createStartFormDataInternal(processDefinition);
        }

      }, targetProcessApplication);
    }
    else {
      return createStartFormDataInternal(processDefinition);
    }

  }

  protected StartFormData createStartFormDataInternal(ProcessDefinitionEntity processDefinition) {
    StartFormDataImpl startFormData = new StartFormDataImpl();

    if (formKey != null) {
      startFormData.setFormKey(formKey.getExpressionText());
    }
    startFormData.setDeploymentId(deploymentId);
    startFormData.setProcessDefinition(processDefinition);
    initializeFormProperties(startFormData, null);
    initializeFormFields(startFormData, null);
    return startFormData;
  }

  public ExecutionEntity submitStartFormData(ExecutionEntity processInstance, VariableMap properties) {
    submitFormVariables(properties, processInstance);
    return processInstance;
  }

  // getters //////////////////////////////////////////////

  public Expression getFormKey() {
    return formKey;
  }
}

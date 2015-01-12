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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.parser.DataAssociation;
import org.camunda.bpm.engine.impl.cmmn.behavior.CallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 *
 * @author Joram Barrez
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  protected String processDefinitionKey;
  protected String binding;
  protected Integer version;
  protected List<DataAssociation> dataInputAssociations = new ArrayList<DataAssociation>();
  protected List<DataAssociation> dataOutputAssociations = new ArrayList<DataAssociation>();
  protected Expression processDefinitionExpression;

  public enum CalledElementBinding {
    LATEST("latest"),
    DEPLOYMENT("deployment"),
    VERSION("version");

    private String value;

    private CalledElementBinding(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

  }

  public CallActivityBehavior(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public CallActivityBehavior(Expression processDefinitionExpression) {
    super();
    this.processDefinitionExpression = processDefinitionExpression;
  }

  public CallActivityBehavior(String processDefinitionKey, String binding, Integer version) {
    this.processDefinitionKey = processDefinitionKey;
    this.binding = binding;
    this.version = version;
  }

  public CallActivityBehavior(Expression processDefinitionExpression, String binding, Integer version) {
    this.processDefinitionExpression = processDefinitionExpression;
    this.binding = binding;
    this.version = version;
  }

  // behavior //////////////////////////////////////////////////////////////////////////////////////////

  public void execute(ActivityExecution execution) throws Exception {
    Map<String, Object> variables = getVariables(dataInputAssociations, execution);
    String businessKey = getBusinessKey(dataInputAssociations, execution);

    startInstance(execution, variables, businessKey);
  }

  protected void startInstance(ActivityExecution execution, Map<String, Object> variables, String businessKey) {
    String processDefinitionKey = getProcessDefinitionKey(execution);

    DeploymentCache deploymentCache = getDeploymentCache();

    ProcessDefinitionImpl processDefinition = null;

    if (isLatestBinding()) {
      processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);

    } else if (isDeploymentBinding()) {
      String deploymentId = getDeploymentId(execution);
      processDefinition = deploymentCache.findDeployedProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);

    } else if (isVersionBinding()) {
      processDefinition = deploymentCache.findDeployedProcessDefinitionByKeyAndVersion(processDefinitionKey, version);
    }

    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition, businessKey);
    subProcessInstance.start(variables);
  }

  public void completing(VariableScope execution, VariableScope subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.
    Map<String, Object> variables = getVariables(dataOutputAssociations, subProcessInstance);
    execution.setVariables(variables);
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }

  // getter // setter /////////////////////////////////////////////////////////////////////////////

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public Expression getProcessDefinitionExpression() {
    return processDefinitionExpression;
  }

  public void setProcessDefinitionExpression(Expression processDefinitionExpression) {
    this.processDefinitionExpression = processDefinitionExpression;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getBinding() {
    return binding;
  }

  public void setBinding(String binding) {
    this.binding = binding;
  }

  public void addDataInputAssociation(DataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  public List<DataAssociation> getDataInputAssociations() {
    return dataInputAssociations;
  }

  public void setDataInputAssociations(List<DataAssociation> dataInputAssociations) {
    this.dataInputAssociations = dataInputAssociations;
  }

  public void addDataOutputAssociation(DataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  public List<DataAssociation> getDataOutputAssociations() {
    return dataOutputAssociations;
  }

  public void setDataOutputAssociations(List<DataAssociation> dataOutputAssociations) {
    this.dataOutputAssociations = dataOutputAssociations;
  }

  // helper ////////////////////////////////////////////////////////////////////////////////

  protected boolean isLatestBinding() {
    String binding = getBinding();
    return binding == null || CallableElementBinding.LATEST.getValue().equals(binding);
  }

  protected boolean isDeploymentBinding() {
    String binding = getBinding();
    return CallableElementBinding.DEPLOYMENT.getValue().equals(binding);
  }

  protected boolean isVersionBinding() {
    String binding = getBinding();
    return CallableElementBinding.VERSION.getValue().equals(binding);
  }

  protected String getProcessDefinitionKey(VariableScope variableScope) {
    if (processDefinitionExpression != null) {
      return (String) processDefinitionExpression.getValue(variableScope);
    }
    return processDefinitionKey;
  }

  protected DeploymentCache getDeploymentCache() {
    return Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();
  }

  protected String getDeploymentId(ActivityExecution execution) {
    PvmExecutionImpl exec = (PvmExecutionImpl) execution;
    ProcessDefinitionImpl definition = exec.getProcessDefinition();
    return definition.getDeploymentId();
  }

  protected Map<String, Object> getVariables(List<DataAssociation> params, VariableScope variableScope) {
    Map<String, Object> result = new HashMap<String, Object>();

    for (DataAssociation param : params) {

      // ignore business key
      if (param.getBusinessKeyExpression() == null) {


        if (param.getVariables() != null) {
          Map<String, Object> allVariables = variableScope.getVariables();
          result.putAll(allVariables);

        } else {

          String targetVariableName = param.getTarget();
          Object value = null;
          Expression sourceExpression = param.getSourceExpression();

          if (sourceExpression != null) {
            value = sourceExpression.getValue(variableScope);

          } else {
            String source = param.getSource();
            value = variableScope.getVariable(source);
          }

          result.put(targetVariableName, value);
        }
      }
    }

    return result;
  }

  protected String getBusinessKey(List<DataAssociation> params, VariableScope variableScope) {

    String result = null;

    for (DataAssociation param : params) {

      Expression businessKeyExpression = param.getBusinessKeyExpression();
      if (businessKeyExpression != null) {
        result = (String) businessKeyExpression.getValue(variableScope);
      }

    }

    return result;
  }
}

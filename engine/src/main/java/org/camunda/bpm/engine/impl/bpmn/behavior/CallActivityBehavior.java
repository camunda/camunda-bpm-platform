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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.data.AbstractDataAssociation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {
  
  protected String processDefinitonKey;
  private List<AbstractDataAssociation> dataInputAssociations = new ArrayList<AbstractDataAssociation>();
  private List<AbstractDataAssociation> dataOutputAssociations = new ArrayList<AbstractDataAssociation>();
  private Expression processDefinitionExpression;

  public CallActivityBehavior(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  public CallActivityBehavior(Expression processDefinitionExpression) {
    super();
    this.processDefinitionExpression = processDefinitionExpression;
  }

  public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  public void addDataOutputAssociation(AbstractDataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  public void execute(ActivityExecution execution) throws Exception {
    
	String processDefinitonKey = this.processDefinitonKey;
    if (processDefinitionExpression != null) {
      processDefinitonKey = (String) processDefinitionExpression.getValue(execution);
    }
    
    ProcessDefinitionImpl processDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition);

    // copy process variables / businessKey
    String businessKey = null;
    for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
      Object value = null;
        if (dataInputAssociation.getBusinessKeyExpression() != null) {
          businessKey = (String) dataInputAssociation.getBusinessKeyExpression().getValue(execution);
        }
        else if (dataInputAssociation.getVariables() != null) {
          Map<String, Object> variables = execution.getVariables();
          if (variables != null && !variables.isEmpty()) {
            Set<String> variableKeys = variables.keySet();
            for (String variableKey : variableKeys) {
              subProcessInstance.setVariable(variableKey, variables.get(variableKey));
            }
          }
        }
        else if (dataInputAssociation.getSourceExpression()!=null) {
          value = dataInputAssociation.getSourceExpression().getValue(execution);
        }
        else {
          value = execution.getVariable(dataInputAssociation.getSource());
        }

        if (value != null) {
          subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
        }
    }

    if (businessKey != null) {
      subProcessInstance.start(businessKey);
    } else {
      subProcessInstance.start();
    }
  }
  
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.

    // copy process variables
    for (AbstractDataAssociation dataOutputAssociation : dataOutputAssociations) {
      Object value = null;
        if (dataOutputAssociation.getVariables() != null) {
          Map<String, Object> variables = execution.getVariables();
          if (variables != null && !variables.isEmpty()) {
            execution.setVariables(subProcessInstance.getVariables());
          }
        }
        else if (dataOutputAssociation.getSourceExpression()!=null) {
          value = dataOutputAssociation.getSourceExpression().getValue(subProcessInstance);
        }
        else {
          value = subProcessInstance.getVariable(dataOutputAssociation.getSource());
        }

        if (value != null) {
          execution.setVariable(dataOutputAssociation.getTarget(), value);
        }
    }
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }
}

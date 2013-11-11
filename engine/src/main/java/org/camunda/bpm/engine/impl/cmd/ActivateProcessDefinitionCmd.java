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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Date;

import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public class ActivateProcessDefinitionCmd extends AbstractSetProcessDefinitionStateCmd {

  public ActivateProcessDefinitionCmd(ProcessDefinitionEntity processDefinitionEntity,
          boolean includeProcessInstances, Date executionDate) {
    super(processDefinitionEntity, includeProcessInstances, executionDate);
  }

  public ActivateProcessDefinitionCmd(String processDefinitionId, String processDefinitionKey,
          boolean includeProcessInstances, Date executionDate) {
    super(processDefinitionId, processDefinitionKey, includeProcessInstances, executionDate);
  }

  protected SuspensionState getProcessDefinitionSuspensionState() {
    return SuspensionState.ACTIVE;
  }

  protected String getDelayedExecutionJobHandlerType() {
    return TimerActivateProcessDefinitionHandler.TYPE;
  }

  protected AbstractSetJobDefinitionStateCmd getSetJobDefinitionStateCmd() {
    return new ActivateJobDefinitionCmd(null, processDefinitionId, processDefinitionKey, false, null);
  }

  protected AbstractSetProcessInstanceStateCmd getSetProcessInstanceStateCmd() {
    return new ActivateProcessInstanceCmd(null, processDefinitionId, processDefinitionKey);
  }

}

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

package org.activiti.engine.impl.history.handler;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 * 
 * BE AWARE: For Start Events this is dine in the ProcessDefinitionEntity!
 */
public class ActivityInstanceStartHandler implements ExecutionListener {

  public void notify(DelegateExecution execution) {
    IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
    
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    String processDefinitionId = executionEntity.getProcessDefinitionId();
    String processInstanceId = executionEntity.getProcessInstanceId();
    String executionId = execution.getId();

    HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
    historicActivityInstance.setId(idGenerator.getNextId());
    historicActivityInstance.setProcessDefinitionId(processDefinitionId);
    historicActivityInstance.setProcessInstanceId(processInstanceId);
    historicActivityInstance.setExecutionId(executionId);
    historicActivityInstance.setActivityId(executionEntity.getActivityId());
    historicActivityInstance.setActivityName((String) executionEntity.getActivity().getProperty("name"));
    historicActivityInstance.setActivityType((String) executionEntity.getActivity().getProperty("type"));
    historicActivityInstance.setStartTime(ClockUtil.getCurrentTime());
    
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(historicActivityInstance);
  }
}

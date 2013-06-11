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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Daniel Meyer
 *
 */
public class GetActivityInstanceCmd implements Command<ActivityInstance> {

  protected String processInstanceId;

  /**
   * @param processInstanceId
   */
  public GetActivityInstanceCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ActivityInstance execute(CommandContext commandContext) {
    
    if(processInstanceId == null) {
      throw new ProcessEngineException("processInstanceId cannot be null");
    }
    
    List<ExecutionEntity> executionList = (List) new ExecutionQueryImpl(commandContext)
      .processInstanceId(processInstanceId)
      .list();
    
    ExecutionEntity processInstance = null;
    
    // find process instance && index executions by parentActivityInstanceId
    Map<String, List<ExecutionEntity>> executionsByParentActIds = new HashMap<String, List<ExecutionEntity>>();
    for (ExecutionEntity executionEntity : executionList) {
      if(executionEntity.isProcessInstance()) {
        processInstance = executionEntity;
      }
      String parentActivityInstanceId = executionEntity.getParentActivityInstanceId();
      List<ExecutionEntity> exeForThisParentActInst = executionsByParentActIds.get(parentActivityInstanceId);
      if(exeForThisParentActInst == null) {
        exeForThisParentActInst = new ArrayList<ExecutionEntity>();
        executionsByParentActIds.put(parentActivityInstanceId, exeForThisParentActInst);
      }
      exeForThisParentActInst.add(executionEntity);                   
    }
        
    // create act instance for process instance
    ActivityInstanceImpl processActInst = new ActivityInstanceImpl();                
    processActInst.setActivityId(processInstance.getProcessDefinitionId());
    processActInst.setBusinessKey(processInstance.getBusinessKey());
    processActInst.setId(processInstanceId);
    processActInst.getExecutionIds().add(processInstanceId);
    
    initActivityInstanceTree(processActInst, executionsByParentActIds);
    
    return processActInst;
  }

  protected void initActivityInstanceTree(ActivityInstance parentActInst, Map<String, List<ExecutionEntity>> executionsByParentActIds) {

    Map<String, ActivityInstanceImpl> childInstances = new HashMap<String, ActivityInstanceImpl>();
    List<ExecutionEntity> childExecutions = executionsByParentActIds.get(parentActInst.getId());

    if(childExecutions == null) {
      return;
    }
    
    for (ExecutionEntity execution : childExecutions) {
      if (!isConcurrentRoot(execution) && !execution.getActivityInstanceId().equals(parentActInst.getId())) {

        ActivityInstance activityInstance = childInstances.get(execution.getActivityInstanceId());
        if (activityInstance != null) {
          // instance already created -> add executionId
          activityInstance.getExecutionIds().add(execution.getId());

        } else {
          // create new activity instance
          ActivityInstanceImpl actInstance = new ActivityInstanceImpl();
          ScopeImpl activity = getActivity(execution);

          actInstance.setActivityId(activity.getId());
          actInstance.setBusinessKey(execution.getBusinessKey());
          actInstance.setId(execution.getActivityInstanceId());
          actInstance.setParentActivityInstanceId(parentActInst.getId());
          actInstance.getExecutionIds().add(execution.getId());

          childInstances.put(actInstance.getId(), actInstance);

        }
      }
    }

    parentActInst.getChildInstances().addAll(childInstances.values());
    for (ActivityInstance childActInstance : parentActInst.getChildInstances()) {
      initActivityInstanceTree(childActInstance, executionsByParentActIds);
    }

  }

  /** returns true if execution is concurrent root. */
  protected boolean isConcurrentRoot(ExecutionEntity execution) {
    List<ExecutionEntity> executions = execution.getExecutions();
    return execution.isScope() && !executions.isEmpty() && executions.get(0).isConcurrent();

  }

  protected ScopeImpl getActivity(ExecutionEntity executionEntity) {
    if(executionEntity.getActivityId() != null) {
      return executionEntity.getActivity();
      
    } else {
      int i = 0;
      while(!executionEntity.getExecutions().isEmpty()) {
        ExecutionEntity childExecution = executionEntity.getExecutions().get(0); 
        if(executionEntity.isScope() && !childExecution.getActivityInstanceId().equals(executionEntity.getActivityInstanceId())) {
          i++;
        }
        executionEntity = childExecution;
      }
      ActivityImpl scope = executionEntity.getActivity();
      for (int j = 0; j < i; j++) {
        if(scope.getParentActivity() != null) { 
        scope = scope.getParentActivity();
        }
      }      
      return scope;
    }
  }


}

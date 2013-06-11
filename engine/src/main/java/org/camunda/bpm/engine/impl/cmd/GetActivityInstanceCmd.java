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
      List<ExecutionEntity> exeForThisParentActInst = executionsByParentActIds.get(executionEntity.getParentActivityInstanceId());
      if(exeForThisParentActInst == null) {
        exeForThisParentActInst = new ArrayList<ExecutionEntity>();
        executionsByParentActIds.put(executionEntity.getParentActivityInstanceId(), exeForThisParentActInst);
      }
      exeForThisParentActInst.add(executionEntity);                   
    }
        
    // create act inst for process instance
    ActivityInstanceImpl processActInst = new ActivityInstanceImpl();                
    processActInst.setActivityId(processInstance.getProcessDefinitionId());
    processActInst.setBusinessKey(processInstance.getBusinessKey());
    processActInst.setId(processInstanceId);
    processActInst.setExecutionId(processInstanceId);
    
    initActivityInstanceTree(processActInst, processInstance, executionsByParentActIds, processInstance.getProcessDefinition());
    
    return processActInst;
  }

  protected void initActivityInstanceTree(ActivityInstanceImpl parentActInst, ExecutionEntity execution, Map<String, List<ExecutionEntity>> executionsByParentActIds,
      ScopeImpl scope) {
    
    List<ActivityInstance> childInstances = parentActInst.getChildInstances();    
    if(!execution.getId().equals(parentActInst.getId()) && !isConcurrentRoot(execution)) {
      
      ActivityInstanceImpl actInst = new ActivityInstanceImpl();     
      ScopeImpl activity = getActivity(execution);
      
      actInst.setActivityId(activity.getId());      
      actInst.setBusinessKey(execution.getBusinessKey());
      actInst.setId(execution.getActivityInstanceId());
      actInst.setExecutionId(execution.getId());
      actInst.setParentActivityInstance(parentActInst);
      childInstances.add(actInst);
      
      parentActInst = actInst;      
    } 
    
    List<ExecutionEntity> executions = execution.getExecutions();
    for (ExecutionEntity executionEntity : executions) {
      initActivityInstanceTree(parentActInst, executionEntity, executionsByParentActIds, scope);    
    }
    
    
  }

  protected boolean isConcurrentRoot(ExecutionEntity execution) {

    List<ExecutionEntity> executions = execution.getExecutions();
    return !executions.isEmpty() && executions.get(0).isConcurrent();

  }

  protected ScopeImpl getActivity(ExecutionEntity executionEntity) {
    if(executionEntity.getActivityId() != null) {
      return executionEntity.getActivity();
      
    } else {
      int i = 0;
      while(!executionEntity.getExecutions().isEmpty()) {
        executionEntity = executionEntity.getExecutions().get(0); 
        i++;
      }
      ActivityImpl scope = executionEntity.getActivity();
      for (int j = 0; j < i; j++) {
        scope = scope.getParentActivity();
      }      
      return scope;
    }
  }


}

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
package org.camunda.bpm.engine.test.util;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExecutionTree implements Execution {

  protected ExecutionTree parent;
  protected List<ExecutionTree> children;
  protected Execution wrappedExecution;

  protected ExecutionTree(Execution execution, List<ExecutionTree> children) {
    this.wrappedExecution = execution;
    this.children = children;
    for (ExecutionTree child : children) {
      child.parent = this;
    }
  }

  public static ExecutionTree forExecution(final String executionId, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl)
        processEngine.getProcessEngineConfiguration();

    CommandExecutor commandExecutor = configuration.getCommandExecutorTxRequired();

    ExecutionTree executionTree = commandExecutor.execute(new Command<ExecutionTree>() {
      public ExecutionTree execute(CommandContext commandContext) {
        ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(executionId);
        return ExecutionTree.forExecution(execution);
      }
    });

    return executionTree;
  }

  protected static ExecutionTree forExecution(ExecutionEntity execution) {
    List<ExecutionTree> children = new ArrayList<ExecutionTree>();

    for (ExecutionEntity child : execution.getExecutions()) {
      children.add(ExecutionTree.forExecution(child));
    }

    return new ExecutionTree(execution, children);

  }

  public List<ExecutionTree> getExecutions() {
    return children;
  }

  public List<ExecutionTree> getLeafExecutions(String activityId) {
    List<ExecutionTree> executions = new ArrayList<ExecutionTree>();

    for (ExecutionTree child : children) {
      if (!child.isEventScope()) {
        if (child.getActivityId() != null) {
          if (activityId.equals(child.getActivityId())) {
            executions.add(child);
          }
        }
        else {
          executions.addAll(child.getLeafExecutions(activityId));
        }
      }
    }

    return executions;
  }

  public String getId() {
    return wrappedExecution.getId();
  }

  public boolean isSuspended() {
    return wrappedExecution.isSuspended();
  }

  public boolean isEnded() {
    return wrappedExecution.isEnded();
  }

  public String getProcessInstanceId() {
    return wrappedExecution.getProcessInstanceId();
  }

  public ExecutionTree getParent() {
    return parent;
  }

  public String getActivityId() {
    return ((PvmExecutionImpl) wrappedExecution).getActivityId();
  }

  public Boolean isScope() {
    return ((PvmExecutionImpl) wrappedExecution).isScope();
  }

  public Boolean isConcurrent() {
    return ((PvmExecutionImpl) wrappedExecution).isConcurrent();
  }

  public Boolean isEventScope() {
    return ((PvmExecutionImpl) wrappedExecution).isEventScope();
  }

  public String getTenantId() {
    return wrappedExecution.getTenantId();
  }

  public Execution getExecution() {
    return wrappedExecution;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendString("", sb);
    return sb.toString();
  }

  public void appendString(String prefix, StringBuilder sb) {
    sb.append(prefix);
    sb.append(executionTreeToString(this));
    sb.append("\n");
    for (ExecutionTree child : getExecutions()) {
      child.appendString(prefix + "   ", sb);
    }
  }

  protected static String executionTreeToString(ExecutionTree executionTree) {
    StringBuilder sb = new StringBuilder();
    sb.append(executionTree.getExecution());

    sb.append("[activityId=");
    sb.append(executionTree.getActivityId());

    sb.append(", isScope=");
    sb.append(executionTree.isScope());

    sb.append(", isConcurrent=");
    sb.append(executionTree.isConcurrent());

    sb.append(", isEventScope=");
    sb.append(executionTree.isEventScope());

    sb.append("]");

    return sb.toString();
  }

}

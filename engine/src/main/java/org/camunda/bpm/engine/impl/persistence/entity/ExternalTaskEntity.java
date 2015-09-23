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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskEntity implements DbEntity, HasDbRevision {

  protected String id;
  protected int revision;
  protected String topicName;
  protected String workerId;
  protected Date lockExpirationTime;

  protected String executionId;
  protected ExecutionEntity execution;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getTopicName() {
    return topicName;
  }
  public void setTopicName(String topic) {
    this.topicName = topic;
  }
  public String getWorkerId() {
    return workerId;
  }
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public int getRevisionNext() {
    return revision + 1;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("topic", topicName);
    persistentState.put("workerId", workerId);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("executionId", executionId);

    return persistentState;
  }

  public void insert() {
    Context.getCommandContext()
      .getExternalTaskManager()
      .insert(this);

    getExecution().addExternalTask(this);
  }

  public void delete() {
    getExecution().removeExternalTask(this);

    Context.getCommandContext()
      .getExternalTaskManager()
      .delete(this);
  }

  public void complete(Map<String, Object> variables) {
    ExecutionEntity associatedExecution = getExecution();

    if (variables != null) {
      associatedExecution.setVariables(variables);
    }

    delete();

    associatedExecution.signal(null, null);
  }

  public void lock(String workerId, long lockDuration) {
    this.workerId = workerId;
    this.lockExpirationTime = new Date(ClockUtil.getCurrentTime().getTime() + lockDuration);
  }

  public ExecutionEntity getExecution() {
    ensureExecutionInitialized();
    return execution;
  }

  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }

  protected void ensureExecutionInitialized() {
    if (execution == null) {
      execution = Context.getCommandContext().getExecutionManager().findExecutionById(executionId);
      EnsureUtil.ensureNotNull(
          "Cannot find execution with id " + executionId + " for external task " + id,
          "execution",
          execution);
    }
  }

  public String toString() {
    return "ExternalTaskEntity ["
        + "id=" + id
        + ", revision=" + revision
        + ", topicName=" + topicName
        + ", workerId=" + workerId
        + ", lockExpirationTime=" + lockExpirationTime
        + ", executionId=" + executionId + "]";
  }


}

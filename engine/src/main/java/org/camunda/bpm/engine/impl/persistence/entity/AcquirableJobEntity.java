/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;

public class AcquirableJobEntity implements DbEntity, HasDbRevision {

  public static final boolean DEFAULT_EXCLUSIVE = true;
  public static final int DEFAULT_RETRIES = 3;

  protected String id;
  protected int revision;

  protected String lockOwner = null;
  protected Date lockExpirationTime = null;
  protected Date duedate;

  protected String deploymentId;
  protected String processInstanceId = null;

  protected boolean isExclusive = DEFAULT_EXCLUSIVE;
  protected int retries = DEFAULT_RETRIES;
  protected long priority = DefaultJobPriorityProvider.DEFAULT_PRIORITY;
  protected String type;
  // entity is active by default
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();


  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("duedate", duedate);
    persistentState.put("suspensionState", suspensionState);
    persistentState.put("deploymentId", deploymentId);
    persistentState.put("priority", priority);
    return persistentState;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public Date getDuedate() {
    return duedate;
  }

  public void setDuedate(Date duedate) {
    this.duedate = duedate;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public boolean isExclusive() {
    return isExclusive;
  }

  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }

  public int getRetries() {
    return retries;
  }

  // special setter for MyBatis which does not influence incidents
  public void setRetriesFromPersistence(int retries) {
    this.retries = retries;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AcquirableJobEntity other = (AcquirableJobEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "[id=" + id
        + ", revision=" + revision
        + ", lockOwner=" + lockOwner
        + ", lockExpirationTime=" + lockExpirationTime
        + ", duedate=" + duedate
        + ", deploymentId=" + deploymentId
        + ", processInstanceId=" + processInstanceId
        + ", isExclusive=" + isExclusive
        + ", retries=" + retries
        + ", priority=" + priority
        + ", type=" + type
        + ", suspensionState=" + suspensionState
        + "]";
  }

}

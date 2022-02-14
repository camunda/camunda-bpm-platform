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
package org.camunda.bpm.engine.impl.db.entitymanager.operation;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.Recyclable;

/**
 * A database operation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class DbOperation implements Recyclable {

  /**
   * The type of the operation.
   */
  protected DbOperationType operationType;

  protected int rowsAffected;
  protected Exception failure;
  protected State state;

  /**
   * The type of the DbEntity this operation is executed on.
   */
  protected Class<? extends DbEntity> entityType;

  public void recycle() {
    // clean out the object state
    operationType = null;
    entityType = null;
  }

  // getters / setters //////////////////////////////////////////

  public Class<? extends DbEntity> getEntityType() {
    return entityType;
  }

  public void setEntityType(Class<? extends DbEntity> entityType) {
    this.entityType = entityType;
  }

  public DbOperationType getOperationType() {
    return operationType;
  }

  public void setOperationType(DbOperationType operationType) {
    this.operationType = operationType;
  }

  public int getRowsAffected() {
    return rowsAffected;
  }

  public void setRowsAffected(int rowsAffected) {
    this.rowsAffected = rowsAffected;
  }

  public boolean isFailed() {
    return state == State.FAILED_CONCURRENT_MODIFICATION 
        || state == State.FAILED_CONCURRENT_MODIFICATION_CRDB 
        || state == State.FAILED_ERROR;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Exception getFailure() {
    return failure;
  }

  public void setFailure(Exception failure) {
    this.failure = failure;
  }

  public enum State
  {
    NOT_APPLIED,
    APPLIED,

    /**
     * Indicates that the operation was not performed for any reason except
     * concurrent modifications.
     */
    FAILED_ERROR,

    /**
     * Indicates that the operation was not performed and that the reason
     * was a concurrent modification to the data to be updated.
     * Applies to databases with isolation level READ_COMMITTED.
     */
    FAILED_CONCURRENT_MODIFICATION,

    /**
     * Indicates that the operation was not performed and was a concurrency
     * conflict. Applies to CockroachDB (with isolation level SERIALIZABLE).
     */
    FAILED_CONCURRENT_MODIFICATION_CRDB
  }

}

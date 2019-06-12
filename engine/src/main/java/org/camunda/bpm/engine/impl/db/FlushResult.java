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
package org.camunda.bpm.engine.impl.db;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;

public class FlushResult {

  protected List<DbOperation> failedOperations;
  protected List<DbOperation> remainingOperations;

  public FlushResult(List<DbOperation> failedOperations, List<DbOperation> remainingOperations) {
    this.failedOperations = failedOperations;
    this.remainingOperations = remainingOperations;
  }

  /**
   * @return the operation that could not be performed
   */
  public List<DbOperation> getFailedOperations() {
    return failedOperations;
  }

  /**
   * @return operations that were not applied, because a preceding operation failed
   */
  public List<DbOperation> getRemainingOperations() {
    return remainingOperations;
  }

  public boolean hasFailures() {
    return !failedOperations.isEmpty();
  }

  public boolean hasRemainingOperations() {
    return !remainingOperations.isEmpty();
  }

  public static FlushResult allApplied() {
    return new FlushResult(Collections.<DbOperation>emptyList(), Collections.<DbOperation>emptyList());
  }

  public static FlushResult withFailures(List<DbOperation> failedOperations) {
    return new FlushResult(failedOperations, Collections.<DbOperation>emptyList());
  }

  public static FlushResult withFailuresAndRemaining(List<DbOperation> failedOperations, List<DbOperation> remainingOperations) {
    return new FlushResult(failedOperations, remainingOperations);
  }
}

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
package org.camunda.bpm.engine.impl.errorcode;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * The set of built-in exception codes the built-in {@link ExceptionCodeProvider}
 * uses to assign a code to a {@link ProcessEngineException}.
 */
public enum BuiltinExceptionCode {

  /**
   * The code assigned to a {@link ProcessEngineException} when no other code is assigned.
   */
  FALLBACK(0),

  /**
   * This code is assigned when an {@link OptimisticLockingException} or {@link CrdbTransactionRetryException} occurs.
   */
  OPTIMISTIC_LOCKING(1),

  /**
   * This code is assigned when a "deadlock" persistence exception is detected.
   */
  DEADLOCK(10_000),

  /**
   * This code is assigned when a "foreign key constraint violation" persistence exception is detected.
   */
  FOREIGN_KEY_CONSTRAINT_VIOLATION(10_001),

  /**
   * This code is assigned when a "column size too small" persistence exception is detected.
   */
  COLUMN_SIZE_TOO_SMALL(10_002);

  protected final int code;

  BuiltinExceptionCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

}

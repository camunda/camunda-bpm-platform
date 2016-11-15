/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.impl.pvm.runtime;

/**
 * Contains operations which are depending on each other.
 * That means the method/operation `operationWhichCanAffectExecution` is called as first
 * if the execution affects the current state the next method `continueOperation` is not called.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface DependingOperations {

  /**
   * Contains an operation which can affect the current execution state.
   *
   * @param execution the execution which is used for the operation
   */
  void operationWhichCanAffectExecution(PvmExecutionImpl execution);

  /**
   * The operation which should be execution afterwards, if the execution state does not change.
   *
   * @param execution the execution which is used for the operation
   */
  void continueOperation(PvmExecutionImpl execution);
}

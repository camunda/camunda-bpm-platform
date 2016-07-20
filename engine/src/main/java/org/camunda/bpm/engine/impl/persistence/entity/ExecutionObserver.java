/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

/**
 * Represents an observer for the exeuction.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface ExecutionObserver {

  /**
   * Callback which is called in the clearExecution method of the ExecutionEntity.
   *
   * @param execution the execution which is been observed
   */
  public void onClear(ExecutionEntity execution);
}

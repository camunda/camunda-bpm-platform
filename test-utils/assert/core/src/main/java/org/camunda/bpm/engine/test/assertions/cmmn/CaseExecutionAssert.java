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
package org.camunda.bpm.engine.test.assertions.cmmn;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.CaseExecution;

public class CaseExecutionAssert extends AbstractCaseAssert<CaseExecutionAssert, CaseExecution> {

  protected CaseExecutionAssert(final ProcessEngine engine, final CaseExecution actual) {
    super(engine, actual, CaseExecutionAssert.class);
  }

  protected static CaseExecutionAssert assertThat(final ProcessEngine engine, final CaseExecution actual) {
    return new CaseExecutionAssert(engine, actual);
  }

  @Override
  public CaseExecutionAssert isAvailable() {
    return super.isAvailable();
  }

  @Override
  public CaseExecutionAssert isEnabled() {
    return super.isEnabled();
  }

  @Override
  public CaseExecutionAssert isDisabled() {
    return super.isDisabled();
  }

  @Override
  public CaseExecutionAssert isActive() {
    return super.isActive();
  }

  @Override
  public CaseExecutionAssert isCompleted() {
    return super.isCompleted();
  }

  @Override
  public CaseExecutionAssert isTerminated() {
    return super.isTerminated();
  }

  @Override
  public CaseInstanceAssert isCaseInstance() {
    return super.isCaseInstance();
  }

  @Override
  public StageAssert isStage() {
    return super.isStage();
  }

  @Override
  public HumanTaskAssert isHumanTask() {
    return super.isHumanTask();
  }

  @Override
  public ProcessTaskAssert isProcessTask() {
    return super.isProcessTask();
  }

  @Override
  public CaseTaskAssert isCaseTask() {
    return super.isCaseTask();
  }

  @Override
  public MilestoneAssert isMilestone() {
    return super.isMilestone();
  }

}

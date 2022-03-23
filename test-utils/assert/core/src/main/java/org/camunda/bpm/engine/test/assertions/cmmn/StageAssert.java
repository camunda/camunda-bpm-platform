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
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;

public class StageAssert extends AbstractCaseAssert<StageAssert, CaseExecution> {

	protected StageAssert(final ProcessEngine engine, final CaseExecution actual) {
		super(engine, actual, StageAssert.class);
	}

	protected static StageAssert assertThat(final ProcessEngine engine, final CaseExecution actual) {
		return new StageAssert(engine, actual);
	}

	@Override
	public StageAssert isAvailable() {
		return super.isAvailable();
	}

	@Override
	public StageAssert isEnabled() {
		return super.isEnabled();
	}

	@Override
	public StageAssert isDisabled() {
		return super.isDisabled();
	}

	@Override
	public StageAssert isActive() {
		return super.isActive();
	}

	@Override
	public StageAssert isCompleted() {
		return super.isCompleted();
	}

	@Override
	public StageAssert isTerminated() {
		return super.isTerminated();
	}

	@Override
	public HumanTaskAssert humanTask(CaseExecutionQuery query) {
		return super.humanTask(query);
	}

	@Override
	public HumanTaskAssert humanTask(String activityId) {
		return super.humanTask(activityId);
	}

	@Override
	public CaseTaskAssert caseTask(CaseExecutionQuery query) {
		return super.caseTask(query);
	}

	@Override
	public CaseTaskAssert caseTask(String activityId) {
		return super.caseTask(activityId);
	}

	@Override
	public ProcessTaskAssert processTask(CaseExecutionQuery query) {
		return super.processTask(query);
	}

	@Override
	public ProcessTaskAssert processTask(String activityId) {
		return super.processTask(activityId);
	}

	@Override
	public StageAssert stage(CaseExecutionQuery query) {
		return super.stage(query);
	}

	@Override
	public StageAssert stage(String activityId) {
		return super.stage(activityId);
	}

	@Override
	public MilestoneAssert milestone(CaseExecutionQuery query) {
		return super.milestone(query);
	}

	@Override
	public MilestoneAssert milestone(String activityId) {
		return super.milestone(activityId);
	}

}

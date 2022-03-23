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

public class MilestoneAssert extends AbstractCaseAssert<MilestoneAssert, CaseExecution> {

	protected MilestoneAssert(final ProcessEngine engine, final CaseExecution actual) {
		super(engine, actual, MilestoneAssert.class);
	}

	protected static MilestoneAssert assertThat(final ProcessEngine engine, final CaseExecution actual) {
		return new MilestoneAssert(engine, actual);
	}

  /**
   *  Verifies the expectation that the {@link CaseExecution} is 'available'.
     *
     * @return  this
    **/
	@Override
	public MilestoneAssert isAvailable() {
		return super.isAvailable();
	}

  /**
   *  Verifies the expectation that the {@link CaseExecution} is 'completed'.
   *  A milestone is 'completed', when his 'occur' transition was performed.
   *
   * @return  this
   **/
	@Override
	public MilestoneAssert isCompleted() {
		return super.isCompleted();
	}

  /**
   *  Verifies the expectation that the {@link CaseExecution} is 'terminated'.
   *
   * @return  this
   **/
	@Override
	public MilestoneAssert isTerminated() {
		return super.isTerminated();
	}

}

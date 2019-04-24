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
package org.camunda.bpm.qa.performance.engine.framework;

import org.camunda.bpm.qa.performance.engine.steps.WaitStep;

/**
 * A step in a performance test.
 *
 * @author Daniel Meyer
 *
 */
public class PerfTestStep {

  protected final PerfTestStepBehavior perfTestStepBehavior;

  protected PerfTestStep nextStep;

  protected String stepName;

  public PerfTestStep(PerfTestStepBehavior behavior) {
    perfTestStepBehavior = behavior;
  }

  public void setNextStep(PerfTestStep step) {
    nextStep = step;
  }

  public PerfTestStepBehavior getStepBehavior() {
    return perfTestStepBehavior;
  }

  public PerfTestStep getNextStep() {
    return nextStep;
  }

  public String getStepName() {
    return perfTestStepBehavior.getClass().getSimpleName();
  }

  public boolean isWaitStep() {
    return perfTestStepBehavior instanceof WaitStep;
  }

}

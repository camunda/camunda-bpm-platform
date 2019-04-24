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
package org.camunda.bpm.qa.performance.engine.steps;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepBehavior;

/**
 * @author: Johannes Heinemann
 */
public class CountJobsStep implements PerfTestStepBehavior {

  ProcessEngine processEngine;

  public CountJobsStep(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public void execute(PerfTestRunContext context) {
    long failedJobs = processEngine.getHistoryService().createHistoricJobLogQuery().failureLog().count();
    long createdJobs = processEngine.getHistoryService().createHistoricJobLogQuery().creationLog().count();
    long successfulJobs = processEngine.getHistoryService().createHistoricJobLogQuery().successLog().count();

    System.out.println("Number of created jobs: " + createdJobs);
    System.out.println("Number of failed jobs: " + failedJobs);
    System.out.println("Number of successful jobs: " + successfulJobs);
  }

}

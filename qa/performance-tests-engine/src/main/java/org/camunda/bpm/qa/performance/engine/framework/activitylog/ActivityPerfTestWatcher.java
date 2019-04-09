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
package org.camunda.bpm.qa.performance.engine.framework.activitylog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.qa.performance.engine.framework.PerfTest;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestPass;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRun;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStep;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestWatcher;
import org.camunda.bpm.qa.performance.engine.junit.PerfTestProcessEngine;
import org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants;

public class ActivityPerfTestWatcher implements PerfTestWatcher {

  public final static List<String> WATCH_ALL_ACTIVITIES = Collections.singletonList("ALL");

  protected List<String> activityIds;
  protected boolean watchAllActivities;

  public ActivityPerfTestWatcher(List<String> activityIds) {
    this.activityIds = activityIds;
    watchAllActivities = WATCH_ALL_ACTIVITIES.equals(activityIds);
  }

  public void beforePass(PerfTestPass pass) {
    // nothing to do
  }

  public void beforeRun(PerfTest test, PerfTestRun run) {
    // nothing to do
  }

  public void beforeStep(PerfTestStep step, PerfTestRun run) {
    // nothing to do
  }

  public void afterStep(PerfTestStep step, PerfTestRun run) {
    // nothing to do
  }

  public void afterRun(PerfTest test, PerfTestRun run) {
    // nothing to do
  }

  public void afterPass(PerfTestPass pass) {
    ProcessEngine processEngine = PerfTestProcessEngine.getInstance();
    HistoryService historyService = processEngine.getHistoryService();

    for (PerfTestRun run : pass.getRuns().values()) {
      logActivityResults(pass, run, historyService);
    }
  }

  protected void logActivityResults(PerfTestPass pass, PerfTestRun run, HistoryService historyService) {
    String processInstanceId = run.getVariable(PerfTestConstants.PROCESS_INSTANCE_ID);
    List<ActivityPerfTestResult> activityResults = new ArrayList<ActivityPerfTestResult>();

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    Date startTime = processInstance.getStartTime();

    List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstanceId)
      .orderByHistoricActivityInstanceStartTime()
      .asc()
      .list();

    for (HistoricActivityInstance activityInstance : activityInstances) {
      if (watchAllActivities || activityIds.contains(activityInstance.getActivityId())) {
        ActivityPerfTestResult result = new ActivityPerfTestResult(activityInstance);
        if (activityInstance.getActivityType().equals("startEvent")) {
          result.setStartTime(startTime);
        }
        activityResults.add(result);
      }
    }

    pass.logActivityResult(processInstanceId, activityResults);
  }

}

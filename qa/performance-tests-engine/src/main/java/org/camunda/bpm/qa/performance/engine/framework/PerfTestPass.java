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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.qa.performance.engine.framework.activitylog.ActivityPerfTestResult;

/**
 * A step in a performance test.
 *
 * @author Daniel Meyer
 *
 */
public class PerfTestPass {

  protected static final IdGenerator idGenerator = new StrongUuidGenerator();

  // pass state
  protected int numberOfThreads;
  protected AtomicLong completedRuns;
  protected PerfTestResult result;
  protected boolean completed;
  protected Map<String, PerfTestRun> runs;
  protected long startTime;
  protected long endTime;


  public PerfTestPass(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
    this.completedRuns = new AtomicLong();
    this.result = new PerfTestResult();
    this.completed = false;
  }

  public void createRuns(PerfTestRunner runner, PerfTestStep firstStep, int numberOfRuns) {
    runs = new HashMap<String, PerfTestRun>();
    for (int i = 0; i < numberOfRuns; i++) {
      String runId = idGenerator.getNextId();
      runs.put(runId, new PerfTestRun(runner, runId, firstStep));
    }
    runs = Collections.unmodifiableMap(runs);
  }

  public int getNumberOfThreads() {
    return numberOfThreads;
  }

  public AtomicLong getCompletedRuns() {
    return completedRuns;
  }

  public PerfTestResult getResult() {
    return result;
  }

  public boolean isCompleted() {
    return completed;
  }

  public Map<String, PerfTestRun> getRuns() {
    return runs;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void startPass() {
    startTime = System.currentTimeMillis();
  }

  public long completeRun() {
    return completedRuns.incrementAndGet();
  }

  public void endPass() {
    endTime = System.currentTimeMillis();
    result.setDuration(endTime - startTime);
    result.setNumberOfThreads(numberOfThreads);
    completed = true;
  }

  public void logStepResult(PerfTestStep currentStep, Object stepResult) {
    result.logStepResult(currentStep, stepResult);
  }

  public void logActivityResult(String identifier, List<ActivityPerfTestResult> results) {
    result.logActivityResult(identifier, results);
  }

  public PerfTestRun getRun(String runId) {
    return runs.get(runId);
  }
}

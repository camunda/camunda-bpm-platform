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

import java.util.concurrent.ExecutionException;

import org.camunda.bpm.qa.performance.engine.junit.PerfTestResultRecorderRule;


/**
 * @author Daniel Meyer
 *
 */
public class PerfTestBuilder {

  protected final PerfTest perfTest;
  protected PerfTestConfiguration perfTestConfiguration;
  protected PerfTestResultRecorderRule resultRecorder;

  public PerfTestBuilder(PerfTestConfiguration perfTestConfiguration,
                                  PerfTestResultRecorderRule resultRecorder) {
    this.perfTestConfiguration = perfTestConfiguration;
    this.resultRecorder = resultRecorder;
    perfTest = new PerfTest();
  }

  public PerfTestBuilder step(PerfTestStepBehavior behavior) {
    PerfTestStep step = new PerfTestStep(behavior);
    perfTest.addStep(step);
    return this;
  }

  public PerfTestResults run() {
    PerfTestRunner testRunner = new PerfTestRunner(perfTest, perfTestConfiguration);
    try {
      PerfTestResults results = testRunner.execute()
        .get();
      resultRecorder.setResults(results);
      return results;

    } catch (ExecutionException e) {
      if(e.getCause() != null) {
        Throwable cause = e.getCause();
        if(cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else {
          throw new PerfTestException(cause);
        }
      }
      else {
        throw new PerfTestException(e);
      }
    } catch (Exception e) {
      throw new PerfTestException(e);
    }

  }

  public PerfTestBuilder steps(int i, PerfTestStepBehavior behavior) {
    for (int j = 0; j < i; j++) {
      step(behavior);
    }
    return this;
  }

}

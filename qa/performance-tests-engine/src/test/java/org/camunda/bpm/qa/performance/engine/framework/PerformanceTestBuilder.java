/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.qa.performance.engine.framework;

import java.util.concurrent.ExecutionException;

import org.camunda.bpm.qa.performance.engine.PerformanceTestResultRecorderRule;


/**
 * @author Daniel Meyer
 *
 */
public class PerformanceTestBuilder {

  protected final PerformanceTest performanceTest;
  protected PerformanceTestConfiguration performanceTestConfiguration;
  protected PerformanceTestResultRecorderRule resultRecorder;

  public PerformanceTestBuilder(PerformanceTestConfiguration performanceTestConfiguration,
                                  PerformanceTestResultRecorderRule resultRecorder) {
    this.performanceTestConfiguration = performanceTestConfiguration;
    this.resultRecorder = resultRecorder;
    performanceTest = new PerformanceTest();
  }

  public PerformanceTestBuilder step(StepBehavior behavior) {
    PerformanceTestStep step = new PerformanceTestStep(behavior);
    performanceTest.addStep(step);
    return this;
  }

  public PerformanceTestResults run() {
    PerformanceTestRunner testRunner = new PerformanceTestRunner(performanceTest, performanceTestConfiguration);
    try {
      PerformanceTestResults results = testRunner.execute()
        .get();
      resultRecorder.setResults(results);
      return results;

    } catch (ExecutionException e) {
      if(e.getCause() != null) {
        Throwable cause = e.getCause();
        if(cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else {
          throw new PerformanceTestException(cause);
        }
      }
      else {
        throw new PerformanceTestException(e);
      }
    } catch (Exception e) {
      throw new PerformanceTestException(e);
    }

  }

}

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

/**
 * Allows to follows the progress of a {@link PerformanceTestRun}.
 *
 * @author Daniel Meyer
 *
 */
public interface PerformanceTestWatcher {

  /**
   * Invoked before a performance test run is started.
   *
   * @param test the {@link PerformanceTest} about to be executed
   * @param run the current {@link PerformanceTestRun}
   */
  void beforeRun(PerformanceTest test, PerformanceTestRun run);

  /**
   * Invoked before a {@link PerformanceTestRun} starts an individual
   * step in the performance test.
   *
   * This method is called by the same {@link Thread} which will
   * execute the step.
   *
   * @param step the {@link PerformanceTestStep} about to be executed.
   * @param run the current {@link PerformanceTestRun}
   */
  void beforeStep(PerformanceTestStep step, PerformanceTestRun run);

  /**
   * Invoked after a {@link PerformanceTestRun} ends an individual
   * step in the performance test.
   *
   * This method is called by the same {@link Thread} which
   * executed the step.
   *
   * @param step the {@link PerformanceTestStep} which has been executed.
   * @param run the current {@link PerformanceTestRun}
   */
  void afterStep(PerformanceTestStep step, PerformanceTestRun run);

  /**
   * Invoked after a performance test run is ended.
   *
   * @param test the {@link PerformanceTest} about to be executed
   * @param run the current {@link PerformanceTestRun}
   */
  void afterRun(PerformanceTest test, PerformanceTestRun run);


}

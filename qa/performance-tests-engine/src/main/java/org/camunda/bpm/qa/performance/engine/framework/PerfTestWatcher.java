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

/**
 * Allows to follows the progress of a {@link PerfTestRun}.
 *
 * @author Daniel Meyer
 *
 */
public interface PerfTestWatcher {

  /**
   * Invoked before a performance test pass is started.
   *
   * @param pass the current {@link PerfTestPass}
   */
  void beforePass(PerfTestPass pass);

  /**
   * Invoked before a performance test run is started.
   *
   * @param test the {@link PerfTest} about to be executed
   * @param run the current {@link PerfTestRun}
   */
  void beforeRun(PerfTest test, PerfTestRun run);

  /**
   * Invoked before a {@link PerfTestRun} starts an individual
   * step in the performance test.
   *
   * This method is called by the same {@link Thread} which will
   * execute the step.
   *
   * @param step the {@link PerfTestStep} about to be executed.
   * @param run the current {@link PerfTestRun}
   */
  void beforeStep(PerfTestStep step, PerfTestRun run);

  /**
   * Invoked after a {@link PerfTestRun} ends an individual
   * step in the performance test.
   *
   * This method is called by the same {@link Thread} which
   * executed the step.
   *
   * @param step the {@link PerfTestStep} which has been executed.
   * @param run the current {@link PerfTestRun}
   */
  void afterStep(PerfTestStep step, PerfTestRun run);

  /**
   * Invoked after a performance test run is ended.
   *
   * @param test the {@link PerfTest} about to be executed
   * @param run the current {@link PerfTestRun}
   */
  void afterRun(PerfTest test, PerfTestRun run);

  /**
   * Invoked after a performance test pass is ended.
   *
   * @param pass the current {@link PerfTestPass}
   */
  void afterPass(PerfTestPass pass);
}

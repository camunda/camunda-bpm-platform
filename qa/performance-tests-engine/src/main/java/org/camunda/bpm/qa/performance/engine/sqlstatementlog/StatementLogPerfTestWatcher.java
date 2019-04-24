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
package org.camunda.bpm.qa.performance.engine.sqlstatementlog;

import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.PerfTest;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestPass;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRun;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStep;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestWatcher;
import org.camunda.bpm.qa.performance.engine.sqlstatementlog.StatementLogSqlSession.SqlStatementLog;

/**
 * {@link PerfTestWatcher} performing statement logging.
 *
 * @author Daniel Meyer
 *
 */
public class StatementLogPerfTestWatcher implements PerfTestWatcher {

  public void beforePass(PerfTestPass pass) {
    // nothing to do
  }

  public void beforeRun(PerfTest test, PerfTestRun run) {
    // nothing to do
  }

  public void beforeStep(PerfTestStep step, PerfTestRun run) {
    StatementLogSqlSession.startLogging();
  }

  public void afterStep(PerfTestStep step, PerfTestRun run) {
    List<SqlStatementLog> loggedStatements = StatementLogSqlSession.stopLogging();
    run.logStepResult(loggedStatements);
  }

  public void afterRun(PerfTest test, PerfTestRun run) {
    // nothing to do
  }

  public void afterPass(PerfTestPass pass) {
    // nothing to do
  }

}

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
package org.camunda.bpm.engine.test.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.test.WatchLogger;
import org.camunda.bpm.engine.test.util.ProcessEngineLoggingRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

@SuppressWarnings("unused")
public class ProcessEngineLoggingRuleTest {

  private static final String PERSISTENCE_LOGGER = "org.camunda.bpm.engine.persistence"; // 03
  private static final String CONTAINER_INTEGRATION_LOGGER = "org.camunda.bpm.container"; //08
  private static final String PROCESS_APPLICATION_LOGGER = "org.camunda.bpm.application"; //07
  private static final String JOB_EXECUTOR_LOGGER = "org.camunda.bpm.engine.jobexecutor"; //14

  public ExpectedException exceptionRule = ExpectedException.none();
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
                                                      .watch(PERSISTENCE_LOGGER, CONTAINER_INTEGRATION_LOGGER)
                                                        .level(Level.DEBUG)
                                                      .watch(PROCESS_APPLICATION_LOGGER, Level.INFO);
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(loggingRule).around(exceptionRule);

  @Test
  public void testWithoutAnnotation() {
    // given

    // when
    logSomethingOnAllLevels();

    List<ILoggingEvent> persistenceLog = loggingRule.getLog(PERSISTENCE_LOGGER);
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    exceptionRule.expect(RuntimeException.class);
    List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);

    // then
    testLogLevel(persistenceLog, Level.DEBUG);
    testLogLevel(containerLog, Level.DEBUG);
    testLogLevel(processAppLogger, Level.INFO);
  }

  @Test
  @WatchLogger(loggerNames = {CONTAINER_INTEGRATION_LOGGER}, level = "WARN")
  public void testOverrideWithAnnotation() {
    // given

    // when
    logSomethingOnAllLevels();

    List<ILoggingEvent> persistenceLog = loggingRule.getLog(PERSISTENCE_LOGGER);
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    exceptionRule.expectMessage(ProcessEngineLoggingRule.NOT_WATCHING_ERROR);
    List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);

    // then
    testLogLevel(persistenceLog, Level.DEBUG);
    testLogLevel(containerLog, Level.WARN);
    testLogLevel(processAppLogger, Level.INFO);
  }
  
  @Test
  @WatchLogger(loggerNames = {JOB_EXECUTOR_LOGGER}, level = "ERROR")
  public void testAddWatchedLoggerWithAnnotation() {
    // given

    // when
    logSomethingOnAllLevels();

    List<ILoggingEvent> persistenceLog = loggingRule.getLog(PERSISTENCE_LOGGER);
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);

    // then
    testLogLevel(persistenceLog, Level.DEBUG);
    testLogLevel(containerLog, Level.DEBUG);
    testLogLevel(processAppLogger, Level.INFO);
    testLogLevel(jobExecutorLogger, Level.ERROR);
  }
  
  @Test
  @WatchLogger(loggerNames = {CONTAINER_INTEGRATION_LOGGER}, level = "OFF")
  public void testTurnOffWatcherWithAnnotation() {
    // given

    // when
    logSomethingOnAllLevels();

    List<ILoggingEvent> persistenceLog = loggingRule.getLog(PERSISTENCE_LOGGER);
    exceptionRule.expectMessage(ProcessEngineLoggingRule.NOT_WATCHING_ERROR);
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);

    // then
    testLogLevel(persistenceLog, Level.DEBUG);
    testLogLevel(processAppLogger, Level.INFO);
    testLogLevel(jobExecutorLogger, Level.ERROR);
  }

  @Test
  @WatchLogger(loggerNames = {JOB_EXECUTOR_LOGGER, PERSISTENCE_LOGGER, CONTAINER_INTEGRATION_LOGGER, PROCESS_APPLICATION_LOGGER}, level = "DEBUG")
  public void testLogOrder() {
    logSomethingOnAllLevels();

    List<ILoggingEvent> fullLog = loggingRule.getLog();
    ILoggingEvent previousLogEntry = null;
    for (ILoggingEvent logEntry : fullLog) {
      System.out.println(logEntry.getTimeStamp() + " - " + logEntry.getLoggerName() + " - " + logEntry.getLevel());
      if(previousLogEntry != null) {
        assertTrue(previousLogEntry.getTimeStamp() <= logEntry.getTimeStamp());
      }
      previousLogEntry = logEntry;
    }
  }

  private void testLogLevel(List<ILoggingEvent> log, Level level) {
    testAtLeastOneLogEntryWithLevelIsPresent(log, level);
    testAllLoggingEntriesAtLeastLevel(log, level);
  }

  private void testAtLeastOneLogEntryWithLevelIsPresent(List<ILoggingEvent> log, Level level) {
    for (ILoggingEvent logEntry : log) {
      if(logEntry.getLevel().equals(level)) {
        return;
      }
    }
    fail("Expected at least one log entry with level " + level + " in log");
  }
  
  private void testAllLoggingEntriesAtLeastLevel(List<ILoggingEvent> log, Level level) {
    for (ILoggingEvent logStatement : log) {
      assertTrue(logStatement.getLevel().isGreaterOrEqual(level));
    }
  }
  
  public void logSomethingOnAllLevels() {
    ProcessEngineLogger.PERSISTENCE_LOGGER.debugJobExecuted(null); // debug
    ProcessEngineLogger.PERSISTENCE_LOGGER.performingDatabaseOperation("test", "test", "test"); // info
    ProcessEngineLogger.PERSISTENCE_LOGGER.removeEntryFromDeploymentCacheFailure("test", "test", new Throwable()); // warn
    ProcessEngineLogger.PERSISTENCE_LOGGER.noDeploymentLockPropertyFound(); // error

    ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.debugAutoCompletedUrl("test"); // debug
    ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.foundConfigJndi("test", "test"); // info
    ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.exceptionWhileStopping("test", "test", new Throwable()); // warn
    ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.interruptedWhileShuttingDownThreadPool(new InterruptedException()); // error

    ProcessEngineLogger.JOB_EXECUTOR_LOGGER.debugAcquiredJobNotFound("test"); // debug
    ProcessEngineLogger.JOB_EXECUTOR_LOGGER.startingUpJobExecutor("test"); // info
    ProcessEngineLogger.JOB_EXECUTOR_LOGGER.warnHistoryCleanupBatchWindowNotFound(); // warn
    ProcessEngineLogger.JOB_EXECUTOR_LOGGER.exceptionDuringJobAcquisition(new Exception()); // error

    ProcessEngineLogger.PROCESS_APPLICATION_LOGGER.paDoesNotProvideExecutionListener("test"); // debug
    ProcessEngineLogger.PROCESS_APPLICATION_LOGGER.detectedPa(Object.class); // info
    ProcessEngineLogger.PROCESS_APPLICATION_LOGGER.alreadyDeployed(); // warn
    ProcessEngineLogger.PROCESS_APPLICATION_LOGGER.couldNotRemoveDefinitionsFromCache(new Throwable()); // error
  }
}

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
package org.camunda.commons.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.camunda.commons.testing.util.ExampleProcessEngineLogger;
import org.junit.Rule;
import org.junit.Test;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

@SuppressWarnings("unused")
public class ProcessEngineLoggingRuleTest {

  private static final String PERSISTENCE_LOGGER = "org.camunda.bpm.engine.persistence"; // 03
  private static final String CONTAINER_INTEGRATION_LOGGER = "org.camunda.bpm.container"; //08
  private static final String PROCESS_APPLICATION_LOGGER = "org.camunda.bpm.application"; //07
  private static final String JOB_EXECUTOR_LOGGER = "org.camunda.bpm.engine.jobexecutor"; //14

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
                                                      .watch(PERSISTENCE_LOGGER, CONTAINER_INTEGRATION_LOGGER)
                                                        .level(Level.DEBUG)
                                                      .watch(PROCESS_APPLICATION_LOGGER, Level.INFO);

  @Test
  public void testWithoutAnnotation() {
    // given

    // when
    logSomethingOnAllLevels();

    List<ILoggingEvent> persistenceLog = loggingRule.getLog(PERSISTENCE_LOGGER);
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    RuntimeException expectedException = null;
    try {
      List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);
    } catch (RuntimeException e) {
      expectedException = e;
    }

    // then
    assertThat(expectedException).isNotNull();
    assertThat(expectedException.getMessage()).contains(ProcessEngineLoggingRule.NOT_WATCHING_ERROR);
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
    RuntimeException expectedException = null;
    try {
      List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);
    } catch (RuntimeException e) {
      expectedException = e;
    }

    // then
    assertThat(expectedException).isNotNull();
    assertThat(expectedException.getMessage()).contains(ProcessEngineLoggingRule.NOT_WATCHING_ERROR);
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
    List<ILoggingEvent> containerLog = loggingRule.getLog(CONTAINER_INTEGRATION_LOGGER);
    List<ILoggingEvent> processAppLogger = loggingRule.getLog(PROCESS_APPLICATION_LOGGER);
    RuntimeException expectedException = null;
    try {
      List<ILoggingEvent> jobExecutorLogger = loggingRule.getLog(JOB_EXECUTOR_LOGGER);
    } catch (RuntimeException e) {
      expectedException = e;
    }

    // then
    assertThat(expectedException).isNotNull();
    assertThat(expectedException.getMessage()).contains(ProcessEngineLoggingRule.NOT_WATCHING_ERROR);
    testLogLevel(persistenceLog, Level.DEBUG);
    testLogLevel(processAppLogger, Level.INFO);
    assertThat(containerLog.size()).isEqualTo(0);
  }

  @Test
  @WatchLogger(loggerNames = {JOB_EXECUTOR_LOGGER, PERSISTENCE_LOGGER, CONTAINER_INTEGRATION_LOGGER, PROCESS_APPLICATION_LOGGER}, level = "DEBUG")
  public void testLogOrder() {
    logSomethingOnAllLevels();

    List<ILoggingEvent> fullLog = loggingRule.getLog();
    ILoggingEvent previousLogEntry = null;
    for (ILoggingEvent logEntry : fullLog) {
      if(previousLogEntry != null) {
        assertThat(previousLogEntry.getTimeStamp() <= logEntry.getTimeStamp()).isTrue();
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
      assertThat(logStatement.getLevel().isGreaterOrEqual(level)).isTrue();
    }
  }
  
  public void logSomethingOnAllLevels() {
    ExampleProcessEngineLogger.PERSISTENCE_LOGGER.debug(); 
    ExampleProcessEngineLogger.PERSISTENCE_LOGGER.info(); 
    ExampleProcessEngineLogger.PERSISTENCE_LOGGER.warn(); 
    ExampleProcessEngineLogger.PERSISTENCE_LOGGER.error(); 

    ExampleProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.debug(); 
    ExampleProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.info(); 
    ExampleProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.warn();
    ExampleProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER.error(); 

    ExampleProcessEngineLogger.JOB_EXECUTOR_LOGGER.debug(); 
    ExampleProcessEngineLogger.JOB_EXECUTOR_LOGGER.info(); 
    ExampleProcessEngineLogger.JOB_EXECUTOR_LOGGER.warn(); 
    ExampleProcessEngineLogger.JOB_EXECUTOR_LOGGER.error(); 

    ExampleProcessEngineLogger.PROCESS_APPLICATION_LOGGER.debug(); 
    ExampleProcessEngineLogger.PROCESS_APPLICATION_LOGGER.info(); 
    ExampleProcessEngineLogger.PROCESS_APPLICATION_LOGGER.warn();
    ExampleProcessEngineLogger.PROCESS_APPLICATION_LOGGER.error();
  }
}

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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ProcessDataLoggingContextMultipleEnginesTest {

  private static final String PVM_LOGGER = "org.camunda.bpm.engine.pvm";
  private static final String DELEGATE_LOGGER = LogEngineNameDelegate.class.getName();

  private static final String PROCESS = "process";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch(PVM_LOGGER, DELEGATE_LOGGER).level(Level.DEBUG);

  protected ProcessEngine engine1;
  protected ProcessEngine engine2;

  @Before
  public void startEngines() {
    engine1 = createProcessEngine("engine1");
    engine2 = createProcessEngine("engine2");
  }

  @After
  public void closeEngine1() {
    try {
      engine1.close();
    }
    finally {
      engine1 = null;
    }
  }

  @After
  public void closeEngine2() {
    try {
      engine2.close();
    }
    finally {
      engine2 = null;
    }
  }

  @Test
  public void shouldHaveProcessEngineNameAvailableInMdc() {
    // given
    engine1.getRepositoryService().createDeployment().addModelInstance("test.bpmn", modelOneTaskProcess()).deploy();

    // when produce logging
    engine1.getRuntimeService().startProcessInstanceByKey(PROCESS);
    engine1.getTaskService().complete(engine1.getTaskService().createTaskQuery().singleResult().getId());

    // then
    List<ILoggingEvent> filteredLog = loggingRule.getLog();
    List<String> engineNames = filteredLog.stream().map(log -> log.getMDCPropertyMap().get("engineName")).collect(Collectors.toList());
    assertThat(engineNames).hasSize(filteredLog.size());
    assertThat(engineNames.stream().distinct().collect(Collectors.toList())).containsExactly("engine1");
  }

  @Test
  public void shouldHaveProcessEngineNameAvailableInMdcForAllEngines() {
    // given
    engine1.getRepositoryService().createDeployment().addModelInstance("test1.bpmn", modelLogDelegateProcess()).deploy();
    engine2.getRepositoryService().createDeployment().addModelInstance("test2.bpmn", modelLogDelegateProcess()).deploy();

    // when
    engine1.getRuntimeService().startProcessInstanceByKey(PROCESS);
    engine2.getRuntimeService().startProcessInstanceByKey(PROCESS);

    // then
    List<ILoggingEvent> log = loggingRule.getFilteredLog(LogEngineNameDelegate.LOG_MESSAGE);
    List<String> engineNames = log.stream().map(l -> l.getMDCPropertyMap().get("engineName")).collect(Collectors.toList());
    // make sure all log entries have access to the engineName MDC property
    assertThat(engineNames).hasSize(log.size());
    assertThat(engineNames.stream().distinct().collect(Collectors.toList())).containsExactlyInAnyOrder("engine1", "engine2");

    List<ILoggingEvent> filteredLogEngine1 = loggingRule.getFilteredLog("engine1");
    List<String> engineNamesEngine1 = filteredLogEngine1.stream().map(l -> l.getMDCPropertyMap().get("engineName")).collect(Collectors.toList());
    assertThat(engineNamesEngine1).hasSameSizeAs(filteredLogEngine1);
    assertThat(engineNamesEngine1.stream().distinct().collect(Collectors.toList())).containsExactly("engine1");

    List<ILoggingEvent> filteredLogEngine2 = loggingRule.getFilteredLog("engine2");
    List<String> engineNamesEngine2 = filteredLogEngine2.stream().map(l -> l.getMDCPropertyMap().get("engineName")).collect(Collectors.toList());
    assertThat(engineNamesEngine2).hasSameSizeAs(filteredLogEngine2);
    assertThat(engineNamesEngine2.stream().distinct().collect(Collectors.toList())).containsExactly("engine2");
  }

  private ProcessEngine createProcessEngine(String name) {
    StandaloneInMemProcessEngineConfiguration processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName(name);
    processEngineConfiguration.setJdbcUrl(String.format("jdbc:h2:mem:%s", name));
    return processEngineConfiguration.buildProcessEngine();
  }

  protected BpmnModelInstance modelOneTaskProcess() {
    return Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
          .userTask("waitState")
        .endEvent("end")
        .done();
  }

  protected BpmnModelInstance modelLogDelegateProcess() {
    return Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
          .serviceTask()
            .camundaClass(LogEngineNameDelegate.class.getName())
        .endEvent("end")
        .done();
  }


}

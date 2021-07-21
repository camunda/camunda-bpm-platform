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
package org.camunda.bpm.quarkus.engine.extension.impl;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.eclipse.microprofile.context.ManagedExecutor;

@Recorder
public class CamundaEngineRecorder {

  protected static final String DEFAULT_JDBC_URL =
      "jdbc:h2:mem:camunda;MVCC=TRUE;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE";

  public void configureProcessEngineCdiBeans(BeanContainer beanContainer) {

    if (BeanManagerLookup.localInstance == null) {
      BeanManagerLookup.localInstance = beanContainer.instance(BeanManager.class);
    }
  }

  public RuntimeValue<ProcessEngineConfigurationImpl> createProcessEngineConfiguration(
      BeanContainer beanContainer,
      CamundaEngineConfig camundaEngineConfig) {

    // TODO: replace Standalone with JTA configuration
    ProcessEngineConfigurationImpl configuration =
        beanContainer.instance(CdiStandaloneProcessEngineConfiguration.class);

    // TODO: replace hardcoded DB configuration with Agroal code
    configuration.setJdbcUrl(DEFAULT_JDBC_URL);
    configuration.setDatabaseSchemaUpdate("true");

    // configure job executor,
    // if not already configured by a custom configuration
    if (configuration.getJobExecutor() == null) {

      int threadPoolSize = camundaEngineConfig.threadPool.size;
      int executorQueueSize = camundaEngineConfig.threadPool.queueSize;

      // create a non-bean ManagedExecutor instance. This instance
      // delegates tasks to the Quarkus core Executor/thread pool.
      ManagedExecutor managedExecutor = ManagedExecutor.builder()
          .maxQueued(executorQueueSize)
          .maxAsync(threadPoolSize)
          .build();

      ManagedJobExecutor quarkusJobExecutor = new ManagedJobExecutor(managedExecutor);
      configuration.setJobExecutor(quarkusJobExecutor);
    }

    List<BpmnParseListener> postBPMNParseListeners = configuration.getCustomPostBPMNParseListeners();
    if (postBPMNParseListeners == null) {
      ArrayList<BpmnParseListener> parseListeners = new ArrayList<>();
      parseListeners.add(new CdiEventSupportBpmnParseListener());
      configuration.setCustomPostBPMNParseListeners(parseListeners);

    } else {
      postBPMNParseListeners.add(new CdiEventSupportBpmnParseListener());

    }

    return new RuntimeValue<>(configuration);
  }

  public RuntimeValue<ProcessEngine> createProcessEngine(
      RuntimeValue<ProcessEngineConfigurationImpl> configurationRuntimeValue) {

    // build process engine
    ProcessEngineConfigurationImpl configuration = configurationRuntimeValue.getValue();
    ProcessEngine processEngine = configuration.buildProcessEngine();

    // register process engine with the runtime container delegate
    RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    return new RuntimeValue<>(processEngine);
  }

  public void registerShutdownTask(ShutdownContext shutdownContext,
                                   RuntimeValue<ProcessEngine> processEngine) {

    // cleanup on application shutdown
    shutdownContext.addShutdownTask(() -> {
      ProcessEngine engine = processEngine.getValue();

      // shutdown the JobExecutor
      ProcessEngineConfigurationImpl configuration
          = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
      JobExecutor executor = configuration.getJobExecutor();
      executor.shutdown();

      // deregister the Process Engine from the runtime container delegate
      RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
      runtimeContainerDelegate.unregisterProcessEngine(processEngine.getValue());

    });
  }

}

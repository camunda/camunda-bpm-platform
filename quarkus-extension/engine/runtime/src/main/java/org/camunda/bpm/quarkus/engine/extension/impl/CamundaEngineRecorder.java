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

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

import static com.arjuna.ats.jta.TransactionManager.transactionManager;
import static io.quarkus.datasource.common.runtime.DataSourceUtil.DEFAULT_DATASOURCE_NAME;

@Recorder
public class CamundaEngineRecorder {

  public void configureProcessEngineCdiBeans(BeanContainer beanContainer) {

    if (BeanManagerLookup.localInstance == null) {
      BeanManagerLookup.localInstance = beanContainer.instance(BeanManager.class);
    }
  }

  public RuntimeValue<ProcessEngineConfigurationImpl> createProcessEngineConfiguration(BeanContainer beanContainer,
                                                                                       CamundaEngineConfig config) {
    QuarkusProcessEngineConfiguration configuration = beanContainer.instance(QuarkusProcessEngineConfiguration.class);

    if (configuration.getDataSource() == null) {
      String datasource = config.datasource.orElse(null);
      configureDatasource(configuration, datasource);
    }

    if (configuration.getTransactionManager() == null) {
      configuration.setTransactionManager(transactionManager());
    }

    // configure job executor,
    // if not already configured by a custom configuration
    if (configuration.getJobExecutor() == null) {

      int maxPoolSize = config.jobExecutor.threadPool.maxPoolSize;
      int queueSize = config.jobExecutor.threadPool.queueSize;

      // create a non-bean ManagedExecutor instance. This instance
      // delegates tasks to the Quarkus core Executor/thread pool.
      ManagedExecutor managedExecutor = ManagedExecutor.builder()
          .maxQueued(queueSize)
          .maxAsync(maxPoolSize)
          .build();

      ManagedJobExecutor quarkusJobExecutor = new ManagedJobExecutor(managedExecutor, config);
      configuration.setJobExecutor(quarkusJobExecutor);
    }

    configureCdiEventBridge(configuration);

    return new RuntimeValue<>(configuration);
  }

  protected void configureDatasource(QuarkusProcessEngineConfiguration configuration, String datasourceName) {
    AgroalDataSource dataSource = null;
    if (datasourceName != null) {
      dataSource = DataSources.fromName(datasourceName);

    } else {
      dataSource = DataSources.fromName(DEFAULT_DATASOURCE_NAME);

    }
    configuration.setDataSource(dataSource);
  }

  protected void configureCdiEventBridge(QuarkusProcessEngineConfiguration configuration) {
    List<BpmnParseListener> postBPMNParseListeners = configuration.getCustomPostBPMNParseListeners();
    if (postBPMNParseListeners == null) {
      ArrayList<BpmnParseListener> parseListeners = new ArrayList<>();
      parseListeners.add(new CdiEventSupportBpmnParseListener());
      configuration.setCustomPostBPMNParseListeners(parseListeners);

    } else {
      postBPMNParseListeners.add(new CdiEventSupportBpmnParseListener());

    }
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
      runtimeContainerDelegate.unregisterProcessEngine(engine);

    });
  }

}

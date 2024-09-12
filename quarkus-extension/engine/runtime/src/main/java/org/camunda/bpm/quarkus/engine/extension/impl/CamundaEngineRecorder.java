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

import static com.arjuna.ats.jta.TransactionManager.transactionManager;
import static io.quarkus.datasource.common.runtime.DataSourceUtil.DEFAULT_DATASOURCE_NAME;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.context.SmallRyeManagedExecutor;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.quarkus.engine.extension.CamundaEngineConfig;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.extension.event.CamundaEngineStartupEvent;
import org.eclipse.microprofile.context.ManagedExecutor;

@Recorder
public class CamundaEngineRecorder {

  public void configureProcessEngineCdiBeans(BeanContainer beanContainer) {

    if (BeanManagerLookup.localInstance == null) {
      BeanManagerLookup.localInstance = getBeanFromContainer(BeanManager.class, beanContainer);
    }
  }

  public RuntimeValue<ProcessEngineConfigurationImpl> createProcessEngineConfiguration(BeanContainer beanContainer,
                                                                                       CamundaEngineConfig config) {

    QuarkusProcessEngineConfiguration configuration = getBeanFromContainer(QuarkusProcessEngineConfiguration.class,
        beanContainer);

    // apply properties from config before any other configuration.
    PropertyHelper.applyProperties(configuration, config.genericConfig(), PropertyHelper.KEBAB_CASE);

    if (configuration.getDataSource() == null) {
      String datasource = config.datasource().orElse(DEFAULT_DATASOURCE_NAME);
      configuration.setDataSource(DataSources.fromName(datasource));
    }

    if (configuration.getTransactionManager() == null) {
      configuration.setTransactionManager(transactionManager());
    }

    // configure job executor,
    // if not already configured by a custom configuration
    if (configuration.getJobExecutor() == null) {
      configureJobExecutor(configuration, config);
    }

    configureCdiEventBridge(configuration);

    return new RuntimeValue<>(configuration);
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

  public void fireCamundaEngineStartEvent() {
    Arc.container().beanManager()
        .getEvent()
        .select(CamundaEngineStartupEvent.class)
        .fire(new CamundaEngineStartupEvent());
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

  protected void configureJobExecutor(ProcessEngineConfigurationImpl configuration,
                                      CamundaEngineConfig config) {

    int maxPoolSize = config.jobExecutor().threadPool().maxPoolSize();
    int queueSize = config.jobExecutor().threadPool().queueSize();

    // create a non-bean ManagedExecutor instance. This instance
    // uses it's own Executor/thread pool.
    ManagedExecutor managedExecutor = SmallRyeManagedExecutor.builder()
        .maxQueued(queueSize)
        .maxAsync(maxPoolSize)
        .withNewExecutorService()
        .build();
    ManagedJobExecutor quarkusJobExecutor = new ManagedJobExecutor(managedExecutor);

    // apply job executor configuration properties
    PropertyHelper
        .applyProperties(quarkusJobExecutor, config.jobExecutor().genericConfig(), PropertyHelper.KEBAB_CASE);

    configuration.setJobExecutor(quarkusJobExecutor);
  }

  /**
   * Retrieves a bean of the given class from the bean container.
   *
   * @param beanClass     the class of the desired bean to fetch from the container
   * @param beanContainer the bean container
   * @param <T>           the type of the bean to fetch
   * @return the bean
   */
  protected <T> T getBeanFromContainer(Class<T> beanClass, BeanContainer beanContainer) {
    try (BeanContainer.Instance<T> beanManager = beanContainer.beanInstanceFactory(beanClass).create()) {
      return beanManager.get();
    }
  }
}

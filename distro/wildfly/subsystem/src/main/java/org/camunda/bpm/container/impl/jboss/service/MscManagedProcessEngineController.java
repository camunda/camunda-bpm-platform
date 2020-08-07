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
package org.camunda.bpm.container.impl.jboss.service;

import org.camunda.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration;
import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.plugin.JBossConnectProcessEnginePlugin;
import org.camunda.bpm.container.impl.jboss.util.JBossCompatibilityExtension;
import org.camunda.bpm.container.impl.jboss.util.Tccl;
import org.camunda.bpm.container.impl.jboss.util.Tccl.Operation;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngineController;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Service responsible for starting / stopping a managed process engine inside the Msc</p>
 *
 * <p>This service is used for managing process engines that are started / stopped
 * through the application server management infrastructure.</p>
 *
 * <p>This is the Msc counterpart of the {@link JmxManagedProcessEngineController}</p>
 *
 * @author Daniel Meyer
 */
public class MscManagedProcessEngineController extends MscManagedProcessEngine {

  private final static Logger LOGGER = Logger.getLogger(MscManagedProcessEngineController.class.getName());

  protected static final String CONNECT_PROCESS_ENGINE_PLUGIN_NAME = "org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin";

  protected InjectedValue<ExecutorService> executorInjector = new InjectedValue<ExecutorService>();

  // Injecting these values makes the MSC aware of our dependencies on these resources.
  // This ensures that they are available when this service is started
  protected final InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();
  protected final InjectedValue<DataSourceReferenceFactoryService> datasourceBinderServiceInjector = new InjectedValue<DataSourceReferenceFactoryService>();
  protected final InjectedValue<MscRuntimeContainerJobExecutor> mscRuntimeContainerJobExecutorInjector = new InjectedValue<MscRuntimeContainerJobExecutor>();

  protected ManagedProcessEngineMetadata processEngineMetadata;

  protected JtaProcessEngineConfiguration processEngineConfiguration;

  /**
   * Instantiate  the process engine controller for a process engine configuration.
   *
   */
  public MscManagedProcessEngineController(ManagedProcessEngineMetadata processEngineConfiguration) {
    this.processEngineMetadata = processEngineConfiguration;
  }

  public void start(final StartContext context) throws StartException {
    context.asynchronous();
    executorInjector.getValue().submit(new Runnable() {
      public void run() {
        try {
          startInternal(context);
          context.complete();

        } catch (StartException e) {
          context.failed(e);

        } catch (Throwable e) {
          context.failed(new StartException(e));

        }
      }
    });
  }

  public void stop(final StopContext context) {
    stopInternal(context);
  }

  protected void stopInternal(StopContext context) {

    try {
      super.stop(context);

    } finally {

      try {
        processEngine.close();

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "exception while closing process engine", e);

      }
    }
  }

  public void startInternal(StartContext context) throws StartException {
    // setting the TCCL to the Classloader of this module.
    // this exploits a hack in MyBatis allowing it to use the TCCL to load the
    // mapping files from the process engine module
    Tccl.runUnderClassloader(new Operation<Void>() {
      public Void run() {
        startProcessEngine();
        return null;
      }

    }, ProcessEngine.class.getClassLoader());

    // invoke super start behavior.
    super.start(context);
  }

  protected void startProcessEngine() {

    processEngineConfiguration = createProcessEngineConfiguration();

    // set the name for the process engine
    processEngineConfiguration.setProcessEngineName(processEngineMetadata.getEngineName());

    // set the value for the history
    processEngineConfiguration.setHistory(processEngineMetadata.getHistoryLevel());

    // use the injected datasource
    processEngineConfiguration.setDataSource((DataSource) datasourceBinderServiceInjector.getValue().getReference().getInstance());

    // use the injected transaction manager
    processEngineConfiguration.setTransactionManager(transactionManagerInjector.getValue());

    // set auto schema update
    if(processEngineMetadata.isAutoSchemaUpdate()) {
      processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    } else {
      processEngineConfiguration.setDatabaseSchemaUpdate("off");
    }

    // set db table prefix
    if( processEngineMetadata.getDbTablePrefix() != null ) {
      processEngineConfiguration.setDatabaseTablePrefix(processEngineMetadata.getDbTablePrefix());
    }

    // set job executor on process engine.
    MscRuntimeContainerJobExecutor mscRuntimeContainerJobExecutor = mscRuntimeContainerJobExecutorInjector.getValue();
    processEngineConfiguration.setJobExecutor(mscRuntimeContainerJobExecutor);

    PropertyHelper.applyProperties(processEngineConfiguration, processEngineMetadata.getConfigurationProperties());

    addProcessEnginePlugins(processEngineConfiguration);

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  protected void addProcessEnginePlugins(JtaProcessEngineConfiguration processEngineConfiguration) {
    // add process engine plugins:
    List<ProcessEnginePluginXml> pluginConfigurations = processEngineMetadata.getPluginConfigurations();

    boolean isConnectPluginAdded = false;

    for (ProcessEnginePluginXml pluginXml : pluginConfigurations) {
      // create plugin instance
      ProcessEnginePlugin plugin = null;
      String pluginClassName = pluginXml.getPluginClass();
      try {
        plugin = (ProcessEnginePlugin) createInstance(pluginClassName);
      } catch(ClassCastException e) {
        throw new ProcessEngineException("Process engine plugin '"+pluginClassName+"' does not implement interface "+ProcessEnginePlugin.class.getName()+"'.");
      }

      // apply configured properties
      Map<String, String> properties = pluginXml.getProperties();
      PropertyHelper.applyProperties(plugin, properties);

      // add to configuration
      processEngineConfiguration.getProcessEnginePlugins().add(plugin);

      if(pluginClassName.equals(CONNECT_PROCESS_ENGINE_PLUGIN_NAME)) {
        isConnectPluginAdded = true;
      }
    }

    // add connect plugin to load connectors
    if (!isConnectPluginAdded) {
      processEngineConfiguration.getProcessEnginePlugins().add(new JBossConnectProcessEnginePlugin());
    }
  }

  protected JtaProcessEngineConfiguration createProcessEngineConfiguration() {

    String configurationClassName = ManagedJtaProcessEngineConfiguration.class.getName();
    if(processEngineMetadata.getConfiguration() != null && !processEngineMetadata.getConfiguration().isEmpty()) {
      configurationClassName = processEngineMetadata.getConfiguration();
    }

    Object configurationObject = createInstance(configurationClassName);

    if (configurationObject instanceof JtaProcessEngineConfiguration) {
      return (JtaProcessEngineConfiguration) configurationObject;

    } else {
      throw new ProcessEngineException("Configuration class '"+configurationClassName+"' " +
      		"is not a subclass of " + JtaProcessEngineConfiguration.class.getName());
    }

  }

  private Object createInstance(String configurationClassName) {
    try {
      Class<?> configurationClass = getClass().getClassLoader().loadClass(configurationClassName);
      return configurationClass.newInstance();

    } catch (Exception e) {
      throw new ProcessEngineException("Could not load '"+configurationClassName+"': the class must be visible from the camunda-wildfly-subsystem module.", e);
    }
  }

  public Injector<TransactionManager> getTransactionManagerInjector() {
    return transactionManagerInjector;
  }

  public Injector<DataSourceReferenceFactoryService> getDatasourceBinderServiceInjector() {
    return datasourceBinderServiceInjector;
  }

  public InjectedValue<MscRuntimeContainerJobExecutor> getMscRuntimeContainerJobExecutorInjector() {
    return mscRuntimeContainerJobExecutorInjector;
  }

  public static void initializeServiceBuilder(ManagedProcessEngineMetadata processEngineConfiguration, MscManagedProcessEngineController service,
          ServiceBuilder<ProcessEngine> serviceBuilder, String jobExecutorName) {

    ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(processEngineConfiguration.getDatasourceJndiName());
    serviceBuilder.addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, service.getTransactionManagerInjector())
      .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, service.getDatasourceBinderServiceInjector())
      .addDependency(ServiceNames.forMscRuntimeContainerDelegate(), MscRuntimeContainerDelegate.class, service.getRuntimeContainerDelegateInjector())
      .addDependency(ServiceNames.forMscRuntimeContainerJobExecutorService(jobExecutorName), MscRuntimeContainerJobExecutor.class, service.getMscRuntimeContainerJobExecutorInjector())
      .addDependency(ServiceNames.forMscExecutorService())
      .setInitialMode(Mode.ACTIVE);

    if(processEngineConfiguration.isDefault()) {
      serviceBuilder.addAliases(ServiceNames.forDefaultProcessEngine());
    }

    JBossCompatibilityExtension.addServerExecutorDependency(serviceBuilder, service.getExecutorInjector(), false);

  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public InjectedValue<ExecutorService> getExecutorInjector() {
    return executorInjector;
  }

  public ManagedProcessEngineMetadata getProcessEngineMetadata() {
    return processEngineMetadata;
  }
}

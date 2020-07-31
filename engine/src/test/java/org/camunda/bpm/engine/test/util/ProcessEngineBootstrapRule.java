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
package org.camunda.bpm.engine.test.util;

import java.util.List;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class ProcessEngineBootstrapRule extends TestWatcher {

  private ProcessEngine processEngine;
  protected Consumer<ProcessEngineConfigurationImpl> processEngineConfigurator;

  public ProcessEngineBootstrapRule() {
    this("camunda.cfg.xml");
  }

  public ProcessEngineBootstrapRule(String configurationResource) {
    this(configurationResource, null);
  }

  public ProcessEngineBootstrapRule(Consumer<ProcessEngineConfigurationImpl> processEngineConfigurator) {
    this("camunda.cfg.xml", processEngineConfigurator);
  }

  public ProcessEngineBootstrapRule(String configurationResource, Consumer<ProcessEngineConfigurationImpl> processEngineConfigurator) {
    this.processEngineConfigurator = processEngineConfigurator;
    this.processEngine = bootstrapEngine(configurationResource);
  }

  public ProcessEngine bootstrapEngine(String configurationResource) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource(configurationResource);
    configureEngine(processEngineConfiguration);
    return processEngineConfiguration.buildProcessEngine();
  }

  public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
    if (processEngineConfigurator != null) {
      processEngineConfigurator.accept(configuration);
    }
    return configuration;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  @Override
  protected void finished(Description description) {
    deleteHistoryCleanupJob();
    processEngine.close();
    ProcessEngines.unregister(processEngine);
    processEngine = null;
  }

  private void deleteHistoryCleanupJob() {
    final List<Job> jobs = processEngine.getHistoryService().findHistoryCleanupJobs();
    for (final Job job: jobs) {
      ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          commandContext.getJobManager().deleteJob((JobEntity) job);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
          return null;
        }
      });
    }
  }

}

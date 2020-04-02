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
package org.camunda.bpm.integrationtest.jobexecutor.beans;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.impl.ManagedJobExecutor;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;

@Startup
@Singleton
public class ManagedJobExecutorBean {

  @Resource
  private ManagedExecutorService managedExecutorService;

  protected ProcessEngine processEngine;
  protected ManagedJobExecutor jobExecutor;

  @PostConstruct
  public void startEngine() {
    JtaProcessEngineConfiguration processEngineConfiguration = new JtaProcessEngineConfiguration();
    processEngineConfiguration.setDatabaseSchemaUpdate("true")
      .setHistory("auto")
      .setDbMetricsReporterActivate(false)
      .setDataSourceJndiName("java:jboss/datasources/ProcessEngine");
    processEngineConfiguration.setTransactionManagerJndiName("java:/TransactionManager");
    jobExecutor = new ManagedJobExecutor(managedExecutorService);
    processEngine = processEngineConfiguration
        .setJobExecutor(jobExecutor)
        .buildProcessEngine();
  }

  @PreDestroy
  public void stopEngine() {
    processEngine.close();
    jobExecutor.shutdown();
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
}

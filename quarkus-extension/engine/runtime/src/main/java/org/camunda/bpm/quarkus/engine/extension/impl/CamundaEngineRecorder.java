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

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;

@Recorder
public class CamundaEngineRecorder {

  protected static final String DEFAULT_JDBC_URL =
      "jdbc:h2:mem:camunda;MVCC=TRUE;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE";

  public RuntimeValue<ProcessEngine> createProcessEngine(BeanContainer beanContainer) {

    // TODO: replace hardcoded DB configuration with Agroal code
    CdiStandaloneProcessEngineConfiguration configuration =
        beanContainer.instance(CdiStandaloneProcessEngineConfiguration.class);
    configuration.setJdbcUrl(DEFAULT_JDBC_URL);
    configuration.setDatabaseSchemaUpdate("true");

    if (BeanManagerLookup.localInstance == null) {
      BeanManagerLookup.localInstance = beanContainer.instance(BeanManager.class);
    }

    // build process engine
    ProcessEngine processEngine = configuration.buildProcessEngine();

    // register process engine with the runtime container delegate
    RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    return new RuntimeValue<>(processEngine);
  }

  public void registerShutdownTask(ShutdownContext shutdownContext,
                                   RuntimeValue<ProcessEngine> processEngine) {

    shutdownContext.addShutdownTask(() -> {
      RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
      runtimeContainerDelegate.unregisterProcessEngine(processEngine.getValue());
    });
  }

}

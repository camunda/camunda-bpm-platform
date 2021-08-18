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
package org.camunda.bpm.quarkus.engine.test.id;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ConfigureDbIdGeneratorTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .engineConfig(() -> (QuarkusProcessEngineConfiguration) new QuarkusProcessEngineConfiguration()
          .setIdGenerator(null))
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  public TaskService taskService;

  @Inject
  protected ProcessEngine processEngine;

  @Test
  public void shouldConfigureDbIdGenerator() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    String id = taskService.createTaskQuery().singleResult().getId();
    assertThat(Long.parseLong(id)).isGreaterThan(0);

    ProcessEngineConfigurationImpl engineConfig =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    assertThat(engineConfig.getIdGenerator()).isInstanceOf(DbIdGenerator.class);
  }

}

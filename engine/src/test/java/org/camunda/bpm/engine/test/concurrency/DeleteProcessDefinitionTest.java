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
package org.camunda.bpm.engine.test.concurrency;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteProcessDefinitionTest extends ConcurrencyTestCase {

  @After
  public void tearDown() {
    repositoryService.createDeploymentQuery().list().forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
    processEngineConfiguration.getDeploymentCache().purgeCache();
  }

  protected static class ControllableDeleteProcessDefinitionCommand extends ControllableCommand<Void> {

    protected String processDefinitionId;

    protected Exception exception;

    public ControllableDeleteProcessDefinitionCommand(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      commandContext.getProcessEngineConfiguration()
          .getRepositoryService()
          .deleteProcessDefinition(processDefinitionId);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

  // Deploy V1
  @Deployment(resources = "org/camunda/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml")
  @Test
  public void shouldDeployProcessAfterDeletionWithSameMessageName() {
    // given

    // Deploy V2
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml")
        .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("otherMessageProcess")
        .list();

    // assume
    assertThat(processDefinitions.size()).isEqualTo(2);

    ThreadControl thread1 = executeControllableCommand(
        new ControllableDeleteProcessDefinitionCommand(processDefinitions.get(0).getId()));
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(
        new ControllableDeleteProcessDefinitionCommand(processDefinitions.get(1).getId()));
    thread2.reportInterrupts();
    thread2.waitForSync();

    //delete process definition, but not commit transaction
    thread1.makeContinue();
    thread1.waitForSync();

    //delete process definition, but not commit transaction
    thread2.makeContinue();
    thread2.waitForSync();

    //commit transaction of thread 1
    thread1.makeContinue();
    thread1.waitUntilDone();

    //when: commit transaction of thread 2
    thread2.makeContinue();
    thread2.waitUntilDone();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().list()).isEmpty();
    assertThat(runtimeService.createEventSubscriptionQuery().list()).isEmpty();

    // successfully deploy a new process definition V1 with the same message name
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml")
        .deploy();
  }

}

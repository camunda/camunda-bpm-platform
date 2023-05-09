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
package org.camunda.bpm.quarkus.engine.test.persistence;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class UserTransactionIntegrationTest {

  @RegisterExtension
  static QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  protected UserTransaction userTransactionManager;

  @Inject
  protected RuntimeService runtimeService;

  @Test
  @Deployment
  public void shouldSucceed() throws Exception {

    try {
      userTransactionManager.begin();

      String id = runtimeService.startProcessInstanceByKey("testTxSuccess").getId();

      // assert that the transaction is in good shape:
      assertEquals(Status.STATUS_ACTIVE, userTransactionManager.getStatus());

      // the process instance is visible form our tx:
      ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
          .processInstanceId(id)
          .singleResult();

      assertNotNull(processInstance);

      userTransactionManager.commit();

      userTransactionManager.begin();

      // the process instance is visible in a new tx:
      processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();

      assertNotNull(processInstance);

      userTransactionManager.commit();
    } catch (Exception e) {
      userTransactionManager.rollback();
      throw e;
    }
  }

  @Test
  @Deployment
  public void shouldMarkAsRollbackOnly() throws Exception {

    /* if we start a transaction here and then start
     * a process instance which synchronously invokes a java delegate,
     * if that delegate fails, the transaction is marked rollback only
     */

    try {
      userTransactionManager.begin();

      try {
        runtimeService.startProcessInstanceByKey("testProcessFailure");
        fail("Exception expected");
      } catch (Exception ex) {
        if (!(ex instanceof RuntimeException)) {
          fail("Wrong exception of type " + ex + " RuntimeException expected!");
        }
        if (!ex.getMessage().contains("I'm a complete failure!")) {
          fail("Different message expected");
        }
      }

      // assert that now our transaction is marked rollback-only:
      assertEquals(Status.STATUS_MARKED_ROLLBACK, userTransactionManager.getStatus());

    } finally {
      // make sure we always rollback
      userTransactionManager.rollback();
    }
  }

  @Test
  @Deployment
  public void shouldNotStoreProcessInstance() throws Exception {

    /* if we start a transaction here and then successfully start
     * a process instance, if our transaction is rolled back,
     * the process instance is not persisted.
     */

    try {
      userTransactionManager.begin();

      String id = runtimeService.startProcessInstanceByKey("testApplicationFailure").getId();

      // assert that the transaction is in good shape:
      assertEquals(Status.STATUS_ACTIVE, userTransactionManager.getStatus());

      // now rollback the transaction (simulating an application failure after the process engine is done).
      userTransactionManager.rollback();

      userTransactionManager.begin();

      // the process instance does not exist:
      ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
          .processInstanceId(id)
          .singleResult();

      assertNull(processInstance);

      userTransactionManager.commit();
    } catch (Exception e) {
      userTransactionManager.rollback();
      throw e;
    }
  }

  @Named
  @Dependent
  public static class FailingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
      throw new RuntimeException("I'm a complete failure!");
    }

  }

}

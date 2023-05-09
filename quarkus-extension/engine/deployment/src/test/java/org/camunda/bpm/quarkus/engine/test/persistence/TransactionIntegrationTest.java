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
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TransactionIntegrationTest {

  @RegisterExtension
  static QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  protected ProcessEngine processEngine;

  @Inject
  protected UserBean userBean;

  @Inject
  protected DataSource dataSource;

  @Inject
  protected RuntimeService runtimeService;

  @Inject
  protected TaskService taskService;

  @Inject
  protected ManagementService managementService;

  protected ProcessEngineConfigurationImpl configuration;

  @BeforeEach
  public void assignConfig() {
    configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
  }

  /**
   * This behavior is not engine but container-specific.
   * A transactional method commits to the database.
   */
  @Test
  @Deployment
  public void shouldSucceed() {
    userBean.hello();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(runtimeService.getVariable(processInstance.getId(), "myVar")).isEqualTo("Hello from Printer!");
  }

  /**
   * This behavior is not engine but container-specific.
   * A transactional method triggers a rollback if an exception occurs.
   */
  @Test
  @Deployment
  public void shouldRollbackOnException() {
    // given
    try {
      try (Connection connection = dataSource.getConnection()) {
        try (Statement statement = connection.createStatement()) {
          statement.execute("create table MY_TABLE (MY_TEXT varchar);");
        }

        try (Statement statement = connection.createStatement()) {
          statement.executeUpdate("insert into MY_TABLE values ('test')");
        }

        try (Statement statement = connection.createStatement()) {
          userBean.hello();
          try (ResultSet resultSet = statement.executeQuery("select count(*) as count from MY_TABLE")) {
            resultSet.next();
            int results = resultSet.getInt("count");
            assertThat(results).isEqualTo(1);
          }
        }

        try {
          // when
          userBean.completeTask(taskService.createTaskQuery().singleResult().getId());
          Assertions.fail();
        } catch (ProcessEngineException ignored) {
        } catch (RuntimeException e) {
          fail(e.getMessage());
        }

        // then
        assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("My Task");
        try (Statement statement = connection.createStatement()) {
          try (ResultSet resultSet = statement.executeQuery("select count(*) as count from MY_TABLE")) {
            resultSet.next();
            int results = resultSet.getInt("count");
            assertThat(results).isEqualTo(1);
          }
        }

        // Cleanup
        try (Statement statement = connection.createStatement()) {
          statement.execute("drop table MY_TABLE if exists;");
        }
      }
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/quarkus/engine/test/persistence/TransactionIntegrationTest.shouldPropagateErrorOnException.bpmn20.xml",
      "org/camunda/bpm/quarkus/engine/test/persistence/TransactionIntegrationTest.shouldThrowException.bpmn20.xml"
  })
  @Test
  public void shouldPropagateErrorOnException() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    // when
    TestHelper.waitForJobExecutorToProcessAllJobs(configuration, 20_000, 1000);

    // then
    Incident incident = runtimeService.createIncidentQuery().activityId("servicetask").singleResult();
    assertThat(incident.getIncidentMessage()).isEqualTo("error");
  }

  @Deployment
  @Test
  public void shouldRollbackInServiceTask() {
    // given
    runtimeService.startProcessInstanceByKey("txRollbackServiceTask");

    // when
    TestHelper.waitForJobExecutorToProcessAllJobs(configuration, 20_000, 1_000);

    // then
    Job job = managementService.createJobQuery().singleResult();

    assertThat(job.getRetries()).isEqualTo(0);
    assertThat(job.getExceptionMessage()).isEqualTo("Unable to commit transaction");

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertThat(stacktrace).contains("setRollbackOnly called");
  }

  @Deployment
  @Test
  public void shouldRollbackInServiceTaskWithCustomRetryCycle() {
    // given
    runtimeService.startProcessInstanceByKey("txRollbackServiceTaskWithCustomRetryCycle");

    // when
    TestHelper.waitForJobExecutorToProcessAllJobs(configuration, 20_000, 1_000);

    // then
    Job job = managementService.createJobQuery().singleResult();

    assertThat(job.getRetries()).isEqualTo(0);
    assertThat(job.getExceptionMessage()).isEqualTo("Unable to commit transaction");

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertThat(stacktrace).contains("setRollbackOnly called");
  }

  @Deployment
  @Test
  public void shouldRollbackOnExceptionInTransactionListener() {
    // given
    runtimeService.startProcessInstanceByKey("failingTransactionListener");

    // when
    TestHelper.waitForJobExecutorToProcessAllJobs(configuration, 20_000, 1_000);

    // then
    Job job = managementService.createJobQuery().singleResult();

    assertThat(job.getRetries()).isEqualTo(0);
    assertThat(job.getExceptionMessage()).isEqualTo("Unable to commit transaction");

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertThat(stacktrace).contains("java.lang.RuntimeException: exception in transaction listener");
  }

  @Dependent
  public static class BeanWithException {

    public void doSomething() {
      throw new RuntimeException("error");
    }

  }

  @Dependent
  @Named
  public static class FailingTransactionListenerDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

      Context.getCommandContext()
          .getTransactionContext()
          .addTransactionListener(TransactionState.COMMITTING, this::throwException);
    }

    protected void throwException(CommandContext context) {
      throw new RuntimeException("exception in transaction listener");
    }

  }

  @Dependent
  @Named
  public static class FooDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) {
      delegateExecution.setVariable("myVar", "Hello from Printer!");
    }

  }

  @Named
  @Dependent
  public static class ServiceTaskBean implements JavaDelegate {

    @Inject
    protected BeanWithException beanWithException;

    @Transactional
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      beanWithException.doSomething();
    }

  }

  @Named
  @Dependent
  public static class TransactionRollbackDelegate implements JavaDelegate {

    @Inject
    protected TransactionManager transactionManager;

    @Override
    public void execute(DelegateExecution execution) throws SystemException {
      transactionManager.setRollbackOnly();
    }

  }

  @ApplicationScoped
  public static class UserBean {

    @Inject
    protected RuntimeService runtimeService;

    @Inject
    protected TaskService taskService;

    @Inject
    protected DataSource dataSource;

    @Transactional
    public void hello() {
      runtimeService.startProcessInstanceByKey("helloProcess");
    }

    @Transactional
    public void completeTask(String taskId) {

      int nrOfRows = 0;
      try {
        try (Connection connection = dataSource.getConnection()) {
          try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select count(*) as count from MY_TABLE")) {
              resultSet.next();
              int results = resultSet.getInt("count");
              assertThat(results).isEqualTo(1);
            }
          }

          try (Statement statement = connection.createStatement()) {
            nrOfRows = statement.executeUpdate("insert into MY_TABLE values ('test')");
          }

          try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select count(*) as count from MY_TABLE")) {
              resultSet.next();
              int results = resultSet.getInt("count");
              assertThat(results).isEqualTo(2);
            }
          }
        }
      } catch (SQLException ignored) {
      }

      if (nrOfRows != 1) {
        throw new RuntimeException("Insert into MY_TABLE failed");
      }

      taskService.complete(taskId);
    }

  }

}
package org.camunda.bpm.engine.spring.test;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.MockExpressionManager;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.Properties;

@RunWith(Enclosed.class)
public class TransactionRollbackTest {

  @Component
  private static class EvaluateDmn implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      try {
        getJavaDelegate().execute(execution);
      } catch (Exception e) {
        throw new BpmnError("DmnFailed", e.getMessage());
      }
    }

    public JavaDelegate getJavaDelegate() {
      return new JavaDelegate() {
        @Override
        public void execute(DelegateExecution execution) throws Exception {
          execution.getProcessEngineServices().getDecisionService()
              .evaluateDecisionByKey("does not exist")
              .variables(Variables.createVariables())
              .evaluate();
        }
      };
    }
  }

  @RunWith(SpringJUnit4ClassRunner.class)
  @ContextConfiguration(classes = TransactionRollbackTest.SpringWithRollback.TestConfig.class)
  @ActiveProfiles("SpringWithRollback")
  public static class SpringWithRollback {
    @Configuration
    @Profile("SpringWithRollback")
    @ImportResource("classpath:org/camunda/bpm/engine/spring/test/junit4/springTypicalUsageTest-context.xml")
    public static class TestConfig {

      @Bean
      public EvaluateDmn evaluateDmn() {
        return new EvaluateDmn();
      }
    }

    @Autowired
    @Rule
    public ProcessEngineRule processEngineRule;


    @Autowired
    public RuntimeService runtimeService;

    @Test(expected = UnexpectedRollbackException.class)
    @Deployment(resources = "org/camunda/bpm/engine/spring/test/TransactionRollback.bpmn")
    public void failsWithRollback() throws Exception {
      runtimeService.startProcessInstanceByKey("TransactionRollback");
    }

  }

  @RunWith(SpringJUnit4ClassRunner.class)
  @ContextConfiguration(classes = TransactionRollbackTest.SpringWithWorkaround.TestConfig.class)
  @ActiveProfiles("SpringWithWorkaround")
  public static class SpringWithWorkaround {
    @Configuration
    @Profile("SpringWithWorkaround")
    @ImportResource("classpath:org/camunda/bpm/engine/spring/test/junit4/springTypicalUsageTest-context.xml")
    public static class TestConfig {

      @Bean
      public JavaDelegate evaluateDmn() {
        return new JavaDelegate() {

          @Autowired
          private LogicExecution logicExecution;

          @Override
          public void execute(DelegateExecution execution) throws Exception {
            try {
              logicExecution.execute(execution);
            } catch (Exception e) {
              throw new BpmnError("DmnFailed", e.getMessage());
            }
          }
        };
      }

      public class LogicExecution implements JavaDelegate {
        public void execute(DelegateExecution execution) throws Exception {
          new EvaluateDmn().getJavaDelegate().execute(execution);
        }
      }

      @Bean
      public LogicExecution createExecutor(PlatformTransactionManager txManager) {
        TransactionProxyFactoryBean proxy = new TransactionProxyFactoryBean();

        // Inject transaction manager here
        proxy.setTransactionManager(txManager);

        // Define wich object instance is to be proxied (your bean)
        proxy.setTarget(new LogicExecution());

        // Programmatically setup transaction attributes
        Properties transactionAttributes = new Properties();
        transactionAttributes.put("*", "PROPAGATION_REQUIRES_NEW");
        proxy.setTransactionAttributes(transactionAttributes);

        // Finish FactoryBean setup
        proxy.afterPropertiesSet();
        return (LogicExecution) proxy.getObject();
      }
    }

    @Autowired
    @Rule
    public ProcessEngineRule processEngineRule;


    @Autowired
    public RuntimeService runtimeService;

    @Test(expected = UnexpectedRollbackException.class)
    @Deployment(resources = "org/camunda/bpm/engine/spring/test/TransactionRollback.bpmn")
    public void failsWithRollback() throws Exception {
      runtimeService.startProcessInstanceByKey("TransactionRollback");
    }

  }


  public static class Standalone {

    @Rule
    public final ProcessEngineRule processEngineRule = new ProcessEngineRule(new StandaloneInMemProcessEngineConfiguration() {{
      jobExecutorActivate = false;
      expressionManager = new MockExpressionManager();
      isDbMetricsReporterActivate = false;
      databaseSchemaUpdate = DB_SCHEMA_UPDATE_CREATE_DROP;
    }}.buildProcessEngine());


    @Test
    @Deployment(resources = "org/camunda/bpm/engine/spring/test/TransactionRollback.bpmn")
    public void worksWithStandalone() throws Exception {
      Mocks.register("evaluateDmn", new EvaluateDmn());

      processEngineRule.getRuntimeService().startProcessInstanceByKey("TransactionRollback");
    }
  }
}

package com.camunda.fox.platform.qa.deployer;

import org.activiti.engine.test.Deployment;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@Deployment(resources = {
  "processes/CdiResolvingBean.bpmn20.xml"
})
public abstract class AbstractSimpleDeploymentTestBase {
  
  @EJB
  private ApplicationArchiveContext contextExecutor;
  
  @Inject
  private ProcessEngine processEngine;
  
  @Inject
  private TestCdiBean cdiBean;
  
  @Test
  public void testInjection() {
    assertNotNull(processEngine);
    assertNotNull(contextExecutor);
    assertNotNull(cdiBean);
  }
  
  @Test
  public void shouldHaveProcessDeployed() throws Exception {
    
    // given
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    
    // when
    long deployedProcessesCount = repositoryService.createDeploymentQuery().count();
    
    // then
    assertEquals(1, deployedProcessesCount);
  }
  
  @Test
  public void shouldBeAbleToExecuteDeployedProcess() throws Exception {
    
    // given
    RuntimeService runtimeService = processEngine.getRuntimeService();
    cdiBean.setInvoked(false);
    
    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CdiResolvingBean");
    
    // then
    assertTrue(cdiBean.isInvoked());
    assertTrue(processInstance.isEnded());
  }
  
  @Test
  @Deployment(resources={
    "processes/CdiResolvingBeanFromJobExecutor.bpmn20.xml"
  })
  public void shouldDeployAnAdditionalProcess() throws Exception {
    // given
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    // when
    ProcessDefinition definition1 = repositoryService
        .createProcessDefinitionQuery()
          .processDefinitionKey("CdiResolvingBeanFromJobExecutor")
          .singleResult();
    
    ProcessDefinition definition2 = repositoryService
        .createProcessDefinitionQuery()
          .processDefinitionKey("CdiResolvingBean")
          .singleResult();
    
    // then
    assertNotNull(definition1);
    assertNotNull(definition2);
  }
  
  @Test
  @Deployment(resources={
    "processes/CdiResolvingBeanFromJobExecutor.bpmn20.xml"
  })
  public void shouldExecuteProcessUsingJobExecutor() throws Exception {

    // given
    RuntimeService runtimeService = processEngine.getRuntimeService();
    HistoryService historyService = processEngine.getHistoryService();
    cdiBean.setInvoked(false);
    
    // when
    runtimeService.startProcessInstanceByKey("CdiResolvingBeanFromJobExecutor");
    
    Thread.sleep(2000);
    
    long finishedProcessInstances = historyService.createHistoricProcessInstanceQuery().finished().count();
    
    // then
    assertTrue(cdiBean.isInvoked());
    assertEquals(1, finishedProcessInstances);
  }
  
  
  @Test
  @Deployment(resources={
    "processes/DelegateExecution.bpmn20.xml"
  })
  public void shouldBeAbleToInvokeDelegate() throws Exception {

    // given
    RuntimeService runtimeService = processEngine.getRuntimeService();
    HistoryService historyService = processEngine.getHistoryService();
    TestDelegate.INVOKED = false;
    
    // when
    runtimeService.startProcessInstanceByKey("DelegateExecution");
    
    long finishedProcessInstances = historyService.createHistoricProcessInstanceQuery().finished().count();
    
    // then
    assertTrue(TestDelegate.INVOKED);
    assertEquals(1, finishedProcessInstances);
  }
}

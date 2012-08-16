package com.camunda.fox.platform.qa.deployer.test;

import org.activiti.engine.test.Deployment;
import com.camunda.fox.platform.qa.deployer.test.demo.DemoDelegate;
import com.camunda.fox.platform.qa.deployer.test.demo.DemoVariableClass;
import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@RunWith(Arquillian.class)
public class ProcessDeploymentTest {

  @org.jboss.arquillian.container.test.api.Deployment
  public static Archive<?> createDeployment() {
    Archive<?> archive = ShrinkWrap
        .create(WebArchive.class, "test.war")
            .addClass(DemoDelegate.class)
            .addClass(DemoVariableClass.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    
    return archive;
  }
  
  @Inject
  private ProcessEngine processEngine;

  @Test
  public void shouldInjectProcessEngine() throws Exception {
    // given
    
    // when
    
    // then
    assertThat(processEngine).isNotNull();
  }
  
  @Test
  @Deployment(resources={
    "processes/ImmediatelyFailing.bpmn20.xml"
  })
  public void shouldDeployProcess() {
    // given
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    // when
    long processDefinitionCount = repositoryService.createProcessDefinitionQuery().count();
    
    // then
    assertThat(processDefinitionCount).isEqualTo(1);
  }
}

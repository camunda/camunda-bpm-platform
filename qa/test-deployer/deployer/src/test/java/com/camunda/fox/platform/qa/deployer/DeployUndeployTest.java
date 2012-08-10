package com.camunda.fox.platform.qa.deployer;

import javax.ejb.EJB;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.test.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.cdi.TestProcessEngineLookup;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import com.camunda.fox.platform.qa.deployer.war.ContextExecutionException;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Arquillian.class)
@Deployment(resources = {
  "processes/CdiResolvingBean.bpmn20.xml"
})
public class DeployUndeployTest {
  
  
  @org.jboss.arquillian.container.test.api.Deployment
  public static Archive<?> createDeplomentArchive() {
    WebArchive archive = ShrinkWrap
                    .create(WebArchive.class)
                      .addClass(TestProcessEngineLookup.class)
                      .addClass(TestCdiBean.class)
                      .addPackages(true, "org.fest")
                      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    
    return archive;
  }
  
  @EJB
  private ApplicationArchiveContext contextExecutor;
  
  @Inject
  private ProcessEngine processEngine;
  
  @Test
  public void shouldBeAbleToResolveCdiBean() throws ContextExecutionException {
    contextExecutor.execute(new ProcessArchiveCallback<Void>() {
      public Void execute() {
        
        try {
          BeanManager beanManager = (BeanManager) InitialContext.doLookup("java:comp/BeanManager");
          assertNotNull(beanManager.getBeans("testCdiBean"));
        } catch (Exception e) {
          throw new RuntimeException("Fail", e);
        }
        return null;
      }
    });
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
}

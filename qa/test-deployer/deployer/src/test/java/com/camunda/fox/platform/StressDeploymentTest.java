package com.camunda.fox.platform;

import com.camunda.fox.platform.qa.deployer.TestCdiBean;
import com.camunda.fox.platform.qa.deployer.TestDelegate;
import org.activiti.engine.test.Deployment;
import com.camunda.fox.engine.util.Instances.InstancesStarter;
import static com.camunda.fox.engine.util.Instances.processInstanceStarter;

import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.junit.Ignore;

import com.camunda.fox.cdi.TestProcessEngineLookup;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@Ignore
@RunWith(Arquillian.class)
public class StressDeploymentTest {
  
  @org.jboss.arquillian.container.test.api.Deployment
  public static Archive<?> createApplicationDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
      .addClass(TestDelegate.class)
      .addClass(TestCdiBean.class)
      .addClass(TestProcessEngineLookup.class)
      .addPackage("com.camunda.fox.platform.qa.util")
      .addPackage("com.camunda.fox.engine.util")
      .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml");  
  }
 
  @Inject
  private ProcessEngine processEngine;
  
  @Test
  @Deployment(resources={
    "processes/CdiResolvingBean.bpmn20.xml",
    "processes/CdiResolvingBeanFromJobExecutor.bpmn20.xml",
    "processes/DelegateExecution.bpmn20.xml",
    "processes/SimpleExpressionEvaluation.bpmn20.xml",
  })
  public void shouldWorkOkWhenManyProcessesAreDeployedInParallel() throws Exception {
    // given
    int processInstanceCount = 5000;
    int threadCount = 10;
    
    String[] processKeys = new String[] {
      "CdiResolvingBean", 
      "CdiResolvingBeanFromJobExecutor", 
      "DelegateExecution", 
      "SimpleExpressionEvaluation"
    };
    
    // when
    InstancesStarter starter = processInstanceStarter()
      .fork(threadCount)
      .processDefinitionKeys(processKeys)
      .count(processInstanceCount)
      .startOn(processEngine);
    
    starter.awaitAllStarted(4000, 20);
    
    long time = starter.runtimeMillis();
    long startedInstances = starter.startedInstances();
    
    // then
    System.out.println("Took " + time + "ms to start " + startedInstances + " process instances");
    //    for (SpawnProcessesRunnable runnable: spawnRunnables) {
    //      runnable.printStatistics();
    //    }
    
    assertEquals(processInstanceCount, startedInstances);
  }
}

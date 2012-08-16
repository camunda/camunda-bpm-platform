package com.camunda.fox.platform;

import org.activiti.engine.test.Deployment;
import static com.camunda.fox.engine.util.Instances.processInstanceStarter;
import com.camunda.fox.engine.util.Instances.InstancesStarter;
import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@Ignore
@RunWith(Arquillian.class)
public class DroolsFlowCompetitionTest {
  
  @org.jboss.arquillian.container.test.api.Deployment
  public static Archive<?> createApplicationDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
      .addPackage("com.camunda.fox.platform.qa.util")
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");  
  }
 
  @Inject
  private ProcessEngine processEngine;
  
  @Test
  // TODO: Default based on class and method name
  @Deployment(resources={
    "processes/ImmediateEnd.bpmn20.xml"
  })
  public void shouldPrintRuntimeForSpawning2000EmptyProcessesSequencially() throws Exception {
    // given
    
    // when 
    InstancesStarter starter = processInstanceStarter()
      .processDefinitionKeys("ImmediateEnd")
      .count(2000)
      .startOn(processEngine);
   
    starter.awaitAllStarted(2000, 0);
    
    long time = starter.runtimeMillis();
    long startedInstances = starter.startedInstances();
    
    // then
    System.out.println("Took " + time + "ms to start " + startedInstances + " process instances");
    System.out.println("Equals " + ((time * 1.0) / startedInstances) + "ms per process instance");
  }
  
  @Test
  @Deployment(resources={
    "processes/ImmediateEnd.bpmn20.xml"
  })
  public void shouldPrintRuntimeForSpawning10000EmptyProcessesInParallel() throws Exception {
    // given
    int threadCount = 10;
    int processesToSpawn = 10000;
    
    String[] processKeys = new String[] {
      "ImmediateEnd"
    };
    
    // when 
    InstancesStarter starter = processInstanceStarter()
      .processDefinitionKeys(processKeys)
      .count(10000)
      .fork(threadCount)
      .startOn(processEngine);
   
    starter.awaitAllStarted(2000, 20);
    
    long time = starter.runtimeMillis();
    long startedInstances = starter.startedInstances();
    
    // then
    System.out.println("Took " + time + "ms to start " + startedInstances + " process instances");
    System.out.println("Equals " + ((time * 1.0) / startedInstances) + "ms per process instance");
  }
}

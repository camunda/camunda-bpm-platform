package org.camunda.bpm.container.impl.jmx.kernel;

import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.camunda.bpm.container.impl.jmx.kernel.util.FailingDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.StartServiceDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.StopServiceDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.TestService;
import org.camunda.bpm.container.impl.jmx.kernel.util.TestServiceType;

/**
 * Testcases for the {@link MBeanServiceContainer} Kernel.
 * 
 * @author Daniel Meyer
 *
 */
public class MBeanServiceContainerTests extends TestCase {
  
  private MBeanServiceContainer serviceContainer;
  
  private ObjectName service1Name = TestServiceType.TYPE1.getServiceName("service1");
  private ObjectName service2Name = TestServiceType.TYPE1.getServiceName("service2");
  private ObjectName service3Name = TestServiceType.TYPE2.getServiceName("service3");
  private ObjectName service4Name = TestServiceType.TYPE2.getServiceName("service4");
  
  private TestService service1 = new TestService();
  private TestService service2 = new TestService();
  private TestService service3 = new TestService();
  private TestService service4 = new TestService();
  
  @Override
  protected void setUp() throws Exception {
    serviceContainer = new MBeanServiceContainer();    
    super.setUp();
  }
  
  protected void tearDown() throws Exception {
    // make sure all MBeans are removed after each test
    MBeanServer mBeanServer = serviceContainer.getmBeanServer();
    if(mBeanServer.isRegistered(service1Name)) {
      mBeanServer.unregisterMBean(service1Name);
    }
    if(mBeanServer.isRegistered(service2Name)) {
      mBeanServer.unregisterMBean(service2Name);
    }
    if(mBeanServer.isRegistered(service3Name)) {
      mBeanServer.unregisterMBean(service3Name);
    }
    if(mBeanServer.isRegistered(service4Name)) {
      mBeanServer.unregisterMBean(service4Name);
    }
    super.tearDown();
  }
  
  public void testStartService() {
    
    // initially the service is not present:
    assertNull(serviceContainer.getService(service1Name));
    
    // we can start a service 
    serviceContainer.startService(service1Name, service1);
    // and get it after that
    assertNotNull(serviceContainer.getService(service1Name));
    assertEquals(service1, serviceContainer.getService(service1Name));
    // as long it is started, I cannot start a second service with the same name:    
    try {
      serviceContainer.startService(service1Name, service1);
      fail("exception expected");
    } catch(Exception e) {
      assertTrue(e.getMessage().contains("service with same name already registered"));
    }
    
    // but, I can start a service with a different name:
    serviceContainer.startService(service2Name, service2);
    // and get it after that
    assertNotNull(serviceContainer.getService(service2Name));
       
  }
  
  public void testStopService() {
        
    // start some service
    serviceContainer.startService(service1Name, service1);
    // it's there 
    assertNotNull(serviceContainer.getService(service1Name));
    
    // stop it:
    serviceContainer.stopService(service1Name);
    // now it's gone 
    assertNull(serviceContainer.getService(service1Name));
   
    try {
      serviceContainer.stopService(service1Name);
      fail("exception expected");
    }catch(Exception e) {
      assertTrue(e.getMessage().contains("no such service registered"));
    }
       
  }
  
  public void testGetServicesByType() {
        
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    List<MBeanService<TestService>> servicesByType1 = serviceContainer.getServicesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());
    
    List<MBeanService<TestService>> servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
    assertEquals(0, servicesByType2.size());
    
    serviceContainer.startService(service3Name, service3);
    serviceContainer.startService(service4Name, service4);
    
    servicesByType1 = serviceContainer.getServicesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());
    
    servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
    assertEquals(2, servicesByType2.size());
       
  }
  
  public void testGetServiceValuesByType() {
        
    // start some services
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    List<MBeanService<TestService>> servicesByType1 = serviceContainer.getServiceValuesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());
    assertTrue(servicesByType1.contains(service1));
    assertTrue(servicesByType1.contains(service2));
    
    List<MBeanService<TestService>> servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
    assertEquals(0, servicesByType2.size());
    
    // start more services
    serviceContainer.startService(service3Name, service3);
    serviceContainer.startService(service4Name, service4);
    
    servicesByType1 = serviceContainer.getServicesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());
    
    servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
    assertEquals(2, servicesByType2.size());
    assertTrue(servicesByType2.contains(service3));
    assertTrue(servicesByType2.contains(service4));
       
  }
  
  public void testGetServiceNames() {
    
    // start some services
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    Set<ObjectName> serviceNames = serviceContainer.getServiceNames(TestServiceType.TYPE1);
    assertEquals(2, serviceNames.size());
    assertTrue(serviceNames.contains(service1Name));
    assertTrue(serviceNames.contains(service2Name));
    
    serviceNames = serviceContainer.getServiceNames(TestServiceType.TYPE2);
    assertEquals(0, serviceNames.size());

    // start more services
    serviceContainer.startService(service3Name, service3);
    serviceContainer.startService(service4Name, service4);
    
    serviceNames = serviceContainer.getServiceNames(TestServiceType.TYPE1);
    assertEquals(2, serviceNames.size());
    assertTrue(serviceNames.contains(service1Name));
    assertTrue(serviceNames.contains(service2Name));
    
    serviceNames = serviceContainer.getServiceNames(TestServiceType.TYPE2);
    assertEquals(2, serviceNames.size());
    assertTrue(serviceNames.contains(service3Name));
    assertTrue(serviceNames.contains(service4Name));
    
  }
  
  public void testDeploymentOperation() {
    
    serviceContainer.createDeploymentOperation("test op")
      .addStep(new StartServiceDeploymentOperationStep(service1Name, service1))      
      .addStep(new StartServiceDeploymentOperationStep(service2Name, service2))
      .execute();
    
    // both services were registered.
    assertEquals(service1, serviceContainer.getService(service1Name));
    assertEquals(service2, serviceContainer.getService(service2Name));
    
  }
  
  public void testFailingDeploymentOperation() {
    
    try {
      serviceContainer.createDeploymentOperation("test failing op")
        .addStep(new StartServiceDeploymentOperationStep(service1Name, service1))
        .addStep(new FailingDeploymentOperationStep())                               // <- this step fails
        .addStep(new StartServiceDeploymentOperationStep(service2Name, service2))
        .execute();
      
      fail("Exception expected");
      
    } catch(Exception e) {
      assertTrue(e.getMessage().contains("Exception while performing 'test failing op => failing step'"));
      
    }
    
    // none of the services were registered
    assertNull(serviceContainer.getService(service1Name));
    assertNull(serviceContainer.getService(service2Name));
    
    // different step ordering //////////////////////////////////
    
    try {
      serviceContainer.createDeploymentOperation("test failing op")
        .addStep(new StartServiceDeploymentOperationStep(service1Name, service1))
        .addStep(new StartServiceDeploymentOperationStep(service2Name, service2))
        .addStep(new FailingDeploymentOperationStep())                               // <- this step fails
        .execute();
      
      fail("Exception expected");
      
    } catch(Exception e) {
      assertTrue(e.getMessage().contains("Exception while performing 'test failing op => failing step'"));
      
    }
    
    // none of the services were registered
    assertNull(serviceContainer.getService(service1Name));
    assertNull(serviceContainer.getService(service2Name));
    
  }
  
  public void testUndeploymentOperation() {
    
    // lets first start some services:
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    // run a composite undeployment operation
    serviceContainer.createUndeploymentOperation("test op")
      .addStep(new StopServiceDeploymentOperationStep(service1Name))      
      .addStep(new StopServiceDeploymentOperationStep(service2Name))
      .execute();
    
    // both services were stopped.
    assertNull(serviceContainer.getService(service1Name));
    assertNull(serviceContainer.getService(service2Name));
    
  }
  
  public void testFailingUndeploymentOperation() {
    
    // lets first start some services:
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    // run a composite undeployment operation with a failing step
    serviceContainer.createUndeploymentOperation("test failing op")
      .addStep(new StopServiceDeploymentOperationStep(service1Name))
      .addStep(new FailingDeploymentOperationStep())                               // <- this step fails
      .addStep(new StopServiceDeploymentOperationStep(service2Name))
      .execute(); // this does not throw an exception even if some steps fail. (exceptions are logged) 
   
    
    // both services were stopped.
    assertNull(serviceContainer.getService(service1Name));
    assertNull(serviceContainer.getService(service2Name));
    
    // different step ordering //////////////////////////////////
    
    serviceContainer.startService(service1Name, service1);
    serviceContainer.startService(service2Name, service2);
    
    // run a composite undeployment operation with a failing step
    serviceContainer.createUndeploymentOperation("test failing op")
      .addStep(new FailingDeploymentOperationStep())                               // <- this step fails
      .addStep(new StopServiceDeploymentOperationStep(service1Name))
      .addStep(new StopServiceDeploymentOperationStep(service2Name))
      .execute();
    
    // both services were stopped.
    assertNull(serviceContainer.getService(service1Name));
    assertNull(serviceContainer.getService(service2Name));
    
  }

}

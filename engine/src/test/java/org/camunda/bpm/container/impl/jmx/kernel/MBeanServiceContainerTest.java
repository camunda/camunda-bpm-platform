package org.camunda.bpm.container.impl.jmx.kernel;

import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.camunda.bpm.container.impl.jmx.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.util.FailingDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.StartServiceDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.StopServiceDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.util.TestService;
import org.camunda.bpm.container.impl.jmx.kernel.util.TestServiceType;
import org.camunda.bpm.container.impl.spi.PlatformService;

/**
 * Testcases for the {@link MBeanServiceContainer} Kernel.
 *
 * @author Daniel Meyer
 *
 */
public class MBeanServiceContainerTest extends TestCase {

  private MBeanServiceContainer serviceContainer;

  private String service1Name = TestServiceType.TYPE1.getTypeName()+ ":type=service1";
  private String service2Name = TestServiceType.TYPE1.getTypeName()+ ":type=service2";
  private String service3Name = TestServiceType.TYPE2.getTypeName()+ ":type=service3";
  private String service4Name = TestServiceType.TYPE2.getTypeName()+ ":type=service4";

  private ObjectName service1ObjectName = MBeanServiceContainer.getObjectName(service1Name);
  private ObjectName service2ObjectName = MBeanServiceContainer.getObjectName(service2Name);
  private ObjectName service3ObjectName = MBeanServiceContainer.getObjectName(service3Name);
  private ObjectName service4ObjectName = MBeanServiceContainer.getObjectName(service4Name);

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
    if(mBeanServer.isRegistered(service1ObjectName)) {
      mBeanServer.unregisterMBean(service1ObjectName);
    }
    if(mBeanServer.isRegistered(service2ObjectName)) {
      mBeanServer.unregisterMBean(service2ObjectName);
    }
    if(mBeanServer.isRegistered(service3ObjectName)) {
      mBeanServer.unregisterMBean(service3ObjectName);
    }
    if(mBeanServer.isRegistered(service4ObjectName)) {
      mBeanServer.unregisterMBean(service4ObjectName);
    }
    super.tearDown();
  }

  public void testStartService() {

    // initially the service is not present:
    assertNull(serviceContainer.getService(service1ObjectName));

    // we can start a service
    serviceContainer.startService(service1Name, service1);
    // and get it after that
    assertNotNull(serviceContainer.getService(service1ObjectName));
    assertEquals(service1, serviceContainer.getService(service1ObjectName));
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
    assertNotNull(serviceContainer.getService(service2ObjectName));

  }

  public void testStopService() {

    // start some service
    serviceContainer.startService(service1Name, service1);
    // it's there
    assertNotNull(serviceContainer.getService(service1ObjectName));

    // stop it:
    serviceContainer.stopService(service1Name);
    // now it's gone
    assertNull(serviceContainer.getService(service1ObjectName));

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

    List<PlatformService<TestService>> servicesByType1 = serviceContainer.getServicesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());

    List<PlatformService<TestService>> servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
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

    List<PlatformService<TestService>> servicesByType1 = serviceContainer.getServiceValuesByType(TestServiceType.TYPE1);
    assertEquals(2, servicesByType1.size());
    assertTrue(servicesByType1.contains(service1));
    assertTrue(servicesByType1.contains(service2));

    List<PlatformService<TestService>> servicesByType2 = serviceContainer.getServicesByType(TestServiceType.TYPE2);
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

    Set<String> serviceNames = serviceContainer.getServiceNames(TestServiceType.TYPE1);
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
    assertEquals(service1, serviceContainer.getService(service1ObjectName));
    assertEquals(service2, serviceContainer.getService(service2ObjectName));

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
      assertTrue(e.getMessage().contains("Exception while performing 'test failing op' => 'failing step'"));

    }

    // none of the services were registered
    assertNull(serviceContainer.getService(service1ObjectName));
    assertNull(serviceContainer.getService(service2ObjectName));

    // different step ordering //////////////////////////////////

    try {
      serviceContainer.createDeploymentOperation("test failing op")
        .addStep(new StartServiceDeploymentOperationStep(service1Name, service1))
        .addStep(new StartServiceDeploymentOperationStep(service2Name, service2))
        .addStep(new FailingDeploymentOperationStep())                               // <- this step fails
        .execute();

      fail("Exception expected");

    } catch(Exception e) {
      assertTrue(e.getMessage().contains("Exception while performing 'test failing op' => 'failing step'"));

    }

    // none of the services were registered
    assertNull(serviceContainer.getService(service1ObjectName));
    assertNull(serviceContainer.getService(service2ObjectName));

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
    assertNull(serviceContainer.getService(service1ObjectName));
    assertNull(serviceContainer.getService(service2ObjectName));

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
    assertNull(serviceContainer.getService(service1ObjectName));
    assertNull(serviceContainer.getService(service2ObjectName));

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
    assertNull(serviceContainer.getService(service1ObjectName));
    assertNull(serviceContainer.getService(service2ObjectName));

  }

}

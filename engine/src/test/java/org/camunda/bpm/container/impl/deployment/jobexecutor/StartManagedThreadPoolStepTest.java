package org.camunda.bpm.container.impl.deployment.jobexecutor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.jmx.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedThreadPool;
import org.camunda.bpm.container.impl.metadata.BpmPlatformXmlImpl;
import org.camunda.bpm.container.impl.metadata.JobExecutorXmlImpl;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.container.impl.metadata.spi.JobExecutorXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ronny Bräunlich
 *
 */
public class StartManagedThreadPoolStepTest {
  
  private MBeanServiceContainer container = new MBeanServiceContainer();
  
  private DeploymentOperation deploymentOperation;
  
  private JobExecutorXmlImpl jobExecutorXml;
  
  private BpmPlatformXml bpmPlatformXml;
  
  private StartManagedThreadPoolStep step;
  
  @Before
  public void setUp(){
    step = new StartManagedThreadPoolStep();
    deploymentOperation = new DeploymentOperation("name", container, Collections.<DeploymentOperationStep> emptyList());
    jobExecutorXml = new JobExecutorXmlImpl();
    bpmPlatformXml = new BpmPlatformXmlImpl(jobExecutorXml, Collections.<ProcessEngineXml>emptyList());
    deploymentOperation.addAttachment(Attachments.BPM_PLATFORM_XML, bpmPlatformXml);
  }
  
  @After
  public void tearDown(){
    container.stopService(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_EXECUTOR);
  }

  @Test
  public void performOperationStepWithDefaultProperties() {
    Map<String, String> properties = new HashMap<String, String>();
    jobExecutorXml.setProperties(properties);
    step.performOperationStep(deploymentOperation);

    PlatformService<JmxManagedThreadPool> service = container.getService(getObjectNameForExecutor());
    ThreadPoolExecutor executor = service.getValue().getThreadPoolExecutor();

    //since no jobs will start, remaining capacity is sufficent to check the size
    assertThat(executor.getQueue().remainingCapacity(), is(3));
    assertThat(executor.getCorePoolSize(), is(3));
    assertThat(executor.getMaximumPoolSize(), is(10));
    assertThat(executor.getKeepAliveTime(TimeUnit.MILLISECONDS), is(0L));
  }

  @Test
  public void performOperationStepWithPropertiesInXml() {
    Map<String, String> properties = new HashMap<String, String>();
    String queueSize = "5";
    String corePoolSize = "12";
    String maxPoolSize = "20";
    String keepAliveTime = "100";
    properties.put(JobExecutorXml.CORE_POOL_SIZE, corePoolSize );
    properties.put(JobExecutorXml.KEEP_ALIVE_TIME, keepAliveTime);
    properties.put(JobExecutorXml.MAX_POOL_SIZE, maxPoolSize);
    properties.put(JobExecutorXml.QUEUE_SIZE, queueSize);
    jobExecutorXml.setProperties(properties);
    step.performOperationStep(deploymentOperation);

    PlatformService<JmxManagedThreadPool> service = container.getService(getObjectNameForExecutor());
    ThreadPoolExecutor executor = service.getValue().getThreadPoolExecutor();

    //since no jobs will start, remaining capacity is sufficent to check the size
    assertThat(executor.getQueue().remainingCapacity(), is(Integer.parseInt(queueSize)));
    assertThat(executor.getCorePoolSize(), is(Integer.parseInt(corePoolSize)));
    assertThat(executor.getMaximumPoolSize(), is(Integer.parseInt(maxPoolSize)));
    assertThat(executor.getKeepAliveTime(TimeUnit.MILLISECONDS), is(Long.parseLong(keepAliveTime)));
  }
  
  private ObjectName getObjectNameForExecutor(){
    String localName = MBeanServiceContainer.composeLocalName(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_EXECUTOR);
    return MBeanServiceContainer.getObjectName(localName);
  }
}

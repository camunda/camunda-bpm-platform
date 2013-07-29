/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.test;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.extension.Attribute;
import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.Element;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.engine.ProcessEngineException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author nico.rehwaldt@camunda.com
 * @author christian.lipphardt@camunda.com
 */
public class JBossSubsystemXMLTest extends AbstractSubsystemTest {

  public static final String SUBSYSTEM_WITH_SINGLE_ENGINE = "subsystemWithSingleEngine.xml";
  public static final String SUBSYSTEM_WITH_ENGINES = "subsystemWithEngines.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY = "subsystemWithProcessEnginesElementOnly.xml";
  public static final String SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES = "subsystemWithEnginesAndProperties.xml";
  public static final String SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES = "subsystemWithDuplicateEngineNames.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR = "subsystemWithJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR = "subsystemWithProcessEnginesAndJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES = "subsystemWithJobExecutorAndProperties.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_WITHOUT_ACQUISITION_STRATEGY = "subsystemWithJobExecutorAndWithoutAcquisitionStrategy.xml";
  
  public static final ServiceName PLATFORM_SERVICE_NAME = ServiceNames.forMscRuntimeContainerDelegate();
  public static final ServiceName PLATFORM_JOBEXECUTOR_SERVICE_NAME = ServiceNames.forMscExecutorService();
    
  public static final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
    .append("camunda-bpm-platform")
    .append("process-engine")
    .append("ProcessEngineService!org.camunda.bpm.ProcessEngineService");
   
  public JBossSubsystemXMLTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }
  
  @Test
  public void testParseSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
//    System.out.println(operations);
    Assert.assertEquals(1, operations.size());
  }
  
  @Test
  public void testParseSubsystemXmlWithEngines() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
//    System.out.println(operations);
    Assert.assertEquals(3, operations.size());
  }
  
  @Test
  public void testParseSubsystemXmlWithEnginesAndProperties() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
//    System.out.println(operations);
    Assert.assertEquals(5, operations.size());
  }
  
  @Test
  public void testInstallSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
//    services.getContainer().dumpServices();
    
    ServiceContainer container = services.getContainer();
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
        
  }
  
  @Test
  public void testInstallSubsystemWithEnginesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
//    services.getContainer().dumpServices();
    
    ServiceContainer container = services.getContainer();
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    Assert.assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));
  }
  
  @Test
  public void testInstallSubsystemWithEnginesAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();
//    container.dumpServices();
    
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    ServiceController<?> defaultEngineService = container.getService(ServiceNames.forManagedProcessEngine("__default"));
    
    Assert.assertNotNull("process engine controller for engine __default is installed ", defaultEngineService);
    
    ManagedProcessEngineMetadata metadata = ((MscManagedProcessEngineController) defaultEngineService.getService()).getProcessEngineMetadata();
    Map<String, String> configurationProperties = metadata.getConfigurationProperties();
    Assert.assertEquals("default", configurationProperties.get("job-name"));
    Assert.assertEquals("default", configurationProperties.get("job-acquisition"));
    Assert.assertEquals("default", configurationProperties.get("job-acquisition-name"));
    
    Map<String, String> foxLegacyProperties = metadata.getFoxLegacyProperties();
    Assert.assertTrue(foxLegacyProperties.isEmpty());
    
    Assert.assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));
    Assert.assertNotNull("process engine controller for engine __emptyPropertiesTag is installed ", container.getService(ServiceNames.forManagedProcessEngine("__emptyPropertiesTag")));
    Assert.assertNotNull("process engine controller for engine __noPropertiesTag is installed ", container.getService(ServiceNames.forManagedProcessEngine("__noPropertiesTag")));
  }
  
  @Test
  public void testInstallSubsystemWithDupliacteEngineNamesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);
//    System.out.println(normalizeXML(subsystemXml));
    try {
      installInController(subsystemXml);
//    services.getContainer().dumpServices();
    } catch (ProcessEngineException fpe) {
      Assert.assertTrue("Duplicate process engine detected!", fpe.getMessage().contains("A process engine with name '__test' already exists."));
    }
  }

  @Test
  public void testInstallSubsystemWithSingleEngineXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_SINGLE_ENGINE);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();

    //    container.dumpServices();
    
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    
    String persistedSubsystemXml = services.getPersistedSubsystemXml();
    compareXml(null, subsystemXml, persistedSubsystemXml);
  }
  
  @Test
  public void testParseSubsystemWithJobExecutorXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
//    System.out.println(operations);
    Assert.assertEquals(4, operations.size());
    
    ModelNode jobExecutor = operations.get(1);
    PathAddress pathAddress = PathAddress.pathAddress(jobExecutor.get(ModelDescriptionConstants.OP_ADDR));
    Assert.assertEquals(2, pathAddress.size());

    PathElement element = pathAddress.getElement(0);
    Assert.assertEquals(ModelDescriptionConstants.SUBSYSTEM, element.getKey());
    Assert.assertEquals(ModelConstants.SUBSYSTEM_NAME, element.getValue());
    element = pathAddress.getElement(1);
    Assert.assertEquals(Element.JOB_EXECUTOR.getLocalName(), element.getKey());
    Assert.assertEquals(Attribute.DEFAULT.getLocalName(), element.getValue());
    
    Assert.assertEquals("job-executor-tp", jobExecutor.get(Element.THREAD_POOL_NAME.getLocalName()).asString());
    
    ModelNode jobAcquisition = operations.get(2);
    Assert.assertEquals("default", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    Assert.assertEquals("SEQUENTIAL", jobAcquisition.get(Element.ACQUISITION_STRATEGY.getLocalName()).asString());
    Assert.assertTrue(jobAcquisition.has(Element.PROPERTIES.getLocalName()));
    Assert.assertTrue(!jobAcquisition.hasDefined(Element.PROPERTIES.getLocalName()));
    
    jobAcquisition = operations.get(3);
    Assert.assertEquals("anders", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    Assert.assertEquals("SEQUENTIAL", jobAcquisition.get(Element.ACQUISITION_STRATEGY.getLocalName()).asString());
  }
  
  @Test
  public void testInstallSubsystemWithJobExecutorXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();
//    container.dumpServices();
    
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
    
  }
  
  @Test
  public void testParseSubsystemWithJobExecutorAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
    Assert.assertEquals(4, operations.size());
  }
  
  @Test
  public void testInstallSubsystemWithJobExecutorAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();
//    container.dumpServices();

    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
  }
  
  @Test
  public void testJobAcquisitionStrategyOptional() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_WITHOUT_ACQUISITION_STRATEGY);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();
//    container.dumpServices();

    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
  }
  
  
  @Test
  public void testParseSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
    Assert.assertEquals(6, operations.size());
  }
  
  @Test
  public void testInstallSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
    ServiceContainer container = services.getContainer();
//    container.dumpServices();
    
    Assert.assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    Assert.assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
    Assert.assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));
    
    Assert.assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    Assert.assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));

    
    String persistedSubsystemXml = services.getPersistedSubsystemXml();
//    System.out.println(persistedSubsystemXml);
    compareXml(null, subsystemXml, persistedSubsystemXml);
  }
  
}

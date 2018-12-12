/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.test;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.extension.*;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.as.threads.ThreadFactoryService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.junit.Assert.*;


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
  public static final String SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS = "subsystemWithEnginesPropertiesPlugins.xml";
  public static final String SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES = "subsystemWithDuplicateEngineNames.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR = "subsystemWithJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR = "subsystemWithProcessEnginesAndJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES = "subsystemWithJobExecutorAndProperties.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_WITHOUT_ACQUISITION_STRATEGY = "subsystemWithJobExecutorAndWithoutAcquisitionStrategy.xml";
  public static final String SUBSYSTEM_WITH_ALL_OPTIONS = "subsystemWithAllOptions.xml";
  public static final String SUBSYSTEM_WITH_REQUIRED_OPTIONS = "subsystemWithRequiredOptions.xml";
  public static final String SUBSYSTEM_WITH_NO_OPTIONS = "subsystemWithNoOptions.xml";

  public static final String LOCK_TIME_IN_MILLIS = "lockTimeInMillis";
  public static final String WAIT_TIME_IN_MILLIS = "waitTimeInMillis";
  public static final String MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";

  public static final ServiceName PLATFORM_SERVICE_NAME = ServiceNames.forMscRuntimeContainerDelegate();
  public static final ServiceName PLATFORM_JOBEXECUTOR_SERVICE_NAME = ServiceNames.forMscExecutorService();
  public static final ServiceName PLATFORM_BPM_PLATFORM_PLUGINS_SERVICE_NAME = ServiceNames.forBpmPlatformPlugins();
  public static final ServiceName PLATFORM_JOBEXECUTOR_MANAGED_THREAD_POOL_SERVICE_NAME =
      ServiceNames.forManagedThreadPool(SubsystemAttributeDefinitons.THREAD_POOL_NAME.getDefaultValue().asString());

  public static final ServiceName PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME
    .append("camunda-bpm-platform")
    .append("process-engine")
    .append("ProcessEngineService!org.camunda.bpm.ProcessEngineService");

  public JBossSubsystemXMLTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension(), getSubsystemRemoveOrderComparator());
  }

  @Test
  public void testParseSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);

    List<ModelNode> operations = parse(subsystemXml);

    assertEquals(1, operations.size());
    //The add subsystem operation will happen first
    ModelNode addSubsystem = operations.get(0);
    assertEquals(ADD, addSubsystem.get(OP).asString());
    PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
    assertEquals(1, addr.size());
    PathElement element = addr.getElement(0);
    assertEquals(SUBSYSTEM, element.getKey());
    assertEquals(ModelConstants.SUBSYSTEM_NAME, element.getValue());
  }

  @Test
  public void testParseSubsystemXmlWithEngines() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(3, operations.size());
  }

  @Test
  public void testParseSubsystemXmlWithEnginesAndProperties() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(5, operations.size());
  }

  @Test
  public void testParseSubsystemXmlWithEnginesPropertiesPlugins() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS);

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(3, operations.size());
  }

  @Test
  public void testInstallSubsystemWithEnginesPropertiesPlugins() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();

    ServiceContainer container = services.getContainer();

    assertNotNull("platform service should be installed", container.getRequiredService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getRequiredService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));

    ServiceController<?> defaultEngineService = container.getService(ServiceNames.forManagedProcessEngine("__default"));

    assertNotNull("process engine controller for engine __default is installed ", defaultEngineService);

    ManagedProcessEngineMetadata metadata = ((MscManagedProcessEngineController) defaultEngineService.getService()).getProcessEngineMetadata();
    Map<String, String> configurationProperties = metadata.getConfigurationProperties();
    assertEquals("default", configurationProperties.get("job-name"));
    assertEquals("default", configurationProperties.get("job-acquisition"));
    assertEquals("default", configurationProperties.get("job-acquisition-name"));

    Map<String, String> foxLegacyProperties = metadata.getFoxLegacyProperties();
    assertTrue(foxLegacyProperties.isEmpty());

    assertNotNull("process engine controller for engine __default is installed ", container.getRequiredService(ServiceNames.forManagedProcessEngine("__default")));
    assertNotNull("process engine controller for engine __test is installed ", container.getRequiredService(ServiceNames.forManagedProcessEngine("__test")));

    // check we have parsed the plugin configurations
    metadata = ((MscManagedProcessEngineController) container.getRequiredService(ServiceNames.forManagedProcessEngine("__test")).getService())
        .getProcessEngineMetadata();
    List<ProcessEnginePluginXml> pluginConfigurations = metadata.getPluginConfigurations();

    ProcessEnginePluginXml processEnginePluginXml = pluginConfigurations.get(0);
    assertEquals("org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin", processEnginePluginXml.getPluginClass());
    Map<String, String> processEnginePluginXmlProperties = processEnginePluginXml.getProperties();
    assertEquals("abc", processEnginePluginXmlProperties.get("test"));
    assertEquals("123", processEnginePluginXmlProperties.get("number"));
    assertEquals("true", processEnginePluginXmlProperties.get("bool"));

    processEnginePluginXml = pluginConfigurations.get(1);
    assertEquals("org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin", processEnginePluginXml.getPluginClass());
    processEnginePluginXmlProperties = processEnginePluginXml.getProperties();
    assertEquals("cba", processEnginePluginXmlProperties.get("test"));
    assertEquals("321", processEnginePluginXmlProperties.get("number"));
    assertEquals("false", processEnginePluginXmlProperties.get("bool"));

    // test correct subsystem removal
    assertRemoveSubsystemResources(services);
    try {
      ServiceController<?> service = container.getRequiredService(ServiceNames.forManagedProcessEngine("__default"));
      fail("Service '" + service.getName() + "' should have been removed.");
    } catch (Exception expected) {
      // nop
    }
    try {
      ServiceController<?> service = container.getRequiredService(ServiceNames.forManagedProcessEngine("__test"));
      fail("Service '" + service.getName() + "' should have been removed.");
    } catch (Exception expected) {
      // nop
    }
  }

  @Test
  public void testInstallSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();

    ServiceContainer container = services.getContainer();

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));
    assertNull(container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
  }

  @Test
  public void testInstallSubsystemXmlPlatformPlugins() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();

    ServiceContainer container = services.getContainer();
    ServiceController<BpmPlatformPlugins> serviceController = (ServiceController<BpmPlatformPlugins>) container.getService(PLATFORM_BPM_PLATFORM_PLUGINS_SERVICE_NAME);
    assertNotNull(serviceController);
    BpmPlatformPlugins platformPlugins = serviceController.getValue();
    assertNotNull(platformPlugins);
    assertEquals(1, platformPlugins.getPlugins().size());
    assertTrue(platformPlugins.getPlugins().get(0) instanceof ExampleBpmPlatformPlugin);
  }

  @Test
  public void testInstallSubsystemWithEnginesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();


    ServiceContainer container = services.getContainer();
    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));

    assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));
  }

  @Test
  public void testInstallSubsystemWithEnginesAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();


    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));

    ServiceController<?> defaultEngineService = container.getService(ServiceNames.forManagedProcessEngine("__default"));

    assertNotNull("process engine controller for engine __default is installed ", defaultEngineService);

    ManagedProcessEngineMetadata metadata = ((MscManagedProcessEngineController) defaultEngineService.getService()).getProcessEngineMetadata();
    Map<String, String> configurationProperties = metadata.getConfigurationProperties();
    assertEquals("default", configurationProperties.get("job-name"));
    assertEquals("default", configurationProperties.get("job-acquisition"));
    assertEquals("default", configurationProperties.get("job-acquisition-name"));

    Map<String, String> foxLegacyProperties = metadata.getFoxLegacyProperties();
    assertTrue(foxLegacyProperties.isEmpty());

    assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));
    assertNotNull("process engine controller for engine __emptyPropertiesTag is installed ", container.getService(ServiceNames.forManagedProcessEngine("__emptyPropertiesTag")));
    assertNotNull("process engine controller for engine __noPropertiesTag is installed ", container.getService(ServiceNames.forManagedProcessEngine("__noPropertiesTag")));
  }

  @Test
  public void testInstallSubsystemWithDuplicateEngineNamesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);

    try {
      createKernelServicesBuilder(null)
          .setSubsystemXml(subsystemXml)
          .build();

    } catch (XMLStreamException fpe) {
      assertTrue("Duplicate process engine detected!", fpe.getNestedException().getMessage().contains("A process engine with name '__test' already exists."));
    }
  }

  @Test
  public void testInstallSubsystemWithSingleEngineXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_SINGLE_ENGINE);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));

    assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));

    String persistedSubsystemXml = services.getPersistedSubsystemXml();
    compareXml(null, subsystemXml, persistedSubsystemXml);
  }

  @Test
  public void testParseSubsystemWithJobExecutorXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));

    List<ModelNode> operations = parse(subsystemXml);
//    System.out.println(operations);
    assertEquals(4, operations.size());

    ModelNode jobExecutor = operations.get(1);
    PathAddress pathAddress = PathAddress.pathAddress(jobExecutor.get(ModelDescriptionConstants.OP_ADDR));
    assertEquals(2, pathAddress.size());

    PathElement element = pathAddress.getElement(0);
    assertEquals(ModelDescriptionConstants.SUBSYSTEM, element.getKey());
    assertEquals(ModelConstants.SUBSYSTEM_NAME, element.getValue());
    element = pathAddress.getElement(1);
    assertEquals(Element.JOB_EXECUTOR.getLocalName(), element.getKey());
    assertEquals(Attribute.DEFAULT.getLocalName(), element.getValue());

    assertEquals("job-executor-tp", jobExecutor.get(Element.THREAD_POOL_NAME.getLocalName()).asString());

    ModelNode jobAcquisition = operations.get(2);
    assertEquals("default", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertEquals("SEQUENTIAL", jobAcquisition.get(Element.ACQUISITION_STRATEGY.getLocalName()).asString());
    assertFalse(jobAcquisition.has(Element.PROPERTIES.getLocalName()));

    jobAcquisition = operations.get(3);
    assertEquals("anders", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertEquals("SEQUENTIAL", jobAcquisition.get(Element.ACQUISITION_STRATEGY.getLocalName()).asString());
  }

  @Test
  public void testInstallSubsystemWithJobExecutorXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR);
    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();

    commonSubsystemServicesAreInstalled(container);
  }

  @Test
  public void testParseSubsystemWithJobExecutorAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES);

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(5, operations.size());

    // "default" job acquisition ///////////////////////////////////////////////////////////
    ModelNode jobAcquisition = operations.get(2);
    assertEquals("default", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertFalse(jobAcquisition.has(Element.PROPERTIES.getLocalName()));

    // "anders" job acquisition ////////////////////////////////////////////////////////////
    jobAcquisition = operations.get(3);
    assertEquals("anders", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertTrue(jobAcquisition.has(Element.PROPERTIES.getLocalName()));
    assertTrue(jobAcquisition.hasDefined(Element.PROPERTIES.getLocalName()));

    ModelNode properties = jobAcquisition.get(Element.PROPERTIES.getLocalName());
    assertEquals(3, properties.asPropertyList().size());

    assertTrue(properties.has(LOCK_TIME_IN_MILLIS));
    assertTrue(properties.hasDefined(LOCK_TIME_IN_MILLIS));
    assertEquals(600000, properties.get(LOCK_TIME_IN_MILLIS).asInt());

    assertTrue(properties.has(WAIT_TIME_IN_MILLIS));
    assertTrue(properties.hasDefined(WAIT_TIME_IN_MILLIS));
    assertEquals(10000, properties.get(WAIT_TIME_IN_MILLIS).asInt());

    assertTrue(properties.has(MAX_JOBS_PER_ACQUISITION));
    assertTrue(properties.hasDefined(MAX_JOBS_PER_ACQUISITION));
    assertEquals(5, properties.get(MAX_JOBS_PER_ACQUISITION).asInt());

    // "mixed" job acquisition ////////////////////////////////////////////////////////////
    jobAcquisition = operations.get(4);
    assertEquals("mixed", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertTrue(jobAcquisition.has(Element.PROPERTIES.getLocalName()));
    assertTrue(jobAcquisition.hasDefined(Element.PROPERTIES.getLocalName()));

    properties = jobAcquisition.get(Element.PROPERTIES.getLocalName());
    assertEquals(1, properties.asPropertyList().size());

    assertTrue(properties.has(LOCK_TIME_IN_MILLIS));
    assertTrue(properties.hasDefined(LOCK_TIME_IN_MILLIS));
    assertEquals(500000, properties.get(LOCK_TIME_IN_MILLIS).asInt());

    assertFalse(properties.has(WAIT_TIME_IN_MILLIS));
    assertFalse(properties.hasDefined(WAIT_TIME_IN_MILLIS));
    assertFalse(properties.has(MAX_JOBS_PER_ACQUISITION));
    assertFalse(properties.hasDefined(MAX_JOBS_PER_ACQUISITION));

  }

  @Test
  public void testInstallSubsystemWithJobExecutorAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();

    commonSubsystemServicesAreInstalled(container);

    // "default" job acquisition ///////////////////////////////////////////////////////////
    ServiceController<?> defaultJobAcquisitionService = container.getService(ServiceNames.forMscRuntimeContainerJobExecutorService("default"));
    assertNotNull("platform job acquisition service 'default' should be installed", defaultJobAcquisitionService);

    Object value = defaultJobAcquisitionService.getValue();
    assertNotNull(value);
    assertTrue(value instanceof JobExecutor);

    JobExecutor defaultJobExecutor = (JobExecutor) value;
    assertEquals(300000, defaultJobExecutor.getLockTimeInMillis());
    assertEquals(5000, defaultJobExecutor.getWaitTimeInMillis());
    assertEquals(3, defaultJobExecutor.getMaxJobsPerAcquisition());

    // ServiceName: 'org.camunda.bpm.platform.job-executor.job-executor-tp'
    ServiceController<ManagedQueueExecutorService> managedQueueExecutorServiceController = (ServiceController<ManagedQueueExecutorService>) container.getService(ServiceNames.forManagedThreadPool(SubsystemAttributeDefinitons.DEFAULT_JOB_EXECUTOR_THREADPOOL_NAME));
    assertNotNull(managedQueueExecutorServiceController);
    ManagedQueueExecutorService managedQueueExecutorService = managedQueueExecutorServiceController.getValue();
    assertNotNull(managedQueueExecutorService);
    assertEquals("Number of core threads is wrong", SubsystemAttributeDefinitons.DEFAULT_CORE_THREADS, managedQueueExecutorService.getCoreThreads());
    assertEquals("Number of max threads is wrong", SubsystemAttributeDefinitons.DEFAULT_MAX_THREADS, managedQueueExecutorService.getMaxThreads());
    assertEquals(SubsystemAttributeDefinitons.DEFAULT_KEEPALIVE_TIME, TimeUnit.NANOSECONDS.toSeconds(managedQueueExecutorService.getKeepAlive()));
    assertEquals(false, managedQueueExecutorService.isBlocking());
    assertEquals(SubsystemAttributeDefinitons.DEFAULT_ALLOW_CORE_TIMEOUT, managedQueueExecutorService.isAllowCoreTimeout());

    ServiceController<ThreadFactoryService> threadFactoryService = (ServiceController<ThreadFactoryService>) container.getService(ServiceNames.forThreadFactoryService(SubsystemAttributeDefinitons.DEFAULT_JOB_EXECUTOR_THREADPOOL_NAME));
    assertNotNull(threadFactoryService);

    // "anders" job acquisition /////////////////////////////////////////////////////////
    ServiceController<?> andersJobAcquisitionService = container.getService(ServiceNames.forMscRuntimeContainerJobExecutorService("anders"));
    assertNotNull("platform job acquisition service 'anders' should be installed", andersJobAcquisitionService);

    value = andersJobAcquisitionService.getValue();
    assertNotNull(value);
    assertTrue(value instanceof JobExecutor);

    JobExecutor andersJobExecutor = (JobExecutor) value;
    assertEquals(600000, andersJobExecutor.getLockTimeInMillis());
    assertEquals(10000, andersJobExecutor.getWaitTimeInMillis());
    assertEquals(5, andersJobExecutor.getMaxJobsPerAcquisition());

    // "mixed" job acquisition /////////////////////////////////////////////////////////
    ServiceController<?> mixedJobAcquisitionService = container.getService(ServiceNames.forMscRuntimeContainerJobExecutorService("mixed"));
    assertNotNull("platform job acquisition service 'mixed' should be installed", mixedJobAcquisitionService);

    value = mixedJobAcquisitionService.getValue();
    assertNotNull(value);
    assertTrue(value instanceof JobExecutor);

    JobExecutor mixedJobExecutor = (JobExecutor) value;
    assertEquals(500000, mixedJobExecutor.getLockTimeInMillis());
    // default values
    assertEquals(5000, mixedJobExecutor.getWaitTimeInMillis());
    assertEquals(3, mixedJobExecutor.getMaxJobsPerAcquisition());

  }

  @Test
  public void testJobAcquisitionStrategyOptional() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_WITHOUT_ACQUISITION_STRATEGY);
    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();

    commonSubsystemServicesAreInstalled(container);
  }

  @Test
  public void testParseSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);

    List<ModelNode> operations = parse(subsystemXml);
    System.out.println(operations);
    assertEquals(6, operations.size());
  }

  @Test
  public void testInstallSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();

    commonSubsystemServicesAreInstalled(container);
    assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));

    String persistedSubsystemXml = services.getPersistedSubsystemXml();
    compareXml(null, subsystemXml, persistedSubsystemXml);
  }

  @Test
  public void testParseAndMarshalModelWithAllAvailableOptions() throws Exception {
    parseAndMarshalSubsystemModelFromFile(SUBSYSTEM_WITH_ALL_OPTIONS);
  }

  @Test
  public void testParseAndMarshalModelWithRequiredOptionsOnly() throws Exception {
    parseAndMarshalSubsystemModelFromFile(SUBSYSTEM_WITH_REQUIRED_OPTIONS);
  }

  // HELPERS

  /**
   *  Parses and marshall the given subsystemXmlFile into one controller, then reads the model into second one.
   *  Compares the models afterwards.
   * @param subsystemXmlFile the name of the subsystem xml file
   * @throws Exception
   */
  protected void parseAndMarshalSubsystemModelFromFile(String subsystemXmlFile) throws Exception {
    String subsystemXml = FileUtils.readFile(subsystemXmlFile);
    // Parse the subsystem xml and install into the first controller
    KernelServices servicesA = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();
    // Get the model and the persisted xml from the first controller
    ModelNode modelA = servicesA.readWholeModel();
    String marshalled = servicesA.getPersistedSubsystemXml();

    // Make sure the xml is the same
    compareXml(null, subsystemXml, marshalled);

    // Install the persisted xml from the first controller into a second controller
    KernelServices servicesB = createKernelServicesBuilder(null).setSubsystemXml(marshalled).build();
    ModelNode modelB = servicesB.readWholeModel();

    // Make sure the models from the two controllers are identical
    compare(modelA, modelB);
  }

  protected void commonSubsystemServicesAreInstalled(ServiceContainer container) {
    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(PROCESS_ENGINE_SERVICE_BINDING_SERVICE_NAME));
    assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
    assertNotNull("platform jobexecutor managed threadpool service should be installed", container.getService(PLATFORM_JOBEXECUTOR_MANAGED_THREAD_POOL_SERVICE_NAME));
    assertNotNull("bpm platform plugins service should be installed", container.getService(PLATFORM_BPM_PLATFORM_PLUGINS_SERVICE_NAME));
  }

  protected static Comparator<PathAddress> getSubsystemRemoveOrderComparator() {
    final List<PathAddress> REMOVE_ORDER_OF_SUBSYSTEM = Arrays.asList(
        PathAddress.pathAddress(BpmPlatformExtension.SUBSYSTEM_PATH),
        PathAddress.pathAddress(BpmPlatformExtension.JOB_EXECUTOR_PATH),
        PathAddress.pathAddress(BpmPlatformExtension.JOB_ACQUISTIONS_PATH),
        PathAddress.pathAddress(BpmPlatformExtension.PROCESS_ENGINES_PATH)
    );

    return new Comparator<PathAddress>() {
      protected PathAddress normalizePathAddress(PathAddress pathAddress) {
        if (pathAddress.size() == 1 && pathAddress.equals(REMOVE_ORDER_OF_SUBSYSTEM.get(0))) {
          return pathAddress;
        } else {
          // strip subsystem parentAddress
          return pathAddress.subAddress(1, pathAddress.size());
        }
      }

      protected int getOrder(PathAddress pathAddress) {
        for (PathAddress orderedAddress : REMOVE_ORDER_OF_SUBSYSTEM) {
          if (orderedAddress.getLastElement().getKey().equals(pathAddress.getLastElement().getKey())) {
            return REMOVE_ORDER_OF_SUBSYSTEM.indexOf(orderedAddress);
          }
        }
        throw new IllegalArgumentException("Unable to find a match for path address: " + pathAddress);
      }

      @Override
      public int compare(PathAddress o1, PathAddress o2) {
        int orderO1 = getOrder(normalizePathAddress(o1));
        int orderO2 = getOrder(normalizePathAddress(o2));

        if (orderO1 < orderO2) {
          return -1;
        } else if (orderO1 > orderO2) {
          return 1;
        } else {
          return 0;
        }
      }
    };
  }
}

/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.extension.Attribute;
import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.Element;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.junit.Test;


/**
 *
 * @author nico.rehwaldt@camunda.com
 * @author christian.lipphardt@camunda.com
 */
public class JBossSubsystemXMLTest extends AbstractSubsystemTest {

  public static final String SUBSYSTEM_WITH_SINGLE_ENGINE = "subsystemWithSingleEngine.xml";
  public static final String SUBSYSTEM_WITH_ENGINES = "subsystemWithEngines.xml";
  public static final String SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS_AND_JOB_EXECUTOR_WITH_EXPRESSIONS = "subsystemWithProcessEnginesPropertiesPluginsAndJobExecutorWithExpressions.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY = "subsystemWithProcessEnginesElementOnly.xml";
  public static final String SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES = "subsystemWithEnginesAndProperties.xml";
  public static final String SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS = "subsystemWithEnginesPropertiesPlugins.xml";
  public static final String SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES = "subsystemWithDuplicateEngineNames.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR = "subsystemWithJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR = "subsystemWithProcessEnginesAndJobExecutor.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES = "subsystemWithJobExecutorAndProperties.xml";
  public static final String SUBSYSTEM_WITH_JOB_EXECUTOR_WITHOUT_ACQUISITION_STRATEGY = "subsystemWithJobExecutorAndWithoutAcquisitionStrategy.xml";

  public static final String LOCK_TIME_IN_MILLIS = "lockTimeInMillis";
  public static final String WAIT_TIME_IN_MILLIS = "waitTimeInMillis";
  public static final String MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";

  public static final ServiceName PLATFORM_SERVICE_NAME = ServiceNames.forMscRuntimeContainerDelegate();
  public static final ServiceName PLATFORM_JOBEXECUTOR_SERVICE_NAME = ServiceNames.forMscExecutorService();

  public static final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME
    .append("camunda-bpm-platform")
    .append("process-engine")
    .append("ProcessEngineService!org.camunda.bpm.ProcessEngineService");

  public JBossSubsystemXMLTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }
  
  private static Map<String, String> EXPRESSION_PROPERTIES = new HashMap<>();
  
  static {
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.isDefault", "true");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.datasource", "java:jboss/datasources/ExampleDS");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.history-level", "audit");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.configuration", "org.camunda.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.property.job-acquisition-name", "default");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.plugin.ldap.class", "org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.plugin.ldap.property.test", "abc");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.plugin.ldap.property.number", "123");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.plugin.ldap.property.bool", "true");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.job-executor.thread-pool-name", "job-executor-tp");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.job-executor.job-acquisition.default.acquisition-strategy", "SEQUENTIAL");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.job-executor.job-acquisition.default.property.lockTimeInMillis", "300000");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.job-executor.job-acquisition.default.property.waitTimeInMillis", "5000");
    EXPRESSION_PROPERTIES.put("org.camunda.bpm.jboss.job-executor.job-acquisition.default.property.maxJobsPerAcquisition", "3");                                             
  }

  @Test
  public void testParseSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);

    List<ModelNode> operations = parse(subsystemXml);

    assertEquals(1, operations.size());
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
    assertNotNull("process engine service should be bound in JNDI", container.getRequiredService(processEngineServiceBindingServiceName));

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
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

  }

  @Test
  public void testInstallSubsystemXmlPlatformPlugins() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();

    ServiceContainer container = services.getContainer();
    ServiceController<?> serviceController = container.getService(ServiceNames.forBpmPlatformPlugins());
    assertNotNull(serviceController);
    Object platformPlugins = serviceController.getValue();
    assertTrue(platformPlugins instanceof BpmPlatformPlugins);
    assertNotNull(platformPlugins);
    List<BpmPlatformPlugin> plugins = ((BpmPlatformPlugins) platformPlugins).getPlugins();
    assertEquals(1, plugins.size());
    assertTrue(plugins.get(0) instanceof ExampleBpmPlatformPlugin);
  }

  @Test
  public void testInstallSubsystemWithEnginesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);

    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();


    ServiceContainer container = services.getContainer();
    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

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
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

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

    } catch (ProcessEngineException fpe) {
      assertTrue("Duplicate process engine detected!", fpe.getMessage().contains("A process engine with name '__test' already exists."));
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
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

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

    jobAcquisition = operations.get(3);
    assertEquals("anders", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());
    assertEquals("SEQUENTIAL", jobAcquisition.get(Element.ACQUISITION_STRATEGY.getLocalName()).asString());
  }

  @Test
  public void testInstallSubsystemWithJobExecutorXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();
//    container.dumpServices();

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

    assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));

  }

  @Test
  public void testParseSubsystemWithJobExecutorAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_JOB_EXECUTOR_AND_PROPERTIES);

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(5, operations.size());

    // "default" job acquisition ///////////////////////////////////////////////////////////
    ModelNode jobAcquisition = operations.get(2);
    assertEquals("default", jobAcquisition.get(Attribute.NAME.getLocalName()).asString());

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

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

    assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));

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
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();
//    container.dumpServices();

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

    assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
  }


  @Test
  public void testParseSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));

    List<ModelNode> operations = parse(subsystemXml);
    assertEquals(6, operations.size());
  }

  @Test
  public void testInstallSubsystemXmlWithEnginesAndJobExecutor() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_AND_JOB_EXECUTOR);
//    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = createKernelServicesBuilder(null)
        .setSubsystemXml(subsystemXml)
        .build();
    ServiceContainer container = services.getContainer();
//    container.dumpServices();

    assertNotNull("platform service should be installed", container.getService(PLATFORM_SERVICE_NAME));
    assertNotNull("platform jobexecutor service should be installed", container.getService(PLATFORM_JOBEXECUTOR_SERVICE_NAME));
    assertNotNull("process engine service should be bound in JNDI", container.getService(processEngineServiceBindingServiceName));

    assertNotNull("process engine controller for engine __default is installed ", container.getService(ServiceNames.forManagedProcessEngine("__default")));
    assertNotNull("process engine controller for engine __test is installed ", container.getService(ServiceNames.forManagedProcessEngine("__test")));


    String persistedSubsystemXml = services.getPersistedSubsystemXml();
//    System.out.println(persistedSubsystemXml);
    compareXml(null, subsystemXml, persistedSubsystemXml);
  }
  
  @Test
  public void testParseSubsystemXmlWithEnginePropertiesPluginsAndJobExecutorWithExpressions() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS_AND_JOB_EXECUTOR_WITH_EXPRESSIONS);

    List<ModelNode> operations = parse(subsystemXml);

    assertEquals(4, operations.size());
    // all elements with expression allowed should be an expression now
    assertExpressionType(operations.get(1), "default", "datasource", "history-level", "configuration");
    assertExpressionType(operations.get(1).get("properties"), "job-acquisition-name");
    assertExpressionType(operations.get(1).get("plugins").get(0), "class");
    assertExpressionType(operations.get(1).get("plugins").get(0).get("properties"), "test", "number", "bool");
    assertExpressionType(operations.get(2), "thread-pool-name");
    assertExpressionType(operations.get(3), "acquisition-strategy");
    assertExpressionType(operations.get(3).get("properties"), "lockTimeInMillis", "waitTimeInMillis", "maxJobsPerAcquisition");
    // all other elements should be string still
    assertStringType(operations.get(1), "name");// process-engine name
    assertStringType(operations.get(3), "name");// job-acquisition name
  }

  @Test
  public void testInstallSubsystemXmlWithEnginePropertiesPluginsAndJobExecutorWithExpressions() throws Exception {
    System.getProperties().putAll(EXPRESSION_PROPERTIES);
    try {
      String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS_AND_JOB_EXECUTOR_WITH_EXPRESSIONS);
      KernelServices services = createKernelServicesBuilder(null)
          .setSubsystemXml(subsystemXml)
          .build();
      ServiceContainer container = services.getContainer();
  
      assertNotNull("platform service should be installed", container.getRequiredService(PLATFORM_SERVICE_NAME));
      assertNotNull("process engine service should be bound in JNDI", container.getRequiredService(processEngineServiceBindingServiceName));

      ServiceController<?> defaultEngineService = container.getService(ServiceNames.forManagedProcessEngine("__test"));

      assertNotNull("process engine controller for engine __test is installed ", defaultEngineService);

      ManagedProcessEngineMetadata metadata = ((MscManagedProcessEngineController) defaultEngineService.getService()).getProcessEngineMetadata();
      Map<String, String> configurationProperties = metadata.getConfigurationProperties();
      assertEquals("default", configurationProperties.get("job-acquisition-name"));

      Map<String, String> foxLegacyProperties = metadata.getFoxLegacyProperties();
      assertTrue(foxLegacyProperties.isEmpty());

      assertNotNull("process engine controller for engine __test is installed ", container.getRequiredService(ServiceNames.forManagedProcessEngine("__test")));

      // check we have parsed the plugin configurations
      List<ProcessEnginePluginXml> pluginConfigurations = metadata.getPluginConfigurations();
      
      assertEquals(1, pluginConfigurations.size());

      ProcessEnginePluginXml processEnginePluginXml = pluginConfigurations.get(0);
      assertEquals("org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin", processEnginePluginXml.getPluginClass());
      Map<String, String> processEnginePluginXmlProperties = processEnginePluginXml.getProperties();
      assertEquals("abc", processEnginePluginXmlProperties.get("test"));
      assertEquals("123", processEnginePluginXmlProperties.get("number"));
      assertEquals("true", processEnginePluginXmlProperties.get("bool"));
      
      String persistedSubsystemXml = services.getPersistedSubsystemXml();
      compareXml(null, subsystemXml, persistedSubsystemXml);
      
    } finally {
      for (String key : EXPRESSION_PROPERTIES.keySet()) {
        System.clearProperty(key);
      }
    }
  }
  
  private void assertExpressionType(ModelNode operation, String... elements) {
    assertModelType(ModelType.EXPRESSION, operation, elements);
  }
  
  private void assertStringType(ModelNode operation, String... elements) {
    assertModelType(ModelType.STRING, operation, elements);
  }
  
  private void assertModelType(ModelType type, ModelNode operation, String... elements) {
    for (String element : elements) {
      assertEquals(type, operation.get(element).getType());
    }
  }
}

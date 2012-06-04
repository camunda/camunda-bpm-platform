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
package com.camunda.fox.platform.subsystem.impl;

import java.util.List;

import junit.framework.Assert;

import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;

/**
 *
 * @author nico.rehwaldt@camunda.com
 * @author christian.lipphardt@camunda.com
 */
public class JBossSubsystemXMLTest extends AbstractSubsystemTest {

  public static final String SUBSYSTEM_WITH_ENGINES = "subsystemWithEngines.xml";
  public static final String SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY = "subsystemWithProcessEnginesElementOnly.xml";
  public static final String SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES = "subsystemWithEnginesAndProperties.xml";
  public static final String SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES = "subsystemWithDuplicateEngineNames.xml";

  public JBossSubsystemXMLTest() {
    super(FoxPlatformExtension.SUBSYSTEM_NAME, new FoxPlatformExtension());
  }

  @Test
  public void testParseSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
    System.out.println(operations);
    Assert.assertEquals(1, operations.size());
  }
  
  @Test
  public void testParseSubsystemXmlWithEngines() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);
    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
    System.out.println(operations);
    Assert.assertEquals(3, operations.size());
  }
  
  @Test
  public void testParseSubsystemXmlWithEnginesAndProperties() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
    System.out.println(normalizeXML(subsystemXml));
    
    List<ModelNode> operations = parse(subsystemXml);
    System.out.println(operations);
    Assert.assertEquals(5, operations.size());
  }
  
  @Test
  public void testInstallSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
//    services.getContainer().dumpServices();
    Assert.assertEquals(4, services.getContainer().getServiceNames().size());
  }
  
  @Test
  public void testInstallSubsystemWithEnginesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES);
    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
//    services.getContainer().dumpServices();
    Assert.assertEquals(6, services.getContainer().getServiceNames().size());
  }
  
  @Test
  public void testInstallSubsystemWithEnginesAndPropertiesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
    System.out.println(normalizeXML(subsystemXml));
    KernelServices services = installInController(subsystemXml);
//    services.getContainer().dumpServices();
    Assert.assertEquals(8, services.getContainer().getServiceNames().size());
  }
  
  @Test
  public void testInstallSubsystemWithDupliacteEngineNamesXml() throws Exception {
    String subsystemXml = FileUtils.readFile(SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);
    System.out.println(normalizeXML(subsystemXml));
    try {
      installInController(subsystemXml);
//    services.getContainer().dumpServices();
    } catch (FoxPlatformException fpe) {
      Assert.assertTrue("Duplicate process engine detected!", fpe.getMessage().contains("A process engine with name '__test' already exists."));
    }
  }
}

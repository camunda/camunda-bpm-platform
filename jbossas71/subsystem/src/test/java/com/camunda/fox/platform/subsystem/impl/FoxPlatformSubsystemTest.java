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

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;
import com.camunda.fox.platform.subsystem.impl.extension.ModelConstants;

/**
 *
 * @author christian.lipphardt@camunda.com
 */
public class FoxPlatformSubsystemTest extends AbstractSubsystemBaseTest {

  public FoxPlatformSubsystemTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new FoxPlatformExtension());
  }

  @Override
  protected String getSubsystemXml() throws IOException {
    try {
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES);
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  public void testSubsystem() throws Exception {
    final String configId = null;
    final AdditionalInitialization additionalInit = createAdditionalInitialization();

    // Parse the subsystem xml and install into the first controller
    final String subsystemXml = configId == null ? getSubsystemXml() : getSubsystemXml(configId);
    final KernelServices servicesA = super.installInController(additionalInit, subsystemXml);
    Assert.assertNotNull(servicesA);
    //Get the model and the persisted xml from the first controller
    final ModelNode modelA = servicesA.readWholeModel();
    Assert.assertNotNull(modelA);

    // Test marshaling
    final String marshalled = servicesA.getPersistedSubsystemXml();
    servicesA.shutdown();


    // validate the the normalized xmls
    String normalizedSubsystem = normalizeXML(subsystemXml);
//    compareXml(configId, normalizedSubsystem, normalizeXML(marshalled));

    //Install the persisted xml from the first controller into a second controller
    final KernelServices servicesB = super.installInController(additionalInit, marshalled);
    final ModelNode modelB = servicesB.readWholeModel();

    //Make sure the models from the two controllers are identical
    compare(modelA, modelB);

    // Test the describe operation
    final ModelNode operation = createDescribeOperation();
    final ModelNode result = servicesB.executeOperation(operation);
    Assert.assertTrue("the subsystem describe operation has to generate a list of operations to recreate the subsystem",
            !result.hasDefined(ModelDescriptionConstants.FAILURE_DESCRIPTION));
    final List<ModelNode> operations = result.get(ModelDescriptionConstants.RESULT).asList();
    servicesB.shutdown();

    final KernelServices servicesC = super.installInController(additionalInit, operations);
    final ModelNode modelC = servicesC.readWholeModel();

    compare(modelA, modelC);

    assertRemoveSubsystemResources(servicesA, getIgnoredChildResourcesForRemovalTest());
  }
}

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
package org.camunda.bpm.container.impl.jboss.util;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.jboss.as.controller.SimpleMapAttributeDefinition;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.util.ResourceBundle;

/**
 * This class must be in package 'org.camunda.bpm.container.impl.jboss.util' because of protected method test.
 */
public class ReflectionCompatibilityTest {

  @Test
  public void reflectionAccessOnFieldValueTypesWorks() {

    // prepare test setup
    ModelNode testModel = ModelNode.fromJSONString(
        "{" +
            "\"type\" : \"LIST\", " +
            "\"description\" : \"Extend the process engine through various plugins.\"," +
            "\"expressions-allowed\" : false," +
            "\"nillable\" : true," +
            "\"requires\" : [\"class\"]," +
            "\"allowed\" : [" +
            "\"class\"," +
            "\"properties\"" +
            "]" +
            "}"
    );
    StandardResourceDescriptionResolver testStandardResourceDescriptionResolver = new StandardResourceDescriptionResolver("camunda-bpm-platform", BpmPlatformExtension.class.getPackage().getName() + ".TestLocalDescriptions", BpmPlatformExtension.class.getClassLoader());
    SimpleMapAttributeDefinition simpleMapAttributeDefinition = new SimpleMapAttributeDefinition.Builder("test-map", false).build();
    FixedObjectTypeAttributeDefinition fixedObjectTypeAttributeDefinition = FixedObjectTypeAttributeDefinition.Builder.of("test-fixed-object", simpleMapAttributeDefinition).build();
    ResourceBundle resourceBundle = testStandardResourceDescriptionResolver.getResourceBundle(null);

    // test addValueTypeDescription for MapAttributeDefinitions
    fixedObjectTypeAttributeDefinition.addValueTypeDescription(testModel, "plugins", resourceBundle, testStandardResourceDescriptionResolver, null);
  }

}

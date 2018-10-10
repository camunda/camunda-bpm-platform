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

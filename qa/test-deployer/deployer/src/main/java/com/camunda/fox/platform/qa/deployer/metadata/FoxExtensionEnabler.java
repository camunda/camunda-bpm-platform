package com.camunda.fox.platform.qa.deployer.metadata;

import org.jboss.arquillian.test.spi.TestClass;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxExtensionEnabler {

  private final MetadataExtractor metadataExtractor;

  public FoxExtensionEnabler(TestClass testClass) {
    this.metadataExtractor = new MetadataExtractor(testClass);
  }

  public FoxExtensionEnabler(MetadataExtractor metadataExtractor) {
    this.metadataExtractor = metadataExtractor;
  }

  public boolean isExtensionRequired() {
    return hasDeploymentAnnotation();
  }

  // ---------------------------------------------------------------------------------------------------
  // Internal methods
  // ---------------------------------------------------------------------------------------------------
  private boolean hasDeploymentAnnotation() {
    return metadataExtractor.deployment().isDefinedOnClassLevel()
            || metadataExtractor.deployment().isDefinedOnAnyMethod();
  }
}

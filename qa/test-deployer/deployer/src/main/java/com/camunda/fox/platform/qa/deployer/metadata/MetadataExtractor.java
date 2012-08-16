package com.camunda.fox.platform.qa.deployer.metadata;

import org.activiti.engine.test.Deployment;
import org.jboss.arquillian.test.spi.TestClass;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class MetadataExtractor {

  private final TestClass testClass;
  private final AnnotationInspector<Deployment> processDeploymentInspector;

  public MetadataExtractor(TestClass testClass) {
    this.testClass = testClass;
    this.processDeploymentInspector = new AnnotationInspector<Deployment>(testClass, Deployment.class);
  }

  public AnnotationInspector<Deployment> deployment() {
    return processDeploymentInspector;
  }

  public boolean hasPlatformTestAnnotation() {
    return false;
  }

  public Class<?> getJavaClass() {
    return testClass.getJavaClass();
  }
}

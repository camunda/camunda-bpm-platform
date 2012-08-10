package com.camunda.fox.platform.qa.deployer.metadata;

import org.activiti.engine.test.Deployment;
import com.camunda.fox.platform.qa.deployer.configuration.FoxConfiguration;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class MetadataProvider {

  private final FoxConfiguration configuration;
  private final MetadataExtractor metadataExtractor;
  private final Method testMethod;

  public MetadataProvider(Method testMethod, MetadataExtractor metadataExtractor, FoxConfiguration configuration) {
    this.metadataExtractor = metadataExtractor;
    this.configuration = configuration;
    this.testMethod = testMethod;
  }

  // ---------------------------------------------------------------------------------------------------
  // Public API methods
  // ---------------------------------------------------------------------------------------------------
  
  public Set<String> getProcessDeploymentResources() {
    Set<String> deploymentResources = new HashSet<String>();
    
    Deployment deploymentOnClassLevel = metadataExtractor.deployment().getAnnotationOnClassLevel();
    if (deploymentOnClassLevel != null) {
      deploymentResources.addAll(Arrays.asList(deploymentOnClassLevel.resources()));
    }
    
    Deployment deploymentOnMethodLevel = metadataExtractor.deployment().getOn(testMethod);
    if (deploymentOnMethodLevel != null) {
      deploymentResources.addAll(Arrays.asList(deploymentOnMethodLevel.resources()));
    }
    
    return deploymentResources;
  }
}

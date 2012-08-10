package com.camunda.fox.platform.qa.deployer.configuration;

import java.io.Serializable;


/**
 * 
 * @author nico.rehwaldt@camunda.com
 */
public class FoxDeploymentConfiguration implements Serializable {

  public static final String EXTENSION_QUALIFIER = "fox";
  public static final String PROPERTY_PREFIX = "arquillian.extension.fox.deployment.";
  
  public static final String PROPERTIES_FILE = "fox-deployment.properties";
  
  /**
   * The [/application name]/module name/ prefix of the global jndi name pointing to the application extension archive
   */
  private String extensionArchiveJndiPrefix;

  private String processEngineName = "default";
  
  public void setExtensionArchiveJndiPrefix(String applicationArchiveName) {
    this.extensionArchiveJndiPrefix = applicationArchiveName;
  }

  public String getExtensionArchiveJndiPrefix() {
    return extensionArchiveJndiPrefix;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }
}

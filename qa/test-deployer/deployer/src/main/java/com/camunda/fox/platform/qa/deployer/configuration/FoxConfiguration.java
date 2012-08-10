package com.camunda.fox.platform.qa.deployer.configuration;

import java.io.Serializable;


/**
 * 
 * @author nico.rehwaldt@camunda.com
 */
public class FoxConfiguration implements Serializable {

  public static final String EXTENSION_QUALIFIER = "fox";
  public static final String PROPERTY_PREFIX = "arquillian.extension.fox.";
  
  public static final String PROPERTIES_FILE = "fox-arquillian.properties";
}

package org.camunda.bpm.container.impl.metadata;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 */
public class PropertyHelperTest extends TestCase {
  
  protected static final String JOB_EXECUTOR_DEPLOYMENT_AWARE_PROP = "jobExecutorDeploymentAware";
  protected static final String MAIL_SERVER_PORT_PROP = "mailServerPort";
  protected static final String JDBC_URL_PROP = "jdbcUrl";
  protected static final String DB_IDENTITY_USED_PROP = "dbIdentityUsed";

  /**
   * Assert that String, int and boolean properties can be set.
   */
  public void testProcessEngineConfigurationProperties() {
    ProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();
    
    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put(JOB_EXECUTOR_DEPLOYMENT_AWARE_PROP, "true");
    propertiesToSet.put(MAIL_SERVER_PORT_PROP, "42");
    propertiesToSet.put(JDBC_URL_PROP, "someUrl");
    
    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);
    
    Assert.assertTrue(engineConfiguration.isJobExecutorDeploymentAware());
    Assert.assertEquals(42, engineConfiguration.getMailServerPort());
    Assert.assertEquals("someUrl", engineConfiguration.getJdbcUrl());
  }
  
  /**
   * Assures that property names are matched on the setter name according to java beans conventions
   * and not on the field name.
   */
  public void testConfigurationPropertiesWithMismatchingFieldAndSetter() {
    ProcessEngineConfigurationImpl engineConfiguration = new StandaloneProcessEngineConfiguration();
    
    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put(DB_IDENTITY_USED_PROP, "false");
    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);
    
    Assert.assertFalse(engineConfiguration.isDbIdentityUsed());
    
    propertiesToSet.put(DB_IDENTITY_USED_PROP, "true");
    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);
    
    Assert.assertTrue(engineConfiguration.isDbIdentityUsed());
  }
  
  public void testNonExistingPropertyForProcessEngineConfiguration() {
    ProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();
    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put("aNonExistingProperty", "someValue");
    
    try {
      PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);
      Assert.fail();
    } catch (Exception e) {
      // happy path
    }
  }
}

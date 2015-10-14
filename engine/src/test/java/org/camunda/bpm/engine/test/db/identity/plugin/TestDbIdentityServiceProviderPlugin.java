/**
 * 
 */
package org.camunda.bpm.engine.test.db.identity.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

/**
 * @author Simon Jonischkeit
 *
 */
public class TestDbIdentityServiceProviderPlugin implements ProcessEnginePlugin {
  
  TestDbIdentityServiceProviderFactory testFactory;
  
  public TestDbIdentityServiceProviderPlugin() {
    testFactory = new TestDbIdentityServiceProviderFactory();
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setIdentityProviderSessionFactory(testFactory);

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // nothing to do here

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // nothing to do here

  }

}

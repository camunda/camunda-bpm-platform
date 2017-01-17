/**
 *
 */
package org.camunda.bpm.engine.test.api.identity.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.test.api.identity.MyNullSaltGenerator;

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
    processEngineConfiguration.setSaltGenerator(new MyNullSaltGenerator());
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // nothing to do here

  }

}

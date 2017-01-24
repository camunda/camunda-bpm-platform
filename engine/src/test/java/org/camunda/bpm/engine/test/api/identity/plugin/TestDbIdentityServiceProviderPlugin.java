/**
 *
 */
package org.camunda.bpm.engine.test.api.identity.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.digest.ShaHashDigest;
import org.camunda.bpm.engine.test.api.identity.util.MyConstantSaltGenerator;

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
    processEngineConfiguration.setPasswordEncryptor(new ShaHashDigest());

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator(""));
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // nothing to do here

  }

}

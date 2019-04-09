/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

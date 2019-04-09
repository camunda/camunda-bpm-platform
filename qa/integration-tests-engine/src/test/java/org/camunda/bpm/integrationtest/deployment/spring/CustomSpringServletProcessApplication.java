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
package org.camunda.bpm.integrationtest.deployment.spring;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.engine.spring.application.SpringServletProcessApplication;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 *
 */
public class CustomSpringServletProcessApplication extends SpringServletProcessApplication {

  private boolean isPostDeployInvoked = false;
  private boolean isPreUndeployInvoked = false;

  @PostDeploy
  public void postDeploy() {
    isPostDeployInvoked = true;
  }

  @PreUndeploy
  public void preUndeploy() {
    isPreUndeployInvoked = true;
  }

  @Override
  public void start() {
    Assert.assertFalse(isPostDeployInvoked);
    super.start();
    Assert.assertTrue("@PostDeploy Method not invoked", isPostDeployInvoked);
  }

  @Override
  public void stop() {
    Assert.assertFalse(isPreUndeployInvoked);
    super.stop();
    Assert.assertTrue("@PreUndeploy Method not invoked", isPreUndeployInvoked);
  }

}

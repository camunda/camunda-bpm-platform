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
package org.camunda.bpm.integrationtest.functional.el;

import org.camunda.bpm.integrationtest.functional.el.beans.ResolveExpressionBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class ElResolverLookupTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name = "pa")
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(NullELResolver.class)
      .addClass(ResolveExpressionBean.class)
      .addAsResource(
          "org/camunda/bpm/integrationtest/functional/el/services/org.camunda.bpm.application.ProcessApplicationElResolver",
          "META-INF/services/org.camunda.bpm.application.ProcessApplicationElResolver")
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/elServiceTaskProcess.bpmn20.xml");
  }

  @Test
  @OperateOnDeployment("pa")
  public void testNullElResolverIsIgnored() {
    // The expression should be resolved correctly although the NullElResolver
    // is present
    runtimeService.startProcessInstanceByKey("elServiceTaskProcess");

    Assert.assertNotNull(taskService.createTaskQuery().singleResult());
  }
}

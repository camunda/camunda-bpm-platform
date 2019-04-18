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

import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.integrationtest.functional.el.beans.ResolveFormDataBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefan Hentschel.
 */
@RunWith(Arquillian.class)
public class ElResolveStartFormDataTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(ResolveFormDataBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/elStartFormProcessWithFormData.bpmn20.xml");
  }

  @Test
  public void testStartFormDataWithDefaultValueExpression() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    StartFormData formData = formService.getStartFormData(processDefinitionId);
    Object defaultValue = formData.getFormFields().get(0).getValue().getValue();

    Assert.assertNotNull(defaultValue);
    Assert.assertEquals("testString123", defaultValue);
  }

  @Test
  public void testStartFormDataWithLabelExpression() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    StartFormData formData = formService.getStartFormData(processDefinitionId);

    String label = formData.getFormFields().get(0).getLabel();
    Assert.assertNotNull(label);
    Assert.assertEquals("testString123", label);
  }

}

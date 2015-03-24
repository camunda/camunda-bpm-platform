/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.functional.el;

import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.integrationtest.functional.el.beans.ResolveFormDataBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefan Hentschel.
 */
@RunWith(Arquillian.class)
public class ElResolveFormBean extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name = "pa")
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(ResolveFormDataBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/elUserTaskProcessWithFormData.bpmn20.xml");
  }

  @Test
  @OperateOnDeployment("pa")
  public void testFormDataWithExpression() {
    // The expression should be resolved correctly
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("elUserTaskProcess");

    StartFormData startFormData = formService.getStartFormData(instance.getProcessDefinitionId());
    TypedValue value = startFormData.getFormFields().get(0).getValue();

    Assert.assertNotNull(value);
    Assert.assertEquals("testString123", value.toString());
  }

}

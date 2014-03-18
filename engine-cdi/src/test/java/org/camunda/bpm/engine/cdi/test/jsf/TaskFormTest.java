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
package org.camunda.bpm.engine.cdi.test.jsf;

import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;

import org.camunda.bpm.engine.cdi.compat.CamundaTaskForm;
import org.camunda.bpm.engine.cdi.compat.FoxTaskForm;
import org.camunda.bpm.engine.cdi.jsf.TaskForm;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniel Meyer
 */
public class TaskFormTest extends CdiProcessEngineTestCase {

  @Test
  public void testTaskFormInjectable() throws Exception {

    Set<Bean<?>> taskForm = beanManager.getBeans(TaskForm.class);
    try {
      Bean<? extends Object> bean = beanManager.resolve(taskForm);
      Assert.assertNotNull(bean);
    }catch(AmbiguousResolutionException e) {
      Assert.fail("Injection of TaskForm is ambiguous.");
    }

    Set<Bean<?>> foxTaskForm = beanManager.getBeans(FoxTaskForm.class);
    try {
      Bean<? extends Object> bean = beanManager.resolve(foxTaskForm);
      Assert.assertNotNull(bean);
    }catch(AmbiguousResolutionException e) {
      Assert.fail("Injection of FoxTaskForm is ambiguous.");
    }

    Set<Bean<?>> camundaTaskForm = beanManager.getBeans(CamundaTaskForm.class);
    try {
      Bean<? extends Object> bean = beanManager.resolve(camundaTaskForm);
      Assert.assertNotNull(bean);
    }catch(AmbiguousResolutionException e) {
      Assert.fail("Injection of CamundaTaskForm is ambiguous.");
    }

  }

}

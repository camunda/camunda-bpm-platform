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

package org.camunda.bpm.engine.test.api.externaltask;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskCreatedListener;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nikola Koevski
 */
public class ExternalTaskCreatedNotifierTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testExternalTaskCreatedNotification() {

    final AtomicBoolean isExternalTaskCreated = new AtomicBoolean(false);

    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration())
      .addExternalTaskCreatedListener(new ExternalTaskCreatedListener() {
        @Override
        public void onExternalTaskCreated() {
          isExternalTaskCreated.set(true);
        }
    });

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    assertEquals(true, isExternalTaskCreated.get());
  }
}

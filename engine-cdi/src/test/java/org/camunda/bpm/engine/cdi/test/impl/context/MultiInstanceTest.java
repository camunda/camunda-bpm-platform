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
package org.camunda.bpm.engine.cdi.test.impl.context;

import java.util.Arrays;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class MultiInstanceTest extends CdiProcessEngineTestCase {

  @Test
  @Deployment
  public void testParallelMultiInstanceServiceTasks() {

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    businessProcess.setVariable("list", Arrays.asList(new String[]{"1","2"}));
    businessProcess.startProcessByKey("miParallelScriptTask");

  }

}

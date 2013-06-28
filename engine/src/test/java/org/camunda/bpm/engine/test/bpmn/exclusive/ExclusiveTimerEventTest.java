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
package org.camunda.bpm.engine.test.bpmn.exclusive;

import java.util.Date;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


public class ExclusiveTimerEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testCatchingTimerEvent() throws Exception {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveTimers");
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    assertEquals(3, jobQuery.count());

    // After setting the clock to time '50minutes and 5 seconds', the timers should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L);

    assertEquals(0, jobQuery.count());
    assertProcessEnded(pi.getProcessInstanceId());


  }


}
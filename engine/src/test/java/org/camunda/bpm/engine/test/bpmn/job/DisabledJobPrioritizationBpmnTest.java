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
package org.camunda.bpm.engine.test.bpmn.job;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class DisabledJobPrioritizationBpmnTest extends PluggableProcessEngineTestCase {

  protected void setUp() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(false);
  }

  protected void tearDown() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioProcess.bpmn20.xml")
  public void testJobPriority() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task1")
      .startBeforeActivity("task2")
      .execute();

    // then
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(2, jobs.size());

    for (Job job : jobs) {
      assertNotNull(job);
      assertEquals(0, job.getPriority());
    }
  }
}

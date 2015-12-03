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
package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Stefan Hentschel.
 */
public class JobRetryCmdWithDefaultPropertyTest extends ResourceProcessEngineTestCase {

  public JobRetryCmdWithDefaultPropertyTest() {
    super("org/camunda/bpm/engine/test/bpmn/async/default.job.retry.property.camunda.cfg.xml");
  }

  /**
   * Check if property "DefaultNumberOfRetries" will be used
   */
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedTask.bpmn20.xml" })
  public void testDefaultNumberOfRetryProperty() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");
    assertNotNull(pi);

    Job job = managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    assertEquals(2, job.getRetries());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testOverwritingPropertyWithBpmnExtension() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertNotNull(pi);

    Job job = managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    try {
      managementService.executeJob(job.getId());
      fail("Exception expected!");
    } catch(Exception e) {
      // expected
    }

    job = managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertEquals(4, job.getRetries());

  }
}

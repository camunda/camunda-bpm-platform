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
package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Daniel Meyer
 *
 */
public class TestJobDefinitionCreationOnUpgrade extends AbstractDbUpgradeTestCase {

  @Test
  public void testJobDefinitionsCreated() {
    final String key = "TestFixture70.testJobDefinitionsCreated";
    final ProcessInstance migratedInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(key).singleResult();

    //// given

    // initially the job definition does not exist
    Assert.assertEquals(0, managementService.createJobDefinitionQuery().processDefinitionKey(key).count());

    //// if

    // I start a new instance of the process (and thus trigger the parsing of the XML)
    ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(key);

    //// then

    // the job definition is created
    Assert.assertEquals(1, managementService.createJobDefinitionQuery().processDefinitionKey(key).count());

    // the existing job is NOT associated with the new job definition
    Job migratedJob = managementService.createJobQuery().processInstanceId(migratedInstance.getId()).singleResult();
    Assert.assertNull(migratedJob.getJobDefinitionId());

    // the new job is associated with the new job definition
    Job newJob = managementService.createJobQuery().processInstanceId(newInstance.getId()).singleResult();
    Assert.assertNotNull(newJob.getJobDefinitionId());

    // I can complete both instances:
    managementService.executeJob(migratedJob.getId());
    managementService.executeJob(newJob.getId());


  }
}

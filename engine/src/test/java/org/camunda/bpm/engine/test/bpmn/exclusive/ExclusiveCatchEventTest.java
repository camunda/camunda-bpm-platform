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
package org.camunda.bpm.engine.test.bpmn.exclusive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Stefan Hentschel
 */
public class ExclusiveCatchEventTest extends PluggableProcessEngineTest {
  
  @Deployment
  @Test
  public void testNonExclusiveCatchEvent() {
    // start process 
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 1 non-exclusive job in the database:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertFalse(((JobEntity)job).isExclusive());

    testRule.waitForJobExecutorToProcessAllJobs(6000L);
    
    // all the jobs are done
    assertEquals(0, managementService.createJobQuery().count());      
  }

  @Deployment
  @Test
  public void testExclusiveCatchEvent() {
    // start process 
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 1 exclusive job in the database:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertTrue(((JobEntity)job).isExclusive());

    testRule.waitForJobExecutorToProcessAllJobs(6000L);
    
    // all the jobs are done
    assertEquals(0, managementService.createJobQuery().count());      
  }
  
  @Deployment
  @Test
  public void testExclusiveCatchEventConcurrent() {
    // start process 
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 2 exclusive jobs in the database:
    assertEquals(2, managementService.createJobQuery().count());

    testRule.waitForJobExecutorToProcessAllJobs(6000L);
    
    // all the jobs are done
    assertEquals(0, managementService.createJobQuery().count());      
  }
  
}

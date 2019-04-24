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
package org.camunda.bpm.integrationtest.functional.jodatime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class JodaTimeClassloadingTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment().addAsResource("org/camunda/bpm/integrationtest/functional/jodatime/JodaTimeClassloadingTest.bpmn20.xml");
  }
  
  
  private Date testExpression(String timeExpression) {
    // Set the clock fixed
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("dueDate", timeExpression);

    // After process start, there should be timer created
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables1);
    Assert.assertEquals(1, managementService.createJobQuery().processInstanceId(pi1.getId()).count());

    List<Job> jobs = managementService.createJobQuery().executable().list();
    Assert.assertEquals(1, jobs.size());
    runtimeService.deleteProcessInstance(pi1.getId(), "test");
    
    return jobs.get(0).getDuedate();
  }

  @Test
  public void testTimeExpressionComplete() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dt));
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dt), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dueDate));
  }

  @Test
  public void testTimeExpressionWithoutSeconds() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dt));
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dt), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dueDate));
  }

  @Test
  public void testTimeExpressionWithoutMinutes() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH").format(new Date()));
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH").format(dt), new SimpleDateFormat("yyyy-MM-dd'T'HH").format(dueDate));
  }

  @Test
  public void testTimeExpressionWithoutTime() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(dt), new SimpleDateFormat("yyyy-MM-dd").format(dueDate));
  }

  @Test
  public void testTimeExpressionWithoutDay() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM").format(new Date()));
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM").format(dt), new SimpleDateFormat("yyyy-MM").format(dueDate));
  }

  @Test
  public void testTimeExpressionWithoutMonth() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy").format(new Date()));
    Assert.assertEquals(new SimpleDateFormat("yyyy").format(dt), new SimpleDateFormat("yyyy").format(dueDate));
  }

}

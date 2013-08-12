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

package org.camunda.bpm.engine.test.bpmn.usertask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.joda.time.Period;


/**
 * @author Frederik Heremans
 */
public class TaskDueDateExtensionsTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testDueDateExtension() throws Exception {
    
    Date date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("06-07-1986 12:10:00");
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", date);
    
    // Start process-instance, passing date that should be used as dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(task.getDueDate());
    assertEquals(date, task.getDueDate());
  }
  
  @Deployment
  public void testDueDateStringExtension() throws Exception {
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", "1986-07-06T12:10:00");
    
    // Start process-instance, passing date that should be used as dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(task.getDueDate());
    Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("06-07-1986 12:10:00");
    assertEquals(date, task.getDueDate());
  }
  
  @Deployment
  public void testRelativeDueDate() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", "P2DT2H30M");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    
    Date dueDate = task.getDueDate();
    assertNotNull(dueDate);
    
    Period period = new Period(task.getCreateTime().getTime(), dueDate.getTime());
    assertEquals(period.getDays(), 2);
    assertEquals(period.getHours(), 2);
    assertEquals(period.getMinutes(), 30);
  }
}

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
package org.camunda.bpm.engine.cdi.test.impl.event;

import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.annotation.event.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ApplicationScoped
public class TestEventListener {

  public void reset() {
    startActivityService1 = 0;
    endActivityService1 = 0;
    takeTransition1 = 0;
    createTaskUser1 = 0;
    assignTaskUser1 = 0;
    completeTaskUser1 = 0;
    deleteTaskUser1 = 0;

    eventsReceivedByKey.clear();
    eventsReceived.clear();
  }

  private final Set<BusinessProcessEvent> eventsReceivedByKey = new HashSet<BusinessProcessEvent>();

  // receives all events related to "process1"
  public void onProcessEventByKey(@Observes @BusinessProcessDefinition("process1") BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    assertEquals("process1", businessProcessEvent.getProcessDefinition().getKey());
    eventsReceivedByKey.add(businessProcessEvent);
  }

  public Set<BusinessProcessEvent> getEventsReceivedByKey() {
    return eventsReceivedByKey;
  }


  // ---------------------------------------------------------

  private final Set<BusinessProcessEvent> eventsReceived = new HashSet<BusinessProcessEvent>();

  // receives all events
  public void onProcessEvent(@Observes BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    eventsReceived.add(businessProcessEvent);
  }

  public Set<BusinessProcessEvent> getEventsReceived() {
    return eventsReceived;
  }

  // ---------------------------------------------------------

  private int startActivityService1 = 0;
  private int endActivityService1 = 0;
  private int takeTransition1 = 0;

  public void onStartActivityService1(@Observes @StartActivity("service1") BusinessProcessEvent businessProcessEvent) {
    assertEquals("service1", businessProcessEvent.getActivityId());
    assertNotNull(businessProcessEvent);
    assertNull(businessProcessEvent.getTask());
    assertNull(businessProcessEvent.getTaskId());
    assertNull(businessProcessEvent.getTaskDefinitionKey());
    startActivityService1 += 1;
  }

  public void onEndActivityService1(@Observes @EndActivity("service1") BusinessProcessEvent businessProcessEvent) {
    assertEquals("service1", businessProcessEvent.getActivityId());
    assertNotNull(businessProcessEvent);
    assertNull(businessProcessEvent.getTask());
    assertNull(businessProcessEvent.getTaskId());
    assertNull(businessProcessEvent.getTaskDefinitionKey());
    endActivityService1 += 1;
  }

  public void takeTransition1(@Observes @TakeTransition("t1") BusinessProcessEvent businessProcessEvent) {
    assertEquals("t1", businessProcessEvent.getTransitionName());
    assertNotNull(businessProcessEvent);
    assertNull(businessProcessEvent.getTask());
    assertNull(businessProcessEvent.getTaskId());
    assertNull(businessProcessEvent.getTaskDefinitionKey());
    takeTransition1 += 1;
  }

  public int getEndActivityService1() {
    return endActivityService1;
  }

  public int getStartActivityService1() {
    return startActivityService1;
  }

  public int getTakeTransition1() {
    return takeTransition1;
  }


  // ---------------------------------------------------------

  private int createTaskUser1 = 0;
  private int assignTaskUser1 = 0;
  private int completeTaskUser1 = 0;
  private int deleteTaskUser1 = 0;

  public void onCreateTask(@Observes @CreateTask("user1") BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    assertNotNull(businessProcessEvent.getTask());
    assertNotNull(businessProcessEvent.getTaskId());
    assertEquals("user1", businessProcessEvent.getTaskDefinitionKey());
    createTaskUser1++;
  }

  public void onAssignTask(@Observes @AssignTask("user1") BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    assertNotNull(businessProcessEvent.getTask());
    assertNotNull(businessProcessEvent.getTaskId());
    assertEquals("user1", businessProcessEvent.getTaskDefinitionKey());
    assignTaskUser1++;
  }

  public void onCompleteTask(@Observes @CompleteTask("user1") BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    assertNotNull(businessProcessEvent.getTask());
    assertNotNull(businessProcessEvent.getTaskId());
    assertEquals("user1", businessProcessEvent.getTaskDefinitionKey());
    completeTaskUser1++;
  }

  public void onDeleteTask(@Observes @DeleteTask("user1") BusinessProcessEvent businessProcessEvent) {
    assertNotNull(businessProcessEvent);
    assertNotNull(businessProcessEvent.getTask());
    assertNotNull(businessProcessEvent.getTaskId());
    assertEquals("user1", businessProcessEvent.getTaskDefinitionKey());
    deleteTaskUser1++;
  }

  public int getCreateTaskUser1() {
    return createTaskUser1;
  }

  public int getAssignTaskUser1() {
    return assignTaskUser1;
  }

  public int getCompleteTaskUser1() {
    return completeTaskUser1;
  }

  public int getDeleteTaskUser1() {
    return deleteTaskUser1;
  }

}

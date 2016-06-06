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
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class EventNotificationTest extends CdiProcessEngineTestCase {

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testReceiveAll() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    // complete user task
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(16, listenerBean.getEventsReceived().size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml",
      "org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process2.bpmn20.xml" })
  public void testSelectEventsPerProcessDefinition() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getEventsReceivedByKey().size());
    //start the 2 processes
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceivedByKey().size());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testSelectEventsPerActivity() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getEndActivityService1());
    assertEquals(0, listenerBean.getStartActivityService1());
    assertEquals(0, listenerBean.getTakeTransition1());

    // start the process
    runtimeService.startProcessInstanceByKey("process1");

    // assert
    assertEquals(1, listenerBean.getEndActivityService1());
    assertEquals(1, listenerBean.getStartActivityService1());
    assertEquals(1, listenerBean.getTakeTransition1());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml"})
  public void testSelectEventsPerTask() {
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getCreateTaskUser1());
    assertEquals(0, listenerBean.getAssignTaskUser1());
    assertEquals(0, listenerBean.getCompleteTaskUser1());
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), "demo");

    taskService.complete(task.getId());

    assertEquals(1, listenerBean.getCreateTaskUser1());
    assertEquals(1, listenerBean.getAssignTaskUser1());
    assertEquals(1, listenerBean.getCompleteTaskUser1());
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    listenerBean.reset();
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process1");

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    assertEquals(1, listenerBean.getDeleteTaskUser1());
  }

  @Test
  @Deployment
  public void testMultiInstanceEvents(){
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertThat(listenerBean.getEventsReceived().size(), is(0));
    runtimeService.startProcessInstanceByKey("process1");
    waitForJobExecutorToProcessAllJobs(TimeUnit.SECONDS.toMillis(5L), 500L);

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));

    // 2: start event (start + end)
    // 1: transition to first mi activity
    // 2: first mi body (start + end)
    // 4: two instances of the inner activity (start + end)
    // 1: transition to second mi activity
    // 2: second mi body (start + end)
    // 4: two instances of the inner activity (start + end)
    // 1: transition to the user task
    // 2: user task (start + task create event)
    // = 19
    assertThat(listenerBean.getEventsReceived().size(), is(19));
  }

  @Test
  @Deployment
  public void testMultiInstanceEventsAfterExternalTrigger() {

    runtimeService.startProcessInstanceByKey("process");

    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // 6: three user task instances (complete + end)
    // 1: one mi body instance (end)
    // 1: one sequence flow instance (take)
    // 2: one end event instance (start + end)
    // = 5
    Set<BusinessProcessEvent> eventsReceived = listenerBean.getEventsReceived();
    assertThat(eventsReceived.size(), is(10));
  }

}

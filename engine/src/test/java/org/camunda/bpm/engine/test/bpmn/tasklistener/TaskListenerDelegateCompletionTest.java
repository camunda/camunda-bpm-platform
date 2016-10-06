package org.camunda.bpm.engine.test.bpmn.tasklistener;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class TaskListenerDelegateCompletionTest {

  protected static final String COMPLETE_LISTENER = "org.camunda.bpm.engine.test.bpmn.tasklistener.util.CompletingTaskListener";
  protected static final String TASK_LISTENER_PROCESS = "taskListenerProcess";
  protected static final String ACTIVITY_ID = "UT";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void setUp() {
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();
  }

  @After
  public void cleanUp() {
    if (runtimeService.createProcessInstanceQuery().count() > 0) {
      runtimeService.deleteProcessInstance(runtimeService.createProcessInstanceQuery().singleResult().getId(),null,true);
    }
  }


  protected static BpmnModelInstance setupProcess(String eventName) {
    return Bpmn.createExecutableProcess(TASK_LISTENER_PROCESS)
        .startEvent()
          .userTask(ACTIVITY_ID)
          .camundaTaskListenerClass(eventName,COMPLETE_LISTENER)
        .endEvent()
        .done();
  }

  @Test
  public void testCompletionIsPossibleOnCreation () {
    //given
    createProcessWithListener(TaskListener.EVENTNAME_CREATE);

    //when
    runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);

    //then
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task, is(nullValue()));
  }

  @Test
  public void testCompletionIsPossibleOnAssignment () {
    //given
    createProcessWithListener(TaskListener.EVENTNAME_ASSIGNMENT);

    //when
    runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(),"test assignee");

    //then
    task = taskService.createTaskQuery().singleResult();
    assertThat(task, is(nullValue()));
  }

  @Test
  public void testCompletionIsNotPossibleOnComplete () {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("invalid task state"));
    //given
    createProcessWithListener(TaskListener.EVENTNAME_COMPLETE);

    //when
    runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);
    Task task = taskService.createTaskQuery().singleResult();

    taskService.complete(task.getId());
  }

  @Test
  public void testCompletionIsNotPossibleOnDelete () {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("invalid task state"));

    //given
    createProcessWithListener(TaskListener.EVENTNAME_DELETE);

    //when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);
    runtimeService.deleteProcessInstance(processInstance.getId(),"test reason");
  }

  protected void createProcessWithListener(String eventName) {
    BpmnModelInstance bpmnModelInstance = setupProcess(eventName);
    testHelper.deploy(bpmnModelInstance);
  }

}

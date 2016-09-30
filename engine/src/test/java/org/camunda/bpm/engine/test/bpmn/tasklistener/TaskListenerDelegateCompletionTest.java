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
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static junit.framework.TestCase.fail;
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
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected String event;
  protected BpmnModelInstance process;

  @Before
  public void setUp() {
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();
  }

  protected static BpmnModelInstance setupProcess(String eventName) {
    return taskListener(Bpmn.createExecutableProcess(TASK_LISTENER_PROCESS)
        .startEvent()
          .userTask(ACTIVITY_ID)
        .endEvent()
        .done(), eventName);
  }

  protected static BpmnModelInstance taskListener(BpmnModelInstance targetModel, String eventName) {
    CamundaTaskListener taskListener = targetModel.newInstance(CamundaTaskListener.class);
    taskListener.setCamundaClass(COMPLETE_LISTENER);
    taskListener.setCamundaEvent(eventName);

    UserTask task = targetModel.getModelElementById(ACTIVITY_ID);
    task.builder().addExtensionElement(taskListener);
    return targetModel;
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
    //given
    createProcessWithListener(TaskListener.EVENTNAME_COMPLETE);

    //when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);
    Task task = taskService.createTaskQuery().singleResult();

    try {
      taskService.complete(task.getId());
      fail("expected exception to be thrown");
      //then
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(),containsString("invalid task state"));
      runtimeService.deleteProcessInstance(processInstance.getId(),"clean up");
    }
  }

  @Test
  public void testCompletionIsNotPossibleOnDelete () {

    //given
    createProcessWithListener(TaskListener.EVENTNAME_DELETE);

    //when
    ProcessInstance started = runtimeService.startProcessInstanceByKey(TASK_LISTENER_PROCESS);

    try {
      runtimeService.deleteProcessInstance(started.getId(),"test reason");
      fail("expected exception to be thrown");
      //then
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(),containsString("invalid task state"));
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
    }
  }

  protected void createProcessWithListener(String eventName) {
    this.event = eventName;
    this.process = setupProcess(eventName);
    testHelper.deploy(process);
  }

}

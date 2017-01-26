package org.camunda.bpm.pa;

import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.fileValue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.admin.impl.web.SetupResource;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.pa.demo.InvoiceDemoDataGenerator;
import org.joda.time.DateTime;

/**
 *
 * @author nico.rehwaldt
 */
@ProcessApplication("camunda-test-processes")
public class DevProcessApplication extends ServletProcessApplication {

  private final static Logger LOGGER = Logger.getLogger(DevProcessApplication.class.getName());

  @PostDeploy
  public void startProcesses(final ProcessEngine engine) throws Exception {
    createAdminDemoData(engine);
    createTasklistDemoData(engine);
    new Thread() {
      @Override
      public void run() {
        try {
          createReportDemoData(engine);
          createCockpitDemoData(engine);
          LOGGER.info("Done generating demo data.");
        }
        catch(Exception e) {
          LOGGER.log(Level.WARNING, "Exception while generating demo data", e);
        }
      }
    }.start();
  }

  protected void createCockpitDemoData(final ProcessEngine engine) throws Exception {
    RuntimeService runtimeService = engine.getRuntimeService();

    Map<String, Object> vars1 = new HashMap<String, Object>();
    vars1.put("booleanVar", true);
    runtimeService.startProcessInstanceByKey("ProcessWithExclusiveGateway", "secondUserTask", vars1);

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("booleanVar", false);
    runtimeService.startProcessInstanceByKey("ProcessWithExclusiveGateway", "firstUserTask", vars2);

    runtimeService.startProcessInstanceByKey("multipleFailingServiceTasks", "aBusinessKey");

    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");

    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");

    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(10));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(12));
    runtimeService.startProcessInstanceByKey("CallingCallActivity");

    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("FailingErrorMessage");


    runtimeService.startProcessInstanceByKey("OrderProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("OrderProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("OrderProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("OrderProcess");

    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("FailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(10));
    runtimeService.startProcessInstanceByKey("FailingProcess");

    runtimeService.startProcessInstanceByKey("FailingSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("FailingSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));

    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    ClockUtil.setCurrentTime(createArtificalDate(10));
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");

    runtimeService.startProcessInstanceByKey("CallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("CallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("CallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("CallActivity");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("CallActivity");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("value1", "a");
    params.put("value2", "b");
    params.put("value3", "c");

    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);

    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    ClockUtil.setCurrentTime(createArtificalDate(10));
    runtimeService.startProcessInstanceByKey("processWithSubProcess");

    runtimeService.startProcessInstanceByKey("executionProcess");
    ClockUtil.setCurrentTime(createArtificalDate(2));
    runtimeService.startProcessInstanceByKey("executionProcess");
    ClockUtil.setCurrentTime(createArtificalDate(4));
    runtimeService.startProcessInstanceByKey("executionProcess");
    ClockUtil.setCurrentTime(createArtificalDate(6));
    runtimeService.startProcessInstanceByKey("executionProcess");
    ClockUtil.setCurrentTime(createArtificalDate(8));
    runtimeService.startProcessInstanceByKey("executionProcess");
    ClockUtil.setCurrentTime(createArtificalDate(10));
    runtimeService.startProcessInstanceByKey("executionProcess");

    runtimeService.startProcessInstanceByKey("changeVariablesProcess");
    runtimeService.startProcessInstanceByKey("changeVariablesProcess");
    runtimeService.startProcessInstanceByKey("changeVariablesProcess");
    runtimeService.startProcessInstanceByKey("changeVariablesProcess");

    runtimeService.startProcessInstanceByKey("asyncAfter");

    ClockUtil.reset();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("changeVariablesProcess");
    TaskService taskService = engine.getTaskService();
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "localTaskVariable", "foo");

    CaseService caseService = engine.getCaseService();
    caseService
      .withCaseDefinitionByKey("loanApplicationCase")
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", "xyz")
      .create();

    caseService.createCaseInstanceByKey("CallingCase");

    Task standaloneTask = taskService.newTask();
    standaloneTask.setName("A Standalone Task");
    standaloneTask.setAssignee("jonny1");
    taskService.saveTask(standaloneTask);

    String standaloneTaskId = taskService
        .createTaskQuery()
        .taskName("A Standalone Task")
        .singleResult()
        .getId();

    taskService.setVariable(standaloneTaskId, "aVariable", "abc");
    taskService.setVariable(standaloneTaskId, "anotherVariable", 123456l);

    startInvoiceInstances(engine);
    startInvoiceInstancesForTenant(engine, "tenant1");
    startInvoiceInstancesForTenant(engine, "tenant1");
    startInvoiceInstancesForTenant(engine, "tenant2");

    new Thread(){
      @Override
      public void run() {
        ((ProcessEngineImpl) engine).getProcessEngineConfiguration().getJobExecutor().start();
      }
    }.start();

    createExternalTaskDemoData(engine);
  }

  private void createExternalTaskDemoData(ProcessEngine engine) {
    RuntimeService runtimeService = engine.getRuntimeService();
    ExternalTaskService externalTaskService = engine.getExternalTaskService();
    String workerId = "AWorker";
    String topicName = "ATopic";
    long lockDuration = 5 * 60L * 1000L;
    String errorDetails = "java.lang.RuntimeException: A exception message!\n" +
      "  at org.camunda.bpm.pa.service.FailingDelegate.execute(FailingDelegate.java:10)\n" +
      "  at org.camunda.bpm.engine.impl.delegate.JavaDelegateInvocation.invoke(JavaDelegateInvocation.java:34)\n" +
      "  at org.camunda.bpm.engine.impl.delegate.DelegateInvocation.proceed(DelegateInvocation.java:37)\n" +
      "  ...\n";


    // create 5 tasks
    for(int i=0; i<5; i++) {
      runtimeService.startProcessInstanceByKey("SimpleExternalTaskProcess");
    }

    // complete two tasks
    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(2, workerId, false)
      .topic(topicName, lockDuration)
      .execute();
    for(LockedExternalTask task: lockedTasks){
      externalTaskService.complete(task.getId(), workerId);
    }

    // fail the remaining 3 tasks
    lockedTasks = externalTaskService.fetchAndLock(3, workerId, false)
      .topic(topicName, lockDuration)
      .execute();
    for(LockedExternalTask task: lockedTasks){
      externalTaskService.handleFailure(task.getId(), workerId, "This is an error!", errorDetails, 0, 0L);
    }

    // create 2 more tasks
    for(int i=0; i<2; i++) {
      runtimeService.startProcessInstanceByKey("SimpleExternalTaskProcess");
    }

    // fail them and then complete them
    lockedTasks = externalTaskService.fetchAndLock(2, workerId, false)
      .topic(topicName, lockDuration)
      .execute();
    for(LockedExternalTask task: lockedTasks){
      externalTaskService.handleFailure(task.getId(), workerId, "This is an error!", errorDetails, 1, 0L);
    }
    lockedTasks = externalTaskService.fetchAndLock(2, workerId, false)
      .topic(topicName, lockDuration)
      .execute();
    for(LockedExternalTask task: lockedTasks){
      externalTaskService.complete(task.getId(), workerId);
    }

    // create 6 more tasks
    List<ProcessInstance> piList = new LinkedList<ProcessInstance>();
    for(int i=0; i<6; i++) {
      piList.add(runtimeService.startProcessInstanceByKey("SimpleExternalTaskProcess"));
    }

    // delete two of them
    runtimeService.deleteProcessInstance(piList.get(0).getId(), "This process was annoying!");
    runtimeService.deleteProcessInstance(piList.get(1).getId(), "This process was annoying!");

  }

  private void createAdminDemoData(ProcessEngine engine) throws Exception {
    UserDto user = new UserDto();
    UserProfileDto profile = new UserProfileDto();
    profile.setId("jonny1");
    profile.setFirstName("Jonny");
    profile.setLastName("Prosciutto");
    UserCredentialsDto credentials = new UserCredentialsDto();
    credentials.setPassword("jonny1");
    user.setProfile(profile);
    user.setCredentials(credentials);

    // manually perform setup
    new SetupResource().createInitialUser(engine.getName(), user);
  }

  private void createTasklistDemoData(ProcessEngine engine) {

    // create invoice demo data
    new InvoiceDemoDataGenerator().createDemoData(engine);

    // create task with form from deployment
    engine.getRuntimeService().startProcessInstanceByKey("process-with-deployment-form");
    engine.getRuntimeService().startProcessInstanceByKey("process-with-invalid-form");
    engine.getRuntimeService().startProcessInstanceByKey("process-with-http-form");

    engine.getCaseService().createCaseInstanceByKey("case-with-deployment-form");
  }

  private Date createArtificalDate(int offset) {
    DateTime dt = new DateTime();
    dt = dt.minusDays(offset);
    dt = dt.minusHours(offset);
    dt = dt.minusMinutes(offset);
    dt = dt.minusSeconds(offset);
    return dt.toDate();
  }


  private void startInvoiceInstances(ProcessEngine processEngine) {

    InputStream invoiceInputStream = getClass().getClassLoader().getResourceAsStream("invoice.pdf");

    // process instance 1
    processEngine.getRuntimeService().createProcessInstanceByKey("invoice")
      .processDefinitionWithoutTenantId()
      .setVariables(createVariables()
        .putValue("creditor", "Great Pizza for Everyone Inc.")
        .putValue("amount", 30.00d)
        .putValue("invoiceCategory", "Travel Expenses")
        .putValue("invoiceNumber", "GPFE-23232323")
        .putValue("invoiceDocument", fileValue("invoice.pdf")
            .file(invoiceInputStream)
            .mimeType("application/pdf")
            .create()))
      .execute();

    IoUtil.closeSilently(invoiceInputStream);
    invoiceInputStream = getClass().getClassLoader().getResourceAsStream("invoice.pdf");

    // process instance 2
    ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceByKey("invoice")
      .processDefinitionWithoutTenantId()
      .setVariables(createVariables()
        .putValue("creditor", "Bobby's Office Supplies")
        .putValue("amount", 900.00d)
        .putValue("invoiceCategory", "Misc")
        .putValue("invoiceNumber", "BOS-43934")
        .putValue("invoiceDocument", fileValue("invoice.pdf")
            .file(invoiceInputStream)
            .mimeType("application/pdf")
            .create()))
      .execute();

    try {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -14);
      ClockUtil.setCurrentTime(calendar.getTime());
      processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(Groups.CAMUNDA_ADMIN));
      Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
      processEngine.getTaskService().claim(task.getId(), "demo");
      processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approved", true));
    }
    finally{
      ClockUtil.reset();
      processEngine.getIdentityService().clearAuthentication();
    }

    IoUtil.closeSilently(invoiceInputStream);
    invoiceInputStream = getClass().getClassLoader().getResourceAsStream("invoice.pdf");

    // process instance 3
    pi = processEngine.getRuntimeService().createProcessInstanceByKey("invoice")
      .processDefinitionWithoutTenantId()
      .setVariables(createVariables()
        .putValue("creditor", "Papa Steve's all you can eat")
        .putValue("amount", 10.99d)
        .putValue("invoiceCategory", "Travel Expenses")
        .putValue("invoiceNumber", "PSACE-5342")
        .putValue("invoiceDocument", fileValue("invoice.pdf")
            .file(invoiceInputStream)
            .mimeType("application/pdf")
            .create()))
      .execute();

  }

  private void startInvoiceInstancesForTenant(ProcessEngine processEngine, String tenantId) {
    InputStream invoiceInputStream = getClass().getClassLoader().getResourceAsStream("invoice.pdf");

    processEngine.getRuntimeService().createProcessInstanceByKey("invoice")
      .processDefinitionTenantId(tenantId)
      .setVariables(createVariables()
        .putValue("creditor", "Fruits Inc.")
        .putValue("amount", 20.50d)
        .putValue("invoiceCategory", "Travel Expenses")
        .putValue("invoiceNumber", "GREEN-14492")
        .putValue("invoiceDocument", fileValue("invoice.pdf")
            .file(invoiceInputStream)
            .mimeType("application/pdf")
            .create()))
      .execute();

    IoUtil.closeSilently(invoiceInputStream);
  }

  protected void createReportDemoData(ProcessEngine engine) {

    LOGGER.info("Generating random report data for cockpit");

    Calendar instance = Calendar.getInstance();
    Date currentTime = instance.getTime();

    BpmnModelInstance model = createProcessWithUserTask("my-reporting-process", "Report Process");

    startAndCompleteReportingInstances(engine, model, currentTime, 24);
    startAndCompleteReportingInstances(engine, model, currentTime, 16);
    startAndCompleteReportingInstances(engine, model, currentTime, 8);
  }

  protected void startAndCompleteReportingInstances(ProcessEngine engine, BpmnModelInstance model, Date currentTime, int offset) {
    Calendar calendar = Calendar.getInstance();

    int currentMonth = calendar.get(Calendar.MONTH);
    int currentYear = calendar.get(Calendar.YEAR);

    calendar.add(Calendar.MONTH, offset * (-1));
    ClockUtil.setCurrentTime(calendar.getTime());

    createDeployment(engine, "reports", model);

    RuntimeService runtimeService = engine.getRuntimeService();
    TaskService taskService = engine.getTaskService();

    while (calendar.get(Calendar.YEAR) < currentYear || calendar.get(Calendar.MONTH) <= currentMonth) {

      int numOfInstances = getRandomBetween(10, 50);

      for(int i = 0; i <= numOfInstances; i++) {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("my-reporting-process");

        try {

          int min = getRandomBetween(1, 10);
          int max = offset > 0 ? offset * 30 : min;
          int randomDuration = getRandomBetween(min, max);

          Calendar calendarInstance = Calendar.getInstance();
          calendarInstance.setTime(ClockUtil.getCurrentTime());
          calendarInstance.add(Calendar.DAY_OF_YEAR, randomDuration);
          ClockUtil.setCurrentTime(calendarInstance.getTime());

          String processInstanceId = pi.getId();
          String taskId = taskService
              .createTaskQuery()
              .processInstanceId(processInstanceId)
              .singleResult()
              .getId();
          taskService.complete(taskId);

        }
        finally {
          ClockUtil.setCurrentTime(calendar.getTime());
        }
      }

      offset--;
      calendar.add(Calendar.MONTH, 1);
      ClockUtil.setCurrentTime(calendar.getTime());

      if (calendar.get(Calendar.YEAR) > currentYear) {
        break;
      }

    }

    ClockUtil.reset();
  }

  protected int getRandomBetween(int min, int max) {
    return (int)(Math.random() * (max - min) + min);
  }

  protected void createDeployment(ProcessEngine engine, String deploymentName, BpmnModelInstance model) {
    engine.getRepositoryService()
      .createDeployment()
      .name(deploymentName)
      .addModelInstance("path/to/my/process.bpmn", model)
      .deploy();
  }

  protected BpmnModelInstance createProcessWithUserTask(String key, String name) {
    return Bpmn.createExecutableProcess(key)
      .name(name)
      .startEvent()
      .userTask()
      .endEvent()
    .done();
  }

}

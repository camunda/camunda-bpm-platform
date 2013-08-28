package org.camunda.bpm.pa;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.admin.impl.web.SetupResource;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.pa.demo.InvoiceDemoDataGenerator;

/**
 *
 * @author nico.rehwaldt
 */
@ProcessApplication("camunda-test-processes")
public class DevProcessApplication extends ServletProcessApplication {

  @PostDeploy
  public void startProcesses(ProcessEngine engine) throws Exception {
    createAdminDemoData(engine);
    createTasklistDemoData(engine);
    createCockpitDemoData(engine);
  }

  private void createCockpitDemoData(ProcessEngine engine) throws Exception {
    RuntimeService runtimeService = engine.getRuntimeService();

    Map<String, Object> vars1 = new HashMap<String, Object>();
    vars1.put("booleanVar", true);
    runtimeService.startProcessInstanceByKey("ProcessWithExclusiveGateway", "secondUserTask", vars1);

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("booleanVar", false);
    runtimeService.startProcessInstanceByKey("ProcessWithExclusiveGateway", "firstUserTask", vars2);

    runtimeService.startProcessInstanceByKey("multipleFailingServiceTasks", "aBusinessKey");

    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");

    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");
    runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingSameProcess");

    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");
    runtimeService.startProcessInstanceByKey("CallingCallActivity");

    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("CallActivity");

    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");
    runtimeService.startProcessInstanceByKey("OrderProcess");

    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");

    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");
    runtimeService.startProcessInstanceByKey("AnotherFailingProcess");

    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");
    runtimeService.startProcessInstanceByKey("CallActivity");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("value1", "a");
    params.put("value2", "b");
    params.put("value3", "c");

    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);
    runtimeService.startProcessInstanceByKey("cornercasesProcess", params);

    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    runtimeService.startProcessInstanceByKey("processWithSubProcess");
    runtimeService.startProcessInstanceByKey("processWithSubProcess");

    runtimeService.startProcessInstanceByKey("executionProcess");
    runtimeService.startProcessInstanceByKey("executionProcess");
    runtimeService.startProcessInstanceByKey("executionProcess");
    runtimeService.startProcessInstanceByKey("executionProcess");
    runtimeService.startProcessInstanceByKey("executionProcess");
    runtimeService.startProcessInstanceByKey("executionProcess");

    ((ProcessEngineImpl) engine).getProcessEngineConfiguration().getJobExecutor().start();
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
  }
}


package org.camunda.bpm.pa;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;

/**
 *
 * @author nico.rehwaldt
 */
@ProcessApplication("cockpit-test-processes")
public class DevProcessApplication extends ServletProcessApplication {

  @PostDeploy
  public void startProcesses(ProcessEngine engine) {

    RuntimeService runtimeService = engine.getRuntimeService();

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
    
    final IdentityService identityService = engine.getIdentityService();
    
    User jonny1 = identityService.newUser("jonny1");
    jonny1.setFirstName("John");
    jonny1.setLastName("Doe");    
    identityService.saveUser(jonny1);
    

  }
  
}

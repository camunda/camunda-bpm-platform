package my.own.custom.spring.boot.project;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/process/start")
public class ProcessStartService {

  @Autowired
  private RuntimeService runtimeService;

  @POST
  public ProcessInstanceDto startProcess() {
    ProcessInstance testProcess = runtimeService.startProcessInstanceByKey("TestProcess");
    return ProcessInstanceDto.fromProcessInstance(testProcess);
  }

}

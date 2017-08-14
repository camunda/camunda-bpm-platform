package my.own.custom.spring.boot.project;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/rest")
public class JerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerAdditionalResources() {
    register(ProcessStartService.class);
  }

}

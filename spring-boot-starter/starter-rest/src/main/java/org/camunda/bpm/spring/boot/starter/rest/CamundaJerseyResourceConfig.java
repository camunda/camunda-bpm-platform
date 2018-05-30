package org.camunda.bpm.spring.boot.starter.rest;

import javax.ws.rs.ApplicationPath;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

@ApplicationPath("/rest")
public class CamundaJerseyResourceConfig extends ResourceConfig implements InitializingBean {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(CamundaJerseyResourceConfig.class);

  @Override
  public void afterPropertiesSet() throws Exception {
    registerCamundaRestResources();
    registerAdditionalResources();
  }

  protected void registerCamundaRestResources() {
    log.info("Configuring camunda rest api.");

    this.registerClasses(CamundaRestResources.getResourceClasses());
    this.registerClasses(CamundaRestResources.getConfigurationClasses());
    this.register(JacksonFeature.class);

    log.info("Finished configuring camunda rest api.");
  }

  protected void registerAdditionalResources() {

  }

}

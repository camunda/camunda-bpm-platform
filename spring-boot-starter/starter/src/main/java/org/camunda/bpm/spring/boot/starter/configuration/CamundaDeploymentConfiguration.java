package org.camunda.bpm.spring.boot.starter.configuration;

import org.springframework.core.io.Resource;

import java.util.Set;

public interface CamundaDeploymentConfiguration extends CamundaProcessEngineConfiguration {

  Set<Resource> getDeploymentResources();

}

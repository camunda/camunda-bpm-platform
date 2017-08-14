package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.jobexecutor.FoxFailedJobCommandFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaFailedJobConfiguration;

import java.util.ArrayList;

/**
 * Register parseListener to setup failed job retry specification.
 */
public class DefaultFailedJobConfiguration extends AbstractCamundaConfiguration implements CamundaFailedJobConfiguration {

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {

    if (configuration.getCustomPostBPMNParseListeners() == null) {
      configuration.setCustomPostBPMNParseListeners(new ArrayList<>());
    }

    configuration.getCustomPostBPMNParseListeners().add(new FoxFailedJobParseListener());
    configuration.setFailedJobCommandFactory(new FoxFailedJobCommandFactory());
  }
}

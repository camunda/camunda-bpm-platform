package org.camunda.bpm.spring.boot.starter.property;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

public final class Defaults extends SpringProcessEngineConfiguration {

  public static final Defaults INSTANCE = new Defaults();

  private Defaults() {

  }

  @Override
  public ProcessEngine buildProcessEngine() {
    throw new UnsupportedOperationException("use only for default values!");
  }
}

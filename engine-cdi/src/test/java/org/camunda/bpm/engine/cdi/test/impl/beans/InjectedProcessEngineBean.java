package org.camunda.bpm.engine.cdi.test.impl.beans;

import org.camunda.bpm.engine.ProcessEngine;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@Named
public class InjectedProcessEngineBean {

  @Inject
  public ProcessEngine processEngine;
}

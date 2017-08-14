package org.camunda.bpm.spring.boot.starter.test.helper;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(ProcessEngineRuleRunner.class)
public abstract class AbstractProcessEngineRuleTest {

  @Rule
  public final ProcessEngineRule processEngine = new StandaloneInMemoryTestConfiguration().rule();

}

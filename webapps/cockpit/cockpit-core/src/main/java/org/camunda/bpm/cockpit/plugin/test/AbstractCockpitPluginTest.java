package org.camunda.bpm.cockpit.plugin.test;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.impl.DefaultRuntimeDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

/**
 *
 * @author nico.rehwaldt
 */
public class AbstractCockpitPluginTest {

  private static TestCockpitRuntimeDelegate RUNTIME_DELEGATE = new TestCockpitRuntimeDelegate();

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @BeforeClass
  public static void beforeClass() {
    Cockpit.setCockpitRuntimeDelegate(RUNTIME_DELEGATE);
  }

  @AfterClass
  public static void afterClass() {
    Cockpit.setCockpitRuntimeDelegate(null);
  }

  @Before
  public void before() {
    RUNTIME_DELEGATE.ENGINE = getProcessEngine();
  }

  @After
  public void after() {
    RUNTIME_DELEGATE.ENGINE = null;
  }

  public ProcessEngine getProcessEngine() {
    return processEngineRule.getProcessEngine();
  }

  protected CommandExecutor getCommandExecutor() {
    return Cockpit.getCommandExecutor("default");
  }

  protected QueryService getQueryService() {
    return Cockpit.getQueryService("default");
  }

  private static class TestCockpitRuntimeDelegate extends DefaultRuntimeDelegate {

    public ProcessEngine ENGINE;

    @Override
    public ProcessEngine getProcessEngine(String processEngineName) {

      // always return default engine for plugin tests
      return ENGINE;
    }
  }
}

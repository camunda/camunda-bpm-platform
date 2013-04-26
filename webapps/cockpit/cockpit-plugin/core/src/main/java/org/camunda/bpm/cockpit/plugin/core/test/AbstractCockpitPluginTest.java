package org.camunda.bpm.cockpit.plugin.core.test;

import org.camunda.bpm.cockpit.plugin.core.Cockpit;
import org.camunda.bpm.cockpit.plugin.core.db.CommandExecutor;
import org.camunda.bpm.cockpit.plugin.core.db.QueryService;
import org.camunda.bpm.cockpit.plugin.core.impl.DefaultRuntimeDelegate;
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
    protected ProcessEngine getProcessEngine(String processEngineName) {

      // always return default engine for plugin tests
      return ENGINE;
    }
  }
}

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestRule extends AuthorizationTestBaseRule {

  protected AuthorizationExceptionInterceptor interceptor;
  protected CommandExecutor replacedCommandExecutor;

  protected AuthorizationScenarioInstance scenarioInstance;

  public AuthorizationTestRule(ProcessEngineRule engineRule) {
    super(engineRule);
    this.interceptor = new AuthorizationExceptionInterceptor();
  }

  public void start(AuthorizationScenario scenario) {
    start(scenario, null, new HashMap<String, String>());
  }

  public void start(AuthorizationScenario scenario, String userId, Map<String, String> resourceBindings) {
    Assert.assertNull(interceptor.getLastException());
    scenarioInstance = new AuthorizationScenarioInstance(scenario, engineRule.getAuthorizationService(), resourceBindings);
    enableAuthorization(userId);
    interceptor.activate();
  }

  /**
   * Assert the scenario conditions. If no exception or the expected one was thrown.
   *
   * @param scenario the scenario to assert on
   * @return true if no exception was thrown, false otherwise
   */
  public boolean assertScenario(AuthorizationScenario scenario) {

    interceptor.deactivate();
    disableAuthorization();
    scenarioInstance.tearDown(engineRule.getAuthorizationService());
    scenarioInstance.assertAuthorizationException(interceptor.getLastException());
    scenarioInstance = null;

    return scenarioSucceeded();
  }

  /**
   * No exception was expected and no was thrown
   */
  public boolean scenarioSucceeded() {
    return interceptor.getLastException() == null;
  }

  public boolean scenarioFailed() {
    return interceptor.getLastException() != null;
  }

  protected void starting(Description description) {
    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    interceptor.reset();
    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor);
    interceptor.setNext(engineConfiguration.getCommandInterceptorsTxRequired().get(1));

    super.starting(description);
  }

  protected void finished(Description description) {
    super.finished(description);

    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();

    engineConfiguration.getCommandInterceptorsTxRequired().get(0).setNext(interceptor.getNext());
    interceptor.setNext(null);
  }

  public static Collection<AuthorizationScenario[]> asParameters(AuthorizationScenario... scenarios) {
    List<AuthorizationScenario[]> scenarioList = new ArrayList<AuthorizationScenario[]>();
    for (AuthorizationScenario scenario : scenarios) {
      scenarioList.add(new AuthorizationScenario[]{ scenario });
    }

    return scenarioList;
  }

  public AuthorizationScenarioInstanceBuilder init(AuthorizationScenario scenario) {
    AuthorizationScenarioInstanceBuilder builder = new AuthorizationScenarioInstanceBuilder();
    builder.scenario = scenario;
    builder.rule = this;
    return builder;
  }

  public static class AuthorizationScenarioInstanceBuilder {
    protected AuthorizationScenario scenario;
    protected AuthorizationTestRule rule;
    protected String userId;
    protected Map<String, String> resourceBindings = new HashMap<String, String>();

    public AuthorizationScenarioInstanceBuilder withUser(String userId) {
      this.userId = userId;
      return this;
    }

    public AuthorizationScenarioInstanceBuilder bindResource(String key, String value) {
      resourceBindings.put(key, value);
      return this;
    }

    public void start() {
      rule.start(scenario, userId, resourceBindings);
    }
  }

}

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
package org.camunda.bpm.engine.test.api.identity;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.digest.Default16ByteSaltGenerator;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class SaltHashingTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      // apply configuration options here
      configuration.setSaltGenerator(new Default16ByteSaltGenerator());
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected final static String PASSWORD = "password";

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  protected IdentityService identityService;
  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void initialize() {
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void saltHashingOnHashedPasswordWithoutSaltThrowsNoError() {
    // given
    processEngineConfiguration.setSaltGenerator(new MyNullSaltGenerator());
    User user = identityService.newUser("johndoe");
    user.setPassword(PASSWORD);

    // when
    identityService.saveUser(user);

    // then
    assertThat(identityService.checkPassword("johndoe", PASSWORD), is(true));
    identityService.deleteUser("johndoe");
  }

  @Test
  public void enteringTheSamePasswordShouldProduceTwoDifferentEncryptedPassword() {
    // given
    User user1 = identityService.newUser("johndoe");
    user1.setPassword(PASSWORD);
    identityService.saveUser(user1);

    // when
    User user2 = identityService.newUser("kermit");
    user2.setPassword(PASSWORD);
    identityService.saveUser(user2);

    // then
    assertThat(user1.getPassword(), is(not(user2.getPassword())));
    identityService.deleteUser("johndoe");
    identityService.deleteUser("kermit");
  }

  @Test
  public void ensurePasswordIsCorrectlyHashed() {
    // given
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator());
    User user = identityService.newUser("johndoe");
    user.setPassword(PASSWORD);
    identityService.saveUser(user);

    // when
    user = identityService.createUserQuery().userId("johndoe").singleResult();

    // then
    // obtain the expected value on the command line like so: echo -n password12345678910 | openssl dgst -binary -sha1 | openssl base64
    assertThat(user.getPassword(), is("{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg="));
    identityService.deleteUser("johndoe");
  }
}

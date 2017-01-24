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

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.digest.*;
import org.camunda.bpm.engine.test.api.identity.util.*;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class PasswordHashingTest {

  protected static ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected static ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected final static String PASSWORD = "password";
  protected final static String USER_NAME = "johndoe";
  protected final static String ALGORITHM_NAME = "awesome";

  protected IdentityService identityService;
  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected PasswordEncryptor camundaDefaultEncryptor;
  protected List<PasswordEncryptor> camundaDefaultPasswordChecker;
  protected SaltGenerator camundaDefaultSaltGenerator;


  @Before
  public void initialize() {
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    camundaDefaultEncryptor = processEngineConfiguration.getPasswordEncryptor();
    camundaDefaultPasswordChecker = processEngineConfiguration.getCustomPasswordChecker();
    camundaDefaultSaltGenerator = processEngineConfiguration.getSaltGenerator();
  }

  @After
  public void cleanUp() {
    removeAllUser();
    resetEngineConfiguration();
  }

  protected void removeAllUser() {
    List<User> list = identityService.createUserQuery().list();
    for (User user : list) {
      identityService.deleteUser(user.getId());
    }
  }

  protected void resetEngineConfiguration() {
    setEncryptors(camundaDefaultEncryptor, camundaDefaultPasswordChecker);
    processEngineConfiguration.setSaltGenerator(camundaDefaultSaltGenerator);
  }

  @Test
  public void saltHashingOnHashedPasswordWithoutSaltThrowsNoError() {
    // given
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator(null));
    User user = identityService.newUser(USER_NAME);
    user.setPassword(PASSWORD);

    // when
    identityService.saveUser(user);

    // then
    assertThat(identityService.checkPassword(USER_NAME, PASSWORD), is(true));
  }

  @Test
  public void enteringTheSamePasswordShouldProduceTwoDifferentEncryptedPassword() {
    // given
    User user1 = identityService.newUser(USER_NAME);
    user1.setPassword(PASSWORD);
    identityService.saveUser(user1);

    // when
    User user2 = identityService.newUser("kermit");
    user2.setPassword(PASSWORD);
    identityService.saveUser(user2);

    // then
    assertThat(user1.getPassword(), is(not(user2.getPassword())));
  }

  @Test
  public void ensurePasswordIsCorrectlyHashedWithSHA1() {
    // given
    setDefaultEncryptor(new ShaHashDigest());
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator("12345678910"));
    User user = identityService.newUser(USER_NAME);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);

    // when
    user = identityService.createUserQuery().userId(USER_NAME).singleResult();

    // then
    // obtain the expected value on the command line like so: echo -n password12345678910 | openssl dgst -binary -sha1 | openssl base64
    assertThat(user.getPassword(), is("{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg="));
  }

  @Test
  public void ensurePasswordIsCorrectlyHashedWithSHA512() {
    // given
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator("12345678910"));
    User user = identityService.newUser(USER_NAME);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);

    // when
    user = identityService.createUserQuery().userId(USER_NAME).singleResult();

    // then
    // obtain the expected value on the command line like so: echo -n password12345678910 | openssl dgst -binary -sha512 | openssl base64
    assertThat(user.getPassword(), is("{SHA-512}sM1U4nCzoDbdUugvJ7dJ6rLc7t1ZPPsnAbUpTqi5nXCYp7PTZCHExuzjoxLLYoUK" +
      "Gd637jKqT8d9tpsZs3K5+g=="));
  }

  @Test
  public void twoEncryptorsWithSamePrefixThrowError() {

    // given two algorithms with the same prefix
    List<PasswordEncryptor> additionalEncryptorsForPasswordChecking = new LinkedList<PasswordEncryptor>();
    additionalEncryptorsForPasswordChecking.add(new ShaHashDigest());
    PasswordEncryptor defaultEncryptor = new ShaHashDigest();

    // then
    thrown.expect(PasswordEncryptionException.class);
    thrown.expectMessage("Hash algorithm with the name 'SHA' was already added");

    // when
    setEncryptors(defaultEncryptor, additionalEncryptorsForPasswordChecking);
  }

  @Test
  public void prefixThatCannotBeResolvedThrowsError() {
    // given
    setDefaultEncryptor(new MyCustomPasswordEncryptorCreatingPrefixThatCannotBeResolved());
    User user = identityService.newUser(USER_NAME);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);
    user = identityService.createUserQuery().userId(USER_NAME).singleResult();

    // then
    thrown.expect(PasswordEncryptionException.class);
    thrown.expectMessage("Could not resolve hash algorithm name of a hashed password");

    // when
    identityService.checkPassword(user.getId(), PASSWORD);
  }

  @Test
  public void plugInCustomPasswordEncryptor() {
    // given
    setEncryptors(new MyCustomPasswordEncryptor(PASSWORD, ALGORITHM_NAME), Collections.<PasswordEncryptor>emptyList());
    User user = identityService.newUser(USER_NAME);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);

    // when
    user = identityService.createUserQuery().userId(USER_NAME).singleResult();

    // then
    assertThat(user.getPassword(), is("{" + ALGORITHM_NAME + "}xxx"));
  }

  @Test
  public void useSeveralCustomEncryptors() {

    // given three users with different hashed passwords
    processEngineConfiguration.setSaltGenerator(new MyConstantSaltGenerator("12345678910"));

    String userName1 = "Kermit";
    createUserWithEncryptor(userName1, new MyCustomPasswordEncryptor(PASSWORD, ALGORITHM_NAME));

    String userName2 = "Fozzie";
    String anotherAlgorithmName = "marvelousAlgorithm";
    createUserWithEncryptor(userName2, new MyCustomPasswordEncryptor(PASSWORD, anotherAlgorithmName));

    String userName3 = "Gonzo";
    createUserWithEncryptor(userName3, new ShaHashDigest());

    List<PasswordEncryptor> additionalEncryptorsForPasswordChecking = new LinkedList<PasswordEncryptor>();
    additionalEncryptorsForPasswordChecking.add(new MyCustomPasswordEncryptor(PASSWORD, ALGORITHM_NAME));
    additionalEncryptorsForPasswordChecking.add(new MyCustomPasswordEncryptor(PASSWORD, anotherAlgorithmName));
    PasswordEncryptor defaultEncryptor = new ShaHashDigest();
    setEncryptors(defaultEncryptor, additionalEncryptorsForPasswordChecking);

    // when
    User user1 = identityService.createUserQuery().userId(userName1).singleResult();
    User user2 = identityService.createUserQuery().userId(userName2).singleResult();
    User user3 = identityService.createUserQuery().userId(userName3).singleResult();

    // then
    assertThat(user1.getPassword(), is("{" + ALGORITHM_NAME + "}xxx"));
    assertThat(user2.getPassword(), is("{" + anotherAlgorithmName + "}xxx"));
    assertThat(user3.getPassword(), is("{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg="));
  }

  protected void createUserWithEncryptor(String userName, PasswordEncryptor encryptor) {
    setEncryptors(encryptor, Collections.<PasswordEncryptor>emptyList());
    User user = identityService.newUser(userName);
    user.setPassword(PASSWORD);
    identityService.saveUser(user);
  }

  protected void setDefaultEncryptor(PasswordEncryptor defaultEncryptor) {
    setEncryptors(defaultEncryptor, Collections.<PasswordEncryptor>emptyList());
  }

  protected void setEncryptors(PasswordEncryptor defaultEncryptor, List<PasswordEncryptor> additionalEncryptorsForPasswordChecking) {
    processEngineConfiguration.setPasswordManager(new PasswordManager(defaultEncryptor, additionalEncryptorsForPasswordChecking));
  }

}

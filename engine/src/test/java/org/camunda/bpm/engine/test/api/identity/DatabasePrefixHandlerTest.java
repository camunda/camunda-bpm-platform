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

import org.camunda.bpm.engine.impl.digest.DatabasePrefixHandler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DatabasePrefixHandlerTest {

  DatabasePrefixHandler prefixHandler;

  @Before
  public void inti() {
    prefixHandler = new DatabasePrefixHandler();
  }

  @Test
  public void testGeneratePrefix() {

    // given
    String algorithmName = "test";

    // when
    String prefix = prefixHandler.generatePrefix(algorithmName);

    // then
    assertThat(prefix, is("{test}"));
  }

  @Test
  public void testRetrieveAlgorithmName(){

    // given
    String encryptedPasswordWithPrefix = "{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String algorithmName = prefixHandler.retrieveAlgorithmName(encryptedPasswordWithPrefix);

    // then
    assertThat(algorithmName, is("SHA"));
  }

  @Test
  public void retrieveAlgorithmForInvalidInput() {

    // given
    String encryptedPasswordWithPrefix = "xxx{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String algorithmName = prefixHandler.retrieveAlgorithmName(encryptedPasswordWithPrefix);

    // then
    assertThat(algorithmName, is(nullValue()));
  }

  @Test
  public void retrieveAlgorithmWithMissingAlgorithmPrefix() {

    // given
    String encryptedPasswordWithPrefix = "n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String algorithmName = prefixHandler.retrieveAlgorithmName(encryptedPasswordWithPrefix);

    // then
    assertThat(algorithmName, is(nullValue()));
  }

  @Test
  public void retrieveAlgorithmWithErroneousAlgorithmPrefix() {

    // given
    String encryptedPasswordWithPrefix = "{SHAn3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String algorithmName = prefixHandler.retrieveAlgorithmName(encryptedPasswordWithPrefix);

    // then
    assertThat(algorithmName, is(nullValue()));
  }

  @Test
  public void removePrefix() {

    // given
    String encryptedPasswordWithPrefix = "{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String encryptedPassword = prefixHandler.removePrefix(encryptedPasswordWithPrefix);

    // then
    assertThat(encryptedPassword, is("n3fE9/7XOmgD3BkeJlC+JLyb/Qg="));

  }

  @Test
  public void removePrefixForInvalidInput() {

    // given
    String encryptedPasswordWithPrefix = "xxx{SHA}n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String encryptedPassword = prefixHandler.removePrefix(encryptedPasswordWithPrefix);

    // then
    assertThat(encryptedPassword, is(nullValue()));

  }

  @Test
  public void removePrefixWithMissingAlgorithmPrefix() {

    // given
    String encryptedPasswordWithPrefix = "n3fE9/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String encryptedPassword = prefixHandler.removePrefix(encryptedPasswordWithPrefix);

    // then
    assertThat(encryptedPassword, is(nullValue()));

  }

  @Test
  public void removePrefixWithErroneousAlgorithmPrefix() {

    // given
    String encryptedPasswordWithPrefix = "SHAn3fE9}/7XOmgD3BkeJlC+JLyb/Qg=";

    // when
    String encryptedPassword = prefixHandler.removePrefix(encryptedPasswordWithPrefix);

    // then
    assertThat(encryptedPassword, is(nullValue()));
  }


}

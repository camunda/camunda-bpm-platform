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
package org.camunda.bpm.engine.impl.digest;


/**
 * The {@link PasswordEncryptor} provides the api to customize
 * the encryption of passwords.
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public interface PasswordEncryptor {

  /**
   * Encrypt the given password
   *
   * @param password
   * @return
   */
  public String encrypt(String password);

  /**
   * Returns true if the given plain text equals to the encrypted password.
   *
   * @param password
   * @param encrypted
   *
   * @return
   */
  public boolean check(String password, String encrypted);

  /**
   * In order to distinguish which algorithm was used to hash the
   * password, it needs a unique id. In particular, this is needed
   * for {@link #check}.
   *
   * @return the name of the algorithm
   */
  public String hashAlgorithmName();
}

/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.digest;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Different Camunda versions use different hashing algorithms. In addition, it is possible
 * to add a custom hashing algorithm. The {@link PasswordManager} ensures that the right
 * algorithm is used for the encryption.
 *
 * Default algorithms:
 * Version:           |    Algorithm
 * <= Camunda 7.6     | SHA1
 * >= Camunda 7.7     | SHA512
 */
public class PasswordManager {

  public static final SecurityLogger LOG = ProcessEngineLogger.SECURITY_LOGGER;

  protected Map<String, PasswordEncryptor> passwordChecker = new HashMap<String, PasswordEncryptor>();
  protected PasswordEncryptor defaultPasswordEncryptor;

  protected DatabasePrefixHandler prefixHandler = new DatabasePrefixHandler();

  public PasswordManager(PasswordEncryptor defaultPasswordEncryptor, List<PasswordEncryptor> customPasswordChecker) {
    // add default password encryptors for password checking
    // for Camunda 7.6 and earlier
    addPasswordCheckerAndThrowErrorIfAlreadyAvailable(new ShaHashDigest());
    // from Camunda 7.7
    addPasswordCheckerAndThrowErrorIfAlreadyAvailable(new Sha512HashDigest());

    // add custom encryptors
    addAllPasswordChecker(customPasswordChecker);

    addDefaultEncryptor(defaultPasswordEncryptor);
  }

  protected void addAllPasswordChecker(List<PasswordEncryptor> list) {
    for (PasswordEncryptor encryptor : list) {
      addPasswordCheckerAndThrowErrorIfAlreadyAvailable(encryptor);
    }
  }

  protected void addPasswordCheckerAndThrowErrorIfAlreadyAvailable(PasswordEncryptor encryptor) {
    if(passwordChecker.containsKey(encryptor.hashAlgorithmName())){
      throw LOG.hashAlgorithmForPasswordEncryptionAlreadyAvailableException(encryptor.hashAlgorithmName());
    }
    passwordChecker.put(encryptor.hashAlgorithmName(), encryptor);
  }

  protected void addDefaultEncryptor(PasswordEncryptor defaultPasswordEncryptor) {
    this.defaultPasswordEncryptor = defaultPasswordEncryptor;
    passwordChecker.put(defaultPasswordEncryptor.hashAlgorithmName(), defaultPasswordEncryptor);
  }

  public String encrypt(String password){
    String prefix = prefixHandler.generatePrefix(defaultPasswordEncryptor.hashAlgorithmName());
    return prefix + defaultPasswordEncryptor.encrypt(password);
  }

  public boolean check(String password, String encrypted){
    PasswordEncryptor encryptor = getCorrectEncryptorForPassword(encrypted);
    String encryptedPasswordWithoutPrefix = prefixHandler.removePrefix(encrypted);
    ensureNotNull("encryptedPasswordWithoutPrefix", encryptedPasswordWithoutPrefix);
    return encryptor.check(password, encryptedPasswordWithoutPrefix);
  }

  protected PasswordEncryptor getCorrectEncryptorForPassword(String encryptedPassword) {
    String hashAlgorithmName = prefixHandler.retrieveAlgorithmName(encryptedPassword);
    if(hashAlgorithmName == null || !passwordChecker.containsKey(hashAlgorithmName)){
      throw LOG.cannotResolveAlgorithmPrefixFromGivenPasswordException(hashAlgorithmName, passwordChecker.keySet());
    }
    return passwordChecker.get(hashAlgorithmName);
  }

}

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

import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import java.security.SecureRandom;
import java.util.Random;

public abstract class Base64EncodedSaltGenerator implements SaltGenerator {

  protected Random secureRandom = new SecureRandom();

  @Override
  public String generateSalt() {

    byte[] byteSalt = generateByteSalt();
    return encodeSalt(byteSalt);
  }

  protected byte[] generateByteSalt() {
    byte[] salt = new byte[getSaltLengthInByte()];
    secureRandom.nextBytes(salt);
    return salt;
  }

  protected String encodeSalt(byte[] salt) {
    return new String(Base64.encodeBase64(salt));
  }

  protected abstract Integer getSaltLengthInByte();

}

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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

/**
 * @author Daniel Meyer
 *
 */
public abstract class Base64EncodedHashDigest {

  public String encrypt(String password) {

    // create hash as byte array
    byte[] hash = createByteHash(password);

    // stringify hash (default implementation use BASE64 encoding)
    return encodeHash(hash);

  }

  public boolean check(String password, String encrypted) {
    return encrypt(password).equals(encrypted);
  }

  protected byte[] createByteHash(String password) {
    MessageDigest digest = createDigestInstance();
    try {
      digest.update(password.getBytes("UTF-8"));
      return digest.digest();

    } catch (UnsupportedEncodingException e) {
      throw new ProcessEngineException("UnsupportedEncodingException while calculating password digest");
    }
  }

  protected MessageDigest createDigestInstance() {
    try {
      return MessageDigest.getInstance(hashAlgorithmName());

    } catch (NoSuchAlgorithmException e) {
      throw new ProcessEngineException("Cannot lookup " + hashAlgorithmName() + " algorithm");

    }
  }

  protected String encodeHash(byte[] hash) {
    return new String(Base64.encodeBase64(hash));
  }

  /** allows subclasses to select the hash algorithm */
  protected abstract String hashAlgorithmName();

}

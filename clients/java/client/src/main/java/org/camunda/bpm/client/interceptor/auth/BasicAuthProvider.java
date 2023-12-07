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
package org.camunda.bpm.client.interceptor.auth;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

/**
 * <p>Provides HTTP Basic Authentication by using the request interceptor api</p>
 *
 * @author Tassilo Weidner
 */
public class BasicAuthProvider implements ClientRequestInterceptor {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String username;
  protected String password;

  public BasicAuthProvider(String username, String password) {
    if (username == null || password == null) {
      throw LOG.basicAuthCredentialsNullException();
    }

    this.username = username;
    this.password = password;
  }

  @Override
  public void intercept(ClientRequestContext requestContext) {
    String authToken = username + ":" + password;
    String encodedAuthToken = encodeToBase64(authToken);

    requestContext.addHeader(AUTHORIZATION, "Basic " + encodedAuthToken);
  }

  protected String encodeToBase64(String decodedString) {
    byte[] stringAsBytes = decodedString.getBytes(Charset.defaultCharset());

    return Base64.getEncoder()
      .encodeToString(stringAsBytes);
  }

}

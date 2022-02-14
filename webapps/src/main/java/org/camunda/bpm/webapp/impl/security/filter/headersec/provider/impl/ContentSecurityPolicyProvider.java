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
package org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl;

import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.HeaderSecurityProvider;

import java.util.Map;

public class ContentSecurityPolicyProvider extends HeaderSecurityProvider {

  public static final String HEADER_NAME = "Content-Security-Policy";
  public static final String HEADER_DEFAULT_VALUE = "base-uri 'self'";

  public static final String DISABLED_PARAM = "contentSecurityPolicyDisabled";
  public static final String VALUE_PARAM = "contentSecurityPolicyValue";

  @Override
  public Map<String, String> initParams() {
    initParams.put(VALUE_PARAM, null);
    initParams.put(DISABLED_PARAM, null);

    return initParams;
  }

  @Override
  public void parseParams() {
    String disabled = initParams.get(DISABLED_PARAM);

    if (isEmpty(disabled)) {
      setDisabled(false);

    } else {
      setDisabled(Boolean.valueOf(disabled));

    }

    String value = initParams.get(VALUE_PARAM);
    if (!isEmpty(value)) {
      value = normalizeString(value);
      setValue(value);

    } else {
      setValue(HEADER_DEFAULT_VALUE);

    }
  }

  protected String normalizeString(String value) {
    return value
      .trim()
      .replaceAll("\\s+", " "); // replaces [\t\n\x0B\f\r]
  }

  @Override
  public String getHeaderName() {
    return HEADER_NAME;
  }

}

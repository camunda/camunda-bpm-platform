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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.HeaderSecurityProvider;
import org.camunda.bpm.webapp.impl.util.ServletFilterUtil;

import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class XssProtectionProvider extends HeaderSecurityProvider {

  public static final String HEADER_NAME = "X-XSS-Protection";
  public static final XssProtectionOption HEADER_DEFAULT_VALUE = XssProtectionOption.BLOCK;

  public static final String DISABLED_PARAM = "xssProtectionDisabled";
  public static final String OPTION_PARAM = "xssProtectionOption";
  public static final String VALUE_PARAM = "xssProtectionValue";

  public Map<String, String> initParams() {
    initParams.put(DISABLED_PARAM, null);
    initParams.put(OPTION_PARAM, null);
    initParams.put(VALUE_PARAM, null);

    return initParams;
  }

  public void parseParams() {

    String disabled = initParams.get(DISABLED_PARAM);

    if (ServletFilterUtil.isEmpty(disabled)) {
      setDisabled(false);

    } else {
      setDisabled(Boolean.parseBoolean(disabled));

    }

    String value = initParams.get(VALUE_PARAM);
    String option = initParams.get(OPTION_PARAM);

    if (!isDisabled()) {
      if (!ServletFilterUtil.isEmpty(value) && !ServletFilterUtil.isEmpty(option)) {
        throw new ProcessEngineException(this.getClass().getSimpleName() + ": cannot set both " + VALUE_PARAM + " and " + OPTION_PARAM + ".");
      }

      if (!ServletFilterUtil.isEmpty(value)) {
        setValue(value);

      } else if (!ServletFilterUtil.isEmpty(option)) {
        setValue(parseValue(option).getHeaderValue());

      } else {
        setValue(HEADER_DEFAULT_VALUE.getHeaderValue());

      }
    }
  }

  protected XssProtectionOption parseValue(String optionValue) {

    for (XssProtectionOption option : XssProtectionOption.values()) {

      if (option.getName().equalsIgnoreCase(optionValue)) {
        return option;

      }
    }

    throw new ProcessEngineException(this.getClass().getSimpleName() + ": cannot set non-existing option " + optionValue + " for " + OPTION_PARAM + ".");
  }

  public String getHeaderName() {
    return HEADER_NAME;
  }

}

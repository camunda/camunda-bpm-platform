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

import java.util.Arrays;
import java.util.Map;

import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.DISABLED;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.INCLUDE_SUBDOMAINS_DISABLED;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.MAX_AGE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.Parameters.VALUE;

public class StrictTransportSecurityProvider extends HeaderSecurityProvider {

  public static final String HEADER_NAME = "Strict-Transport-Security";

  public static final int MAX_AGE_DEFAULT_VALUE = 31_536_000; // = one year

  public static final String VALUE_PART_MAX_AGE = "max-age=";
  public static final String VALUE_PART_INCLUDE_SUBDOMAINS = "; includeSubDomains";

  public enum Parameters {

    DISABLED("hstsDisabled"),

    VALUE("hstsValue"),
    MAX_AGE("hstsMaxAge"),
    INCLUDE_SUBDOMAINS_DISABLED("hstsIncludeSubdomainsDisabled");

    protected String name;

    Parameters(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  @Override
  public Map<String, String> initParams() {

    Arrays.asList(Parameters.values()).forEach(parameter -> {
      initParams.put(parameter.getName(), null);
    });

    return initParams;
  }

  @Override
  public void parseParams() {

    String disabled = getParameterValue(DISABLED);
    if (ServletFilterUtil.isEmpty(disabled)) {
      setDisabled(true);

    } else {
      setDisabled(Boolean.parseBoolean(disabled));

    }

    if (!isDisabled()) {
      boolean isAnyParameterDefined =
        checkAnyParameterDefined(MAX_AGE, INCLUDE_SUBDOMAINS_DISABLED);

      String value = getParameterValue(VALUE);
      boolean isValueParameterDefined = !ServletFilterUtil.isEmpty(value);

      if (isValueParameterDefined && isAnyParameterDefined) {
        String className = this.getClass().getSimpleName();
        throw exceptionParametersCannotBeSet(className);

      } else if (isValueParameterDefined) {
        setValue(value);

      } else if (isAnyParameterDefined) {

        StringBuilder headerValueStringBuilder = new StringBuilder();

        headerValueStringBuilder.append(VALUE_PART_MAX_AGE);

        String maxAge = getParameterValue(MAX_AGE);
        if (ServletFilterUtil.isEmpty(maxAge)) {
          headerValueStringBuilder.append(MAX_AGE_DEFAULT_VALUE);

        } else {
          int maxAgeParsed = Integer.parseInt(maxAge);
          headerValueStringBuilder.append(maxAgeParsed);

        }

        String includeSubdomainsDisabled = getParameterValue(INCLUDE_SUBDOMAINS_DISABLED);
        if (!ServletFilterUtil.isEmpty(includeSubdomainsDisabled) && !Boolean.parseBoolean(includeSubdomainsDisabled)) {
          headerValueStringBuilder.append(VALUE_PART_INCLUDE_SUBDOMAINS);

        }

        setValue(headerValueStringBuilder.toString());

      } else {
        setValue(VALUE_PART_MAX_AGE + MAX_AGE_DEFAULT_VALUE);

      }
    }
  }

  protected ProcessEngineException exceptionParametersCannotBeSet(String className) {
    return new ProcessEngineException(className + ": cannot set " + VALUE.getName() +
      " in conjunction with " + MAX_AGE.getName() + " or " +
      INCLUDE_SUBDOMAINS_DISABLED.getName() + ".");
  }

  protected String getParameterValue(Parameters parameter) {
    String parameterName = parameter.getName();
    return initParams.get(parameterName);
  }

  protected boolean checkAnyParameterDefined(Parameters... parameters) {
    for (Parameters parameter : parameters) {
      String parameterValue = getParameterValue(parameter);
      if (!ServletFilterUtil.isEmpty(parameterValue)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getHeaderName() {
    return HEADER_NAME;
  }

}

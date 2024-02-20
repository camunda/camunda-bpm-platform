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

package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS;

import java.util.Objects;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cfg.ConfigurationLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.dmn.instance.Decision;

/**
 * Class that encapsulates the business logic of parsing HistoryTimeToLive of different deployable resources (process, definition, case).
 */
public class HistoryTimeToLiveParser {

  protected static final ConfigurationLogger LOG = ConfigurationLogger.CONFIG_LOGGER;

  protected final boolean enforceNonNullValue;
  protected final String httlConfigValue;

  protected HistoryTimeToLiveParser(boolean enforceNonNullValue, String httlConfigValue) {
    this.enforceNonNullValue = enforceNonNullValue;
    this.httlConfigValue = httlConfigValue;
  }

  public static HistoryTimeToLiveParser create() {
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    Objects.requireNonNull(config, "HistoryTimeToLiveParser requires a non null config to be created");

    return create(config);
  }

  public static HistoryTimeToLiveParser create(CommandContext context) {
    ProcessEngineConfigurationImpl config = context.getProcessEngineConfiguration();

    return create(config);
  }

  public static HistoryTimeToLiveParser create(ProcessEngineConfigurationImpl config) {
    boolean enforceHistoryTimeToLive = config.isEnforceHistoryTimeToLive();
    String historyTimeToLive = config.getHistoryTimeToLive();

    return new HistoryTimeToLiveParser(enforceHistoryTimeToLive, historyTimeToLive);
  }

  public void validate(Integer historyTimeToLive) {
    if (enforceNonNullValue && historyTimeToLive == null) {
      throw new NotAllowedException("Null historyTimeToLive values are not allowed");
    }
  }

  public Integer parse(Element processElement, String definitionKey, boolean skipEnforceTtl) {
    String historyTimeToLiveString = processElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "historyTimeToLive");

    return parseAndValidate(historyTimeToLiveString, definitionKey, skipEnforceTtl);
  }

  public Integer parse(Case caseElement, String definitionKey, boolean skipEnforceTtl) {
    String historyTimeToLiveString = caseElement.getCamundaHistoryTimeToLiveString();

    return parseAndValidate(historyTimeToLiveString, definitionKey, skipEnforceTtl);
  }

  public Integer parse(Decision decision, String definitionKey, boolean skipEnforceTtl) {
    String historyTimeToLiveString = decision.getCamundaHistoryTimeToLiveString();

    return parseAndValidate(historyTimeToLiveString, definitionKey, skipEnforceTtl);
  }

  /**
   * Parses the given HistoryTimeToLive String expression and then executes any applicable validation before returning
   * the parsed value.
   *
   * @param historyTimeToLiveString the history time to live string expression in ISO-8601 format
   * @param definitionKey           the correlated definition key that this historyTimeToLive was fetched from
   *                                (process definition key for processes, decision definition key for decisions, case definition key for cases).
   * @param skipEnforceTtl skips enforcing the TTL.
   * @return the parsed integer value of history time to live
   * @throws NotValidException in case enforcement of non-null values is on and the parsed result was null
   */
  protected Integer parseAndValidate(String historyTimeToLiveString, String definitionKey, boolean skipEnforceTtl) throws NotValidException {
    HTTLParsedResult result = new HTTLParsedResult(historyTimeToLiveString);

    if (!skipEnforceTtl) {
      if (result.isInValidAgainstConfig()) {
        throw LOG.logErrorNoTTLConfigured();
      }

      if (result.hasLongerModelValueThanGlobalConfig()) {
        LOG.logModelHTTLLongerThanGlobalConfiguration(definitionKey);
      }
    }

    return result.valueAsInteger;
  }

  protected class HTTLParsedResult {

    protected final boolean systemDefaultConfigWillBeUsed;
    protected final String value;
    protected final Integer valueAsInteger;

    public HTTLParsedResult(String historyTimeToLiveString) {
      this.systemDefaultConfigWillBeUsed = (historyTimeToLiveString == null);
      this.value = systemDefaultConfigWillBeUsed ? httlConfigValue : historyTimeToLiveString;
      this.valueAsInteger = ParseUtil.parseHistoryTimeToLive(value);
    }

    protected boolean isInValidAgainstConfig() {
      return enforceNonNullValue && (valueAsInteger == null);
    }

    protected boolean hasLongerModelValueThanGlobalConfig() {
      return !systemDefaultConfigWillBeUsed // only values originating from models make sense to be logged
          && valueAsInteger != null
          && httlConfigValue != null && !httlConfigValue.isEmpty()
          && valueAsInteger > ParseUtil.parseHistoryTimeToLive(httlConfigValue);
    }
  }
}

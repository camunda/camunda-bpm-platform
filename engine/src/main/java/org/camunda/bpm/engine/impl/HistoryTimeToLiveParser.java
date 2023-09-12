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
import java.util.Optional;
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
 * <p>
 * Furthermore, it considers notifying the users with a logging message when parsing historyTimeToLive values that are
 * the same with the default camunda modeler TTL value (see {@code CAMUNDA_MODELER_TTL_DEFAULT_VALUE}).
 */
public class HistoryTimeToLiveParser {

  protected static final ConfigurationLogger LOG = ConfigurationLogger.CONFIG_LOGGER;

  protected static final int CAMUNDA_MODELER_TTL_DEFAULT_VALUE = 180; // This value is hardcoded into camunda modeler

  protected final boolean enforceNonNullValue;
  protected final String configValue;

  protected HistoryTimeToLiveParser(boolean enforceNonNullValue, String configValue) {
    this.enforceNonNullValue = enforceNonNullValue;
    this.configValue = configValue;
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

  public Integer parse(Element processElement, String processDefinitionId) {
    String historyTimeToLiveString = processElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "historyTimeToLive", configValue);

    return parseAndValidate(historyTimeToLiveString, processDefinitionId);
  }

  public Integer parse(Case caseElement, String processDefinitionId) {
    String historyTimeToLiveString = getValueOrConfig(caseElement.getCamundaHistoryTimeToLiveString());

    return parseAndValidate(historyTimeToLiveString, processDefinitionId);
  }

  public Integer parse(Decision decision) {
    String historyTimeToLiveString = getValueOrConfig(decision.getCamundaHistoryTimeToLiveString());

    return parseAndValidate(historyTimeToLiveString);
  }

  /**
   * Parses the given HistoryTimeToLive String expression and then executes any applicable validation before returning
   * the parsed value.
   *
   * @param historyTimeToLiveString the history time to live string expression in ISO-8601 format
   * @return the parsed integer value of history time to live
   * @throws NotValidException in case enforcement of non-null values is on and the parsed result was null
   */
  protected Integer parseAndValidate(String historyTimeToLiveString, String processDefinitionId) throws NotValidException {
    Integer result = ParseUtil.parseHistoryTimeToLive(historyTimeToLiveString);

    if (enforceNonNullValue && result == null) {
      throw new NotValidException("History Time To Live cannot be null");
    }

    if (result != null && result == CAMUNDA_MODELER_TTL_DEFAULT_VALUE) {
      LOG.logHistoryTimeToLiveDefaultValueWarning(processDefinitionId);
    }

    return result;
  }

  /**
   * Parses the given HistoryTimeToLive String expression and then executes any applicable validation before returning
   * the parsed value. Overloaded method without using processDefinitionId for cases where it is unavailable.
   *
   * @param historyTimeToLiveString the history time to live string expression in ISO-8601 format
   * @return the parsed integer value of history time to live
   * @throws NotValidException in case enforcement of non-null values is on and the parsed result was null
   */
  protected Integer parseAndValidate(String historyTimeToLiveString) throws NotValidException {
    return parseAndValidate(historyTimeToLiveString, null);
  }

  protected String getValueOrConfig(String value) {
    return Optional.ofNullable(value).orElse(configValue);
  }
}
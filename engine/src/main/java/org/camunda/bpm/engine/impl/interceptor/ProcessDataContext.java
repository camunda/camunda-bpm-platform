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
package org.camunda.bpm.engine.impl.interceptor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.commons.logging.MdcAccess;

/**
 * Holds the contextual process data.<br>
 *
 * New context properties are always part of a section that can be started by
 * {@link #pushSection(ExecutionEntity)}. The section keeps track of all pushed
 * properties. Those can easily be cleared by popping the section with
 * {@link #popSection()} afterwards, e.g. after the successful execution.<br>
 *
 * A property can be pushed to the logging context (MDC) if there is a configured
 * non-empty context name for it in the {@link ProcessEngineConfigurationImpl
 * process engine configuration}. The following configuration options are
 * available:
 * <ul>
 * <li>loggingContextActivityId - the context property for the activity id</li>
 * <li>loggingContextApplicationName - the context property for the application name</li>
 * <li>loggingContextBusinessKey - the context property for the business key</li>
 * <li>loggingContextDefinitionId - the context property for the definition id</li>
 * <li>loggingContextProcessInstanceId - the context property for the instance id</li>
 * <li>loggingContextTenantId - the context property for the tenant id</li>
 * </ul>
 */
public class ProcessDataContext {

  public static final String PROPERTY_ACTIVITY_ID = "activityId";

  protected static final String NULL_VALUE = "~NULL_VALUE~";

  protected String mdcPropertyActivityId;
  protected String mdcPropertyApplicationName;
  protected String mdcPropertyBusinessKey;
  protected String mdcPropertyDefinitionId;
  protected String mdcPropertyInstanceId;
  protected String mdcPropertyTenantId;

  protected List<String> mdcPropertyNames = new ArrayList<>();
  protected Map<String, String> basicToMdcPropertyNames = new HashMap<>();
  protected boolean handleMdc = false;

  protected Map<String, Deque<String>> propertyValues = new HashMap<>();

  protected boolean startNewSection = false;
  protected Deque<List<String>> sections = new ArrayDeque<>();

  public ProcessDataContext(ProcessEngineConfigurationImpl configuration) {
    mdcPropertyActivityId = configuration.getLoggingContextActivityId();
    if (isNotBlank(mdcPropertyActivityId)) {
      mdcPropertyNames.add(mdcPropertyActivityId);
      basicToMdcPropertyNames.put(PROPERTY_ACTIVITY_ID, mdcPropertyActivityId);
    }
    mdcPropertyApplicationName = configuration.getLoggingContextApplicationName();
    if (isNotBlank(mdcPropertyApplicationName)) {
      mdcPropertyNames.add(mdcPropertyApplicationName);
    }
    mdcPropertyBusinessKey = configuration.getLoggingContextBusinessKey();
    if (isNotBlank(mdcPropertyBusinessKey)) {
      mdcPropertyNames.add(mdcPropertyBusinessKey);
    }
    mdcPropertyDefinitionId = configuration.getLoggingContextProcessDefinitionId();
    if (isNotBlank(mdcPropertyDefinitionId)) {
      mdcPropertyNames.add(mdcPropertyDefinitionId);
    }
    mdcPropertyInstanceId = configuration.getLoggingContextProcessInstanceId();
    if (isNotBlank(mdcPropertyInstanceId)) {
      mdcPropertyNames.add(mdcPropertyInstanceId);
    }
    mdcPropertyTenantId = configuration.getLoggingContextTenantId();
    if (isNotBlank(mdcPropertyTenantId)) {
      mdcPropertyNames.add(mdcPropertyTenantId);
    }
    handleMdc = !mdcPropertyNames.isEmpty();
  }

  /**
   * Start a new section that keeps track of the pushed properties.
   *
   * If logging context properties are defined, the MDC is updated as well. This
   * also includes clearing the MDC for the first section that is pushed for the
   * logging context so that only the current properties will be present in the
   * MDC (might be less than previously present in the MDC). The previous
   * logging context needs to be reset in the MDC when this one is closed. This
   * can be achieved by using {@link #updateMdc(String)} with the previous
   * logging context.
   *
   * @param execution
   *          the execution to retrieve the context data from
   *
   * @return <code>true</code> if the section contains any updates and therefore
   *         should be popped later by {@link #popSection()}
   */
  public boolean pushSection(ExecutionEntity execution) {
    if (handleMdc && propertyValues.isEmpty()) {
      clearMdc();
    }
    startNewSection = true;
    addToStack(execution.getActivityId(), PROPERTY_ACTIVITY_ID, mdcPropertyActivityId != null);
    addToStack(execution.getProcessDefinitionId(), mdcPropertyDefinitionId);
    addToStack(execution.getProcessInstanceId(), mdcPropertyInstanceId);
    addToStack(execution.getTenantId(), mdcPropertyTenantId);

    if (isNotBlank(mdcPropertyApplicationName)) {
      ProcessApplicationReference currentPa = Context.getCurrentProcessApplication();
      if (currentPa != null) {
        addToStack(currentPa.getName(), mdcPropertyApplicationName);
      }
    }

    if (isNotBlank(mdcPropertyBusinessKey)) {
      addToStack(execution.getBusinessKey(), mdcPropertyBusinessKey);
    }

    return !startNewSection;
  }

  /**
   * Pop the latest section, remove all pushed properties of that section and -
   * if logging context properties are defined - update the MDC accordingly.
   */
  public void popSection() {
    List<String> section = sections.pollFirst();
    if (section != null) {
      for (String property : section) {
        removeFromStack(property);
      }
    }
  }

  /** Remove all logging context properties from the MDC */
  public void clearMdc() {
    if (handleMdc) {
      for (String property : mdcPropertyNames) {
        MdcAccess.remove(getMdcProperty(property));
      }
    }
  }

  /** Update the MDC with the current values of this logging context */
  public void updateMdc() {
    if (handleMdc && !propertyValues.isEmpty()) {
      // only update MDC if this context has set anything as well
      for (String property : mdcPropertyNames) {
        updateMdc(getMdcProperty(property));
      }
    }
  }

  /** Preserve the current MDC values and store them in the context */
  public void fetchCurrentContext() {
    if (handleMdc) {
      startNewSection = true;
      for (String property : mdcPropertyNames) {
        String mdcProperty = getMdcProperty(property);
        addToStack(MdcAccess.get(mdcProperty), mdcProperty, false);
      }
      startNewSection = false;
    }
  }

  /**
   * @param property
   *          the property to retrieve the latest value for
   * @return the latest value of the property if there is one, <code>null</code>
   *         otherwise
   */
  public String getLatestPropertyValue(String property) {
    if (!propertyValues.isEmpty()) {
      Deque<String> deque = propertyValues.get(property);
      if (deque != null) {
        return deque.peekFirst();
      }
    }
    return null;
  }

  protected void addToStack(String value, String property) {
    addToStack(value, property, true);
  }

  protected void addToStack(String value, String property, boolean updateMdc) {
    if (!isNotBlank(property)) {
      return;
    }
    Deque<String> deque = getDeque(property);
    String current = deque.peekFirst();
    if (valuesEqual(current, value)) {
      return;
    }
    addToCurrentSection(property);
    if (value == null) {
      deque.addFirst(NULL_VALUE);
      if (handleMdc && updateMdc) {
        MdcAccess.remove(getMdcProperty(property));
      }
    } else {
      deque.addFirst(value);
      if (handleMdc && updateMdc) {
        MdcAccess.put(getMdcProperty(property), value);
      }
    }
  }

  protected void removeFromStack(String property) {
    if (property == null) {
      return;
    }
    getDeque(property).removeFirst();
    if (handleMdc) {
      updateMdc(getMdcProperty(property));
    }
  }

  protected Deque<String> getDeque(String property) {
    Deque<String> deque = propertyValues.get(property);
    if (deque == null) {
      deque = new ArrayDeque<>();
      propertyValues.put(property, deque);
    }
    return deque;
  }

  protected void addToCurrentSection(String property) {
    List<String> section = sections.peekFirst();
    if (startNewSection) {
      section = new ArrayList<>();
      sections.addFirst(section);
      startNewSection = false;
    }
    section.add(property);
  }

  protected void updateMdc(String property) {
    if (handleMdc) {
      String previousValue = propertyValues.containsKey(property) ? propertyValues.get(property).peekFirst() : null;
      String mdcProperty = getMdcProperty(property);
      if (isNull(previousValue)) {
        MdcAccess.remove(mdcProperty);
      } else {
        MdcAccess.put(mdcProperty, previousValue);
      }
    }
  }

  protected String getMdcProperty(String property) {
    return basicToMdcPropertyNames.containsKey(property) ? basicToMdcPropertyNames.get(property) : property;
  }

  protected static boolean isNotBlank(String property) {
    return property != null && !property.trim().isEmpty();
  }

  protected static boolean valuesEqual(String val1, String val2) {
    if (isNull(val1)) {
      return val2 == null;
    }
    return val1.equals(val2);
  }

  protected static boolean isNull(String value) {
    return value == null || NULL_VALUE.equals(value);
  }

}

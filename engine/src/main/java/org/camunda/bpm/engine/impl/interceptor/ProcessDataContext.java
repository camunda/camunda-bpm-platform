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
import java.util.function.Supplier;

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

  protected static final String NULL_VALUE = "~NULL_VALUE~";

  protected String mdcPropertyActivityId;
  protected String mdcPropertyActivityName;
  protected String mdcPropertyApplicationName;
  protected String mdcPropertyBusinessKey;
  protected String mdcPropertyDefinitionId;
  protected String mdcPropertyDefinitionKey;
  protected String mdcPropertyInstanceId;
  protected String mdcPropertyTenantId;
  protected String mdcPropertyEngineName;

  protected boolean handleMdc = false;

  protected ProcessDataStack activityIdStack;
  /**
   * All data stacks we need to keep for MDC logging
   */
  protected Map<String, ProcessDataStack> mdcDataStacks = new HashMap<>();
  protected ProcessDataSections sections = new ProcessDataSections();

  public ProcessDataContext(ProcessEngineConfigurationImpl configuration) {
    this(configuration, false);
  }

  public ProcessDataContext(ProcessEngineConfigurationImpl configuration, boolean initFromCurrentMdc) {
    mdcPropertyActivityId = configuration.getLoggingContextActivityId();

    // always keep track of activity ids, because those are used to
    // populate the Job#getFailedActivityId field. This is independent
    // of the logging configuration
    activityIdStack = new ProcessDataStack(isNotBlank(mdcPropertyActivityId) ? mdcPropertyActivityId : null);
    if (isNotBlank(mdcPropertyActivityId)) {
      mdcDataStacks.put(mdcPropertyActivityId, activityIdStack);
    }
    mdcPropertyActivityName = initProperty(configuration::getLoggingContextActivityName);
    mdcPropertyApplicationName = initProperty(configuration::getLoggingContextApplicationName);
    mdcPropertyBusinessKey = initProperty(configuration::getLoggingContextBusinessKey);
    mdcPropertyDefinitionId = initProperty(configuration::getLoggingContextProcessDefinitionId);
    mdcPropertyDefinitionKey = initProperty(configuration::getLoggingContextProcessDefinitionKey);
    mdcPropertyInstanceId = initProperty(configuration::getLoggingContextProcessInstanceId);
    mdcPropertyTenantId = initProperty(configuration::getLoggingContextTenantId);
    mdcPropertyEngineName = initProperty(configuration::getLoggingContextEngineName);

    handleMdc = !mdcDataStacks.isEmpty();

    if (initFromCurrentMdc) {
      mdcDataStacks.values().forEach(stack -> {
        boolean valuePushed = stack.pushCurrentValueFromMdc();
        if (valuePushed) {
          sections.addToCurrentSection(stack);
        }
      });

      sections.sealCurrentSection();
    }
  }

  protected String initProperty(final Supplier<String> configSupplier) {
    final String configValue = configSupplier.get();
    if (isNotBlank(configValue)) {
      mdcDataStacks.put(configValue, new ProcessDataStack(configValue));
    }
    return configValue;
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
    if (handleMdc && hasNoMdcValues()) {
      clearMdc();
    }

    int numSections = sections.size();

    addToStack(activityIdStack, execution.getActivityId());
    addToStack(execution.getCurrentActivityName(), mdcPropertyActivityName);
    addToStack(execution.getProcessDefinitionId(), mdcPropertyDefinitionId);
    addToStack(execution.getProcessInstanceId(), mdcPropertyInstanceId);
    addToStack(execution.getTenantId(), mdcPropertyTenantId);
    addToStack(execution.getProcessEngine().getName(), mdcPropertyEngineName);

    if (isNotBlank(mdcPropertyApplicationName)) {
      ProcessApplicationReference currentPa = Context.getCurrentProcessApplication();
      if (currentPa != null) {
        addToStack(currentPa.getName(), mdcPropertyApplicationName);
      }
    }

    if (isNotBlank(mdcPropertyBusinessKey)) {
      addToStack(execution.getBusinessKey(), mdcPropertyBusinessKey);
    }

    if (isNotBlank(mdcPropertyDefinitionKey)) {
      addToStack(execution.getProcessDefinition().getKey(), mdcPropertyDefinitionKey);
    }

    sections.sealCurrentSection();

    boolean newSectionCreated = numSections != sections.size();

    return newSectionCreated;
  }

  protected boolean hasNoMdcValues() {
    return mdcDataStacks.values().stream()
        .allMatch(ProcessDataStack::isEmpty);
  }


  /**
   * Pop the latest section, remove all pushed properties of that section and -
   * if logging context properties are defined - update the MDC accordingly.
   */
  public void popSection() {
    sections.popCurrentSection();
  }

  /** Remove all logging context properties from the MDC */
  public void clearMdc() {
    if (handleMdc) {
      mdcDataStacks.values().forEach(ProcessDataStack::clearMdcProperty);
    }
  }

  /** Update the MDC with the current values of this logging context */
  public void updateMdcFromCurrentValues() {
    if (handleMdc) {
      mdcDataStacks.values().forEach(ProcessDataStack::updateMdcWithCurrentValue);
    }
  }

  /**
   * @return the latest value of the activity id property if exists, <code>null</code>
   *         otherwise
   */
  public String getLatestActivityId() {
    return activityIdStack.getCurrentValue();
  }

  protected void addToStack(String value, String property) {
    if (!isNotBlank(property)) {
      return;
    }

    ProcessDataStack stack = mdcDataStacks.get(property);
    addToStack(stack, value);
  }

  protected void addToStack(ProcessDataStack stack, String value) {
    String current = stack.getCurrentValue();
    if (valuesEqual(current, value)) {
      return;
    }

    stack.pushCurrentValue(value);
    sections.addToCurrentSection(stack);
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

  protected static class ProcessDataStack {

    protected String mdcName;
    protected Deque<String> deque = new ArrayDeque<>();

    /**
     * @param mdcName is optional. If present, any additions to a stack will also be reflected in the MDC context
     */
    public ProcessDataStack(String mdcName) {
      this.mdcName = mdcName;
    }

    public boolean isEmpty() {
      return deque.isEmpty();
    }

    public String getCurrentValue() {
      return deque.peekFirst();
    }

    public void pushCurrentValue(String value) {
      deque.addFirst(value != null ? value : NULL_VALUE);

      updateMdcWithCurrentValue();
    }

    /**
     * @return true if a value was obtained from the mdc
     *   and added to the stack
     */
    public boolean pushCurrentValueFromMdc() {
      if (isNotBlank(mdcName)) {
        String mdcValue = MdcAccess.get(mdcName);

        deque.addFirst(mdcValue != null ? mdcValue : NULL_VALUE);
        return true;
      } else {
        return false;
      }
    }

    public void removeCurrentValue() {
      deque.removeFirst();

      updateMdcWithCurrentValue();
    }

    public void clearMdcProperty() {
      if (isNotBlank(mdcName)) {
        MdcAccess.remove(mdcName);
      }
    }

    public void updateMdcWithCurrentValue() {
      if (isNotBlank(mdcName)) {
        String currentValue = getCurrentValue();

        if (isNull(currentValue)) {
          MdcAccess.remove(mdcName);
        } else {
          MdcAccess.put(mdcName, currentValue);
        }
      }
    }
  }

  protected static class ProcessDataSections {

    /**
     * Keeps track of when we added values to which stack (as we do not add
     * a new value to every stack with every update, but only changed values)
     */
    protected Deque<List<ProcessDataStack>> sections = new ArrayDeque<>();

    protected boolean currentSectionSealed = true;

    /**
     * Adds a stack to the current section. If the current section is already sealed,
     * a new section is created.
     */
    public void addToCurrentSection(ProcessDataStack stack) {
      List<ProcessDataStack> currentSection;

      if (currentSectionSealed) {
        currentSection = new ArrayList<>();
        sections.addFirst(currentSection);
        currentSectionSealed = false;

      } else {
        currentSection = sections.peekFirst();
      }

      currentSection.add(stack);
    }

    /**
     * Pops the current section and removes the
     * current values from the referenced stacks (including updates
     * to the MDC)
     */
    public void popCurrentSection() {
      List<ProcessDataStack> section = sections.pollFirst();
      if (section != null) {
        section.forEach(ProcessDataStack::removeCurrentValue);
      }

      currentSectionSealed = true;
    }

    /**
     * After a section is sealed, a new section will be created
     * with the next call to {@link #addToCurrentSection(ProcessDataStack)}
     */
    public void sealCurrentSection() {
      currentSectionSealed = true;
    }

    public int size() {
      return sections.size();
    }
  }
}

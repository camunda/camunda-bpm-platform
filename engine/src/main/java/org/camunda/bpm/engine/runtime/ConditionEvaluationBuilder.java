/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.runtime;

import java.util.List;
import java.util.Map;

/**
 * <p>A fluent builder for defining conditional start event correlation</p>
 *
 * @author Yana Vasileva
 */
public interface ConditionEvaluationBuilder {

  /**
   * <p>
   * Correlate the condition such that the process instance has a business key with
   * the given name. If the condition is correlated to a conditional start
   * event then the given business key is set on the created process instance.
   * Is only supported for {@link #evaluateStartConditions()}.</p>
   *
   * @param businessKey
   *          the businessKey to correlate on.
   * @return the builder
   */
  ConditionEvaluationBuilder processInstanceBusinessKey(String businessKey);

  /**
   * <p>Correlate the condition such that a process definition with the given id is selected.
   * Is only supported for {@link #evaluateStartConditions()}.</p>
   *
   * @param processDefinitionId the id of the process definition to correlate on.
   * @return the builder
   */
  ConditionEvaluationBuilder processDefinitionId(String processDefinitionId);

  /**
   * <p>Pass a variable to the condition.</p>
   *
   * <p>Invoking this method multiple times allows passing multiple variables.</p>
   *
   * @param variableName the name of the variable to set
   * @param variableValue the value of the variable to set
   * @return the builder
   */
  ConditionEvaluationBuilder setVariable(String variableName, Object variableValue);

  /**
   * <p>Pass a variables to the condition.</p>
   *
   * @param variables
   *          the map of variables
   * @return the builder
   */
  ConditionEvaluationBuilder setVariables(Map<String, Object> variables);

  /**
   * Specify a tenant to correlate a condition to. The condition can only be
   * correlated on process definitions which belongs to the given tenant.
   *
   * @param tenantId
   *          the id of the tenant
   * @return the builder
   */
  ConditionEvaluationBuilder tenantId(String tenantId);

  /**
   * Specify that the condition can only be correlated on process
   * definitions which belongs to no tenant.
   *
   * @return the builder
   */
  ConditionEvaluationBuilder withoutTenantId();

  /**
   *
   * @return the list of the newly created process instances
   */
  List<ProcessInstance> evaluateStartConditions();

}

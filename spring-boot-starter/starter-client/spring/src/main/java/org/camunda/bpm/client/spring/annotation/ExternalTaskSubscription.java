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
package org.camunda.bpm.client.spring.annotation;

import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.springframework.core.annotation.AliasFor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to subscribe the External Task Client to a topic.
 *
 * <p>Heads-up: <ul><li>for attributes of type {@link String}, the string <strong>$null$</strong>
 * is reserved and used as default value
 * <li>for attributes of type {@link Long}, the value {@link Long#MIN_VALUE} is reserved and used as
 * default value
 */
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface ExternalTaskSubscription {

  String STRING_NULL_VALUE = "$null$";
  long LONG_NULL_VALUE = Long.MIN_VALUE;

  /**
   * @return autoOpen <ul>
   * <li>{@code true}: the client immediately starts to fetch for External Tasks
   * <li>{@code false}: topic subscription can be opened after application start by calling
   * {@link SpringTopicSubscription#open()}
   */
  boolean autoOpen() default true;

  /**
   * Alias for {@link #value()}.
   *
   * @return topicName of the Service Task in the BPMN process model the client subscribes to
   */
  @AliasFor("value")
  String topicName() default STRING_NULL_VALUE;

  /**
   * Alias for {@link #topicName()}.
   *
   * @return topicName of the Service Task in the BPMN process model the client subscribes to
   */
  @AliasFor("topicName")
  String value() default STRING_NULL_VALUE;

  /**
   * @return lockDuration <ul>
   * <li> in milliseconds to lock the external tasks
   * <li> must be greater than zero
   * <li> the default lock duration is 20 seconds (20,000 milliseconds)
   * <li> overrides the lock duration configured on bootstrapping the client
   * </ul>
   */
  long lockDuration() default LONG_NULL_VALUE;

  /**
   * @return variableNames of variables which are supposed to be retrieved. All variables are
   * retrieved by default.
   */
  String[] variableNames() default {STRING_NULL_VALUE};

  /**
   * @return localVariables
   * whether or not variables from greater scope than the external task
   * should be fetched. <code>false</code> means all variables visible
   * in the scope of the external task will be fetched,
   * <code>true</code> means only local variables (to the scope of the
   * external task) will be fetched
   */
  boolean localVariables() default false;

  /**
   * @return businessKey to filter for external tasks that are supposed to be fetched and locked
   */
  String businessKey() default STRING_NULL_VALUE;

  /**
   * @return processDefinitionId to filter for external tasks that are supposed to be fetched and
   * locked
   */
  String processDefinitionId() default STRING_NULL_VALUE;

  /**
   * @return processDefinitionIds to filter for external tasks that are supposed to be fetched and
   * locked
   */
  String[] processDefinitionIdIn() default {STRING_NULL_VALUE};

  /**
   * @return processDefinitionKey to filter for external tasks that are supposed to be fetched and
   * locked
   */
  String processDefinitionKey() default STRING_NULL_VALUE;

  /**
   * @return processDefinitionKeyIn to filter for external tasks that are supposed to be fetched
   * and locked
   */
  String[] processDefinitionKeyIn() default {STRING_NULL_VALUE};

  /**
   * @return processDefinitionVersionTag to filter for external tasks that are supposed to be
   * fetched and locked
   */
  String processDefinitionVersionTag() default STRING_NULL_VALUE;

  /**
   * @return processVariables of which the external tasks to be retrieved are related to. Each
   * value is instance of {@link ProcessVariable}
   */
  ProcessVariable[] processVariables()
      default {@ProcessVariable(name = STRING_NULL_VALUE, value = STRING_NULL_VALUE)};

  /**
   * @return Filter for external tasks without tenant
   */
  boolean withoutTenantId() default false;

  /**
   * @return tenantIds to filter for external tasks that are supposed to be fetched and locked
   */
  String[] tenantIdIn() default {STRING_NULL_VALUE};

  /**
   * @return includeExtensionProperties
   * whether or not to include custom extension properties for fetched
   * external tasks. <code>true</code> means all extensionProperties
   * defined in the external task activity will be provided.
   * <code>false</code> means custom extension properties are not
   * available within the external-task-client. The default is
   * <code>false</code>.
   */
  boolean includeExtensionProperties() default false;

  /**
   * Element of {@link #processVariables()}
   */
  @Documented
  @Target(TYPE)
  @Retention(RUNTIME)
  @interface ProcessVariable {

    /**
     * @return name of process variable
     */
    String name();

    /**
     * @return value of process variable
     */
    String value();
  }

}

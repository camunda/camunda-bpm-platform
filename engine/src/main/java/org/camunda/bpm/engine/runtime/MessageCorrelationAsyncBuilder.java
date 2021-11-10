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
package org.camunda.bpm.engine.runtime;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

/**
 * A fluent builder for defining asynchronous message correlation
 */
public interface MessageCorrelationAsyncBuilder {

  /**
   * <p>
   * Correlate the message such that the process instances with the given ids
   * are selected.
   * </p>
   *
   * @param ids
   *          the ids of the process instances to correlate to; at least one of
   *          {@link #processInstanceIds(List)},
   *          {@link #processInstanceQuery(ProcessInstanceQuery)}, or
   *          {@link #historicProcessInstanceQuery(HistoricProcessInstanceQuery)}
   *          has to be set.
   * @return the builder
   * @throws NullValueException
   *           when <code>ids</code> is <code>null</code>
   */
  MessageCorrelationAsyncBuilder processInstanceIds(List<String> ids);

  /**
   * <p>
   * Correlate the message such that the process instances found by the given
   * query are selected.
   * </p>
   *
   * @param processInstanceQuery
   *          the query to select process instances to correlate to; at least
   *          one of {@link #processInstanceIds(List)},
   *          {@link #processInstanceQuery(ProcessInstanceQuery)}, or
   *          {@link #historicProcessInstanceQuery(HistoricProcessInstanceQuery)}
   *          has to be set.
   * @return the builder
   * @throws NullValueException
   *           when <code>processInstanceQuery</code> is <code>null</code>
   */
  MessageCorrelationAsyncBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery);

  /**
   * <p>
   * Correlate the message such that the process instances found by the given
   * query are selected.
   * </p>
   *
   * @param historicProcessInstanceQuery
   *          the query to select process instances to correlate to; at least
   *          one of {@link #processInstanceIds(List)},
   *          {@link #processInstanceQuery(ProcessInstanceQuery)}, or
   *          {@link #historicProcessInstanceQuery(HistoricProcessInstanceQuery)}
   *          has to be set.
   * @return the builder
   * @throws NullValueException
   *           when <code>historicProcessInstanceQuery</code> is <code>null</code>
   */
  MessageCorrelationAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery);

  /**
   * <p>
   * Pass a variable to the execution waiting on the message. Use this method
   * for passing the message's payload.
   * </p>
   *
   * <p>
   * Invoking this method multiple times allows passing multiple variables.
   * </p>
   *
   * @param variableName
   *          the name of the variable to set
   * @param variableValue
   *          the value of the variable to set
   * @return the builder
   * @throws NullValueException
   *           when <code>variableName</code> is <code>null</code>
   */
  MessageCorrelationAsyncBuilder setVariable(String variableName, Object variableValue);

  /**
   * <p>
   * Pass a map of variables to the execution waiting on the message. Use this
   * method for passing the message's payload
   * </p>
   *
   * @param variables
   *          the map of variables
   * @return the builder
   */
  MessageCorrelationAsyncBuilder setVariables(Map<String, Object> variables);

  /**
   * Correlates a message asynchronously to executions that are waiting for this
   * message based on the provided queries and list of process instance ids,
   * whereby query results and list of ids will be merged.
   *
   * @return the batch which correlates the message asynchronously
   *
   * @throws NullValueException
   *           when neither {@link #processInstanceIds(List)},
   *           {@link #processInstanceQuery(ProcessInstanceQuery)}, nor
   *           {@link #historicProcessInstanceQuery(HistoricProcessInstanceQuery)}}
   *           have been set
   * @throws BadUserRequestException
   *           when no process instances are found with the given ids or queries
   * @throws AuthorizationException
   *           when the user has no {@link BatchPermissions#CREATE} or
   *           {@link BatchPermissions#CREATE_BATCH_SET_VARIABLES} permission on
   *           {@link Resources#BATCH}
   */
  Batch correlateAllAsync();

}

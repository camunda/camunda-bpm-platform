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
package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Report;

/**
 * <p>Defines a report query for {@link HistoricProcessInstance}s.</p>
 *
 * @author Roman Smirnov
 *
 */
public interface HistoricProcessInstanceReport extends Report {

  /**
   * Only takes historic process instances into account that were started before the given date.
   *
   * @throws NotValidException if the given started before date is null
   *
   */
  HistoricProcessInstanceReport startedBefore(Date startedBefore);

  /**
   * Only takes historic process instances into account that were started after the given date.
   *
   * @throws NotValidException if the given started after date is null
   */
  HistoricProcessInstanceReport startedAfter(Date startedAfter);

  /**
   * Only takes historic process instances into account for the given process definition ids.
   *
   * @throws NotValidException if one of the given ids is null
   */
  HistoricProcessInstanceReport processDefinitionIdIn(String... processDefinitionIds);

  /**
   * Only takes historic process instances into account for the given process definition keys.
   *
   * @throws NotValidException if one of the given ids is null
   */
  HistoricProcessInstanceReport processDefinitionKeyIn(String... processDefinitionKeys);

}

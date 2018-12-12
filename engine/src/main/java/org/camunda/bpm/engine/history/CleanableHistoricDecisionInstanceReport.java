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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Query;

/**
 * Defines a report query for finished decision instances.
 *
 */
public interface CleanableHistoricDecisionInstanceReport extends Query<CleanableHistoricDecisionInstanceReport, CleanableHistoricDecisionInstanceReportResult>{

  /**
   * Only takes historic decision instances into account for the given decision definition ids.
   *
   * @throws NotValidException if one of the given ids is null
   */
  CleanableHistoricDecisionInstanceReport decisionDefinitionIdIn(String... decisionDefinitionIds);

  /**
   * Only takes historic decision instances into account for the given decision definition keys.
   *
   * @throws NotValidException if one of the given keys is null
   */
  CleanableHistoricDecisionInstanceReport decisionDefinitionKeyIn(String... decisionDefinitionKeys);

  /**
   * Only select historic decision instances with one of the given tenant ids.
   *
   * @throws NotValidException if one of the given ids is null
   */
  CleanableHistoricDecisionInstanceReport tenantIdIn(String... tenantIds);

  /**
   * Only selects historic decision instances which have no tenant id.
   */
  CleanableHistoricDecisionInstanceReport withoutTenantId();

  /**
   * Only selects historic decision instances which have more than zero finished instances.
   */
  CleanableHistoricDecisionInstanceReport compact();

  /**
   * Order by finished decision instances amount (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  CleanableHistoricDecisionInstanceReport orderByFinished();

}

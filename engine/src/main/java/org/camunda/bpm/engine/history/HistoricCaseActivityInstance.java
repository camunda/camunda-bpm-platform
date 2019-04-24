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

/**
 * Represents one execution of a case activity which is stored permanent for statistics, audit and other business intelligence purposes.
 *
 * @author Sebastian Menski
 */
public interface HistoricCaseActivityInstance {

   /** The id of the case activity instance (== as the id of the runtime activity). */
   String getId();

   /** The id of the parent case activity instance. */
   String getParentCaseActivityInstanceId();

   /** The unique identifier of the case activity in the case. */
   String getCaseActivityId();

   /** The display name for the case activity. */
   String getCaseActivityName();

   /** The display type for the case activity. */
   String getCaseActivityType();

   /** The case definition reference. */
   String getCaseDefinitionId();

   /** The case instance reference. */
   String getCaseInstanceId();

   /** The case execution reference. */
   String getCaseExecutionId();

   /** The corresponding task in case of a human task activity. */
   String getTaskId();

   /** The corresponding process in case of a process task activity. */
   String getCalledProcessInstanceId();

   /** The corresponding case in case of a case task activity. */
   String getCalledCaseInstanceId();

   /**
    * The id of the tenant this historic case activity instance belongs to. Can be <code>null</code>
    * if the historic case activity instance belongs to no single tenant.
    */
   String getTenantId();

   /** The time when the case activity was created. */
   Date getCreateTime();

   /** The time when the case activity ended */
   Date getEndTime();

   /** Difference between {@link #getEndTime()} and {@link #getCreateTime()}.  */
   Long getDurationInMillis();

   /** Check if the case activity is required. */
   boolean isRequired();

    /** Check if the case activity is available. */
   boolean isAvailable();

   /** Check if the case activity is enabled. */
   boolean isEnabled();

   /** Check if the case activity is disabled. */
   boolean isDisabled();

   /** Check if the case activity is active. */
   boolean isActive();

   /** Check if the case activity is completed. */
   boolean isCompleted();

   /** Check if the case activity is terminated. */
   boolean isTerminated();

}

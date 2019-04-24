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

/**
 * @author Roman Smirnov
 *
 */
public interface HistoricCaseActivityStatistics {

  /**
   * The case activity id.
   */
  String getId();

  /**
   * The number of available case activity instances.
   */
  long getAvailable();

  /**
   * The number of enabled case activity instances.
   */
  long getEnabled();

  /**
   * The number of disabled case activity instances.
   */
  long getDisabled();

  /**
   * The number of active case activity instances.
   */
  long getActive();

  /**
   * The number of completed case activity instances.
   */
  long getCompleted();

  /**
   * The number of terminated case activity instances.
   */
  long getTerminated();

}

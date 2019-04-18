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

import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * Contains the possible properties which can be used in a {@link HistoricDetailQuery}.
 *
 * @author Tom Baeyens
 */
public interface HistoricDetailQueryProperty {

  public static final QueryProperty PROCESS_INSTANCE_ID = new QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty VARIABLE_NAME = new QueryPropertyImpl("NAME_");
  public static final QueryProperty VARIABLE_TYPE = new QueryPropertyImpl("TYPE_");
  public static final QueryProperty VARIABLE_REVISION = new QueryPropertyImpl("REV_");
  public static final QueryProperty TIME = new QueryPropertyImpl("TIME_");
  public static final QueryProperty SEQUENCE_COUNTER = new QueryPropertyImpl("SEQUENCE_COUNTER_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");
}

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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.management.SchemaLogQuery;
import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogQueryImpl extends AbstractQuery<SchemaLogQuery, SchemaLogEntry> implements SchemaLogQuery {

  private static final long serialVersionUID = 1L;
  private static final QueryProperty TIMESTAMP_PROPERTY = new QueryPropertyImpl("TIMESTAMP_");

  protected String version;

  public SchemaLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public SchemaLogQuery version(String version) {
    ensureNotNull("version", version);
    this.version = version;
    return this;
  }

  @Override
  public SchemaLogQuery orderByTimestamp() {
    orderBy(TIMESTAMP_PROPERTY);
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getSchemaLogManager().findSchemaLogEntryCountByQueryCriteria(this);
  }

  @Override
  public List<SchemaLogEntry> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getSchemaLogManager().findSchemaLogEntriesByQueryCriteria(this, page);
  }
}
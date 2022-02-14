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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.SchemaLogQueryImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.management.SchemaLogQuery;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogManager extends AbstractManager {

  public Long findSchemaLogEntryCountByQueryCriteria(SchemaLogQuery schemaLogQuery) {
    if (isAuthorized()) {
      return (Long) getDbEntityManager().selectOne("selectSchemaLogEntryCountByQueryCriteria", schemaLogQuery);
    } else {
      return 0L;
    }
  }

  @SuppressWarnings("unchecked")
  public List<SchemaLogEntry> findSchemaLogEntriesByQueryCriteria(SchemaLogQueryImpl schemaLogQueryImpl, Page page) {
    if (isAuthorized()) {
      return getDbEntityManager().selectList("selectSchemaLogEntryByQueryCriteria", schemaLogQueryImpl, page);
    } else {
      return Collections.emptyList();
    }
  }

  private boolean isAuthorized() {
    try {
      getAuthorizationManager().checkCamundaAdminOrPermission(CommandChecker::checkReadSchemaLog);
      return true;
    } catch (AuthorizationException e) {
      return false;
    }
  }
}

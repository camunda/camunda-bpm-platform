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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.schemaLogEntryByTimestamp;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManagerFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.SchemaLogEntryEntity;
import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogQueryTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  Date startDate;
  SchemaLogEntryEntity dummySchemaLogEntry;

  @Before
  public void init() {
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    startDate = new Date();
    dummySchemaLogEntry = createDummySchemaLogEntry();
  }

  @Test
  public void testQuerySchemaLogEntrySingleResult() {
    // given a clean database

    // when
    SchemaLogEntry schemaLogEntry = managementService.createSchemaLogQuery().singleResult();

    // then expect one entry
    assertThat(schemaLogEntry.getId(), is("0"));
    assertThat(schemaLogEntry.getTimestamp(), notNullValue());
    assertThat(schemaLogEntry.getVersion(), notNullValue());
  }

  @Test
  public void testQuerySchemaLogEntryList() {
    // given a clean database

    // when
    List<SchemaLogEntry> schemaLogEntries = managementService.createSchemaLogQuery().list();

    // then expect one entry
    assertThat(managementService.createSchemaLogQuery().count(), is(1L));
    SchemaLogEntry schemaLogEntry = schemaLogEntries.get(0);
    assertThat(schemaLogEntry.getId(), is("0"));
    assertThat(schemaLogEntry.getTimestamp(), notNullValue());
    assertThat(schemaLogEntry.getVersion(), notNullValue());
  }

  @Test
  public void testOrderByTimestamp() {
    // given two schema log entries
    populateTable();

    // then sorting works
    verifySorting(managementService.createSchemaLogQuery().orderByTimestamp().asc().list(), schemaLogEntryByTimestamp());
    verifySorting(managementService.createSchemaLogQuery().orderByTimestamp().desc().list(), inverted(schemaLogEntryByTimestamp()));

    cleanupTable();
  }

  @Test
  public void testFilterByVersion() {
    // given two schema log entries
    populateTable();

    // when
    SchemaLogEntry schemaLogEntry = managementService.createSchemaLogQuery().version("dummyVersion").singleResult();

    // then
    assertThat(schemaLogEntry.getId(), is(dummySchemaLogEntry.getId()));
    assertThat(schemaLogEntry.getTimestamp(), is(dummySchemaLogEntry.getTimestamp()));
    assertThat(schemaLogEntry.getVersion(), is(dummySchemaLogEntry.getVersion()));

    cleanupTable();
  }

  @Test
  public void testSortedPagedQuery() {
    // given two schema log entries
    populateTable();

    // then paging works
    // ascending order
    List<SchemaLogEntry> schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().asc().listPage(0, 1);
    assertThat(schemaLogEntry.size(), is(1));
    assertThat(schemaLogEntry.get(0).getId(), is("0"));

    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().asc().listPage(1, 1);
    assertThat(schemaLogEntry.size(), is(1));
    assertThat(schemaLogEntry.get(0).getId(), is("uniqueId"));

    // descending order
    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().desc().listPage(0, 1);
    assertThat(schemaLogEntry.size(), is(1));
    assertThat(schemaLogEntry.get(0).getId(), is("uniqueId"));

    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().desc().listPage(1, 1);
    assertThat(schemaLogEntry.size(), is(1));
    assertThat(schemaLogEntry.get(0).getId(), is("0"));

    cleanupTable();
  }

  private void populateTable() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {

        DbEntityManagerFactory dbEntityManagerFactory = new DbEntityManagerFactory(Context.getProcessEngineConfiguration().getIdGenerator());
        DbEntityManager newEntityManager = dbEntityManagerFactory.openSession();
        newEntityManager.insert(dummySchemaLogEntry);
        newEntityManager.flush();
        return null;
      }
    });
    assertThat(managementService.createSchemaLogQuery().count(), is(2L));
  }

  private void cleanupTable() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        dbEntityManager.delete(dummySchemaLogEntry);
        dbEntityManager.flush();
        return null;
      }
    });
    assertThat(managementService.createSchemaLogQuery().count(), is(1L));
  }

  private SchemaLogEntryEntity createDummySchemaLogEntry() {
    SchemaLogEntryEntity dummy = new SchemaLogEntryEntity();
    dummy.setId("uniqueId");
    dummy.setTimestamp(new Date());
    dummy.setVersion("dummyVersion");
    return dummy;
  }
}
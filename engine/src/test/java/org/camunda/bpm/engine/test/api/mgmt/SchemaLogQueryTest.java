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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.schemaLogEntryByTimestamp;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

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
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogQueryTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected SchemaLogEntryEntity dummySchemaLogEntry;
  protected long initialEntryCount;

  @Before
  public void init() {
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    initialEntryCount = managementService.createSchemaLogQuery().count();
    dummySchemaLogEntry = createDummySchemaLogEntry();
  }

  @Test
  public void testQuerySchemaLogEntryList() {
    // given (at least) one schema log entry

    // when
    List<SchemaLogEntry> schemaLogEntries = managementService.createSchemaLogQuery().list();

    // then expect (at least) one entry
    assertThat(managementService.createSchemaLogQuery().count()).isGreaterThan(0L);

    // in case of tests that upgrade the schema there are more than one entry
    for (SchemaLogEntry schemaLogEntry : schemaLogEntries) {
      assertThat(Integer.parseInt(schemaLogEntry.getId())).isGreaterThanOrEqualTo(0);
      assertThat(schemaLogEntry.getTimestamp()).isNotNull();
      assertThat(schemaLogEntry.getVersion()).isNotNull();
    }

    cleanupTable();
  }

  @Test
  public void testOrderByTimestamp() {
    // given (at least) two schema log entries
    populateTable();

    // then sorting works
    verifySorting(managementService.createSchemaLogQuery().orderByTimestamp().asc().list(), schemaLogEntryByTimestamp());
    verifySorting(managementService.createSchemaLogQuery().orderByTimestamp().desc().list(), inverted(schemaLogEntryByTimestamp()));

    cleanupTable();
  }

  @Test
  public void testFilterByVersion() {
    // given (at least) two schema log entries
    populateTable();

    // when
    SchemaLogEntry schemaLogEntry = managementService.createSchemaLogQuery().version("dummyVersion").singleResult();

    // then
    assertThat(schemaLogEntry.getId()).isEqualTo(dummySchemaLogEntry.getId());
    assertThat(schemaLogEntry.getTimestamp()).isNotNull();
    assertThat(schemaLogEntry.getVersion()).isEqualTo(dummySchemaLogEntry.getVersion());

    cleanupTable();
  }

  /**
   * There should always be an entry with id "0" that has the oldest timestamp
   * and thus should be at the beginning/end of the list. (according to the
   * ordering)
   */
  @Test
  public void testSortedPagedQuery() {
    // given (at least) two schema log entries
    populateTable();
    // in case of tests that upgrade the schema there are more than two entries we want to check the last page.
    int count = (int) managementService.createSchemaLogQuery().count();

    // then paging works
    // ascending order
    List<SchemaLogEntry> schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().asc().listPage(0, 1);
    assertThat(schemaLogEntry).hasSize(1);
    assertThat(schemaLogEntry.get(0).getId()).isEqualTo("0");

    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().asc().listPage(count - 1, 1);
    assertThat(schemaLogEntry).hasSize(1);
    assertThat(schemaLogEntry.get(0).getId()).isNotEqualTo("0");

    // descending order
    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().desc().listPage(0, 1);
    assertThat(schemaLogEntry).hasSize(1);
    assertThat(schemaLogEntry.get(0).getId()).isNotEqualTo("0");

    schemaLogEntry = managementService.createSchemaLogQuery().orderByTimestamp().desc().listPage(count - 1, 1);
    assertThat(schemaLogEntry).hasSize(1);
    assertThat(schemaLogEntry.get(0).getId()).isEqualTo("0");

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
    assertThat(managementService.createSchemaLogQuery().count()).isEqualTo(initialEntryCount + 1);
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
    assertThat(managementService.createSchemaLogQuery().count()).isEqualTo(initialEntryCount);
  }

  private SchemaLogEntryEntity createDummySchemaLogEntry() {
    SchemaLogEntryEntity dummy = new SchemaLogEntryEntity();
    dummy.setId("uniqueId");
    dummy.setTimestamp(new Date());
    dummy.setVersion("dummyVersion");
    return dummy;
  }
}
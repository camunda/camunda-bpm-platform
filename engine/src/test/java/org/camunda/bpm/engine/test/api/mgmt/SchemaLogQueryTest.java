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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
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
  
  Date startDate;

  @Before
  public void init() {
    managementService = engineRule.getManagementService();
    startDate = new Date();
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
    //given a clean database

    //when
    List<SchemaLogEntry> schemaLogEntries = managementService.createSchemaLogQuery().list();

    //then expect one entry
    assertThat(managementService.createSchemaLogQuery().count(), is(1L));
    SchemaLogEntry schemaLogEntry = schemaLogEntries.get(0);
    assertThat(schemaLogEntry.getId(), is("0"));
    assertThat(schemaLogEntry.getTimestamp(), notNullValue());
    assertThat(schemaLogEntry.getVersion(), notNullValue());
  }
}
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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.impl.persistence.entity.SchemaLogEntryEntity;
import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.management.SchemaLogQuery;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogRestServiceQueryTest extends AbstractRestServiceTest {

  /**
   * 
   */
  private static final String SCHEMA_LOG_URL = TEST_RESOURCE_ROOT_PATH + SchemaLogRestService.PATH;

  private static final String SCHEMA_LOG_ENTRY_MOCK_ID = "schema-log-entry-mock-id";
  private static final String SCHEMA_LOG_ENTRY_MOCK_VERSION = "schema-log-entry-mock-version";
  private static final Date SCHEMA_LOG_ENTRY_MOCK_TIMESTAMP = new Date();

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  private SchemaLogQuery mockedQuery;

  private List<SchemaLogEntry> mockedSchemaLogEntries;

  @Before
  public void init() {
    mockedQuery = Mockito.mock(SchemaLogQuery.class);

    mockedSchemaLogEntries = createMockedSchemaLogEntries();
    when(mockedQuery.list()).thenReturn(mockedSchemaLogEntries);

    when(processEngine.getManagementService().createSchemaLogQuery()).thenReturn(mockedQuery);
  }
  
  private List<SchemaLogEntry> createMockedSchemaLogEntries(){
    List<SchemaLogEntry> entries = new ArrayList<SchemaLogEntry>();
    SchemaLogEntryEntity entry = new SchemaLogEntryEntity();

    entry.setId(SCHEMA_LOG_ENTRY_MOCK_ID);
    entry.setVersion(SCHEMA_LOG_ENTRY_MOCK_VERSION);
    entry.setTimestamp(SCHEMA_LOG_ENTRY_MOCK_TIMESTAMP);

    entries.add(entry);
    return entries;
  }

  @Test
  public void testGetSchemaLog() {
    given()
      .queryParam("version", SCHEMA_LOG_ENTRY_MOCK_VERSION)
      .queryParam("sortBy", "timestamp")
      .queryParam("sortOrder", "asc")
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body("[0].version", is(SCHEMA_LOG_ENTRY_MOCK_VERSION))
        .body("[0].timestamp", notNullValue())
      .when().get(SCHEMA_LOG_URL);

    verify(mockedQuery).version(SCHEMA_LOG_ENTRY_MOCK_VERSION);
    verify(mockedQuery).orderByTimestamp();
    verify(mockedQuery).list();
  }

  @Test
  public void testGetSchemaLogAsPost() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("version", SCHEMA_LOG_ENTRY_MOCK_VERSION);
    params.put("sortBy", "timestamp");
    params.put("sortOrder", "asc");

    given()
      .contentType(MediaType.APPLICATION_JSON)
      .body(params)
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body("[0].version", is(SCHEMA_LOG_ENTRY_MOCK_VERSION))
        .body("[0].timestamp", notNullValue())
      .when().post(SCHEMA_LOG_URL);
    
    verify(mockedQuery).version(SCHEMA_LOG_ENTRY_MOCK_VERSION);
    verify(mockedQuery).orderByTimestamp();
    verify(mockedQuery).list();
  }
}

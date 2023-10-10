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
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.PERSISTENCE_EXCEPTION_MESSAGE;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.wrapPersistenceException;
import static org.camunda.bpm.engine.rest.exception.ExceptionLogger.REST_API;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_USER_FIRST_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.exceptions.PersistenceException;
import org.camunda.bpm.engine.ProcessEnginePersistenceException;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for Connection Exceptions that originate from the persistence layer.
 */
@RunWith(Parameterized.class)
public class PersistenceConnectionExceptionLoggingTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(REST_API);

  protected static final String USER_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/user";

  private final ConnectionSubclass subclass;

  public PersistenceConnectionExceptionLoggingTest(ConnectionSubclass subclass) {
    this.subclass = subclass;
  }

  @Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    ConnectionSubclass[] values = ConnectionSubclass.values();

    return Arrays.stream(values)
        .map(c -> new Object[] {c})
        .collect(Collectors.toList());
  }

  @Test
  public void shouldLogPersistenceConnectionExceptionOnError() {
    stubFailingUserQuery(subclass);

    String expectedMessage = PERSISTENCE_EXCEPTION_MESSAGE;

    given().queryParam("firstName", EXAMPLE_USER_FIRST_NAME)
        .then().expect()
        .statusCode(500)
        .body("type", equalTo("ProcessEnginePersistenceException"))
        .body("message", equalTo(expectedMessage))
        .body("code", equalTo(0))
        .when().get(USER_QUERY_URL);

    verifyLogs(Level.ERROR, expectedMessage);
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(logLevel);
    assertThat(logs.get(0).getMessage()).containsIgnoringCase(message);
  }

  protected void stubFailingUserQuery(ConnectionSubclass subclass) {
    UserQuery result = mock(UserQuery.class);


    when(result.list()).thenThrow(createPersistenceException("list", subclass));
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(result);
  }

  private ProcessEnginePersistenceException createPersistenceException(String operation, ConnectionSubclass subclass) {
    SQLException sqlException = new SQLException(subclass.message(), subclass.sqlState());
    PersistenceException persistenceException = new PersistenceException("Failed to execute " + operation, sqlException);

    return wrapPersistenceException(persistenceException);
  }

  public enum ConnectionSubclass {

    CLIENT_UNABLE("001", "SQL-client unable to establish SQL-connection"),
    NAME_IN_USE("002", "connection name in use"),
    NOT_EXISTS("003", "connection does not exist"),
    SERVER_REJECT("004", "SQL-server rejected establishment of SQL-connection"),
    FAILURE("006", "connection failure"),
    UNKNOWN_TX_RESOLUTION("007", "transaction resolution unknown"),

    UNKNOWN("XXX", "This is a dummy subclass");

    private final String subclass;
    private final String message;

    ConnectionSubclass(String subclass, String message) {
      this.subclass = subclass;
      this.message = message;
    }

    public String sqlState() {
      return "08" + subclass;
    }

    public String message() {
      return message;
    }
  }

}

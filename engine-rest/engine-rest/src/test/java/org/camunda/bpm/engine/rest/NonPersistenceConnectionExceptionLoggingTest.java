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
import java.sql.SQLNonTransientConnectionException;
import java.util.List;
import org.apache.ibatis.exceptions.PersistenceException;
import org.camunda.bpm.engine.ProcessEnginePersistenceException;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests Connection Exceptions that do not originate from persistence layer but are still connection exceptions.
 */
public class NonPersistenceConnectionExceptionLoggingTest extends AbstractRestServiceTest {

  protected static final String USER_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/user";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(REST_API);

  @Test
  public void shouldLogNonPersistenceExceptionOnWarning() {
    stubFailingUserQuery(new SQLNonTransientConnectionException());

    String expectedMessage = PERSISTENCE_EXCEPTION_MESSAGE;

    given().queryParam("firstName", EXAMPLE_USER_FIRST_NAME)
        .then().expect()
        .statusCode(500)
        .body("type", equalTo("ProcessEnginePersistenceException"))
        .body("message", equalTo(expectedMessage))
        .body("code", equalTo(0))
        .when().get(USER_QUERY_URL);

    verifyLogs(Level.WARN, expectedMessage);
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(logLevel);
    assertThat(logs.get(0).getMessage()).containsIgnoringCase(message);
  }

  protected void stubFailingUserQuery(Exception secondRootCause) {
    UserQuery result = mock(UserQuery.class);

    PersistenceException persistenceException = new PersistenceException("Failed to execute list", secondRootCause);
    ProcessEnginePersistenceException exception = wrapPersistenceException(persistenceException);

    when(result.list()).thenThrow(exception);
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(result);
  }
}

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.junit.Before;
import org.junit.Test;

/**
*
* @author Roman Smirnov
*
*/
public class AbstractCaseExecutionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String CASE_EXECUTION_URL = TEST_RESOURCE_ROOT_PATH + "/case-execution";
  protected static final String SINGLE_CASE_EXECUTION_URL = CASE_EXECUTION_URL + "/{id}";

  private CaseService caseServiceMock;
  private CaseExecutionQuery caseExecutionQueryMock;

  @Before
  public void setUpRuntime() {
    CaseExecution mockCaseExecution = MockProvider.createMockCaseExecution();

    caseServiceMock = mock(CaseService.class);

    when(processEngine.getCaseService()).thenReturn(caseServiceMock);

    caseExecutionQueryMock = mock(CaseExecutionQuery.class);

    when(caseServiceMock.createCaseExecutionQuery()).thenReturn(caseExecutionQueryMock);
    when(caseExecutionQueryMock.caseExecutionId(MockProvider.EXAMPLE_CASE_EXECUTION_ID)).thenReturn(caseExecutionQueryMock);
    when(caseExecutionQueryMock.singleResult()).thenReturn(mockCaseExecution);
  }

  @Test
  public void testDefinitionRetrieval() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_EXECUTION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID))
        .body("caseInstanceId", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID))
        .body("active", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ACTIVE))
        .body("active", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ENABLED))
    .when()
      .get(SINGLE_CASE_EXECUTION_URL);

    verify(caseServiceMock).createCaseExecutionQuery();
    verify(caseExecutionQueryMock).caseExecutionId(MockProvider.EXAMPLE_CASE_EXECUTION_ID);
    verify(caseExecutionQueryMock).singleResult();
  }

}

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
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.junit.Before;
import org.junit.Test;

/**
*
* @author Roman Smirnov
*
*/
public class AbstractCaseInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String CASE_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/case-instance";
  protected static final String SINGLE_CASE_INSTANCE_URL = CASE_INSTANCE_URL + "/{id}";

  private CaseService caseServiceMock;
  private CaseInstanceQuery caseInstanceQueryMock;

  @Before
  public void setUpRuntime() {
    CaseInstance mockCaseInstance = MockProvider.createMockCaseInstance();

    caseServiceMock = mock(CaseService.class);

    when(processEngine.getCaseService()).thenReturn(caseServiceMock);

    caseInstanceQueryMock = mock(CaseInstanceQuery.class);

    when(caseServiceMock.createCaseInstanceQuery()).thenReturn(caseInstanceQueryMock);
    when(caseInstanceQueryMock.caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID)).thenReturn(caseInstanceQueryMock);
    when(caseInstanceQueryMock.singleResult()).thenReturn(mockCaseInstance);
  }

  @Test
  public void testCaseInstanceRetrieval() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY))
        .body("caseDefinitionId", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID))
        .body("active", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_ACTIVE))
    .when()
      .get(SINGLE_CASE_INSTANCE_URL);

    verify(caseServiceMock).createCaseInstanceQuery();
    verify(caseInstanceQueryMock).caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseInstanceQueryMock).singleResult();
  }

}

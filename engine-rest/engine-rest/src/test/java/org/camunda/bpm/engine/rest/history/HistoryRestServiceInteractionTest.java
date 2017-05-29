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
package org.camunda.bpm.engine.rest.history;

import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.jayway.restassured.http.ContentType;
import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String HISTORY_CLEANUP_ASYNC_URL = TEST_RESOURCE_ROOT_PATH + "/history/cleanup";
  protected static final String FIND_HISTORY_CLEANUP_JOB_URL = TEST_RESOURCE_ROOT_PATH + "/history/find-cleanup-job";

  private HistoryService historyServiceMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);
    Job mockJob = MockProvider.createMockJob();
    when(historyServiceMock.cleanUpHistoryAsync(anyBoolean()))
        .thenReturn(mockJob);
    when(historyServiceMock.findHistoryCleanupJob())
        .thenReturn(mockJob);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
  }

  @Test
  public void testFindHistoryCleanupJob() {
    given().contentType(ContentType.JSON)
        .then()
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(FIND_HISTORY_CLEANUP_JOB_URL);

   verify(historyServiceMock).findHistoryCleanupJob();
  }

  @Test
  public void testFindNoHistoryCleanupJob() {
    when(historyServiceMock.findHistoryCleanupJob())
        .thenReturn(null);

    given().contentType(ContentType.JSON)
        .then()
        .expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .when().get(FIND_HISTORY_CLEANUP_JOB_URL);

   verify(historyServiceMock).findHistoryCleanupJob();
  }

  @Test
  public void testHistoryCleanupImmediatelyDueDefault() {
    given().contentType(ContentType.JSON)
        .then()
        .expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORY_CLEANUP_ASYNC_URL);

    verify(historyServiceMock).cleanUpHistoryAsync(true);
  }

  @Test
  public void testHistoryCleanupImmediatelyDue() {
    given().contentType(ContentType.JSON)
        .queryParam("immediatelyDue", true)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORY_CLEANUP_ASYNC_URL);

    verify(historyServiceMock).cleanUpHistoryAsync(true);
  }

  @Test
  public void testHistoryCleanup() {
    given().contentType(ContentType.JSON).queryParam("immediatelyDue", false)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORY_CLEANUP_ASYNC_URL);

    verify(historyServiceMock).cleanUpHistoryAsync(false);
  }


}

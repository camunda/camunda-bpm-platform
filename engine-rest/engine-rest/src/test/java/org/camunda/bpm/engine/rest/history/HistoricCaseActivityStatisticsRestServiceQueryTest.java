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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricCaseActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatisticsQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.response.Response;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricCaseActivityStatisticsRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_CASE_ACTIVITY_STATISTICS_URL = HISTORY_URL + "/case-definition/{id}/statistics";

  protected static HistoricCaseActivityStatisticsQuery historicCaseActivityStatisticsQuery;

  @BeforeClass
  public static void setUpRuntimeData() {
    List<HistoricCaseActivityStatistics> mocks = MockProvider.createMockHistoricCaseActivityStatistics();

    historicCaseActivityStatisticsQuery = mock(HistoricCaseActivityStatisticsQuery.class);
    when(processEngine.getHistoryService().createHistoricCaseActivityStatisticsQuery(eq(MockProvider.EXAMPLE_CASE_DEFINITION_ID))).thenReturn(historicCaseActivityStatisticsQuery);
    when(historicCaseActivityStatisticsQuery.list()).thenReturn(mocks);
  }

  @Test
  public void testHistoricCaseActivityStatisticsRetrieval() {
    given().pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(HISTORIC_CASE_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testSimpleTaskQuery() {
    Response response = given()
          .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
         .then().expect()
           .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_ACTIVITY_STATISTICS_URL);

    String content = response.asString();
    List<String> result = from(content).getList("");
    Assert.assertEquals(2, result.size());

    Assert.assertNotNull(result.get(0));
    Assert.assertNotNull(result.get(1));

    String id = from(content).getString("[0].id");
    long available = from(content).getLong("[0].available");
    long active = from(content).getLong("[0].active");
    long completed = from(content).getLong("[0].completed");
    long disabled = from(content).getLong("[0].disabled");
    long enabled = from(content).getLong("[0].enabled");
    long terminated = from(content).getLong("[0].terminated");

    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.EXAMPLE_AVAILABLE_LONG, available);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVE_LONG, active);
    Assert.assertEquals(MockProvider.EXAMPLE_COMPLETED_LONG, completed);
    Assert.assertEquals(MockProvider.EXAMPLE_DISABLED_LONG, disabled);
    Assert.assertEquals(MockProvider.EXAMPLE_ENABLED_LONG, enabled);
    Assert.assertEquals(MockProvider.EXAMPLE_TERMINATED_LONG, terminated);

    id = from(content).getString("[1].id");
    available = from(content).getLong("[1].available");
    active = from(content).getLong("[1].active");
    completed = from(content).getLong("[1].completed");
    disabled = from(content).getLong("[1].disabled");
    enabled = from(content).getLong("[1].enabled");
    terminated = from(content).getLong("[1].terminated");

    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_AVAILABLE_LONG, available);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_ACTIVE_LONG, active);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_COMPLETED_LONG, completed);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_DISABLED_LONG, disabled);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_ENABLED_LONG, enabled);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_TERMINATED_LONG, terminated);

  }

}

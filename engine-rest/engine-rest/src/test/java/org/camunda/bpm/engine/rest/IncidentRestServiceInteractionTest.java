package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.IncidentQueryImpl;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.ws.rs.core.Response.Status;


public class IncidentRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String INCIDENT_URL = TEST_RESOURCE_ROOT_PATH + "/incident";
  protected static final String SINGLE_INCIDENT_URL = INCIDENT_URL + "/{id}";

  private RuntimeServiceImpl mockRuntimeService;
  private IncidentQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    List<Incident> incidents = MockProvider.createMockIncidents();

    mockedQuery = setupMockIncidentQuery(incidents);
  }

  private IncidentQuery setupMockIncidentQuery(List<Incident> incidents) {
    IncidentQuery sampleQuery = mock(IncidentQuery.class);

    when(sampleQuery.incidentId(anyString())).thenReturn(sampleQuery);
    when(sampleQuery.singleResult()).thenReturn(mock(Incident.class));

    mockRuntimeService = mock(RuntimeServiceImpl.class);
    when(processEngine.getRuntimeService()).thenReturn(mockRuntimeService);
    when(mockRuntimeService.createIncidentQuery()).thenReturn(sampleQuery);

    return sampleQuery;
  }

  @Test
  public void testGetIncident() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_INCIDENT_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_INCIDENT_URL);

    verify(mockRuntimeService).createIncidentQuery();
    verify(mockedQuery).incidentId(MockProvider.EXAMPLE_INCIDENT_ID);
    verify(mockedQuery).singleResult();
  }

  @Test
  public void testGetUnexistingIncident() {
    when(mockedQuery.singleResult()).thenReturn(null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_INCIDENT_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(SINGLE_INCIDENT_URL);

    verify(mockRuntimeService).createIncidentQuery();
    verify(mockedQuery).incidentId(MockProvider.EXAMPLE_INCIDENT_ID);
    verify(mockedQuery).singleResult();
  }

  @Test
  public void testResolveIncident() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_INCIDENT_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_INCIDENT_URL);

    verify(mockRuntimeService).resolveIncident(MockProvider.EXAMPLE_INCIDENT_ID);
  }

  @Test
  public void testResolveUnexistingIncident() {
    doThrow(new NotFoundException()).when(mockRuntimeService).resolveIncident(anyString());

    given()
      .pathParam("id", MockProvider.EXAMPLE_INCIDENT_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .delete(SINGLE_INCIDENT_URL);

    verify(mockRuntimeService).resolveIncident(MockProvider.EXAMPLE_INCIDENT_ID);
  }
}

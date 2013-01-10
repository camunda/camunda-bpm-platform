package org.camunda.bpm.engine.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.jayway.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.cxf.jaxrs.client.WebClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.specification.RequestSpecification;

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_DEFINITION_KEY = "aKey";
  private static final String EXAMPLE_DEFINITION_ID = "anId";
  
  private static final String PROCESS_DEFINITION_QUERY_URL = "/process-definition/query";
  
  private ProcessDefinitionQuery mockedQuery;
  private WebClient client;
  
  private ProcessDefinitionQuery setUpMockDefinitionQuery(List<ProcessDefinition> mockedDefinitions) {
    ProcessDefinitionQuery sampleDefinitionsQuery = mock(ProcessDefinitionQuery.class);
    when(sampleDefinitionsQuery.list()).thenReturn(mockedDefinitions);
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(sampleDefinitionsQuery);
    when(sampleDefinitionsQuery.processDefinitionKeyLike(any(String.class))).thenReturn(sampleDefinitionsQuery);
    return sampleDefinitionsQuery;
  }
  
  private ProcessDefinition createMockDefinition(String id, String key) {
    ProcessDefinition mockDefinition = mock(ProcessDefinition.class);
    when(mockDefinition.getId()).thenReturn(id);
    when(mockDefinition.getKey()).thenReturn(key);
    return mockDefinition;
  }
  
  @Before
  public void injectMockedQuery() {
    List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(createMockDefinition(EXAMPLE_DEFINITION_ID, EXAMPLE_DEFINITION_KEY));
    mockedQuery = setUpMockDefinitionQuery(definitions);
    
    client = WebClient.create(SERVER_ADDRESS);
    client.accept(MediaType.APPLICATION_JSON);
    client.path(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testProcessDefinitionRetrieval() throws JSONException {
    
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    
    String queryKey = "Key";
    client.query("keyLike", queryKey);
    
    String content = client.get(String.class);

    System.out.println(content);
    
    // assert query invocation
    inOrder.verify(mockedQuery).processDefinitionKeyLike(queryKey);
    inOrder.verify(mockedQuery).list();
    
    Response response = client.getResponse();
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    JSONObject jsonResponse = new JSONObject(content);
    JSONArray jsonDefinitions = jsonResponse.getJSONArray("data");
    Assert.assertEquals("There should be one process definition returned.", 1, jsonDefinitions.length());
    
    JSONObject jsonDefinition = jsonDefinitions.getJSONObject(0);
    String jsonDefId = jsonDefinition.getString("id");
    String jsonDefKey = jsonDefinition.getString("key");
    
    Assert.assertEquals(EXAMPLE_DEFINITION_ID, jsonDefId);
    Assert.assertEquals(EXAMPLE_DEFINITION_KEY, jsonDefKey);
  }
  
  @Test
  public void testEmptyQuery() throws JSONException {
    String queryKey = "";
    client.query("keyLike", queryKey);
    
    Response response = client.get();
    Assert.assertEquals("Querying with an empty query string should be valid.", Status.OK.getStatusCode(), response.getStatus());
  }
  
  @Test
  public void testAdditionalParameters() {

    Map<String, String> queryParameters = getCompleteQueryParameters();
    
    RequestSpecification spec = given();
    for (Entry<String, String> queryParam : queryParameters.entrySet()) {
      spec.queryParam(queryParam.getKey(), queryParam.getValue());
    }
    
    spec.expect().statusCode(Status.OK.getStatusCode()).get("/process-definition/query");
    
    // assert query invocation
    verify(mockedQuery).processDefinitionCategory(queryParameters.get("category"));
    verify(mockedQuery).processDefinitionCategoryLike(queryParameters.get("categoryLike"));
    verify(mockedQuery).processDefinitionName(queryParameters.get("name"));
    verify(mockedQuery).processDefinitionNameLike(queryParameters.get("nameLike"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("key"));
    verify(mockedQuery).processDefinitionKeyLike(queryParameters.get("keyLike"));
    verify(mockedQuery).processDefinitionVersion(Integer.parseInt(queryParameters.get("ver")));
    verify(mockedQuery).latestVersion();
    verify(mockedQuery).processDefinitionResourceName(queryParameters.get("resourceName"));
    verify(mockedQuery).processDefinitionResourceNameLike(queryParameters.get("resourceNameLike"));
    verify(mockedQuery).startableByUser(queryParameters.get("startableBy"));
    verify(mockedQuery).active();
    verify(mockedQuery).suspended();
    verify(mockedQuery).list();
  }
  
  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    
    parameters.put("category", "cat");
    parameters.put("categoryLike", "catlike");
    parameters.put("name", "name");
    parameters.put("nameLike", "namelike");
    parameters.put("deploymentId", "depId");
    parameters.put("key", "key");
    parameters.put("keyLike", "keylike");
    parameters.put("ver", "0");
    parameters.put("latest", "true");
    parameters.put("resourceName", "res");
    parameters.put("resourceNameLike", "resLike");
    parameters.put("startableBy", "kermit");
    parameters.put("suspended", "true");
    parameters.put("active", "true");
    
    return parameters;
  }
  
  
}

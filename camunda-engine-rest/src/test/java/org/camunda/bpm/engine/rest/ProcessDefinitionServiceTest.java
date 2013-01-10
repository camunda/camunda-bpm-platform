package org.camunda.bpm.engine.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_DEFINITION_KEY = "aKey";
  private static final String EXAMPLE_DEFINITION_ID = "anId";
  
  private ProcessDefinitionQuery mockedQuery;
  
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
  }
  
  @Test
  public void testProcessDefinitionRetrieval() throws JSONException {
    
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    
    String queryKey = "Key";
    WebClient client = WebClient.create(SERVER_ADDRESS);
    client.accept(MediaType.APPLICATION_JSON);
    client.path("/process-definition/query");
    client.query("key", queryKey);
    
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
    WebClient client = WebClient.create(SERVER_ADDRESS);
    client.accept(MediaType.APPLICATION_JSON);
    client.path("/process-definition/query");
    client.query("key", queryKey);
    
    Response response = client.get();
    Assert.assertEquals("Querying with an empty query string should be valid.", Status.OK.getStatusCode(), response.getStatus());
  }
  
  /**
   * Test the behavior when not setting the "pid" parameter at all.
   */
  @Test
  public void testNonExistingQueryParameters() {
    WebClient client = WebClient.create(SERVER_ADDRESS);
    client.accept(MediaType.APPLICATION_JSON);
    client.path("/process-definition/query");
    
    Response response = client.get();
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }
  
  @Test
  public void testAdditionalParameters() {
  }
  
  
}

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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {
  
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
  
  @Test
  public void testProcessDefinitionRetrieval() {
    List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(createMockDefinition("anId", "aKey"));
    ProcessDefinitionQuery mockedQuery = setUpMockDefinitionQuery(definitions);
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    
    String queryKey = "Key";
    WebClient client = WebClient.create(SERVER_ADDRESS);
    client.accept(MediaType.APPLICATION_JSON);
    client.path("/process-definition/query");
    client.query("pid", queryKey);
    
    String content = client.get(String.class);
    System.out.println(content);
    
    Response response = client.getResponse();
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    
    inOrder.verify(mockedQuery).processDefinitionKeyLike(queryKey);
    inOrder.verify(mockedQuery).list();
    
    // TODO assert response content
  }
  
}

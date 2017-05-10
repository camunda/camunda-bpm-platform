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
package org.camunda.bpm.engine.rest.standalone;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractEmptyBodyFilterTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TEST_RESOURCE_ROOT_PATH = "/rest-test/rest";
  protected static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_URL = PROCESS_DEFINITION_URL + "/key/" + MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY;
  protected static final String START_PROCESS_INSTANCE_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/start";

  protected ProcessInstantiationBuilder mockInstantiationBuilder;
  protected RuntimeService runtimeServiceMock;

  protected ApacheHttpClient4 client;
  protected final String BASE_URL = "http://localhost:38080";

  @Before
  public void setUpRuntimeData() {
    setUpApacheHTTPClient();

    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();

    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    mockInstantiationBuilder = mock(ProcessInstantiationBuilder.class);
    when(mockInstantiationBuilder.setVariables(any(Map.class))).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.businessKey(anyString())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.caseInstanceId(anyString())).thenReturn(mockInstantiationBuilder);
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    ProcessInstanceWithVariables resultInstanceWithVariables = MockProvider.createMockInstanceWithVariables();
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean())).thenReturn(resultInstanceWithVariables);

    ProcessDefinitionQuery processDefinitionQueryMock = mock(ProcessDefinitionQuery.class);
    when(processDefinitionQueryMock.processDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.withoutTenantId()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.latestVersion()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.singleResult()).thenReturn(mockDefinition);

    RepositoryService repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.createProcessDefinitionQuery()).thenReturn(processDefinitionQueryMock);
  }

  private void setUpApacheHTTPClient() {
    DefaultApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

    DefaultHttpClient defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    HttpParams params = defaultHttpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 3 * 60 * 1000);
    HttpConnectionParams.setSoTimeout(params, 10 * 60 * 1000);
  }

  @After
  public void destroyApacheHttpClient() {
    client.destroy();
  }

  @Test
  public void testBodyIsEmpty() throws JSONException {
    ClientResponse response = client.resource(BASE_URL + START_PROCESS_INSTANCE_BY_KEY_URL)
      .accept(MediaType.APPLICATION_JSON)
      .entity("", MediaType.APPLICATION_JSON)
      .post(ClientResponse.class);

    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, response.getEntity(JSONObject.class).get("id"));
    response.close();
  }

  @Test
  public void testBodyIsNull() throws JSONException {
    ClientResponse response = client.resource(BASE_URL + START_PROCESS_INSTANCE_BY_KEY_URL)
      .accept(MediaType.APPLICATION_JSON)
      .entity(null, MediaType.APPLICATION_JSON)
      .post(ClientResponse.class);

    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, response.getEntity(JSONObject.class).get("id"));
    response.close();
  }

  @Test
  public void testBodyIsNullAndMediaTypeIsNull() throws JSONException {
    ClientResponse response = client.resource(BASE_URL + START_PROCESS_INSTANCE_BY_KEY_URL)
      .entity(null, (MediaType) null)
      .post(ClientResponse.class);

    assertEquals(415, response.getStatus());
    response.close();
  }

  @Test
  public void testBodyIsNullAndMediaTypeHasISOCharset() throws JSONException {
    ClientResponse response = client.resource(BASE_URL + START_PROCESS_INSTANCE_BY_KEY_URL)
      .accept(MediaType.APPLICATION_JSON)
      .entity(null, ContentType.create(MediaType.APPLICATION_JSON, "iso-8859-1").toString())
      .post(ClientResponse.class);

    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, response.getEntity(JSONObject.class).get("id"));
    response.close();
  }

  @Test
  public void testBodyIsEmptyJSONObject() throws JSONException {
    ClientResponse response = client.resource(BASE_URL + START_PROCESS_INSTANCE_BY_KEY_URL)
      .accept(MediaType.APPLICATION_JSON)
      .entity(EMPTY_JSON_OBJECT, POST_JSON_CONTENT_TYPE)
      .post(ClientResponse.class);

    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, response.getEntity(JSONObject.class).get("id"));
    response.close();
  }

}

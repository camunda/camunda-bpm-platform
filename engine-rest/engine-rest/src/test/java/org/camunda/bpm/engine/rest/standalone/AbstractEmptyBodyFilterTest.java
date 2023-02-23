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
package org.camunda.bpm.engine.rest.standalone;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractEmptyBodyFilterTest extends AbstractRestServiceTest {

  protected static final String TEST_RESOURCE_ROOT_PATH = "/rest-test/rest";
  protected static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_URL = PROCESS_DEFINITION_URL + "/key/" + MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY;
  protected static final String START_PROCESS_INSTANCE_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/start";

  protected ProcessInstantiationBuilder mockInstantiationBuilder;
  protected RuntimeService runtimeServiceMock;

  protected CloseableHttpClient client;
  protected RequestConfig reqConfig;

  @Before
  public void setUpHttpClientAndRuntimeData() {
    client = HttpClients.createDefault();
    reqConfig = RequestConfig.custom().setConnectTimeout(3 * 60 * 1000).setSocketTimeout(10 * 60 * 1000).build();

    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();

    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    mockInstantiationBuilder = mock(ProcessInstantiationBuilder.class);
    when(mockInstantiationBuilder.setVariables(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.businessKey(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.caseInstanceId(any())).thenReturn(mockInstantiationBuilder);
    when(runtimeServiceMock.createProcessInstanceById(any())).thenReturn(mockInstantiationBuilder);

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

  @After
  public void tearDown() throws IOException {
    client.close();
  }

  @Test
  public void testBodyIsEmpty() throws IOException {
    evaluatePostRequest(new ByteArrayEntity("".getBytes("UTF-8")), ContentType.create(MediaType.APPLICATION_JSON).toString(), 200, true);
  }

  @Test
  public void testBodyIsNull() throws IOException {
    evaluatePostRequest(null, ContentType.create(MediaType.APPLICATION_JSON).toString(), 200, true);
  }

  @Test
  public void testBodyIsNullAndContentTypeIsNull() throws IOException {
    evaluatePostRequest(null, null, 415, false);
  }

  @Test
  public void testBodyIsNullAndContentTypeHasISOCharset() throws IOException {
    evaluatePostRequest(null, ContentType.create(MediaType.APPLICATION_JSON, "iso-8859-1").toString(), 200, true);
  }

  @Test
  public void testBodyIsEmptyJSONObject() throws IOException {
    evaluatePostRequest(new ByteArrayEntity(EMPTY_JSON_OBJECT.getBytes("UTF-8")), ContentType.create(MediaType.APPLICATION_JSON).toString(), 200, true);
  }

  private void evaluatePostRequest(HttpEntity reqBody, String reqContentType, int expectedStatusCode, boolean assertResponseBody) throws IOException {
    HttpPost post = new HttpPost("http://localhost:" + PORT + START_PROCESS_INSTANCE_BY_KEY_URL);
    post.setConfig(reqConfig);

    if(reqContentType != null) {
      post.setHeader(HttpHeaders.CONTENT_TYPE, reqContentType);
    }

    post.setEntity(reqBody);

    CloseableHttpResponse response = client.execute(post);

    assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());

    if(assertResponseBody) {
      assertThat(EntityUtils.toString(response.getEntity(), "UTF-8"), containsString(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    }

    response.close();
  }

}

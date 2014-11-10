/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.integrationtest.functional.connect;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpBaseRequest;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.impl.HttpConnectorImpl;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.connect.httpclient.soap.SoapHttpRequest;
import org.camunda.connect.httpclient.soap.impl.SoapHttpConnectorImpl;
import org.camunda.connect.impl.DebugRequestInterceptor;
import org.camunda.connect.spi.Connector;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import connectjar.org.apache.http.HttpVersion;
import connectjar.org.apache.http.client.methods.CloseableHttpResponse;
import connectjar.org.apache.http.message.BasicHttpResponse;

/**
 * <p>Smoketest Make sure camunda connect can be used in a process application </p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class PaConnectSupportTest extends AbstractFoxPlatformIntegrationTest {

  public static final String METHOD = "POST";
  public static final String URL = "http://camunda.org";
  public static final String CONTENT_TYPE = "text/plain";
  public static final String SOAP_ACTION = "doIt";
  public static final String PAYLOAD = "Hello world!";

  protected DebugRequestInterceptor interceptor;

  @Deployment
  public static Archive<?> createDeployment() {
    WebArchive archive = initWebArchiveDeployment()
      .addAsResource("org/camunda/bpm/integrationtest/functional/connect/PaConnectSupportTest.connectorServiceTask.bpmn20.xml");

    return processArchiveDeployment(archive);
  }

  @Before
  public void createInterceptor() {
    interceptor = new DebugRequestInterceptor(new TestResponse());
  }

  @Test
  public void httpConnectorShouldBeAvailable() {
    HttpConnector httpConnector = new HttpConnectorImpl();

    httpConnector
      .addRequestInterceptor(interceptor)
      .createRequest()
      .url(URL)
      .contentType(CONTENT_TYPE)
      .payload(PAYLOAD)
      .post()
      .execute();

    verifyRequest(false);
  }

  @Test
  public void soapConnectorShouldBeAvailable() {
    SoapHttpConnector soapHttpConnector = new SoapHttpConnectorImpl();

    soapHttpConnector
      .addRequestInterceptor(interceptor)
      .createRequest()
      .url(URL)
      .contentType(CONTENT_TYPE)
      .soapAction(SOAP_ACTION)
      .payload(PAYLOAD)
      .execute();

    verifyRequest(true);
  }

  @Test
  public void connectorServiceTask() {
    Connector connector = Connectors.getConnector(SoapHttpConnector.ID);
    connector.addRequestInterceptor(interceptor);

    runtimeService.startProcessInstanceByKey("testProcess");
    verifyRequest(true);

    // remove interceptor
    connector.getRequestInterceptors().clear();
  }

  protected void verifyRequest(boolean isSoapRequest) {
    HttpBaseRequest request = interceptor.getRequest();
    assertEquals(METHOD, request.getMethod());
    assertEquals(URL, request.getUrl());
    assertEquals(CONTENT_TYPE, request.getContentType());
    assertEquals(PAYLOAD, request.getPayload());

    if (isSoapRequest) {
      SoapHttpRequest soap = (SoapHttpRequest) request;
      assertEquals(SOAP_ACTION, soap.getSoapAction());
    }
  }

  class TestResponse extends BasicHttpResponse implements CloseableHttpResponse {

    public TestResponse() {
      super(HttpVersion.HTTP_1_1, 200, "OK");
    }

    public void close() throws IOException {

    }

  }

}

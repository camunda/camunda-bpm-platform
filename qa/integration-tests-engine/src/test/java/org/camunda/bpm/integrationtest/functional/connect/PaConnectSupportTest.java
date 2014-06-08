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

import java.io.ByteArrayInputStream;

import org.camunda.bpm.connect.interceptor.ConnectorInvocation;
import org.camunda.bpm.connect.interceptor.RequestInterceptor;
import org.camunda.bpm.connect.soap.httpclient.SoapHttpConnector;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Smoketest Make sure camunda connect can be used in a process application </p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class PaConnectSupportTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment();
  }

  @Test
  public void connectShouldBeAvailable() {
    SoapHttpConnector soapHttpConnector = new SoapHttpConnector();

    soapHttpConnector.addRequestInterceptor(new RequestInterceptor() {

      public Object handleInvocation(ConnectorInvocation invocation) throws Exception {
        // by not calling invocation.proceed(), here, we make sure the connector does not actually execute the request
        // nevertheless it will use the internal apache httpclient api for creating a post request etc.
        return null;
      }

    });

    soapHttpConnector.createRequest()
      .endpointUrl("http://foo")
      .soapAction("bar")
      .soapEnvelope(new ByteArrayInputStream("foo".getBytes()))
      .execute();
  }

}

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
package org.camunda.bpm.run.qa.webapps;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.camunda.bpm.TestProperties;
import org.camunda.bpm.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.util.logging.Logger;

/**
 * NOTE: copied from
 * <a href="https://github.com/camunda/camunda-bpm-platform/blob/master/qa/integration-tests-webapps/integration-tests/src/test/java/org/camunda/bpm/AbstractWebIntegrationTest.java">platform</a>,
 * might be removed with https://jira.camunda.com/browse/CAM-11379
 */
public abstract class AbstractWebIT {

  private final static Logger LOGGER = Logger.getLogger(AbstractWebIT.class.getName());

  protected String TASKLIST_PATH = "app/tasklist/default/";
  public static final String HOST_NAME = "localhost";
  public String APP_BASE_PATH;

  protected String appUrl;
  protected TestUtil testUtil;
  protected TestProperties testProperties;

  protected static ChromeDriverService service;

  public ApacheHttpClient4 client;
  public DefaultHttpClient defaultHttpClient;
  public String httpPort;

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    testUtil = new TestUtil(testProperties);
  }

  @After
  public void destroyClient() {
    client.destroy();
  }

  public void createClient(String ctxPath) throws Exception {
    testProperties = new TestProperties();

    APP_BASE_PATH = testProperties.getApplicationPath("/" + ctxPath);
    LOGGER.info("Connecting to application "+APP_BASE_PATH);

    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    HttpParams params = defaultHttpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 3 * 60 * 1000);
    HttpConnectionParams.setSoTimeout(params, 10 * 60 * 1000);
  }

  public void preventRaceConditions() throws InterruptedException {
    // just wait some seconds before starting because of Wildfly / Cargo race conditions
    Thread.sleep(5 * 1000);
  }

  protected String getWebappCtxPath() {
    return testProperties.getStringProperty("http.ctx-path.webapp", null);
  }
}

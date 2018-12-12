/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.util.logging.Logger;

/**
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AbstractWebappIntegrationTest {

  private final static Logger LOGGER = Logger.getLogger(AbstractWebappIntegrationTest.class.getName());

  public static final String HOST_NAME = "localhost";
  public String httpPort;
  public String APP_BASE_PATH;

  public ApacheHttpClient4 client;
  public DefaultHttpClient defaultHttpClient;

  protected TestProperties testProperties;
  protected static ChromeDriverService service;

  @Before
  public void createClient() throws Exception {
    testProperties = new TestProperties();

    String applicationContextPath = getApplicationContextPath();
    APP_BASE_PATH = testProperties.getApplicationPath("/" + applicationContextPath);
    LOGGER.info("Connecting to application "+APP_BASE_PATH);

    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    HttpParams params = defaultHttpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 3 * 60 * 1000);
    HttpConnectionParams.setSoTimeout(params, 10 * 60 * 1000);
  }

  @After
  public void destroyClient() {
    client.destroy();
  }

  /**
   * <p>subclasses must override this method and provide the local context path of the application they are testing.</p>
   *
   * <p>Example: <code>cycle/</code></p>
   */
  protected abstract String getApplicationContextPath();

}

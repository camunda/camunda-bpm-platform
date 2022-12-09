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
package org.camunda.bpm.webapp.impl.security.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.impl.DefaultCockpitRuntimeDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;
import org.camunda.bpm.webapp.impl.security.filter.util.FilterRules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Parameterized.class)
public class SecurityFilterRulesTest {

  public static final String FILTER_RULES_FILE = "src/main/webapp/WEB-INF/securityFilterRules.json";

  protected static final String EMPTY_PATH = "";
  protected static final String CUSTOM_APP_PATH = "/my-custom/application/path";

  public static List<SecurityFilterRule> FILTER_RULES;

  public static Authentication NO_AUTHENTICATION = null;
  public static Authentication LOGGED_IN_USER = new Authentication("user", "default");

  public static final String TESTUSER_ID = "testuser";

  protected String applicationPath;

  @Parameterized.Parameters
  public static Collection<String> data() {
    return Arrays.asList(EMPTY_PATH, CUSTOM_APP_PATH);
  }

  public SecurityFilterRulesTest(String applicationPath) throws IOException {
    this.applicationPath = applicationPath;
    FILTER_RULES = loadFilterRules(applicationPath);
  }

  @Before
  public void createEngine()
  {
    final ProcessEngine engine = Mockito.mock(ProcessEngine.class);

    Cockpit.setCockpitRuntimeDelegate(new DefaultCockpitRuntimeDelegate() {

      @Override
      public ProcessEngine getProcessEngine(String processEngineName) {
        if ("default".equals(processEngineName)) {
          return engine;
        }
        else {
          return null;
        }
      }
    });
  }

  @After
  public void after() {
    Authentications.setCurrent(null);
    Cockpit.setCockpitRuntimeDelegate(null);
  }

  @Test
  public void shouldHaveRulesLoaded() throws Exception {
    assertThat(FILTER_RULES).hasSize(1);
  }


  @Test
  public void shouldPassPasswordPolicy() {
    assertThat(isAuthorized("GET",
      applicationPath + "/api/engine/engine/default/identity/password-policy")).isTrue();
    assertThat(isAuthorized("POST",
      applicationPath + "/api/engine/engine/default/identity/password-policy")).isTrue();
  }

  @Test
  public void shouldPassStaticCockpitPluginResources_GET() throws Exception {
    assertThat(isAuthorized("GET",
      applicationPath + "/api/cockpit/plugin/some-plugin/static/foo.html")).isTrue();
    assertThat(isAuthorized("GET",
      applicationPath + "/api/cockpit/plugin/bar/static/foo.html")).isTrue();
  }

  @Test
  public void shouldRejectEngineApi_GET() throws Exception {

    authenticatedForEngine("otherEngine", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("POST", applicationPath + "/api/engine/engine/default/bar");

        assertThat(authorization.isGranted()).isFalse();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldGrantEngineApi_GET() throws Exception {

    authenticatedForEngine("default", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("POST", applicationPath + "/api/engine/engine/default/bar");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }

  @Test
  public void shouldRejectCockpitPluginApi_GET() throws Exception {

    authenticatedForEngine("otherEngine", new Runnable() {
      @Override
      public void run() {

        Authorization authorization = getAuthorization("POST",
          applicationPath + "/api/cockpit/plugin/" +
            "reporting-process-count/default/process-instance-count");

        assertThat(authorization.isGranted()).isFalse();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassCockpitPluginApi_GET_LOGGED_IN() throws Exception {
    authenticatedForEngine("default", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("POST",
            applicationPath + "/api/cockpit/plugin/" +
              "reporting-process-count/default/process-instance-count");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }

  @Test
  public void shouldPassCockpit_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/app/cockpit/non-existing-engine/foo");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassCockpit_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "cockpit", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/app/cockpit/default/");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }

  @Test
  public void shouldPassCockpitNonExistingEngine_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "cockpit", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/app/cockpit/non-existing-engine/");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }


  @Test
  public void shouldRejectTasklistApi_GET() throws Exception {

    authenticatedForEngine("otherEngine", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("POST",
            applicationPath + "/api/tasklist/plugin/example-plugin/default/example-resource");

        assertThat(authorization.isGranted()).isFalse();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassTasklistApi_GET_LOGGED_IN() throws Exception {
    authenticatedForEngine("default", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("POST",
            applicationPath + "/api/tasklist/plugin/example-plugin/default/example-resource");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }

  @Test
  public void shouldRejectTasklistApi_GET_LOGGED_OUT() throws Exception
  {
    Authorization authorization =
      getAuthorization("POST",
        applicationPath + "/api/tasklist/plugin/example-plugin/default/example-resource");

    assertThat(authorization.isGranted()).isFalse();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassTasklistPluginResource_GET_LOGGED_IN() throws Exception {

    authenticatedForEngine("default", new Runnable() {
      @Override
      public void run() {

        Authorization authorization =
          getAuthorization("GET",
            applicationPath + "/api/tasklist/plugin/example-plugin/static/example-resource");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassTasklistPluginResource_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET",
        applicationPath + "/api/tasklist/plugin/example-plugin/static/example-resource");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }


  @Test
  public void shouldPassTasklist_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/app/tasklist/non-existing-engine");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassTasklist_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "tasklist", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/app/tasklist/default/");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }

  @Test
  public void shouldRejectAdminApi_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/api/admin/auth/user/some-engine/");

    assertThat(authorization.isGranted()).isFalse();
    assertThat(authorization.isAuthenticated()).isFalse();

    authorization =
      getAuthorization("GET", applicationPath + "/api/admin/setup/some-engine/");

    assertThat(authorization.isGranted()).isFalse();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassAdminApi_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "admin", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/api/admin/foo/");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassAdminApi_AnonymousEndpoints_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/api/admin/auth/user/bar");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();

    authorization =
      getAuthorization("POST", applicationPath + "/api/admin/auth/user/bar/logout");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();

    authorization =
      getAuthorization("POST", applicationPath + "/api/admin/auth/user/bar/login/some-app");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();

    authorization =
      getAuthorization("POST", applicationPath + "/api/admin/setup/some-engine/user/create");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }


  @Test
  public void shouldRejectAdminApiPlugin_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET",
        applicationPath + "/api/admin/plugin/adminPlugins/some-engine/endpoint");

    assertThat(authorization.isGranted()).isFalse();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassAdminApiPlugin_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "admin", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET",
            applicationPath + "/api/admin/plugin/adminPlugins/some-engine");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassAdmin_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/app/admin/default");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassAdmin_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "admin", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/app/admin/default/");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isTrue();
      }
    });
  }


  @Test
  public void shouldPassAdminResources_GET_LOGGED_OUT() throws Exception {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/app/admin/scripts");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  @Test
  public void shouldPassAdminResources_GET_LOGGED_IN() throws Exception {

    authenticatedForApp("default", "admin", new Runnable() {

      @Override
      public void run() {
        Authorization authorization =
          getAuthorization("GET", applicationPath + "/app/admin/scripts");

        assertThat(authorization.isGranted()).isTrue();
        assertThat(authorization.isAuthenticated()).isFalse();
      }
    });
  }

  @Test
  public void shouldPassAdminLicenseCheck_GET_LOGGED_OUT() {

    Authorization authorization =
      getAuthorization("GET", applicationPath + "/api/admin/plugin/license/default/check-key");

    assertThat(authorization.isGranted()).isTrue();
    assertThat(authorization.isAuthenticated()).isFalse();
  }

  protected Authorization getAuthorization(String method, String uri) {
    return FilterRules.authorize(method, uri, FILTER_RULES);
  }

  protected boolean isAuthorized(String method, String uri) {
    return getAuthorization(method, uri).isGranted();
  }

  private static List<SecurityFilterRule> loadFilterRules(String appPath) throws IOException {
    InputStream is = null;

    try {
      is = new FileInputStream(FILTER_RULES_FILE);
      return FilterRules.load(is, appPath);
    } finally {
      IoUtil.closeSilently(is);
    }
  }

  private void authenticatedForEngine(String engineName, Runnable codeBlock) {
    UserAuthentication engineAuth = new UserAuthentication(LOGGED_IN_USER.getIdentityId(), engineName);

    Authentications authentications = new Authentications();
    authentications.addOrReplace(engineAuth);

    Authentications.setCurrent(authentications);

    try {
      codeBlock.run();
    } finally {
      Authentications.clearCurrent();
    }
  }

  private void authenticatedForApp(String engineName, String appName, Runnable codeBlock) {
    HashSet<String> authorizedApps = new HashSet<>(Arrays.asList(appName));

    UserAuthentication engineAuth = new UserAuthentication(LOGGED_IN_USER.getIdentityId(), engineName);
    engineAuth.setGroupIds(Collections.<String> emptyList());
    engineAuth.setTenantIds(Collections.<String> emptyList());
    engineAuth.setAuthorizedApps(authorizedApps);

    Authentications authentications = new Authentications();
    authentications.addOrReplace(engineAuth);

    Authentications.setCurrent(authentications);

    try {
      codeBlock.run();
    } finally {
      Authentications.clearCurrent();
    }
  }
}

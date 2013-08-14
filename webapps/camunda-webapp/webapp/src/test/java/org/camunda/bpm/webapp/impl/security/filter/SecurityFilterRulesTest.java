package org.camunda.bpm.webapp.impl.security.filter;

import java.io.InputStream;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.webapp.impl.security.filter.util.FilterRules;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.junit.After;
import org.junit.BeforeClass;

/**
 *
 * @author nico.rehwaldt
 */
public class SecurityFilterRulesTest {

  public static final String FILTER_RULES_FILE = "src/main/webapp/WEB-INF/securityFilterRules.json";

  public static List<SecurityFilterRule> FILTER_RULES;

  public static Authentication NO_AUTHENTICATION = null;
  public static Authentication LOGGED_IN_USER = new Authentication("user", "default");

  @BeforeClass
  public static void beforeClass() throws Exception {
    FILTER_RULES = loadFilterRules();
  }

  @After
  public void after() {
    Authentications.setCurrent(null);
  }

  @Test
  public void shouldHaveRulesLoaded() throws Exception {
    assertThat(FILTER_RULES).hasSize(1);
  }

  @Test
  public void shouldPassStaticPluginResources_GET() throws Exception {
    assertThat(isAuthorized("GET", "/camunda/api/cockpit/plugin/some-plugin/static/foo.html")).isTrue();
    assertThat(isAuthorized("GET", "/camunda/api/cockpit/plugin/bar/static/foo.html")).isTrue();
  }

  @Test
  public void shouldRejectDynamicPluginResources_GET() throws Exception {

    authenticatedForEngine("otherEngine", new Runnable() {
      @Override
      public void run() {
        assertThat(isAuthorized("GET", "/camunda/api/cockpit/plugin/reporting-process-count/default/process-instance-count")).isFalse();
      }
    });
  }

  @Test
  public void shouldPassDynamicPluginResources_GET_LOGGED_IN() throws Exception {
    authenticatedForEngine("default", new Runnable() {
      @Override
      public void run() {
        assertThat(isAuthorized("POST", "/camunda/api/cockpit/plugin/reporting-process-count/default/process-instance-count")).isTrue();
      }
    });
  }

  protected boolean isAuthorized(String method, String uri) {

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(method);
    when(request.getContextPath()).thenReturn("/camunda");
    when(request.getRequestURI()).thenReturn(uri);

    return FilterRules.isAuthorized(request, FILTER_RULES);
  }

  private static List<SecurityFilterRule> loadFilterRules() throws FileNotFoundException, IOException {
    InputStream is = null;

    try {
      is = new FileInputStream(FILTER_RULES_FILE);
      return FilterRules.load(is);
    } finally {
      IoUtil.closeSilently(is);
    }
  }

  private void authenticatedForEngine(String engineName, Runnable codeBlock) {
    Authentication engineAuth = new Authentication("testuser", engineName);

    Authentications authentications = new Authentications();
    authentications.addAuthentication(engineAuth);

    Authentications.setCurrent(authentications);

    try {
      codeBlock.run();
    } finally {
      Authentications.clearCurrent();
    }
  }
}

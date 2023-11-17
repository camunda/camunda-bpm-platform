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
package org.camunda.bpm.spring.boot.starter.webapp;

import static java.util.Collections.singletonMap;
import static org.glassfish.jersey.servlet.ServletProperties.JAXRS_APPLICATION_CLASS;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionTrackingMode;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import org.camunda.bpm.admin.impl.web.AdminApplication;
import org.camunda.bpm.admin.impl.web.bootstrap.AdminContainerBootstrap;
import org.camunda.bpm.cockpit.impl.web.CockpitApplication;
import org.camunda.bpm.cockpit.impl.web.bootstrap.CockpitContainerBootstrap;
import org.camunda.bpm.engine.rest.filter.CacheControlFilter;
import org.camunda.bpm.engine.rest.filter.EmptyBodyFilter;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.camunda.bpm.spring.boot.starter.webapp.filter.AppendTrailingSlashFilter;
import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyProcessEnginesFilter;
import org.camunda.bpm.spring.boot.starter.webapp.filter.LazySecurityFilter;
import org.camunda.bpm.tasklist.impl.web.TasklistApplication;
import org.camunda.bpm.tasklist.impl.web.bootstrap.TasklistContainerBootstrap;
import org.camunda.bpm.webapp.impl.engine.EngineRestApplication;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.filter.CsrfPreventionFilter;
import org.camunda.bpm.webapp.impl.security.filter.SessionCookieFilter;
import org.camunda.bpm.webapp.impl.security.filter.headersec.HttpHeaderSecurityFilter;
import org.camunda.bpm.webapp.impl.security.filter.util.HttpSessionMutexListener;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;
import org.camunda.bpm.welcome.impl.web.WelcomeApplication;
import org.camunda.bpm.welcome.impl.web.bootstrap.WelcomeContainerBootstrap;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * Inspired by:
 * https://groups.google.com/forum/#!msg/camunda-bpm-users/BQHdcLIivzs
 * /iNVix8GkhYAJ (Christoph Berg)
 */
public class CamundaBpmWebappInitializer implements ServletContextInitializer {

  private static final Logger log = LoggerFactory.getLogger(CamundaBpmWebappInitializer.class);

  private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST);

  private ServletContext servletContext;

  private final CamundaBpmProperties properties;

  public CamundaBpmWebappInitializer(CamundaBpmProperties properties) {
    this.properties = properties;
  }

  @Override
  public void onStartup(ServletContext servletContext) {
    this.servletContext = servletContext;

    servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));

    servletContext.addListener(new CockpitContainerBootstrap());
    servletContext.addListener(new AdminContainerBootstrap());
    servletContext.addListener(new TasklistContainerBootstrap());
    servletContext.addListener(new WelcomeContainerBootstrap());
    servletContext.addListener(new HttpSessionMutexListener());

    WebappProperty webapp = properties.getWebapp();
    String applicationPath = webapp.getApplicationPath();

    ServletContextUtil.setAppPath(applicationPath, servletContext);

    // make sure that trailing slashes are added for the registered patterns
    // see AppendTrailingSlashFilter for details
    registerFilter("AppendTrailingSlashFilter", AppendTrailingSlashFilter.class,
        applicationPath + "/app",
        applicationPath + "/app/cockpit",
        applicationPath + "/app/admin",
        applicationPath + "/app/tasklist",
        applicationPath + "/app/welcome");
    registerFilter("Authentication Filter", AuthenticationFilter.class,
        Collections.singletonMap("cacheTimeToLive", getAuthCacheTTL(webapp)),
        applicationPath + "/api/*", applicationPath + "/app/*");
    registerFilter("Security Filter", LazySecurityFilter.class,
        singletonMap("configFile", webapp.getSecurityConfigFile()),
        applicationPath + "/api/*", applicationPath + "/app/*");
    registerFilter("CsrfPreventionFilter", CsrfPreventionFilter.class,
        webapp.getCsrf().getInitParams(),
        applicationPath + "/api/*", applicationPath + "/app/*");
    registerFilter("SessionCookieFilter", SessionCookieFilter.class,
        webapp.getSessionCookie().getInitParams(),
        applicationPath + "/api/*", applicationPath + "/app/*");

    Map<String, String> headerSecurityProperties = webapp
      .getHeaderSecurity()
      .getInitParams();

    registerFilter("HttpHeaderSecurity", HttpHeaderSecurityFilter.class,
        headerSecurityProperties,
        applicationPath + "/api/*", applicationPath + "/app/*");

    registerFilter("Engines Filter", LazyProcessEnginesFilter.class,
        applicationPath + "/api/*",
                   applicationPath + "/app/*",
                   applicationPath + "/",
                   applicationPath);

    registerFilter("EmptyBodyFilter", EmptyBodyFilter.class,
        applicationPath + "/api/*", applicationPath + "/app/*");

    registerFilter("CacheControlFilter", CacheControlFilter.class,
        applicationPath + "/api/*", applicationPath + "/app/*", applicationPath + "/assets/*");

    registerServlet("Cockpit Api", CockpitApplication.class,
        applicationPath + "/api/cockpit/*");
    registerServlet("Admin Api", AdminApplication.class,
        applicationPath + "/api/admin/*");
    registerServlet("Tasklist Api", TasklistApplication.class,
        applicationPath + "/api/tasklist/*");
    registerServlet("Engine Api", EngineRestApplication.class,
        applicationPath + "/api/engine/*");
    registerServlet("Welcome Api", WelcomeApplication.class,
        applicationPath + "/api/welcome/*");
  }

  protected String getAuthCacheTTL(WebappProperty webapp) {
    long authCacheTTL = webapp.getAuth().getCache().getTimeToLive();
    boolean authCacheTTLEnabled = webapp.getAuth().getCache().isTtlEnabled();
    if (authCacheTTLEnabled) {
      return Long.toString(authCacheTTL);

    } else {
      return ""; // Empty string disables TTL

    }
  }

  private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final String... urlPatterns) {
    return registerFilter(filterName, filterClass, null, urlPatterns);
  }

  private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final Map<String, String> initParameters,
                                            final String... urlPatterns) {
    FilterRegistration filterRegistration = servletContext.getFilterRegistration(filterName);

    if (filterRegistration == null) {
      filterRegistration = servletContext.addFilter(filterName, filterClass);
      filterRegistration.addMappingForUrlPatterns(DISPATCHER_TYPES, true, urlPatterns);

      if (initParameters != null) {
        filterRegistration.setInitParameters(initParameters);
      }

      log.debug("Filter {} for URL {} registered.", filterName, urlPatterns);
    }

    return filterRegistration;
  }

  private ServletRegistration registerServlet(final String servletName, final Class<?> applicationClass, final String... urlPatterns) {
    ServletRegistration servletRegistration = servletContext.getServletRegistration(servletName);

    if (servletRegistration == null) {
      servletRegistration = servletContext.addServlet(servletName, ServletContainer.class);
      servletRegistration.addMapping(urlPatterns);
      servletRegistration.setInitParameters(singletonMap(JAXRS_APPLICATION_CLASS, applicationClass.getName()));

      log.debug("Servlet {} for URL {} registered.", servletName, urlPatterns);
    }

    return servletRegistration;
  }
}

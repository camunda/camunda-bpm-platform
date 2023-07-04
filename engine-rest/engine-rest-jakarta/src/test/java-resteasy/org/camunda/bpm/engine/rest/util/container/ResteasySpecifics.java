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
package org.camunda.bpm.engine.rest.util.container;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import jakarta.servlet.DispatcherType;
import jakarta.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.rest.CustomJacksonDateFormatTest;
import org.camunda.bpm.engine.rest.ExceptionHandlerTest;
import org.camunda.bpm.engine.rest.application.TestCustomResourceApplication;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.engine.rest.standalone.NoServletAuthenticationFilterTest;
import org.camunda.bpm.engine.rest.standalone.NoServletEmptyBodyFilterTest;
import org.camunda.bpm.engine.rest.standalone.ServletAuthenticationFilterTest;
import org.camunda.bpm.engine.rest.standalone.ServletEmptyBodyFilterTest;
import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Thorben Lindhauer
 */
public class ResteasySpecifics implements ContainerSpecifics {

  protected static final TestRuleFactory DEFAULT_RULE_FACTORY = new EmbeddedServerRuleFactory(new JaxrsApplication());

  protected static final Map<Class<?>, TestRuleFactory> TEST_RULE_FACTORIES = new HashMap<Class<?>, TestRuleFactory>();

  static {
    TEST_RULE_FACTORIES.put(ExceptionHandlerTest.class,
        new EmbeddedServerRuleFactory(new TestCustomResourceApplication()));

    TEST_RULE_FACTORIES.put(ServletAuthenticationFilterTest.class, new UndertowServletContainerRuleFactory(
        Servlets.deployment()
            .setDeploymentName("rest-test.war")
            .setContextPath("/rest-test/rest")
            .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
            .addListener(Servlets.listener(ResteasyBootstrap.class))
            .addFilter(Servlets.filter("camunda-auth", ProcessEngineAuthenticationFilter.class)
                .addInitParam("authentication-provider",
                    "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider"))
            .addFilterUrlMapping("camunda-auth", "/*", DispatcherType.REQUEST)
            .addServlet(Servlets.servlet("camunda-app", HttpServletDispatcher.class)
                .addMapping("/*")
                .addInitParam("jakarta.ws.rs.Application",
                    "org.camunda.bpm.engine.rest.util.container.JaxrsApplication"))));

    TEST_RULE_FACTORIES.put(NoServletAuthenticationFilterTest.class, new UndertowServletContainerRuleFactory(
        Servlets.deployment()
            .setDeploymentName("rest-test.war")
            .setContextPath("/rest-test/rest")
            .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
            .addListener(Servlets.listener(ResteasyBootstrap.class))
            .addFilter(Servlets.filter("camunda-auth", ProcessEngineAuthenticationFilter.class)
                .addInitParam("authentication-provider",
                    "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider")
                .addInitParam("rest-url-pattern-prefix", ""))
            .addFilterUrlMapping("camunda-auth", "/*", DispatcherType.REQUEST)
            .addFilter(Servlets.filter("Resteasy", FilterDispatcher.class)
                .addInitParam("jakarta.ws.rs.Application",
                    "org.camunda.bpm.engine.rest.util.container.JaxrsApplication"))
            .addFilterUrlMapping("Resteasy", "/*", DispatcherType.REQUEST)));

    TEST_RULE_FACTORIES.put(ServletEmptyBodyFilterTest.class, new UndertowServletContainerRuleFactory(
        Servlets.deployment()
            .setDeploymentName("rest-test.war")
            .setContextPath("/rest-test/rest")
            .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
            .addListener(Servlets.listener(ResteasyBootstrap.class))
            .addFilter(Servlets.filter("EmptyBodyFilter", org.camunda.bpm.engine.rest.filter.EmptyBodyFilter.class)
                .addInitParam("rest-url-pattern-prefix", ""))
            .addFilterUrlMapping("EmptyBodyFilter", "/*", DispatcherType.REQUEST)
            .addServlet(Servlets.servlet("camunda-app", HttpServletDispatcher.class)
                .addMapping("/*")
                .addInitParam("jakarta.ws.rs.Application",
                    "org.camunda.bpm.engine.rest.util.container.JaxrsApplication"))));

    TEST_RULE_FACTORIES.put(NoServletEmptyBodyFilterTest.class, new UndertowServletContainerRuleFactory(
        Servlets.deployment()
            .setDeploymentName("rest-test.war")
            .setContextPath("/rest-test/rest")
            .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
            .addListener(Servlets.listener(ResteasyBootstrap.class))
            .addFilter(Servlets.filter("EmptyBodyFilter", org.camunda.bpm.engine.rest.filter.EmptyBodyFilter.class)
                .addInitParam("rest-url-pattern-prefix", ""))
            .addFilterUrlMapping("EmptyBodyFilter", "/*", DispatcherType.REQUEST)
            .addFilter(Servlets.filter("Resteasy", FilterDispatcher.class)
                .addInitParam("jakarta.ws.rs.Application",
                    "org.camunda.bpm.engine.rest.util.container.JaxrsApplication"))
            .addFilterUrlMapping("Resteasy", "/*", DispatcherType.REQUEST)));

    TEST_RULE_FACTORIES.put(CustomJacksonDateFormatTest.class, new UndertowServletContainerRuleFactory(
        Servlets.deployment()
            .setDeploymentName("rest-test.war")
            .setContextPath("/rest-test")
            .setClassLoader(ResteasyUndertowServerBootstrap.class.getClassLoader())
            .addListener(Servlets.listener(ResteasyBootstrap.class))
            .addListener(Servlets.listener(org.camunda.bpm.engine.rest.CustomJacksonDateFormatListener.class))
            .addInitParameter("org.camunda.bpm.engine.rest.jackson.dateFormat", "yyyy-MM-dd'T'HH:mm:ss")
            .addFilter(Servlets.filter("Resteasy", FilterDispatcher.class)
                .addInitParam("jakarta.ws.rs.Application",
                    "org.camunda.bpm.engine.rest.util.container.JaxrsApplication"))
            .addFilterUrlMapping("Resteasy", "/*", DispatcherType.REQUEST)));
  }

  @Override
  public TestRule getTestRule(Class<?> testClass) {
    TestRuleFactory ruleFactory = DEFAULT_RULE_FACTORY;

    if (TEST_RULE_FACTORIES.containsKey(testClass)) {
      ruleFactory = TEST_RULE_FACTORIES.get(testClass);
    }

    return ruleFactory.createTestRule();
  }

  public static class EmbeddedServerRuleFactory implements TestRuleFactory {

    protected Application jaxRsApplication;

    public EmbeddedServerRuleFactory(Application jaxRsApplication) {
      this.jaxRsApplication = jaxRsApplication;
    }

    @Override
    public TestRule createTestRule() {
      return new ExternalResource() {

        ResteasyServerBootstrap bootstrap = new ResteasyServerBootstrap(jaxRsApplication);

        @Override
        protected void before() throws Throwable {
          bootstrap.start();
        }

        @Override
        protected void after() {
          bootstrap.stop();
        }
      };
    }
  }

  public static class UndertowServletContainerRuleFactory implements TestRuleFactory {

    protected DeploymentInfo deploymentInfo;

    public UndertowServletContainerRuleFactory(DeploymentInfo deploymentInfo) {
      this.deploymentInfo = deploymentInfo;
    }

    @Override
    public TestRule createTestRule() {
      final TemporaryFolder tempFolder = new TemporaryFolder();

      return RuleChain.outerRule(tempFolder).around(new ExternalResource() {

        final AbstractServerBootstrap bootstrap = new ResteasyUndertowServerBootstrap(deploymentInfo);

        @Override
        protected void before() {
          bootstrap.start();
        }

        @Override
        protected void after() {
          bootstrap.stop();
        }
      });
    }

  }

}

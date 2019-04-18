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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.CustomJacksonDateFormatTest;
import org.camunda.bpm.engine.rest.ExceptionHandlerTest;
import org.camunda.bpm.engine.rest.application.TestCustomResourceApplication;
import org.camunda.bpm.engine.rest.standalone.NoServletAuthenticationFilterTest;
import org.camunda.bpm.engine.rest.standalone.NoServletEmptyBodyFilterTest;
import org.camunda.bpm.engine.rest.standalone.ServletAuthenticationFilterTest;
import org.camunda.bpm.engine.rest.standalone.ServletEmptyBodyFilterTest;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Thorben Lindhauer
 *
 */
public class ResteasySpecifics implements ContainerSpecifics {

  protected static final TestRuleFactory DEFAULT_RULE_FACTORY =
      new EmbeddedServerRuleFactory(new JaxrsApplication());

  protected static final Map<Class<?>, TestRuleFactory> TEST_RULE_FACTORIES =
      new HashMap<Class<?>, TestRuleFactory>();

  static {
    TEST_RULE_FACTORIES.put(ExceptionHandlerTest.class, new EmbeddedServerRuleFactory(new TestCustomResourceApplication()));
    TEST_RULE_FACTORIES.put(ServletAuthenticationFilterTest.class, new ServletContainerRuleFactory("auth-filter-servlet-web.xml"));
    TEST_RULE_FACTORIES.put(NoServletAuthenticationFilterTest.class, new ServletContainerRuleFactory("auth-filter-no-servlet-web.xml"));
    TEST_RULE_FACTORIES.put(ServletEmptyBodyFilterTest.class, new ServletContainerRuleFactory("empty-body-filter-servlet-web.xml"));
    TEST_RULE_FACTORIES.put(NoServletEmptyBodyFilterTest.class, new ServletContainerRuleFactory("empty-body-filter-no-servlet-web.xml"));
    TEST_RULE_FACTORIES.put(CustomJacksonDateFormatTest.class, new ServletContainerRuleFactory("custom-date-format-web.xml"));
  }

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

    public TestRule createTestRule() {
      return new ExternalResource() {

        ResteasyServerBootstrap bootstrap = new ResteasyServerBootstrap(jaxRsApplication);

        protected void before() throws Throwable {
          bootstrap.start();
        }

        protected void after() {
          bootstrap.stop();
        }
      };
    }
  }

  public static class ServletContainerRuleFactory implements TestRuleFactory {

    protected String webXmlResource;

    public ServletContainerRuleFactory(String webXmlResource) {
      this.webXmlResource = webXmlResource;
    }

    public TestRule createTestRule() {
      final TemporaryFolder tempFolder = new TemporaryFolder();

      return RuleChain
        .outerRule(tempFolder)
        .around(new ExternalResource() {

          ResteasyTomcatServerBootstrap bootstrap = new ResteasyTomcatServerBootstrap(webXmlResource);

          protected void before() throws Throwable {
            bootstrap.setWorkingDir(tempFolder.getRoot().getAbsolutePath());
            bootstrap.start();
          }

          protected void after() {
            bootstrap.stop();
          }
        });
    }

  }


}

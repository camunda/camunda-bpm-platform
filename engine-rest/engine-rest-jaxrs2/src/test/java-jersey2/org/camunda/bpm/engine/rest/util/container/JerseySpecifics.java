/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.util.container;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

import javax.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;

public class JerseySpecifics implements ContainerSpecifics {

  protected static final TestRuleFactory DEFAULT_RULE_FACTORY = new EmbeddedServerRuleFactory(new JaxrsApplication());
  protected static final Map<Class<?>, TestRuleFactory> TEST_RULE_FACTORIES = new HashMap<Class<?>, TestRuleFactory>();

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

        JerseyServerBootstrap bootstrap = new JerseyServerBootstrap(jaxRsApplication);

        protected void before() throws Throwable {
          bootstrap.start();
        }

        protected void after() {
          bootstrap.stop();
        }
      };
    }
  }

}

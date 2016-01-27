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
package org.camunda.bpm.admin.impl.web.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.impl.DefaultAdminRuntimeDelegate;
import org.camunda.bpm.container.RuntimeContainerDelegate;

/**
 * @author Daniel Meyer
 *
 */
public class AdminContainerBootstrap implements ServletContextListener {

  private AdminEnvironment environment;

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    environment = createAdminEnvironment();
    environment.setup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    environment.tearDown();
  }

  protected AdminEnvironment createAdminEnvironment() {
    return new AdminEnvironment();
  }

  protected static class AdminEnvironment {

    public void tearDown() {
      Admin.setAdminRuntimeDelegate(null);
    }

    public void setup() {
      Admin.setAdminRuntimeDelegate(new DefaultAdminRuntimeDelegate());
    }

    protected RuntimeContainerDelegate getContainerRuntimeDelegate() {
      return RuntimeContainerDelegate.INSTANCE.get();
    }
  }
}

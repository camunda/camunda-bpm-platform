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
package org.camunda.bpm.tasklist.impl.web.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.tasklist.Tasklist;
import org.camunda.bpm.tasklist.impl.DefaultTasklistRuntimeDelegate;

/**
 * @author Roman Smirnov
 *
 */
public class TasklistContainerBootstrap implements ServletContextListener {

  protected TasklistEnvironment environment;

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    environment = createTasklistEnvironment();
    environment.setup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    environment.tearDown();
  }

  protected TasklistEnvironment createTasklistEnvironment() {
    return new TasklistEnvironment();
  }

  protected static class TasklistEnvironment {

    public void tearDown() {
      Tasklist.setTasklistRuntimeDelegate(null);
    }

    public void setup() {
      Tasklist.setTasklistRuntimeDelegate(new DefaultTasklistRuntimeDelegate());
    }

    protected RuntimeContainerDelegate getContainerRuntimeDelegate() {
      return RuntimeContainerDelegate.INSTANCE.get();
    }
  }

}

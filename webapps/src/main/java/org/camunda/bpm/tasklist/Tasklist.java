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
package org.camunda.bpm.tasklist;

/**
 * The tasklist application. Provides access to the tasklist core services.
 *
 * @author Roman Smirnov
 *
 */
public class Tasklist {

  /**
   * The {@link TasklistRuntimeDelegate} is an delegate that will be
   * initialized by bootstrapping camunda admin with an specific
   * instance
   */
  protected static TasklistRuntimeDelegate TASKLIST_RUNTIME_DELEGATE;
  /**
   * Returns an instance of {@link TasklistRuntimeDelegate}
   *
   * @return
   */
  public static TasklistRuntimeDelegate getRuntimeDelegate() {
    return TASKLIST_RUNTIME_DELEGATE;
  }

  /**
   * A setter to set the {@link TasklistRuntimeDelegate}.
   * @param tasklistRuntimeDelegate
   */
  public static void setTasklistRuntimeDelegate(TasklistRuntimeDelegate tasklistRuntimeDelegate) {
    TASKLIST_RUNTIME_DELEGATE = tasklistRuntimeDelegate;
  }

}

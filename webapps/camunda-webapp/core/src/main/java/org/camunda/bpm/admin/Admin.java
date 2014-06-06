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
package org.camunda.bpm.admin;

/**
 * The admin application. Provides access to the admin core services.
 *
 * @author Daniel Meyer
 *
 */
public class Admin {

  /**
   * The {@link AdminRuntimeDelegate} is an delegate that will be
   * initialized by bootstrapping camunda admin with an specific
   * instance
   */
  protected static AdminRuntimeDelegate ADMIN_RUNTIME_DELEGATE;

  /**
   * Returns an instance of {@link AdminRuntimeDelegate}
   *
   * @return
   */
  public static AdminRuntimeDelegate getRuntimeDelegate() {
    return ADMIN_RUNTIME_DELEGATE;
  }

  /**
   * A setter to set the {@link AdminRuntimeDelegate}.
   * @param cockpitRuntimeDelegate
   */
  public static void setAdminRuntimeDelegate(AdminRuntimeDelegate adminRuntimeDelegate) {
    ADMIN_RUNTIME_DELEGATE = adminRuntimeDelegate;
  }

}

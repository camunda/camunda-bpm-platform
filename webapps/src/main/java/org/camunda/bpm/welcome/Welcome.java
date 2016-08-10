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
package org.camunda.bpm.welcome;

/**
 * The welcome application. Provides access to the welcome core services.
 *
 * @author Daniel Meyer
 *
 */
public class Welcome {

  /**
   * The {@link WelcomeRuntimeDelegate} is an delegate that will be
   * initialized by bootstrapping camunda welcome with an specific
   * instance
   */
  protected static WelcomeRuntimeDelegate WELCOME_RUNTIME_DELEGATE;

  /**
   * Returns an instance of {@link WelcomeRuntimeDelegate}
   *
   * @return
   */
  public static WelcomeRuntimeDelegate getRuntimeDelegate() {
    return WELCOME_RUNTIME_DELEGATE;
  }

  /**
   * A setter to set the {@link WelcomeRuntimeDelegate}.
   * @param cockpitRuntimeDelegate
   */
  public static void setRuntimeDelegate(WelcomeRuntimeDelegate welcomeRuntimeDelegate) {
    WELCOME_RUNTIME_DELEGATE = welcomeRuntimeDelegate;
  }

}

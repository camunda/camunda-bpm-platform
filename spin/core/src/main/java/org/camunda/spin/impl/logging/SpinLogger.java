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
package org.camunda.spin.impl.logging;

import org.camunda.commons.logging.BaseLogger;
import org.camunda.spin.impl.test.SpinTestLogger;

/**
 * @author Daniel Meyer
 *
 */
public abstract class SpinLogger extends BaseLogger {

  public static final String PROJECT_CODE = "SPIN";

  public static final SpinCoreLogger CORE_LOGGER = BaseLogger.createLogger(SpinCoreLogger.class, PROJECT_CODE, "org.camunda.spin", "01");
  public static final SpinTestLogger TEST_LOGGER = BaseLogger.createLogger(SpinTestLogger.class, PROJECT_CODE, "org.camunda.spin.test", "02");
}

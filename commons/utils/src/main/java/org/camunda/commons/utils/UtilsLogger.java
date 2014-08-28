/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.commons.utils;

import org.camunda.commons.logging.BaseLogger;

/**
 * @author Sebastian Menski
 */
public class UtilsLogger extends BaseLogger {

  public final static String PROJECT_CODE = "UTILS";

  public final static IoUtilLogger IO_UTIL_LOGGER = BaseLogger.createLogger(IoUtilLogger.class, PROJECT_CODE, "org.camunda.commons.utils.io", "01");
  public final static EnsureUtilLogger ENSURE_UTIL_LOGGER = BaseLogger.createLogger(EnsureUtilLogger.class, PROJECT_CODE, "org.camunda.commons.utils.ensure", "02");
}

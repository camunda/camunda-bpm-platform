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
package org.camunda.spin.json;

import org.camunda.spin.impl.util.IoUtil;

public class JsonTestConstants {

  public final static String EXAMPLE_JSON_FILE_NAME = "org/camunda/spin/json/example.json";
  
  public final static String EXAMPLE_JSON = IoUtil.readFileAsString(EXAMPLE_JSON_FILE_NAME);
  
  public final static String EXAMPLE_INVALID_JSON = "{\"invalid\":";
  
  public final static String EXAMPLE_EMPTY_STRING = "";
  
  /**
   * A json file that can only be parsed when configuring Jackson correctly.
   */
  public final static String EXAMPLE_JACKSON_CONFIGURATION_JSON_FILE_NAME = "org/camunda/spin/json/example_jackson.json";
  
  public final static String EXAMPLE_JACKSON_CONFIGURATION_JSON = IoUtil.readFileAsString(EXAMPLE_JACKSON_CONFIGURATION_JSON_FILE_NAME);
}

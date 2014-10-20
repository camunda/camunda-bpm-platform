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
package org.camunda.spin.impl.json.jackson.format;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class DefaultJsonJacksonTypeDetector extends AbstractJacksonJsonTypeDetector {

  public boolean canHandle(Object object) {
    return true;
  }

  public String detectType(Object object) {
    Class<?> type = object.getClass();
    return TypeFactory.defaultInstance().constructType(type).toCanonical();
  }

}

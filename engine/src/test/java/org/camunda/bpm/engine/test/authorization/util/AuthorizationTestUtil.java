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
package org.camunda.bpm.engine.test.authorization.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestUtil {


  protected static Map<Integer, Resource> resourcesByType = new HashMap<Integer, Resource>();

  static {
    for (Resource resource : Resources.values()) {
      resourcesByType.put(resource.resourceType(), resource);
    }
  }

  public static Resource getResourceByType(int type) {
    return resourcesByType.get(type);
  }
}

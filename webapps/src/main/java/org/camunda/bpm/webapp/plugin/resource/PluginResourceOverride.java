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
package org.camunda.bpm.webapp.plugin.resource;

import java.io.InputStream;

/**
 * Used to replace a plugin resource. An implementation of this interface
 * may conditionally replace the content of another plugin's static resource
 * with it's own content.
 *
 * @author Daniel Meyer
 *
 */
public interface PluginResourceOverride {

  /**
   * Invoked after a static plugin resource has been resolved.
   *
   * If the implementation decides not to modify the resource, it must return the
   * original input stream passed in as parameter.
   *
   * @param inputStream the content of the resource
   * @param requestInfo contains information about the request.
   * @return the original input stream or a modified input stream or null to remove the resource.
   */
  public InputStream filterResource(InputStream inputStream, RequestInfo requestInfo);

}

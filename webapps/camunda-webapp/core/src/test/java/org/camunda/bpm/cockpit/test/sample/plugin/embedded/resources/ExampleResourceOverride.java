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
package org.camunda.bpm.cockpit.test.sample.plugin.embedded.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride;
import org.camunda.bpm.webapp.plugin.resource.RequestInfo;

/**
 * @author Daniel Meyer
 *
 */
public class ExampleResourceOverride implements PluginResourceOverride {

  public static final String REPLACED = "replaced";

  public InputStream filterResource(InputStream inputStream, RequestInfo requestInfo) {
    // overrides all assets with the String "replaced"
    return new ByteArrayInputStream(REPLACED.getBytes());
  }

}

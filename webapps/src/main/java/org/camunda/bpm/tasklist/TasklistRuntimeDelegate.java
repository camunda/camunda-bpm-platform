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

import org.camunda.bpm.tasklist.plugin.spi.TasklistPlugin;
import org.camunda.bpm.webapp.AppRuntimeDelegate;

/**
 * The tasklist application service runtime delegate. Provides access to the
 * application services of the tasklist application.
 *
 * @author Roman Smirnov
 *
 */
public interface TasklistRuntimeDelegate extends AppRuntimeDelegate<TasklistPlugin> {

}

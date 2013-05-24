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
package org.camunda.bpm.cockpit.plugin.base;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class PluginQueryTest extends AbstractCockpitPluginTest {

  @Test
  public void testCustomQuery() {

    List<Execution> result = getQueryService().executeQuery("cockpit.base.selectProcessDefinitionWithFailedJobs", new QueryParameters<Execution>());

    assertThat(result).hasSize(0);
  }
}

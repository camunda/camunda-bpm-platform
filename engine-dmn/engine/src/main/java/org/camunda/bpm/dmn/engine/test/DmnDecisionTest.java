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

package org.camunda.bpm.dmn.engine.test;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.junit.Before;
import org.junit.Rule;

public abstract class DmnDecisionTest {

  @Rule
  public DmnEngineRule dmnEngineRule = new DmnEngineRule(createDmnEngineConfiguration());

  public DmnEngine engine;
  public DmnDecision decision;

  public DmnEngineConfiguration createDmnEngineConfiguration() {
    return new DmnEngineConfigurationImpl();
  }

  @Before
  public void initEngineAndDecision() {
    engine = dmnEngineRule.getEngine();
    decision = dmnEngineRule.getDecision();
  }

}

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

package org.camunda.dmn.engine;

import java.io.InputStream;

import org.camunda.bpm.model.dmn.DmnModelInstance;

public interface DmnEngine {

  DmnEngineConfiguration getConfiguration();

  DmnDecisionModel parseDecisionModel(String filename);

  DmnDecisionModel parseDecisionModel(InputStream inputStream);

  DmnDecisionModel parseDecisionModel(DmnModelInstance modelInstance);

  DmnDecision parseDecision(String filename);

  DmnDecision parseDecision(InputStream inputStream);

  DmnDecision parseDecision(DmnModelInstance modelInstance);

  DmnDecision parseDecision(String filename, String decisionKey);

  DmnDecision parseDecision(InputStream inputStream, String decisionKey);

  DmnDecision parseDecision(DmnModelInstance modelInstance, String decisionKey);

}

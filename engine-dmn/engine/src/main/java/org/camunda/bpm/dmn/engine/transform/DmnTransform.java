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

package org.camunda.bpm.dmn.engine.transform;

import java.io.File;
import java.io.InputStream;

import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.model.dmn.DmnModelInstance;

public interface DmnTransform {

  DmnTransform setModelInstance(File file);

  DmnTransform setModelInstance(InputStream inputStream);

  DmnTransform setModelInstance(DmnModelInstance modelInstance);

  DmnDecisionModel transform();

}

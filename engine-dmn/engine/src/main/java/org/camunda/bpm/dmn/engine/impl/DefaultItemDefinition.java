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
package org.camunda.bpm.dmn.engine.impl;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnTypeDefinition;

/**
 * @author Philipp Ossler
 */
public class DefaultItemDefinition implements DmnItemDefinition {

  @Override
  public String getKey() {
    return "default";
  }

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public DmnTypeDefinition getTypeDefinition() {
    return new DefaultTypeDefinition();
  }

  @Override
  public List<DmnExpression> getAllowedValues() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "DefaultItemDefinition []";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

}

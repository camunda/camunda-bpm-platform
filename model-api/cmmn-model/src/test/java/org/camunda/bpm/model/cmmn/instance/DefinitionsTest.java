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
package org.camunda.bpm.model.cmmn.instance;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;

/**
 * @author Roman Smirnov
 *
 */
public class DefinitionsTest extends CmmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
          new ChildElementAssumption(Import.class),
          new ChildElementAssumption(CaseFileItemDefinition.class),
          new ChildElementAssumption(Case.class),
          new ChildElementAssumption(Process.class),
          new ChildElementAssumption(Decision.class),
          new ChildElementAssumption(ExtensionElements.class, 0, 1),
          new ChildElementAssumption(Relationship.class),
          new ChildElementAssumption(Artifact.class)
        );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
        new AttributeAssumption("id", true),
        new AttributeAssumption("name"),
        new AttributeAssumption("targetNamespace", false, true),
        new AttributeAssumption("expressionLanguage", false, false, CmmnModelConstants.XPATH_NS),
        new AttributeAssumption("exporter"),
        new AttributeAssumption("exporterVersion"),
        new AttributeAssumption("author")
      );
  }

}

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

import java.util.Collection;

import org.camunda.bpm.model.cmmn.MultiplicityEnum;

/**
 * @author Roman Smirnov
 *
 */
public interface CaseFileItem extends CmmnElement {

  String getName();

  void setName(String name);

  Children getChildren();

  void setChildren(Children children);

  MultiplicityEnum getMultiplicity();

  void setMultiplicity(MultiplicityEnum multiplicity);

  CaseFileItemDefinition getDefinitionRef();

  void setDefinitionRef(CaseFileItemDefinition caseFileItemDefinition);

  @Deprecated
  CaseFileItem getSourceRef();

  @Deprecated
  void setSourceRef(CaseFileItem sourceRef);

  Collection<CaseFileItem> getSourceRefs();

  Collection<CaseFileItem> getTargetRefs();

}

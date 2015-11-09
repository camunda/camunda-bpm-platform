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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_FILE_MODEL;

import org.camunda.bpm.model.cmmn.instance.CaseFile;
import org.camunda.bpm.model.cmmn.instance.CaseFileModel;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class CaseFileModelImpl extends CaseFileImpl implements CaseFileModel {

  public CaseFileModelImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseFileModel.class, CMMN_ELEMENT_CASE_FILE_MODEL)
      .namespaceUri(CMMN11_NS)
      .extendsType(CaseFile.class)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<CaseFileModel>() {
        public CaseFileModel newInstance(ModelTypeInstanceContext instanceContext) {
          return new CaseFileModelImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}

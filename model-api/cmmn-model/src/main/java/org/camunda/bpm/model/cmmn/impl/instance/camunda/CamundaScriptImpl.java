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
package org.camunda.bpm.model.cmmn.impl.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_RESOURCE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_SCRIPT_FORMAT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_SCRIPT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import org.camunda.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * @author Roman Smirnov
 *
 */
public class CamundaScriptImpl extends CmmnModelElementInstanceImpl implements CamundaScript {

  protected static Attribute<String> camundaScriptFormatAttribute;
  protected static Attribute<String> camundaResourceAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaScript.class, CAMUNDA_ELEMENT_SCRIPT)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaScript>() {
        public CamundaScript newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaScriptImpl(instanceContext);
        }
      });

    camundaScriptFormatAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_SCRIPT_FORMAT)
        .namespace(CAMUNDA_NS)
        .build();

    camundaResourceAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_RESOURCE)
        .namespace(CAMUNDA_NS)
        .build();

    typeBuilder.build();
  }

  public CamundaScriptImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaScriptFormat() {
    return camundaScriptFormatAttribute.getValue(this);
  }

  public void setCamundaScriptFormat(String scriptFormat) {
    camundaScriptFormatAttribute.setValue(this, scriptFormat);
  }

  public String getCamundaResource() {
    return camundaResourceAttribute.getValue(this);
  }

  public void setCamundaResoure(String resource) {
    camundaResourceAttribute.setValue(this, resource);
  }

}

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

package org.camunda.bpm.dmn.engine.impl.handler;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import org.camunda.bpm.dmn.engine.handler.DmnElementHandler;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.DmnTypeDefinitionImpl;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformerFactory;
import org.camunda.bpm.model.dmn.instance.TypeDefinition;

public class DmnTypeDefinitionHandler implements DmnElementHandler<TypeDefinition, DmnTypeDefinitionImpl> {

  public DmnTypeDefinitionImpl handleElement(DmnElementHandlerContext context, TypeDefinition typeDefinition) {
    DmnTypeDefinitionImpl dmnTypeDefinition = createElement(context, typeDefinition);
    initElement(context, typeDefinition, dmnTypeDefinition);
    return dmnTypeDefinition;
  }

  protected DmnTypeDefinitionImpl createElement(DmnElementHandlerContext context, TypeDefinition typeDefinition) {
    return new DmnTypeDefinitionImpl();
  }

  protected void initElement(DmnElementHandlerContext context, TypeDefinition typeDefinition, DmnTypeDefinitionImpl dmnTypeDefinition) {
    initTypeName(context, typeDefinition, dmnTypeDefinition);
    initDataTypeTransformer(context, typeDefinition, dmnTypeDefinition);
  }

  protected void initTypeName(DmnElementHandlerContext context, TypeDefinition typeDefinition, DmnTypeDefinitionImpl dmnTypeDefinition) {
    dmnTypeDefinition.setTypeName(typeDefinition.getTextContent());
  }

  protected void initDataTypeTransformer(DmnElementHandlerContext context, TypeDefinition typeDefinition, DmnTypeDefinitionImpl dmnTypeDefinition) {
    DataTypeTransformerFactory factory = context.getDataTypeTransformerFactory();
    ensureNotNull("data type transformer factory", factory);

    DataTypeTransformer transformer = factory.getTransformerForType(dmnTypeDefinition.getTypeName());
    ensureNotNull("data type transformer", transformer);

    dmnTypeDefinition.setTransformer(transformer);
  }

}

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

package org.camunda.dmn.engine.impl.handler;

import org.camunda.bpm.model.dmn.instance.TypeDefinition;
import org.camunda.dmn.engine.handler.DmnElementHandler;
import org.camunda.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.dmn.engine.impl.DmnTypeDefinitionImpl;

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
  }

  protected void initTypeName(DmnElementHandlerContext context, TypeDefinition typeDefinition, DmnTypeDefinitionImpl dmnTypeDefinition) {
    dmnTypeDefinition.setTypeName(typeDefinition.getTextContent());
  }


}

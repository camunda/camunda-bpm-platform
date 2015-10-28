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

package org.camunda.bpm.model.dmn.impl.instance;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN11_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_INVOCATION;

import java.util.Collection;

import org.camunda.bpm.model.dmn.instance.Binding;
import org.camunda.bpm.model.dmn.instance.CalledFunction;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.Invocation;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

public class InvocationImpl extends ExpressionImpl implements Invocation {

  protected static ChildElement<CalledFunction> calledFunctionChild;
  protected static ChildElementCollection<Binding> bindingCollection;

  public InvocationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public CalledFunction getCalledFunction() {
    return calledFunctionChild.getChild(this);
  }

  public void setCalledFunction(CalledFunction calledFunction) {
    calledFunctionChild.setChild(this, calledFunction);
  }

  public Collection<Binding> getBindings() {
    return bindingCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Invocation.class, DMN_ELEMENT_INVOCATION)
      .namespaceUri(DMN11_NS)
      .extendsType(Expression.class)
      .instanceProvider(new ModelTypeInstanceProvider<Invocation>() {
        public Invocation newInstance(ModelTypeInstanceContext instanceContext) {
          return new InvocationImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    calledFunctionChild = sequenceBuilder.element(CalledFunction.class)
      .build();

    bindingCollection = sequenceBuilder.elementCollection(Binding.class)
      .build();

    typeBuilder.build();
  }

}

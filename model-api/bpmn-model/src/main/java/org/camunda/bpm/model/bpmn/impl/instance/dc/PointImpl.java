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

package org.camunda.bpm.model.bpmn.impl.instance.dc;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.dc.Point;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The DC point element
 *
 * @author Sebastian Menski
 */
public class PointImpl extends BpmnModelElementInstanceImpl implements Point {

  protected static Attribute<Double> xAttribute;
  protected static Attribute<Double> yAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Point.class, DC_ELEMENT_POINT)
      .namespaceUri(DC_NS)
      .instanceProvider(new ModelTypeInstanceProvider<Point>() {
        public Point newInstance(ModelTypeInstanceContext instanceContext) {
          return new PointImpl(instanceContext);
        }
      });

    xAttribute = typeBuilder.doubleAttribute(DC_ATTRIBUTE_X)
      .required()
      .build();

    yAttribute = typeBuilder.doubleAttribute(DC_ATTRIBUTE_Y)
      .required()
      .build();

    typeBuilder.build();
  }

  public PointImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Double getX() {
    return xAttribute.getValue(this);
  }

  public void setX(double x) {
    xAttribute.setValue(this, x);
  }

  public Double getY() {
    return yAttribute.getValue(this);
  }

  public void setY(double y) {
    yAttribute.setValue(this, y);
  }
}

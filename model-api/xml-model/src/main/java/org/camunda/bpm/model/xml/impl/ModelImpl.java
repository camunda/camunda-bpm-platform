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
package org.camunda.bpm.model.xml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeBuilderImpl;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.impl.util.QName;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class ModelImpl implements Model {

  private final Map<QName, ModelElementType> typesByName = new HashMap<QName, ModelElementType>();
  private final Map<Class<? extends ModelElementInstance>, ModelElementType> typesByClass = new HashMap<Class<? extends ModelElementInstance>, ModelElementType>();
  private final String modelName;


  public ModelImpl(String modelName) {
    this.modelName = modelName;
  }

  public Collection<ModelElementType> getTypes() {
    return new ArrayList<ModelElementType>(typesByName.values());
  }

  public ModelElementType getType(Class<? extends ModelElementInstance> type) {
    return typesByClass.get(type);
  }

  public <T extends ModelElementInstance> ModelElementType getTypeForName(String typeName) {
    return getTypeForName(typeName, null);
  }

  public ModelElementType getTypeForName(String typeName, String namespaceUri) {
    return typesByName.get(ModelUtil.getQName(typeName, namespaceUri));
  }

  public void registerType(ModelElementType modelElementType, Class<? extends ModelElementInstance> instanceType) {
    QName qName = ModelUtil.getQName(modelElementType.getTypeName(), modelElementType.getTypeNamespace());
    typesByName.put(qName, modelElementType);
    typesByClass.put(instanceType, modelElementType);
  }

  public String getModelName() {
    return modelName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ModelImpl other = (ModelImpl) obj;
    if (modelName == null) {
      if (other.modelName != null)
        return false;
    } else if (!modelName.equals(other.modelName))
      return false;
    return true;
  }
}

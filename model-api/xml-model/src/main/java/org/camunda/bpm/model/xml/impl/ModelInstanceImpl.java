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

import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance of a model
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class ModelInstanceImpl implements ModelInstance {

  private final DomDocument document;
  private ModelImpl model;
  private final ModelBuilder modelBuilder;

  public ModelInstanceImpl(ModelImpl model, ModelBuilder modelBuilder, DomDocument document) {
    this.model = model;
    this.modelBuilder = modelBuilder;
    this.document = document;
  }

  public DomDocument getDocument() {
    return document;
  }

  public ModelElementInstance getDocumentElement() {
    DomElement rootElement = document.getRootElement();
    if(rootElement != null) {
      return ModelUtil.getModelElement(rootElement, this);
    } else {
      return null;
    }
  }

  public void setDocumentElement(ModelElementInstance modelElement) {
    ModelUtil.ensureInstanceOf(modelElement, ModelElementInstanceImpl.class);
    DomElement domElement = modelElement.getDomElement();
    document.setRootElement(domElement);
  }

  public <T extends ModelElementInstance> T newInstance(Class<T> type) {
    ModelElementType modelElementType = model.getType(type);
    if(modelElementType != null) {
      return newInstance(modelElementType);
    } else {
      throw new ModelException("Cannot create instance of ModelType "+type+": no such type registered.");
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ModelElementInstance> T newInstance(ModelElementType type) {
    return (T) type.newInstance(this);
  }

  public Model getModel() {
    return model;
  }

  public ModelElementType registerGenericType(String namespaceUri, String localName) {
    ModelElementType elementType = modelBuilder.defineGenericType(localName, namespaceUri);
    model = (ModelImpl) modelBuilder.build();
    return elementType;
  }

  public ModelElementInstance getModelElementById(String id) {
    if (id == null) {
      return null;
    }

    DomElement element = document.getElementById(id);
    if(element != null) {
      return ModelUtil.getModelElement(element, this);
    } else {
      return null;
    }
  }

  public Collection<ModelElementInstance> getModelElementsByType(ModelElementType type) {
    Collection<ModelElementType> extendingTypes = type.getAllExtendingTypes();

    List<ModelElementInstance> instances = new ArrayList<ModelElementInstance>();
    for (ModelElementType modelElementType : extendingTypes) {
      if(!modelElementType.isAbstract()) {
        instances.addAll(modelElementType.getInstances(this));
      }
    }
    return instances;
  }

  /**
   * Clones the model instance but not the model. So only the wrapped DOM document is cloned.
   * Changes of the model are persistent between multiple model instances.
   *
   * @return the new model instance
   */
  public Object clone() {
    return new ModelInstanceImpl(model, modelBuilder, document.clone());
  }
}

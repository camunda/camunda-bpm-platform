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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_AUTHOR;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_EXPORTER;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_EXPORTER_VERSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_EXPRESSION_LANGUAGE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_ID;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_TARGET_NAMESPACE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_DEFINITIONS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.XPATH_NS;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.Artifact;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CaseFileItemDefinition;
import org.camunda.bpm.model.cmmn.instance.Decision;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.Import;
import org.camunda.bpm.model.cmmn.instance.Process;
import org.camunda.bpm.model.cmmn.instance.Relationship;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class DefinitionsImpl extends CmmnModelElementInstanceImpl implements Definitions {

  protected static Attribute<String> idAttribute;
  protected static Attribute<String> nameAttribute;
  protected static Attribute<String> targetNamespaceAttribute;
  protected static Attribute<String> expressionLanguageAttribute;
  protected static Attribute<String> exporterAttribute;
  protected static Attribute<String> exporterVersionAttribute;
  protected static Attribute<String> authorAttribute;

  protected static ChildElementCollection<Import> importCollection;
  protected static ChildElementCollection<CaseFileItemDefinition> caseFileItemDefinitionCollection;
  protected static ChildElementCollection<Case> caseCollection;
  protected static ChildElementCollection<Process> processCollection;
  protected static ChildElementCollection<Relationship> relationshipCollection;

  // cmmn 1.1
  protected static ChildElement<ExtensionElements> extensionElementsChild;
  protected static ChildElementCollection<Decision> decisionCollection;
  protected static ChildElementCollection<Artifact> artifactCollection;

  public DefinitionsImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttribute.getValue(this);
  }

  public void setId(String id) {
    idAttribute.setValue(this, id);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public String getTargetNamespace() {
    return targetNamespaceAttribute.getValue(this);
  }

  public void setTargetNamespace(String namespace) {
    targetNamespaceAttribute.setValue(this, namespace);
  }

  public String getExpressionLanguage() {
    return expressionLanguageAttribute.getValue(this);
  }

  public void setExpressionLanguage(String expressionLanguage) {
    expressionLanguageAttribute.setValue(this, expressionLanguage);
  }

  public String getExporter() {
    return exporterAttribute.getValue(this);
  }

  public void setExporter(String exporter) {
    exporterAttribute.setValue(this, exporter);
  }

  public String getExporterVersion() {
    return exporterVersionAttribute.getValue(this);
  }

  public void setExporterVersion(String exporterVersion) {
    exporterVersionAttribute.setValue(this, exporterVersion);
  }

  public String getAuthor() {
    return authorAttribute.getValue(this);
  }

  public void setAuthor(String author) {
    authorAttribute.setValue(this, author);
  }

  public Collection<Import> getImports() {
    return importCollection.get(this);
  }

  public Collection<CaseFileItemDefinition> getCaseFileItemDefinitions() {
    return caseFileItemDefinitionCollection.get(this);
  }

  public Collection<Case> getCases() {
    return caseCollection.get(this);
  }

  public Collection<Process> getProcesses() {
    return processCollection.get(this);
  }

  public Collection<Decision> getDecisions() {
    return decisionCollection.get(this);
  }

  public ExtensionElements getExtensionElements() {
    return extensionElementsChild.getChild(this);
  }

  public void setExtensionElements(ExtensionElements extensionElements) {
    extensionElementsChild.setChild(this, extensionElements);
  }

  public Collection<Relationship> getRelationships() {
    return relationshipCollection.get(this);
  }

  public Collection<Artifact> getArtifacts() {
    return artifactCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Definitions.class, CMMN_ELEMENT_DEFINITIONS)
      .namespaceUri(CMMN11_NS)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<Definitions>() {
        public Definitions newInstance(ModelTypeInstanceContext instanceContext) {
          return new DefinitionsImpl(instanceContext);
        }
      });

    idAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_ID)
        .idAttribute()
        .build();

    nameAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_NAME)
        .build();

    targetNamespaceAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_TARGET_NAMESPACE)
        .required()
        .build();

    expressionLanguageAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_EXPRESSION_LANGUAGE)
        .defaultValue(XPATH_NS)
        .build();

    exporterAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_EXPORTER)
        .build();

    exporterVersionAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_EXPORTER_VERSION)
        .build();

    authorAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_AUTHOR)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    importCollection = sequenceBuilder.elementCollection(Import.class)
        .build();

    caseFileItemDefinitionCollection = sequenceBuilder.elementCollection(CaseFileItemDefinition.class)
        .build();

    caseCollection = sequenceBuilder.elementCollection(Case.class)
        .build();

    processCollection = sequenceBuilder.elementCollection(Process.class)
        .build();

    decisionCollection = sequenceBuilder.elementCollection(Decision.class)
        .build();

    extensionElementsChild = sequenceBuilder.element(ExtensionElements.class)
        .minOccurs(0)
        .maxOccurs(1)
        .build();

    relationshipCollection = sequenceBuilder.elementCollection(Relationship.class)
        .build();

    artifactCollection = sequenceBuilder.elementCollection(Artifact.class)
        .build();

    typeBuilder.build();
  }

}

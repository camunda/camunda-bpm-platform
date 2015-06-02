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

package org.camunda.bpm.model.dmn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.camunda.bpm.model.dmn.impl.AllowedAnswersImpl;
import org.camunda.bpm.model.dmn.impl.AllowedValueImpl;
import org.camunda.bpm.model.dmn.impl.AuthorityRequirementImpl;
import org.camunda.bpm.model.dmn.impl.BindingImpl;
import org.camunda.bpm.model.dmn.impl.BusinessContextElementImpl;
import org.camunda.bpm.model.dmn.impl.BusinessKnowledgeModelImpl;
import org.camunda.bpm.model.dmn.impl.ClauseImpl;
import org.camunda.bpm.model.dmn.impl.ColumnImpl;
import org.camunda.bpm.model.dmn.impl.ConclusionImpl;
import org.camunda.bpm.model.dmn.impl.ConditionImpl;
import org.camunda.bpm.model.dmn.impl.ContextEntryImpl;
import org.camunda.bpm.model.dmn.impl.ContextImpl;
import org.camunda.bpm.model.dmn.impl.DecisionImpl;
import org.camunda.bpm.model.dmn.impl.DecisionMadeReferenceImpl;
import org.camunda.bpm.model.dmn.impl.DecisionMakerReferenceImpl;
import org.camunda.bpm.model.dmn.impl.DecisionOwnedReferenceImpl;
import org.camunda.bpm.model.dmn.impl.DecisionOwnerReferenceImpl;
import org.camunda.bpm.model.dmn.impl.DecisionRuleImpl;
import org.camunda.bpm.model.dmn.impl.DecisionTableImpl;
import org.camunda.bpm.model.dmn.impl.DefinitionsImpl;
import org.camunda.bpm.model.dmn.impl.DescriptionImpl;
import org.camunda.bpm.model.dmn.impl.DmnElementImpl;
import org.camunda.bpm.model.dmn.impl.DmnElementReferenceImpl;
import org.camunda.bpm.model.dmn.impl.DmnParser;
import org.camunda.bpm.model.dmn.impl.DrgElementImpl;
import org.camunda.bpm.model.dmn.impl.DrgElementReferenceImpl;
import org.camunda.bpm.model.dmn.impl.ElementCollectionImpl;
import org.camunda.bpm.model.dmn.impl.ExpressionImpl;
import org.camunda.bpm.model.dmn.impl.FunctionDefinitionImpl;
import org.camunda.bpm.model.dmn.impl.ImpactedPerformanceIndicatorReferenceImpl;
import org.camunda.bpm.model.dmn.impl.ImpactingDecisionReferenceImpl;
import org.camunda.bpm.model.dmn.impl.ImportImpl;
import org.camunda.bpm.model.dmn.impl.InformationItemImpl;
import org.camunda.bpm.model.dmn.impl.InformationRequirementImpl;
import org.camunda.bpm.model.dmn.impl.InputDataImpl;
import org.camunda.bpm.model.dmn.impl.InputEntryImpl;
import org.camunda.bpm.model.dmn.impl.InputExpressionImpl;
import org.camunda.bpm.model.dmn.impl.InputVariableReferenceImpl;
import org.camunda.bpm.model.dmn.impl.InvocationImpl;
import org.camunda.bpm.model.dmn.impl.ItemComponentImpl;
import org.camunda.bpm.model.dmn.impl.ItemDefinitionImpl;
import org.camunda.bpm.model.dmn.impl.ItemDefinitionReferenceImpl;
import org.camunda.bpm.model.dmn.impl.KnowledgeRequirementImpl;
import org.camunda.bpm.model.dmn.impl.KnowledgeSourceImpl;
import org.camunda.bpm.model.dmn.impl.ListImpl;
import org.camunda.bpm.model.dmn.impl.LiteralExpressionImpl;
import org.camunda.bpm.model.dmn.impl.NamedDmnElementImpl;
import org.camunda.bpm.model.dmn.impl.OrganizationUnitImpl;
import org.camunda.bpm.model.dmn.impl.OutputDefinitionReferenceImpl;
import org.camunda.bpm.model.dmn.impl.OutputEntryImpl;
import org.camunda.bpm.model.dmn.impl.OwnerReferenceImpl;
import org.camunda.bpm.model.dmn.impl.ParameterReferenceImpl;
import org.camunda.bpm.model.dmn.impl.PerformanceIndicatorImpl;
import org.camunda.bpm.model.dmn.impl.QuestionImpl;
import org.camunda.bpm.model.dmn.impl.RelationImpl;
import org.camunda.bpm.model.dmn.impl.RequiredAuthorityReferenceImpl;
import org.camunda.bpm.model.dmn.impl.RequiredDecisionReferenceImpl;
import org.camunda.bpm.model.dmn.impl.RequiredInputReferenceImpl;
import org.camunda.bpm.model.dmn.impl.RequiredKnowledgeReferenceImpl;
import org.camunda.bpm.model.dmn.impl.RuleImpl;
import org.camunda.bpm.model.dmn.impl.SupportedObjectiveReferenceImpl;
import org.camunda.bpm.model.dmn.impl.TextImpl;
import org.camunda.bpm.model.dmn.impl.TypeDefinitionImpl;
import org.camunda.bpm.model.dmn.impl.TypeImpl;
import org.camunda.bpm.model.dmn.impl.TypeRefImpl;
import org.camunda.bpm.model.dmn.impl.UsingProcessReferenceImpl;
import org.camunda.bpm.model.dmn.impl.UsingTaskReferenceImpl;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.IoUtil;

public class Dmn {

  /** the singleton instance of {@link Dmn}. If you want to customize the behavior of Dmn,
   * replace this instance with an instance of a custom subclass of {@link Dmn}. */
  public static Dmn INSTANCE = new Dmn();

  /** the parser used by the Dmn implementation. */
  private DmnParser dmnParser = new DmnParser();
  private final ModelBuilder dmnModelBuilder;

  /** The {@link Model}
   */
  private Model dmnModel;

  /**
   * Allows reading a {@link DmnModelInstance} from a File.
   *
   * @param file the {@link File} to read the {@link DmnModelInstance} from
   * @return the model read
   * @throws DmnModelException if the model cannot be read
   */
  public static DmnModelInstance readModelFromFile(File file) {
    return INSTANCE.doReadModelFromFile(file);
  }

  /**
   * Allows reading a {@link DmnModelInstance} from an {@link InputStream}
   *
   * @param stream the {@link InputStream} to read the {@link DmnModelInstance} from
   * @return the model read
   * @throws ModelParseException if the model cannot be read
   */
  public static DmnModelInstance readModelFromStream(InputStream stream) {
    return INSTANCE.doReadModelFromInputStream(stream);
  }

  /**
   * Allows writing a {@link DmnModelInstance} to a File. It will be
   * validated before writing.
   *
   * @param file the {@link File} to write the {@link DmnModelInstance} to
   * @param modelInstance the {@link DmnModelInstance} to write
   * @throws DmnModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToFile(File file, DmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToFile(file, modelInstance);
  }

  /**
   * Allows writing a {@link DmnModelInstance} to an {@link OutputStream}. It will be
   * validated before writing.
   *
   * @param stream the {@link OutputStream} to write the {@link DmnModelInstance} to
   * @param modelInstance the {@link DmnModelInstance} to write
   * @throws ModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToStream(OutputStream stream, DmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToOutputStream(stream, modelInstance);
  }

  /**
   * Allows the conversion of a {@link DmnModelInstance} to an {@link String}. It will
   * be validated before conversion.
   *
   * @param modelInstance  the model instance to convert
   * @return the XML string representation of the model instance
   */
  public static String convertToString(DmnModelInstance modelInstance) {
    return INSTANCE.doConvertToString(modelInstance);
  }

  /**
   * Validate model DOM document
   *
   * @param modelInstance the {@link DmnModelInstance} to validate
   * @throws ModelValidationException if the model is not valid
   */
  public static void validateModel(DmnModelInstance modelInstance) {
    INSTANCE.doValidateModel(modelInstance);
  }

  /**
   * Allows creating an new, empty {@link DmnModelInstance}.
   *
   * @return the empty model.
   */
  public static DmnModelInstance createEmptyModel() {
    return INSTANCE.doCreateEmptyModel();
  }

  /**
   * Register known types of the Dmn model
   */
  protected Dmn() {
    dmnModelBuilder = ModelBuilder.createInstance("CMMN Model");
    doRegisterTypes(dmnModelBuilder);
    dmnModel = dmnModelBuilder.build();
  }

  protected DmnModelInstance doReadModelFromFile(File file) {
    InputStream is = null;
    try {
      is = new FileInputStream(file);
      return doReadModelFromInputStream(is);

    } catch (FileNotFoundException e) {
      throw new DmnModelException("Cannot read model from file "+file+": file does not exist.");

    } finally {
      IoUtil.closeSilently(is);

    }
  }

  protected DmnModelInstance doReadModelFromInputStream(InputStream is) {
    return dmnParser.parseModelFromStream(is);
  }

  protected void doWriteModelToFile(File file, DmnModelInstance modelInstance) {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      doWriteModelToOutputStream(os, modelInstance);
    }
    catch (FileNotFoundException e) {
      throw new DmnModelException("Cannot write model to file "+file+": file does not exist.");
    } finally {
      IoUtil.closeSilently(os);
    }
  }

  protected void doWriteModelToOutputStream(OutputStream os, DmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // write XML
    IoUtil.writeDocumentToOutputStream(modelInstance.getDocument(), os);
  }

  protected String doConvertToString(DmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // convert to XML string
    return IoUtil.convertXmlDocumentToString(modelInstance.getDocument());
  }

  protected void doValidateModel(DmnModelInstance modelInstance) {
    dmnParser.validateModel(modelInstance.getDocument());
  }

  protected DmnModelInstance doCreateEmptyModel() {
    return dmnParser.getEmptyModel();
  }

  protected void doRegisterTypes(ModelBuilder modelBuilder) {

    AllowedAnswersImpl.registerType(modelBuilder);
    AllowedValueImpl.registerType(modelBuilder);
    AuthorityRequirementImpl.registerType(modelBuilder);
    BindingImpl.registerType(modelBuilder);
    BusinessContextElementImpl.registerType(modelBuilder);
    BusinessKnowledgeModelImpl.registerType(modelBuilder);
    ClauseImpl.registerType(modelBuilder);
    ColumnImpl.registerType(modelBuilder);
    ConclusionImpl.registerType(modelBuilder);
    ConditionImpl.registerType(modelBuilder);
    ContextEntryImpl.registerType(modelBuilder);
    ContextImpl.registerType(modelBuilder);
    DecisionImpl.registerType(modelBuilder);
    DecisionMadeReferenceImpl.registerType(modelBuilder);
    DecisionMakerReferenceImpl.registerType(modelBuilder);
    DecisionOwnedReferenceImpl.registerType(modelBuilder);
    DecisionOwnerReferenceImpl.registerType(modelBuilder);
    DecisionRuleImpl.registerType(modelBuilder);
    DecisionTableImpl.registerType(modelBuilder);
    DefinitionsImpl.registerType(modelBuilder);
    DescriptionImpl.registerType(modelBuilder);
    DmnElementImpl.registerType(modelBuilder);
    DmnElementReferenceImpl.registerType(modelBuilder);
    DrgElementImpl.registerType(modelBuilder);
    DrgElementReferenceImpl.registerType(modelBuilder);
    ElementCollectionImpl.registerType(modelBuilder);
    ExpressionImpl.registerType(modelBuilder);
    FunctionDefinitionImpl.registerType(modelBuilder);
    ImpactedPerformanceIndicatorReferenceImpl.registerType(modelBuilder);
    ImpactingDecisionReferenceImpl.registerType(modelBuilder);
    ImportImpl.registerType(modelBuilder);
    InformationItemImpl.registerType(modelBuilder);
    InformationRequirementImpl.registerType(modelBuilder);
    InputDataImpl.registerType(modelBuilder);
    InputEntryImpl.registerType(modelBuilder);
    InputExpressionImpl.registerType(modelBuilder);
    InputVariableReferenceImpl.registerType(modelBuilder);
    InvocationImpl.registerType(modelBuilder);
    ItemComponentImpl.registerType(modelBuilder);
    ItemDefinitionImpl.registerType(modelBuilder);
    ItemDefinitionReferenceImpl.registerType(modelBuilder);
    KnowledgeRequirementImpl.registerType(modelBuilder);
    KnowledgeSourceImpl.registerType(modelBuilder);
    ListImpl.registerType(modelBuilder);
    LiteralExpressionImpl.registerType(modelBuilder);
    ModelElementInstanceImpl.registerType(modelBuilder);
    NamedDmnElementImpl.registerType(modelBuilder);
    OrganizationUnitImpl.registerType(modelBuilder);
    OutputDefinitionReferenceImpl.registerType(modelBuilder);
    OutputEntryImpl.registerType(modelBuilder);
    OwnerReferenceImpl.registerType(modelBuilder);
    ParameterReferenceImpl.registerType(modelBuilder);
    PerformanceIndicatorImpl.registerType(modelBuilder);
    QuestionImpl.registerType(modelBuilder);
    RelationImpl.registerType(modelBuilder);
    RequiredAuthorityReferenceImpl.registerType(modelBuilder);
    RequiredDecisionReferenceImpl.registerType(modelBuilder);
    RequiredInputReferenceImpl.registerType(modelBuilder);
    RequiredKnowledgeReferenceImpl.registerType(modelBuilder);
    RuleImpl.registerType(modelBuilder);
    SupportedObjectiveReferenceImpl.registerType(modelBuilder);
    TextImpl.registerType(modelBuilder);
    TypeDefinitionImpl.registerType(modelBuilder);
    TypeImpl.registerType(modelBuilder);
    TypeRefImpl.registerType(modelBuilder);
    UsingProcessReferenceImpl.registerType(modelBuilder);
    UsingTaskReferenceImpl.registerType(modelBuilder);

    /** camunda extensions */
  }

  /**
   * @return the {@link Model} instance to use
   */
  public Model getDmnModel() {
    return dmnModel;
  }

  public ModelBuilder getDmnModelBuilder() {
    return dmnModelBuilder;
  }

  /**
   * @param dmnModel the cmmnModel to set
   */
  public void setDmnModel(Model dmnModel) {
    this.dmnModel = dmnModel;
  }

}

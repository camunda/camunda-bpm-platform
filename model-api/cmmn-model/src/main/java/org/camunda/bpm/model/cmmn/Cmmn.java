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
package org.camunda.bpm.model.cmmn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.camunda.bpm.model.cmmn.impl.CmmnParser;
import org.camunda.bpm.model.cmmn.impl.instance.ApplicabilityRuleImpl;
import org.camunda.bpm.model.cmmn.impl.instance.BindingRefinementExpression;
import org.camunda.bpm.model.cmmn.impl.instance.Body;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemDefinitionImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemOnPartImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemStartTriggerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileItemTransitionStandardEvent;
import org.camunda.bpm.model.cmmn.impl.instance.CaseFileModel;
import org.camunda.bpm.model.cmmn.impl.instance.CaseImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseParameterImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.impl.instance.CaseRoles;
import org.camunda.bpm.model.cmmn.impl.instance.CaseTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ChildrenImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CmmnElementImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.impl.instance.DefaultControl;
import org.camunda.bpm.model.cmmn.impl.instance.DefinitionsImpl;
import org.camunda.bpm.model.cmmn.impl.instance.DiscretionaryItemImpl;
import org.camunda.bpm.model.cmmn.impl.instance.EventListenerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ExpressionImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ExtensionElementsImpl;
import org.camunda.bpm.model.cmmn.impl.instance.HumanTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.IfPartImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ImportImpl;
import org.camunda.bpm.model.cmmn.impl.instance.InputCaseParameter;
import org.camunda.bpm.model.cmmn.impl.instance.InputProcessParameter;
import org.camunda.bpm.model.cmmn.impl.instance.InputsCaseParameter;
import org.camunda.bpm.model.cmmn.impl.instance.ManualActivationRuleImpl;
import org.camunda.bpm.model.cmmn.impl.instance.MilestoneImpl;
import org.camunda.bpm.model.cmmn.impl.instance.OnPartImpl;
import org.camunda.bpm.model.cmmn.impl.instance.OutputCaseParameter;
import org.camunda.bpm.model.cmmn.impl.instance.OutputProcessParameter;
import org.camunda.bpm.model.cmmn.impl.instance.OutputsCaseParameter;
import org.camunda.bpm.model.cmmn.impl.instance.ParameterImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ParameterMappingImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanFragmentImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemControlImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemDefinitionImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemOnPartImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemStartTriggerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemTransitionStandardEvent;
import org.camunda.bpm.model.cmmn.impl.instance.PlanningTableImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ProcessImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ProcessParameterImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ProcessTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PropertyImpl;
import org.camunda.bpm.model.cmmn.impl.instance.RepetitionRuleImpl;
import org.camunda.bpm.model.cmmn.impl.instance.RequiredRuleImpl;
import org.camunda.bpm.model.cmmn.impl.instance.RoleImpl;
import org.camunda.bpm.model.cmmn.impl.instance.SentryImpl;
import org.camunda.bpm.model.cmmn.impl.instance.StageImpl;
import org.camunda.bpm.model.cmmn.impl.instance.StartTriggerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.TableItemImpl;
import org.camunda.bpm.model.cmmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.TimerEventListenerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.TimerExpression;
import org.camunda.bpm.model.cmmn.impl.instance.TransformationExpression;
import org.camunda.bpm.model.cmmn.impl.instance.UserEventListenerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.camunda.CamundaInImpl;
import org.camunda.bpm.model.cmmn.impl.instance.camunda.CamundaOutImpl;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.IoUtil;

/**
 * @author Roman Smirnov
 *
 */
public class Cmmn {

  /** the singleton instance of {@link Cmmn}. If you want to customize the behavior of Cmmn,
   * replace this instance with an instance of a custom subclass of {@link Cmmn}. */
  public static Cmmn INSTANCE = new Cmmn();

  /** the parser used by the Cmmn implementation. */
  private CmmnParser cmmnParser = new CmmnParser();
  private final ModelBuilder cmmnModelBuilder;

  /** The {@link Model}
   */
  private Model cmmnModel;

  /**
   * Allows reading a {@link CmmnModelInstance} from a File.
   *
   * @param file the {@link File} to read the {@link CmmnModelInstance} from
   * @return the model read
   * @throws CmmnModelException if the model cannot be read
   */
  public static CmmnModelInstance readModelFromFile(File file) {
    return INSTANCE.doReadModelFromFile(file);
  }

  /**
   * Allows reading a {@link CmmnModelInstance} from an {@link InputStream}
   *
   * @param stream the {@link InputStream} to read the {@link CmmnModelInstance} from
   * @return the model read
   * @throws ModelParseException if the model cannot be read
   */
  public static CmmnModelInstance readModelFromStream(InputStream stream) {
    return INSTANCE.doReadModelFromInputStream(stream);
  }

  /**
   * Allows writing a {@link CmmnModelInstance} to a File. It will be
   * validated before writing.
   *
   * @param file the {@link File} to write the {@link CmmnModelInstance} to
   * @param modelInstance the {@link CmmnModelInstance} to write
   * @throws CmmnModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToFile(File file, CmmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToFile(file, modelInstance);
  }

  /**
   * Allows writing a {@link CmmnModelInstance} to an {@link OutputStream}. It will be
   * validated before writing.
   *
   * @param stream the {@link OutputStream} to write the {@link CmmnModelInstance} to
   * @param modelInstance the {@link CmmnModelInstance} to write
   * @throws ModelException if the model cannot be written
   * @throws ModelValidationException if the model is not valid
   */
  public static void writeModelToStream(OutputStream stream, CmmnModelInstance modelInstance) {
    INSTANCE.doWriteModelToOutputStream(stream, modelInstance);
  }

  /**
   * Allows the conversion of a {@link CmmnModelInstance} to an {@link String}. It will
   * be validated before conversion.
   *
   * @param modelInstance  the model instance to convert
   * @return the XML string representation of the model instance
   */
  public static String convertToString(CmmnModelInstance modelInstance) {
    return INSTANCE.doConvertToString(modelInstance);
  }

  /**
   * Validate model DOM document
   *
   * @param modelInstance the {@link CmmnModelInstance} to validate
   * @throws ModelValidationException if the model is not valid
   */
  public static void validateModel(CmmnModelInstance modelInstance) {
    INSTANCE.doValidateModel(modelInstance);
  }

  /**
   * Allows creating an new, empty {@link CmmnModelInstance}.
   *
   * @return the empty model.
   */
  public static CmmnModelInstance createEmptyModel() {
    return INSTANCE.doCreateEmptyModel();
  }

  /**
   * Register known types of the Cmmn model
   */
  protected Cmmn() {
    cmmnModelBuilder = ModelBuilder.createInstance("CMMN Model");
    doRegisterTypes(cmmnModelBuilder);
    cmmnModel = cmmnModelBuilder.build();
  }

  protected CmmnModelInstance doReadModelFromFile(File file) {
    InputStream is = null;
    try {
      is = new FileInputStream(file);
      return doReadModelFromInputStream(is);

    } catch (FileNotFoundException e) {
      throw new CmmnModelException("Cannot read model from file "+file+": file does not exist.");

    } finally {
      IoUtil.closeSilently(is);

    }
  }

  protected CmmnModelInstance doReadModelFromInputStream(InputStream is) {
    return cmmnParser.parseModelFromStream(is);
  }

  protected void doWriteModelToFile(File file, CmmnModelInstance modelInstance) {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      doWriteModelToOutputStream(os, modelInstance);
    }
    catch (FileNotFoundException e) {
      throw new CmmnModelException("Cannot write model to file "+file+": file does not exist.");
    } finally {
      IoUtil.closeSilently(os);
    }
  }

  protected void doWriteModelToOutputStream(OutputStream os, CmmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // write XML
    IoUtil.writeDocumentToOutputStream(modelInstance.getDocument(), os);
  }

  protected String doConvertToString(CmmnModelInstance modelInstance) {
    // validate DOM document
    doValidateModel(modelInstance);
    // convert to XML string
    return IoUtil.convertXmlDocumentToString(modelInstance.getDocument());
  }

  protected void doValidateModel(CmmnModelInstance modelInstance) {
    cmmnParser.validateModel(modelInstance.getDocument());
  }

  protected CmmnModelInstance doCreateEmptyModel() {
    return cmmnParser.getEmptyModel();
  }

  protected void doRegisterTypes(ModelBuilder modelBuilder) {
    ApplicabilityRuleImpl.registerType(modelBuilder);
    BindingRefinementExpression.registerType(modelBuilder);
    Body.registerType(modelBuilder);
    CaseFileImpl.registerType(modelBuilder);
    CaseFileItemDefinitionImpl.registerType(modelBuilder);
    CaseFileItemImpl.registerType(modelBuilder);
    CaseFileItemOnPartImpl.registerType(modelBuilder);
    CaseFileItemStartTriggerImpl.registerType(modelBuilder);
    CaseFileItemTransitionStandardEvent.registerType(modelBuilder);
    CaseFileModel.registerType(modelBuilder);
    CaseImpl.registerType(modelBuilder);
    CaseParameterImpl.registerType(modelBuilder);
    CasePlanModel.registerType(modelBuilder);
    CaseRoles.registerType(modelBuilder);
    CaseTaskImpl.registerType(modelBuilder);
    ChildrenImpl.registerType(modelBuilder);
    CmmnElementImpl.registerType(modelBuilder);
    ConditionExpression.registerType(modelBuilder);
    DefaultControl.registerType(modelBuilder);
    DefinitionsImpl.registerType(modelBuilder);
    DiscretionaryItemImpl.registerType(modelBuilder);
    EventListenerImpl.registerType(modelBuilder);
    ExpressionImpl.registerType(modelBuilder);
    ExtensionElementsImpl.registerType(modelBuilder);
    HumanTaskImpl.registerType(modelBuilder);
    IfPartImpl.registerType(modelBuilder);
    ImportImpl.registerType(modelBuilder);
    InputCaseParameter.registerType(modelBuilder);
    InputProcessParameter.registerType(modelBuilder);
    InputsCaseParameter.registerType(modelBuilder);
    ManualActivationRuleImpl.registerType(modelBuilder);
    MilestoneImpl.registerType(modelBuilder);
    ModelElementInstanceImpl.registerType(modelBuilder);
    OnPartImpl.registerType(modelBuilder);
    OutputCaseParameter.registerType(modelBuilder);
    OutputProcessParameter.registerType(modelBuilder);
    OutputsCaseParameter.registerType(modelBuilder);
    ParameterImpl.registerType(modelBuilder);
    ParameterMappingImpl.registerType(modelBuilder);
    PlanFragmentImpl.registerType(modelBuilder);
    PlanItemControlImpl.registerType(modelBuilder);
    PlanItemDefinitionImpl.registerType(modelBuilder);
    PlanItemImpl.registerType(modelBuilder);
    PlanItemOnPartImpl.registerType(modelBuilder);
    PlanItemStartTriggerImpl.registerType(modelBuilder);
    PlanItemTransitionStandardEvent.registerType(modelBuilder);
    PlanningTableImpl.registerType(modelBuilder);
    ProcessImpl.registerType(modelBuilder);
    ProcessParameterImpl.registerType(modelBuilder);
    ProcessTaskImpl.registerType(modelBuilder);
    PropertyImpl.registerType(modelBuilder);
    RepetitionRuleImpl.registerType(modelBuilder);
    RequiredRuleImpl.registerType(modelBuilder);
    RoleImpl.registerType(modelBuilder);
    SentryImpl.registerType(modelBuilder);
    StageImpl.registerType(modelBuilder);
    StartTriggerImpl.registerType(modelBuilder);
    TableItemImpl.registerType(modelBuilder);
    TaskImpl.registerType(modelBuilder);
    TimerEventListenerImpl.registerType(modelBuilder);
    TimerExpression.registerType(modelBuilder);
    TransformationExpression.registerType(modelBuilder);
    UserEventListenerImpl.registerType(modelBuilder);

    /** camunda extensions */
    CamundaInImpl.registerType(modelBuilder);
    CamundaOutImpl.registerType(modelBuilder);
  }

  /**
   * @return the {@link Model} instance to use
   */
  public Model getCmmnModel() {
    return cmmnModel;
  }

  public ModelBuilder getCmmnModelBuilder() {
    return cmmnModelBuilder;
  }

  /**
   * @param cmmnModel the cmmnModel to set
   */
  public void setCmmnModel(Model cmmnModel) {
    this.cmmnModel = cmmnModel;
  }

}

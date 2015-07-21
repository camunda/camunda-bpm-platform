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

package org.camunda.bpm.model.dmn.impl;

public final class DmnModelConstants {

  /** The DMN 1.0 namespace */
  public static final String DMN10_NS = "http://www.omg.org/spec/DMN/20130901";

  /** The location of the DMN 1.0 XML schema. */
  public static final String DMN_10_SCHEMA_LOCATION = "DMN10.xsd";

  /** The FEEL namespace */
  public static final String FEEL_NS = "http://www.omg.org/spec/FEEL/20140401";

  /** The location of the FEEL schema. */
  public static final String FEEL_SCHEMA_LOCATION = "FEEL.xsd";

  /** Camunda namespace */
  public static final String CAMUNDA_NS = "http://camunda.org/schema/1.0/dmn";

  /** DMN element */

  public static final String DMN_ELEMENT = "DMNElement";
  public static final String DMN_ELEMENT_ALLOWED_ANSWERS = "allowedAnswers";
  public static final String DMN_ELEMENT_ALLOWED_VALUE = "allowedValue";
  public static final String DMN_ELEMENT_AUTHORITY_REQUIREMENT = "authorityRequirement";
  public static final String DMN_ELEMENT_BINDING = "binding";
  public static final String DMN_ELEMENT_BUSINESS_CONTEXT_ELEMENT = "BusinessContextElement";
  public static final String DMN_ELEMENT_BUSINESS_KNOWLEDGE_MODEL = "BusinessKnowledgeModel";
  public static final String DMN_ELEMENT_CLAUSE = "clause";
  public static final String DMN_ELEMENT_CONCLUSION = "conclusion";
  public static final String DMN_ELEMENT_CONDITION = "condition";
  public static final String DMN_ELEMENT_DECISION = "Decision";
  public static final String DMN_ELEMENT_DECISION_MADE = "decisionMade";
  public static final String DMN_ELEMENT_DECISION_MAKER = "decisionMaker";
  public static final String DMN_ELEMENT_DECISION_OWNED = "decisionOwned";
  public static final String DMN_ELEMENT_DECISION_OWNER = "decisionOwner";
  public static final String DMN_ELEMENT_DECISION_RULE = "DecisionRule";
  public static final String DMN_ELEMENT_DECISION_TABLE = "DecisionTable";
  public static final String DMN_ELEMENT_DEFINITIONS = "Definitions";
  public static final String DMN_ELEMENT_DESCRIPTION = "description";
  public static final String DMN_ELEMENT_DRG_ELEMENT = "DRGElement";
  public static final String DMN_ELEMENT_DRG_ELEMENT_REFERENCE = "drgElement";
  public static final String DMN_ELEMENT_ELEMENT_COLLECTION = "ElementCollection";
  public static final String DMN_ELEMENT_EXPRESSION = "Expression";
  public static final String DMN_ELEMENT_IMPACTED_PERFORMANCE_INDICATOR = "impactedPerformanceIndicator";
  public static final String DMN_ELEMENT_IMPACTING_DECISION = "impactingDecision";
  public static final String DMN_ELEMENT_IMPORT = "Import";
  public static final String DMN_ELEMENT_INFORMATION_ITEM = "InformationItem";
  public static final String DMN_ELEMENT_INFORMATION_REQUIREMENT = "informationRequirement";
  public static final String DMN_ELEMENT_INPUT_DATA = "InputData";
  public static final String DMN_ELEMENT_INPUT_ENTRY = "inputEntry";
  public static final String DMN_ELEMENT_INPUT_EXPRESSION = "inputExpression";
  public static final String DMN_ELEMENT_INPUT_VARIABLE = "inputVariable";
  public static final String DMN_ELEMENT_INVOCATION = "Invocation";
  public static final String DMN_ELEMENT_ITEM_COMPONENT = "itemComponent";
  public static final String DMN_ELEMENT_ITEM_DEFINITION = "ItemDefinition";
  public static final String DMN_ELEMENT_ITEM_DEFINITION_REFERENCE = "itemDefinition";
  public static final String DMN_ELEMENT_KNOWLEDGE_REQUIREMENT = "knowledgeRequirement";
  public static final String DMN_ELEMENT_KNOWLEDGE_SOURCE = "KnowledgeSource";
  public static final String DMN_ELEMENT_LITERAL_EXPRESSION = "LiteralExpression";
  public static final String DMN_ELEMENT_ORGANIZATION_UNIT = "OrganizationUnit";
  public static final String DMN_ELEMENT_OUTPUT_DEFINITION = "outputDefinition";
  public static final String DMN_ELEMENT_OUTPUT_ENTRY = "outputEntry";
  public static final String DMN_ELEMENT_OWNER = "owner";
  public static final String DMN_ELEMENT_PARAMETER = "parameter";
  public static final String DMN_ELEMENT_PERFORMANCE_INDICATOR = "PerformanceIndicator";
  public static final String DMN_ELEMENT_QUESTION = "question";
  public static final String DMN_ELEMENT_REFERENCE = "DMNElementReference";
  public static final String DMN_ELEMENT_REQUIRED_AUTHORITY = "requiredAuthority";
  public static final String DMN_ELEMENT_REQUIRED_DECISION = "requiredDecision";
  public static final String DMN_ELEMENT_REQUIRED_INPUT = "requiredInput";
  public static final String DMN_ELEMENT_REQUIRED_KNOWLEDGE = "requiredKnowledge";
  public static final String DMN_ELEMENT_RULE = "rule";
  public static final String DMN_ELEMENT_SUPPORTED_OBJECT = "supportedObjective";
  public static final String DMN_ELEMENT_TEXT = "text";
  public static final String DMN_ELEMENT_TYPE = "type";
  public static final String DMN_ELEMENT_TYPE_DEFINITION = "typeDefinition";
  public static final String DMN_ELEMENT_TYPE_REF = "typeRef";
  public static final String DMN_ELEMENT_USING_PROCESS = "usingProcess";
  public static final String DMN_ELEMENT_USING_TASK = "usingTask";

  /** DMN attributes */

  public static final String DMN_ATTRIBUTE_AGGREGATION = "aggregation";
  public static final String DMN_ATTRIBUTE_EXPRESSION_LANGUAGE = "expressionLanguage";
  public static final String DMN_ATTRIBUTE_HIT_POLICY = "hitPolicy";
  public static final String DMN_ATTRIBUTE_HREF = "href";
  public static final String DMN_ATTRIBUTE_ID = "id";
  public static final String DMN_ATTRIBUTE_IMPORT_TYPE = "importType";
  public static final String DMN_ATTRIBUTE_IS_COLLECTION = "isCollection";
  public static final String DMN_ATTRIBUTE_IS_COMPLETE = "isComplete";
  public static final String DMN_ATTRIBUTE_IS_CONSISTENT = "isConsistent";
  public static final String DMN_ATTRIBUTE_IS_ORDERED = "isOrdered";
  public static final String DMN_ATTRIBUTE_LOCATION_URI = "locationURI";
  public static final String DMN_ATTRIBUTE_NAME = "name";
  public static final String DMN_ATTRIBUTE_NAMESPACE = "namespace";
  public static final String DMN_ATTRIBUTE_PREFERED_ORIENTATION = "preferedOrientation";
  public static final String DMN_ATTRIBUTE_TYPE_LANGUAGE = "typeLanguage";
  public static final String DMN_ATTRIBUTE_URI = "URI";

  /** FEEL elements */

  public static final String FEEL_ELEMENT_CONTEXT = "Context";
  public static final String FEEL_ELEMENT_FUNCTION_DEFINITION = "FunctionDefinition";
  public static final String FEEL_ELEMENT_LIST = "List";
  public static final String FEEL_ELEMENT_RELATION = "Relation";
  public static final String FEEL_ELEMENT_COLUMN = "column";
  public static final String FEEL_ELEMENT_CONTEXT_ENTRY = "contextEntry";

  /** camunda extensions */

  public static final String CAMUNDA_ATTRIBUTE_OUTPUT = "output";

}

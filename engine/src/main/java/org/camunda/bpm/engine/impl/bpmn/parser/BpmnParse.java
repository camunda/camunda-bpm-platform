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
package org.camunda.bpm.engine.impl.bpmn.parser;

import java.io.InputStream;
import java.net.URL;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelBoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivitiBehaviour;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchLinkEventActivityBehaviour;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ShellActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SignalEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TransactionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.ClassDelegate;
import org.camunda.bpm.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.DelegateExpressionTaskListener;
import org.camunda.bpm.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.ExpressionTaskListener;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.el.UelExpressionCondition;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DefaultTaskFormHandler;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.TaskFormHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.HasDIBounds;
import org.camunda.bpm.engine.impl.pvm.process.Lane;
import org.camunda.bpm.engine.impl.pvm.process.LaneSet;
import org.camunda.bpm.engine.impl.pvm.process.ParticipantProcess;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.scripting.ScriptingEngines;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Parse;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 *
 * @author Tom Baeyens
 * @author Bernd Ruecker
 * @author Joram Barrez
 * @author Christian Stettler
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Esteban Robles
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 * @author Nico Rehwaldt
 */
public class BpmnParse extends Parse {

  protected static final Logger LOGGER = Logger.getLogger(BpmnParse.class.getName());

  public static final String PROPERTYNAME_DOCUMENTATION = "documentation";
  public static final String PROPERTYNAME_INITIAL = "initial";
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";
  public static final String PROPERTYNAME_VARIABLE_DECLARATIONS = "variableDeclarations";
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  public static final String PROPERTYNAME_MESSAGE_JOB_DECLARATION = "messageJobDeclaration";
  public static final String PROPERTYNAME_ISEXPANDED = "isExpanded";
  public static final String PROPERTYNAME_START_TIMER = "timerStart";
  public static final String PROPERTYNAME_COMPENSATION_HANDLER_ID = "compensationHandler";
  public static final String PROPERTYNAME_IS_FOR_COMPENSATION = "isForCompensation";
  public static final String PROPERTYNAME_ERROR_EVENT_DEFINITIONS = "errorEventDefinitions";
  public static final String PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION = "eventDefinitions";
  public static final String PROPERTYNAME_TRIGGERED_BY_EVENT = "triggeredByEvent";
  public static final String PROPERTYNAME_TYPE = "type";

  /* process start authorization specific finals */
  protected static final String POTENTIAL_STARTER = "potentialStarter";
  protected static final String CANDIDATE_STARTER_USERS_EXTENSION = "candidateStarterUsers";
  protected static final String CANDIDATE_STARTER_GROUPS_EXTENSION = "candidateStarterGroups";

  protected static final String ATTRIBUTEVALUE_T_FORMAL_EXPRESSION = BpmnParser.BPMN20_NS + ":tFormalExpression";

  /** The deployment to which the parsed process definitions will be added. */
  protected DeploymentEntity deployment;

  /** The end result of the parsing: a list of process definition. */
  protected List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();

  /** Mapping of found errors in BPMN 2.0 file */
  protected Map<String, Error> errors = new HashMap<String, Error>();

  /** Mapping from a process definition key to his containing list of job declarations **/
  protected Map<String, List<JobDeclaration<?>>> jobDeclarations = new HashMap<String, List<JobDeclaration<?>>>();

  /** A map for storing sequence flow based on their id during parsing. */
  protected Map<String, TransitionImpl> sequenceFlows;

  /** A list of all element IDs. This allows us to parse only what we actually support but
   * still validate the references among elements we do not support. */
  protected List<String> elementIds = new ArrayList<String>();

  /** A map for storing the process references of participants */
  protected Map<String, String> participantProcesses = new HashMap<String, String>();

  /**
   * Mapping containing values stored during the first phase of parsing since
   * other elements can reference these messages.
   *
   * All the map's elements are defined outside the process definition(s), which
   * means that this map doesn't need to be re-initialized for each new process
   * definition.
   */
  protected Map<String, MessageDefinition> messages = new HashMap<String, MessageDefinition>();
  protected Map<String, SignalDefinition> signals = new HashMap<String, SignalDefinition>();



  // Members
  protected ExpressionManager expressionManager;
  protected List<BpmnParseListener> parseListeners;
  protected Map<String, XMLImporter> importers = new HashMap<String, XMLImporter>();
  protected Map<String, String> prefixs = new HashMap<String, String>();
  protected String targetNamespace;

  private Map<String, String> eventLinkTargets = new HashMap<String, String>();
  private Map<String, String> eventLinkSources = new HashMap<String, String>();

  /**
   * Constructor to be called by the {@link BpmnParser}.
   */
  public BpmnParse(BpmnParser parser) {
    super(parser);
    this.expressionManager = parser.getExpressionManager();
    this.parseListeners = parser.getParseListeners();
    setSchemaResource(ReflectUtil.getResource(BpmnParser.BPMN_20_SCHEMA_LOCATION).toString());
  }

  public BpmnParse deployment(DeploymentEntity deployment) {
    this.deployment = deployment;
    return this;
  }

  @Override
  public BpmnParse execute() {
    super.execute(); // schema validation

    try {
      parseRootElement();

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown exception", e);

      // ALL unexpected exceptions should bubble up since they are not handled
      // accordingly by underlying parse-methods and the process can't be deployed
      throw new ProcessEngineException("Error while parsing process: " + e.getMessage(), e);

    } finally {
      if (hasWarnings()) {
        logWarnings();
      }
      if (hasErrors()) {
        throwExceptionForErrors();
      }
    }

    return this;
  }

  /**
   * Parses the 'definitions' root element
   */
  protected void parseRootElement() {
    collectElementIds();
    parseDefinitionsAttributes();
    parseImports();
    parseMessages();
    parseErrors();
    parseSignals();
    parseProcessDefinitions();
    parseCollaboration();

    // Diagram interchange parsing must be after parseProcessDefinitions,
    // since it depends and sets values on existing process definition objects
    parseDiagramInterchangeElements();

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseRootElement(rootElement, getProcessDefinitions());
    }
  }

  protected void collectElementIds() {
    rootElement.collectIds(elementIds);
  }

  protected void parseDefinitionsAttributes() {
    this.targetNamespace = rootElement.attribute("targetNamespace");

    for (String attribute : rootElement.attributes()) {
      if (attribute.startsWith("xmlns:")) {
        String prefixValue = rootElement.attribute(attribute);
        String prefixName = attribute.substring(6);
        this.prefixs.put(prefixName, prefixValue);
      }
    }
  }

  protected String resolveName(String name) {
    if (name == null) {
      return null;
    }
    int indexOfP = name.indexOf(':');
    if (indexOfP != -1) {
      String prefix = name.substring(0, indexOfP);
      String resolvedPrefix = this.prefixs.get(prefix);
      return resolvedPrefix + ":" + name.substring(indexOfP + 1);
    } else {
      return this.targetNamespace + ":" + name;
    }
  }

  /**
   * Parses the rootElement importing structures
   */
  protected void parseImports() {
    List<Element> imports = rootElement.elements("import");
    for (Element theImport : imports) {
      String importType = theImport.attribute("importType");
      XMLImporter importer = this.getImporter(importType, theImport);
      if (importer == null) {
        addError("Could not import item of type " + importType, theImport);
      } else {
        importer.importFrom(theImport, this);
      }
    }
  }

  protected XMLImporter getImporter(String importType, Element theImport) {
    if (this.importers.containsKey(importType)) {
      return this.importers.get(importType);
    } else {
      if (importType.equals("http://schemas.xmlsoap.org/wsdl/")) {
        Class< ? > wsdlImporterClass;
        try {
          wsdlImporterClass = Class.forName("org.camunda.bpm.engine.impl.webservice.CxfWSDLImporter", true, Thread.currentThread().getContextClassLoader());
          XMLImporter newInstance = (XMLImporter) wsdlImporterClass.newInstance();
          this.importers.put(importType, newInstance);
          return newInstance;
        } catch (Exception e) {
          addError("Could not find importer for type " + importType, theImport);
        }
      }
      return null;
    }
  }


  /**
   * Parses the messages of the given definitions file. Messages are not
   * contained within a process element, but they can be referenced from inner
   * process elements.
   */
  public void parseMessages() {
    for (Element messageElement : rootElement.elements("message")) {
      String id = messageElement.attribute("id");
      String name = messageElement.attribute("name");

      MessageDefinition messageDefinition = new MessageDefinition(this.targetNamespace + ":" + id, name);
      this.messages.put(messageDefinition.getId(), messageDefinition);
    }
  }

  /**
   * Parses the signals of the given definitions file. Signals are not
   * contained within a process element, but they can be referenced from inner
   * process elements.
   */
  protected void parseSignals() {
    for (Element signalElement : rootElement.elements("signal")) {
      String id = signalElement.attribute("id");
      String signalName = signalElement.attribute("name");

      for (SignalDefinition signalDefinition : signals.values()) {
        if(signalDefinition.getName().equals(signalName)) {
          addError("duplicate signal name '"+signalName+"'.", signalElement);
        }
      }

      if(id == null) {
        addError("signal must have an id", signalElement);
      }
        else if(signalName == null) {
          addError("signal with id '"+id+"' has no name", signalElement);

      }else {
        SignalDefinition signal = new SignalDefinition();
        signal.setId(this.targetNamespace + ":" + id);
        signal.setName(signalName);
        this.signals.put(signal.getId(), signal);
      }
    }
  }

  public void parseErrors() {
    for (Element errorElement : rootElement.elements("error")) {
      Error error = new Error();

      String id = errorElement.attribute("id");
      if (id == null) {
        addError("'id' is mandatory on error definition", errorElement);
      }
      error.setId(id);

      String errorCode = errorElement.attribute("errorCode");
      if (errorCode != null) {
        error.setErrorCode(errorCode);
      }

      errors.put(id, error);
    }
  }

  /**
   * Parses all the process definitions defined within the 'definitions' root
   * element.
   */
  public void parseProcessDefinitions() {
    for (Element processElement : rootElement.elements("process")) {
      boolean isExecutable = true;
      String isExecutableStr = processElement.attribute("isExecutable");
      if (isExecutableStr != null) {
        if (!Boolean.parseBoolean(isExecutableStr)) {
          isExecutable = false;
          LOGGER.info("Ignoring non-executable process with id='" + processElement.attribute("id") + "'. Set the attribute isExecutable=\"true\" to deploy this process.");
        }
      } else {
        LOGGER.info("Process with id='" + processElement.attribute("id") + "' has no attribute isExecutable. Assuming it is executable. Better set the attribute explicitely, especially to be compatible with future engine versions which might change the default behavior.");
      }

      //Only process executable processes
      if (isExecutable) {
        processDefinitions.add(parseProcess(processElement));
      }
    }
  }

  /**
   * Parses the collaboration definition defined within the 'definitions'
   * root element and get all participants to lookup their process references
   * during DI parsing.
   */
  public void parseCollaboration() {
    Element collaboration = rootElement.element("collaboration");
    if (collaboration != null) {
      for (Element participant : collaboration.elements("participant")) {
        String processRef = participant.attribute("processRef");
        if (processRef != null) {
          ProcessDefinitionImpl procDef = getProcessDefinition(processRef);
          if(procDef != null) {
            // Set participant process on the procDef, so it can get rendered later on if needed
            ParticipantProcess participantProcess = new ParticipantProcess();
            participantProcess.setId(participant.attribute("id"));
            participantProcess.setName(participant.attribute("name"));
            procDef.setParticipantProcess(participantProcess);

            participantProcesses.put(participantProcess.getId(), processRef);
          }
        }
      }
    }
  }

  /**
   * Parses one process (ie anything inside a &lt;process&gt; element).
   *
   * @param processElement
   *          The 'process' element.
   * @return The parsed version of the XML: a {@link ProcessDefinitionImpl}
   *         object.
   */
  public ProcessDefinitionEntity parseProcess(Element processElement) {
    // reset all mappings that are related to one process definition
    sequenceFlows = new HashMap<String, TransitionImpl>();

    ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntity();

    /*
     * Mapping object model - bpmn xml: processDefinition.id -> generated by
     * activiti engine processDefinition.key -> bpmn id (required)
     * processDefinition.name -> bpmn name (optional)
     */
    processDefinition.setKey(processElement.attribute("id"));
    processDefinition.setName(processElement.attribute("name"));
    processDefinition.setCategory(rootElement.attribute("targetNamespace"));
    processDefinition.setProperty(PROPERTYNAME_DOCUMENTATION, parseDocumentation(processElement));
    processDefinition.setTaskDefinitions(new HashMap<String, TaskDefinition>());
    processDefinition.setDeploymentId(deployment.getId());

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Parsing process " + processDefinition.getKey());
    }
    parseScope(processElement, processDefinition);

    // Parse any laneSets defined for this process
    parseLaneSets(processElement, processDefinition);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProcess(processElement, processDefinition);
    }

    // now we have parsed anything we can validate some stuff
    validateActivities(processDefinition.getActivities());

    return processDefinition;
  }

  protected void parseLaneSets(Element parentElement, ProcessDefinitionEntity processDefinition) {
    List<Element> laneSets = parentElement.elements("laneSet");

    if(laneSets != null && laneSets.size() > 0) {
      for(Element laneSetElement : laneSets) {
        LaneSet newLaneSet = new LaneSet();

        newLaneSet.setId(laneSetElement.attribute("id"));
        newLaneSet.setName(laneSetElement.attribute("name"));
        parseLanes(laneSetElement, newLaneSet);

        // Finally, add the set
        processDefinition.addLaneSet(newLaneSet);
      }
    }
  }

  protected void parseLanes(Element laneSetElement, LaneSet laneSet) {
    List<Element> lanes = laneSetElement.elements("lane");
    if(lanes != null && lanes.size() > 0) {
      for(Element laneElement : lanes) {
        // Parse basic attributes
        Lane lane = new Lane();
        lane.setId(laneElement.attribute("id"));
        lane.setName(laneElement.attribute("name"));

        // Parse ID's of flow-nodes that live inside this lane
        List<Element> flowNodeElements = laneElement.elements("flowNodeRef");
        if(flowNodeElements != null && flowNodeElements.size() > 0) {
          for(Element flowNodeElement : flowNodeElements) {
            lane.getFlowNodeIds().add(flowNodeElement.getText());
          }
        }

        laneSet.addLane(lane);
      }
    }
  }


  /**
   * Parses a scope: a process, subprocess, etc.
   *
   * Note that a process definition is a scope on itself.
   *
   * @param scopeElement
   *          The XML element defining the scope
   * @param parentScope
   *          The scope that contains the nested scope.
   */
  public void parseScope(Element scopeElement, ScopeImpl parentScope) {

    // Not yet supported on process level (PVM additions needed):
    // parseProperties(processElement);

    HashMap<String, Element> postponedElements  = new HashMap<String, Element>();

    parseStartEvents(scopeElement, parentScope);
    parseActivities(scopeElement, parentScope, postponedElements);
    parsePostponedElements(scopeElement, parentScope, postponedElements);
    parseEndEvents(scopeElement, parentScope);
    parseBoundaryEvents(scopeElement, parentScope);
    parseSequenceFlow(scopeElement, parentScope);
    parseExecutionListenersOnScope(scopeElement, parentScope);
    parseAssociations(scopeElement, parentScope);

    if(parentScope instanceof ProcessDefinition) {
      parseProcessDefinitionCustomExtensions(scopeElement, (ProcessDefinition) parentScope);
    }

    postponedElements.clear();
  }

  protected void parsePostponedElements(Element scopeElement, ScopeImpl parentScope, HashMap<String, Element> postponedElements) {
    for (Element postponedElement : postponedElements.values()) {
      if(parentScope.findActivity(postponedElement.attribute("id")) == null) { // check whether activity is already parsed
        if(postponedElement.getTagName().equals("intermediateCatchEvent")) {
          parseIntermediateCatchEvent(postponedElement, parentScope, false);
        }
      }
    }
  }

  protected void parseProcessDefinitionCustomExtensions(Element scopeElement, ProcessDefinition definition) {
    parseStartAuthorization(scopeElement, definition);
  }

  protected void parseStartAuthorization(Element scopeElement, ProcessDefinition definition) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) definition;

    // parse activiti:potentialStarters
    Element extentionsElement = scopeElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> potentialStarterElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, POTENTIAL_STARTER);

      for (Element potentialStarterElement : potentialStarterElements) {
        parsePotentialStarterResourceAssignment(potentialStarterElement, processDefinition);
      }
    }

    // parse activiti:candidateStarterUsers
    String candidateUsersString = scopeElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_STARTER_USERS_EXTENSION);
    if (candidateUsersString != null) {
      List<String> candidateUsers = parseCommaSeparatedList(candidateUsersString);
      for (String candidateUser : candidateUsers) {
        processDefinition.addCandidateStarterUserIdExpression(expressionManager.createExpression(candidateUser.trim()));
      }
    }

    // Candidate activiti:candidateStarterGroups
    String candidateGroupsString = scopeElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_STARTER_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      List<String> candidateGroups = parseCommaSeparatedList(candidateGroupsString);
      for (String candidateGroup : candidateGroups) {
        processDefinition.addCandidateStarterGroupIdExpression(expressionManager.createExpression(candidateGroup.trim()));
      }
    }

  }

  protected void parsePotentialStarterResourceAssignment(Element performerElement, ProcessDefinitionEntity processDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        List<String> assignmentExpressions = parseCommaSeparatedList(feElement.getText());
        for (String assignmentExpression : assignmentExpressions) {
          assignmentExpression = assignmentExpression.trim();
          if (assignmentExpression.startsWith(USER_PREFIX)) {
            String userAssignementId = getAssignmentId(assignmentExpression, USER_PREFIX);
            processDefinition.addCandidateStarterUserIdExpression(expressionManager.createExpression(userAssignementId));
          } else if (assignmentExpression.startsWith(GROUP_PREFIX)) {
            String groupAssignementId = getAssignmentId(assignmentExpression, GROUP_PREFIX);
            processDefinition.addCandidateStarterGroupIdExpression(expressionManager.createExpression(groupAssignementId));
          } else { // default: given string is a goupId, as-is.
            processDefinition.addCandidateStarterGroupIdExpression(expressionManager.createExpression(assignmentExpression));
          }
        }
      }
    }
  }

  protected void parseAssociations(Element scopeElement, ScopeImpl parentScope) {
    for (Element associationElement : scopeElement.elements("association")) {
      String sourceRef = associationElement.attribute("sourceRef");
      if(sourceRef == null) {
        addError("association element missing attribute 'sourceRef'", associationElement);
      }
      String targetRef = associationElement.attribute("targetRef");
      if(targetRef == null) {
        addError("association element missing attribute 'targetRef'", associationElement);
      }
      ActivityImpl sourceActivity = parentScope.findActivity(sourceRef);
      ActivityImpl targetActivity = parentScope.findActivity(targetRef);

      // an association may reference elements that are not parsed as activities (like for instance
      // text annotations so do not throw an exception if sourceActivity or targetActivity are null)
      // However, we make sure they reference 'something':
      if(sourceActivity == null && !elementIds.contains(sourceRef)) {
        addError("Invalid reference sourceRef '"+sourceRef+"' of association element ", associationElement);
      } else if(targetActivity == null && !elementIds.contains(targetRef)) {
        addError("Invalid reference targetRef '"+targetRef+"' of association element ", associationElement);
      } else {
        if(sourceActivity != null && sourceActivity.getProperty("type").equals("compensationBoundaryCatch")) {
          Object isForCompensation = targetActivity.getProperty(PROPERTYNAME_IS_FOR_COMPENSATION);
          if(isForCompensation == null || !(Boolean) isForCompensation) {
            addError("compensation boundary catch must be connected to element with isForCompensation=true", associationElement);
          } else {
            ActivityImpl compensatedActivity = sourceActivity.getParentActivity();
            compensatedActivity.setProperty(PROPERTYNAME_COMPENSATION_HANDLER_ID, targetActivity.getId());
          }
        }
      }
    }
  }

  /**
   * Parses the start events of a certain level in the process (process,
   * subprocess or another scope).
   *
   * @param parentElement
   *          The 'parent' element that contains the start events (process,
   *          subprocess).
   * @param scope
   *          The {@link ScopeImpl} to which the start events must be added.
   */
  public void parseStartEvents(Element parentElement, ScopeImpl scope) {
    List<Element> startEventElements = parentElement.elements("startEvent");
    List<ActivityImpl> startEventActivities = new ArrayList<ActivityImpl>();
    for (Element startEventElement : startEventElements) {

      ActivityImpl startEventActivity = createActivityOnScope(startEventElement, scope);
      parseAsynchronousContinuation(startEventElement, startEventActivity);

      if (scope instanceof ProcessDefinitionEntity) {
        parseProcessDefinitionStartEvent(startEventActivity, startEventElement, parentElement, scope);
        startEventActivities.add(startEventActivity);
      } else {
        parseScopeStartEvent(startEventActivity, startEventElement, parentElement, scope);
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseStartEvent(startEventElement, scope, startEventActivity);
      }
      parseExecutionListenersOnScope(startEventElement, startEventActivity);
    }

    if(scope instanceof ProcessDefinitionEntity) {
      selectInitial(startEventActivities, (ProcessDefinitionEntity) scope, parentElement);
      parseStartFormHandlers(startEventElements, (ProcessDefinitionEntity) scope);
    }
  }

  protected void selectInitial(List<ActivityImpl> startEventActivities, ProcessDefinitionEntity processDefinition, Element parentElement) {
    ActivityImpl initial = null;
    // validate that there is s single none start event / timer start event:
    for (ActivityImpl activityImpl : startEventActivities) {
      if(!activityImpl.getProperty("type").equals("messageStartEvent")) {
        if(initial == null) {
          initial = activityImpl;
        } else {
          addError("multiple none start events or timer start events not supported on process definition", parentElement);
        }
      }
    }
    // if there is a single start event, select it as initial, regardless of it's type:
    if(initial == null && startEventActivities.size() == 1) {
      initial = startEventActivities.get(0);
    }
    processDefinition.setInitial(initial);
  }

  protected void parseProcessDefinitionStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ScopeImpl scope) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;

    String initiatorVariableName = startEventElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "initiator");
    if (initiatorVariableName != null) {
      processDefinition.setProperty(PROPERTYNAME_INITIATOR_VARIABLE_NAME, initiatorVariableName);
    }

    // all start events share the same behavior:
    startEventActivity.setActivityBehavior(new NoneStartEventActivityBehavior());

    Element timerEventDefinition = startEventElement.element("timerEventDefinition");
    Element messageEventDefinition = startEventElement.element("messageEventDefinition");
    if (timerEventDefinition != null) {
      parseTimerStartEventDefinition(timerEventDefinition, startEventActivity, processDefinition);
    } else if(messageEventDefinition != null) {
      EventSubscriptionDeclaration messageDefinition = parseMessageEventDefinition(messageEventDefinition);
      startEventActivity.setProperty("type", "messageStartEvent");
      messageDefinition.setActivityId(startEventActivity.getId());
      // create message event subscription:
      messageDefinition.setStartEvent(true);
      addEventSubscriptionDeclaration(messageDefinition, processDefinition, startEventElement);
    }
  }

  protected void parseStartFormHandlers(List<Element> startEventElements, ProcessDefinitionEntity processDefinition) {
    if(processDefinition.getInitial() != null) {
      for (Element startEventElement : startEventElements) {

        if(startEventElement.attribute("id").equals(processDefinition.getInitial().getId())) {

          StartFormHandler startFormHandler;
          String startFormHandlerClassName = startEventElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formHandlerClass");
          if (startFormHandlerClassName != null) {
            startFormHandler = (StartFormHandler) ReflectUtil.instantiate(startFormHandlerClassName);
          } else {
            startFormHandler = new DefaultStartFormHandler();
          }
          startFormHandler.parseConfiguration(startEventElement, deployment, processDefinition, this);

          processDefinition.setStartFormHandler(startFormHandler);
        }

      }
    }
  }

  protected void parseScopeStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ScopeImpl scope) {

    Object triggeredByEvent = scope.getProperty("triggeredByEvent");
    boolean isTriggeredByEvent = triggeredByEvent != null && ((Boolean) triggeredByEvent == true);

    Element errorEventDefinition = startEventElement.element("errorEventDefinition");
    Element messageEventDefinition = startEventElement.element("messageEventDefinition");
    Element signalEventDefinition = startEventElement.element("signalEventDefinition");

    if (isTriggeredByEvent) { // event subprocess

      // all start events of an event subprocess share common behavior
      EventSubProcessStartEventActivityBehavior activityBehavior = new EventSubProcessStartEventActivityBehavior(startEventActivity.getId());
      startEventActivity.setActivityBehavior(activityBehavior);

      String isInterrupting = startEventElement.attribute("isInterrupting");
      if(isInterrupting != null && (isInterrupting.equals("false")||isInterrupting.equals("FALSE"))) {
        activityBehavior.setInterrupting(false);
      }

      // the scope of the event subscription is the parent of the event
      // subprocess (subscription must be created when parent is initialized)
      ScopeImpl catchingScope = ((ActivityImpl) scope).getParent();

      if (errorEventDefinition != null) {
        if(!activityBehavior.isInterrupting()) {
          addError("error start event of event subprocess must be interrupting", startEventElement);
        }
        if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
            scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
            parseErrorStartEventDefinition(errorEventDefinition, startEventActivity, catchingScope);
          } else {
            addError("multiple start events not supported for subprocess", startEventElement);
          }

      } else if (messageEventDefinition != null) {
        EventSubscriptionDeclaration eventSubscriptionDeclaration = parseMessageEventDefinition(messageEventDefinition);
        eventSubscriptionDeclaration.setActivityId(startEventActivity.getId());
        eventSubscriptionDeclaration.setStartEvent(false);
        addEventSubscriptionDeclaration(eventSubscriptionDeclaration, catchingScope, messageEventDefinition);

      } else if (signalEventDefinition != null) {
        EventSubscriptionDeclaration eventSubscriptionDeclaration = parseSignalEventDefinition(signalEventDefinition);
        eventSubscriptionDeclaration.setActivityId(startEventActivity.getId());
        eventSubscriptionDeclaration.setStartEvent(false);
        addEventSubscriptionDeclaration(eventSubscriptionDeclaration, catchingScope, signalEventDefinition);

      } else {
        addError("start event of event subprocess must be of type 'error', 'message' or 'signal'", startEventElement);
      }

    } else { // "regular" subprocess
      Element conditionalEventDefinition = startEventElement.element("conditionalEventDefinition");
      Element timerEventDefinition = startEventElement.element("timerEventDefinition");
      Element escalationEventDefinition = startEventElement.element("escalationEventDefinition");
      Element compensateEventDefinition = startEventElement.element("compensateEventDefinition");

      if (conditionalEventDefinition != null) {
        addError("conditionalEventDefinition is not allowed on start event within a subprocess", conditionalEventDefinition);
      }
      if (timerEventDefinition != null) {
        addError("timerEventDefinition is not allowed on start event within a subprocess", timerEventDefinition);
      }
      if (escalationEventDefinition != null) {
        addError("escalationEventDefinition is not allowed on start event within a subprocess", escalationEventDefinition);
      }
      if (compensateEventDefinition != null) {
        addError("compensateEventDefinition is not allowed on start event within a subprocess", compensateEventDefinition);
      }
      if(errorEventDefinition != null) {
        addError("errorEventDefinition only allowed on start event if subprocess is an event subprocess", errorEventDefinition);
      }
      if(messageEventDefinition != null) {
        addError("messageEventDefinition only allowed on start event if subprocess is an event subprocess", messageEventDefinition);
      }
      if(signalEventDefinition != null) {
        addError("signalEventDefintion only allowed on start event if subprocess is an event subprocess", messageEventDefinition);
      }
      if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
        scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
        startEventActivity.setActivityBehavior(new NoneStartEventActivityBehavior());
      } else {
        addError("multiple start events not supported for subprocess", startEventElement);
      }
    }

  }

  protected void parseErrorStartEventDefinition(Element errorEventDefinition, ActivityImpl startEventActivity, ScopeImpl scope) {
    startEventActivity.setProperty("type", "errorStartEvent");
    String errorRef = errorEventDefinition.attribute("errorRef");
    Error error = null;
    ErrorEventDefinition definition = new ErrorEventDefinition(startEventActivity.getId());
    if (errorRef != null) {
      error = errors.get(errorRef);
      String errorCode = error == null ? errorRef : error.getErrorCode();
      definition.setErrorCode(errorCode);
    }
    definition.setPrecedence(10);
    addErrorEventDefinition(definition, scope);
  }

  protected EventSubscriptionDeclaration parseMessageEventDefinition(Element messageEventDefinition) {
    String messageRef = messageEventDefinition.attribute("messageRef");
    if(messageRef == null) {
      addError("attribute 'messageRef' is required", messageEventDefinition);
    }
    MessageDefinition messageDefinition = messages.get(resolveName(messageRef));
    if(messageDefinition == null) {
      addError("Invalid 'messageRef': no message with id '"+messageRef+"' found.", messageEventDefinition);
    }

    return new EventSubscriptionDeclaration(messageDefinition.getName(), MessageEventHandler.EVENT_HANDLER_TYPE);
  }

  @SuppressWarnings("unchecked")
  protected void addEventSubscriptionDeclaration(EventSubscriptionDeclaration subscription, ScopeImpl scope, Element element) {
    if (subscription.getEventType().equals("message")
         && (subscription.getEventName() == null
         || "".equalsIgnoreCase(subscription.getEventName().trim()))) {
      addError("Cannot have a message event subscription with an empty or missing name", element);
    }

    List<EventSubscriptionDeclaration> eventDefinitions = (List<EventSubscriptionDeclaration>) scope.getProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(eventDefinitions == null) {
      eventDefinitions = new ArrayList<EventSubscriptionDeclaration>();
      scope.setProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION, eventDefinitions);
    } else {
      // if this is a message event, validate that it is the only one with the provided name for this scope
      if(subscription.getEventType().equals("message")) {
        for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
          if(eventDefinition.getEventType().equals("message")
            && eventDefinition.getEventName().equals(subscription.getEventName())
            && eventDefinition.isStartEvent() == subscription.isStartEvent()) {
              addError("Cannot have more than one message event subscription with name '"+subscription.getEventName()+"' for scope '"+scope.getId()+"'", element);
          }
        }
      }
    }
    eventDefinitions.add(subscription);
  }

  /**
   * Parses the activities of a certain level in the process (process,
   * subprocess or another scope).
   *
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeImpl} to which the activities must be added.
   * @param postponedElements
   */
  public void parseActivities(Element parentElement, ScopeImpl scopeElement, HashMap<String, Element> postponedElements) {
    for (Element activityElement : parentElement.elements()) {
      parseActivity(activityElement, parentElement, scopeElement, postponedElements);
    }
  }

  protected void parseActivity(Element activityElement, Element parentElement, ScopeImpl scopeElement, HashMap<String, Element> postponedElements) {
    ActivityImpl activity = null;
    if (activityElement.getTagName().equals("exclusiveGateway")) {
      activity = parseExclusiveGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("inclusiveGateway")) {
      activity = parseInclusiveGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("parallelGateway")) {
      activity = parseParallelGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("scriptTask")) {
      activity = parseScriptTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("serviceTask")) {
      activity = parseServiceTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("businessRuleTask")) {
      activity = parseBusinessRuleTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("task")) {
      activity = parseTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("manualTask")) {
      activity = parseManualTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("userTask")) {
      activity = parseUserTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("sendTask")) {
      activity = parseSendTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("receiveTask")) {
      activity = parseReceiveTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("subProcess")) {
      activity = parseSubProcess(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("callActivity")) {
      activity = parseCallActivity(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("intermediateCatchEvent")) {
      // postpone all intermediate catch events (required for supporting event-based gw)
      postponedElements.put(activityElement.attribute("id"), activityElement);
    } else if (activityElement.getTagName().equals("intermediateThrowEvent")) {
      activity = parseIntermediateThrowEvent(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("eventBasedGateway")) {
      activity = parseEventBasedGateway(activityElement, parentElement, scopeElement);
    } else if(activityElement.getTagName().equals("transaction")) {
      activity = parseTransaction(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals("adHocSubProcess") || activityElement.getTagName().equals("complexGateway")) {
      addWarning("Ignoring unsupported activity type", activityElement);
    }

    // Parse stuff common to activities above
    if (activity != null) {
      parseMultiInstanceLoopCharacteristics(activityElement, activity);
    }
  }

  public void validateActivities(List<ActivityImpl> activities) {
    for (ActivityImpl activity : activities) {
      validateActivity(activity);
      // check children if it is an own scope / subprocess / ...
      if (activity.getActivities().size()>0) {
        validateActivities(activity.getActivities());
      }
    }
  }

  protected void validateActivity(ActivityImpl activity) {
    if (activity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior) {
      validateExclusiveGateway(activity);
    }
  }

  public void validateExclusiveGateway(ActivityImpl activity) {
    if (activity.getOutgoingTransitions().size()==0) {
      // TODO: double check if this is valid (I think in Activiti yes, since we need start events we will need an end event as well)
      addError("Exclusive Gateway '" + activity.getId() + "' has no outgoing sequence flows.", null);
    } else if (activity.getOutgoingTransitions().size()==1) {
      PvmTransition flow = activity.getOutgoingTransitions().get(0);
      Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
      if (condition!=null) {
        addError("Exclusive Gateway '" + activity.getId() + "' has only one outgoing sequence flow ('" + flow.getId() + "'). This is not allowed to have a condition.", null);
      }
    } else {
      String defaultSequenceFlow = (String) activity.getProperty("default");
      boolean hasDefaultFlow = defaultSequenceFlow!=null && defaultSequenceFlow.length()>0;

      ArrayList<PvmTransition> flowsWithoutCondition = new ArrayList<PvmTransition>();
      for (PvmTransition flow : activity.getOutgoingTransitions()) {
          Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
          boolean isDefaultFlow = flow.getId()!=null && flow.getId().equals(defaultSequenceFlow);
          boolean hasConditon = condition!=null;

          if (!hasConditon && !isDefaultFlow) {
            flowsWithoutCondition.add(flow);
          }
          if (hasConditon && isDefaultFlow) {
            addError("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' which is the default flow but has a condition too.", null);
          }
      }
      if (hasDefaultFlow || flowsWithoutCondition.size()>1) {
        // if we either have a default flow (then no flows without conditions are valid at all) or if we have more than one flow without condition this is an error
        for (PvmTransition flow : flowsWithoutCondition) {
          addError("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' without condition which is not the default flow.", null);
        }
      } else if (flowsWithoutCondition.size()==1) {
        // Havinf no default and exactly one flow without condition this is considered the default one now (to not break backward compatibility)
        PvmTransition flow = flowsWithoutCondition.get(0);
        addWarning("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' without condition which is not the default flow. We assume it to be the default flow, but it is bad modeling practice, better set the default flow in your gateway.", null);
      }
    }
  }

  public ActivityImpl parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scopeElement, boolean isAfterEventBasedGateway) {
    ActivityImpl nestedActivity = createActivityOnScope(intermediateEventElement, scopeElement);

    Element timerEventDefinition = intermediateEventElement.element("timerEventDefinition");
    Element signalEventDefinition = intermediateEventElement.element("signalEventDefinition");
    Element messageEventDefinition = intermediateEventElement.element("messageEventDefinition");
    Element linkEventDefinitionElement = intermediateEventElement.element("linkEventDefinition");

    // shared by all events except for link event
    IntermediateCatchEventActivitiBehaviour defaultCatchBehaviour = new IntermediateCatchEventActivitiBehaviour();

    if (timerEventDefinition != null) {
      nestedActivity.setActivityBehavior(defaultCatchBehaviour);
      parseIntemediateTimerEventDefinition(timerEventDefinition, nestedActivity, isAfterEventBasedGateway);

    } else if(signalEventDefinition != null) {
      nestedActivity.setActivityBehavior(defaultCatchBehaviour);
      parseIntemediateSignalEventDefinition(signalEventDefinition, nestedActivity, isAfterEventBasedGateway);

    } else if(messageEventDefinition != null) {
      nestedActivity.setActivityBehavior(defaultCatchBehaviour);
      parseIntermediateMessageEventDefinition(messageEventDefinition, nestedActivity, isAfterEventBasedGateway);

    } else if(linkEventDefinitionElement != null) {
      nestedActivity.setActivityBehavior(new IntermediateCatchLinkEventActivityBehaviour());
      parseIntermediateLinkEventCatchBehavior(intermediateEventElement, nestedActivity, linkEventDefinitionElement);

    } else {
      addError("Unsupported intermediate catch event type", intermediateEventElement);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateCatchEvent(intermediateEventElement, scopeElement, nestedActivity);
    }

    parseExecutionListenersOnScope(intermediateEventElement, nestedActivity);

    return nestedActivity;
  }

  protected void parseIntermediateLinkEventCatchBehavior(Element intermediateEventElement, ActivityImpl activity, Element linkEventDefinitionElement) {

    activity.setProperty("type", "intermediateLinkCatch");

    String linkName = linkEventDefinitionElement.attribute("name");
    String elementName = intermediateEventElement.attribute("name");
    String elementId = intermediateEventElement.attribute("id");

    if (eventLinkTargets.containsKey(linkName)) {
      addError("Multiple Intermediate Catch Events with the same link event name ('"+linkName+"') are not allowed.", intermediateEventElement);
    } else {
      if (!linkName.equals(elementName)) {
        // this is valid - but not a good practice (as it is really confusing for the reader of the process model) - hence we log a warning
        addWarning("Link Event named '" + elementName + "' containes link event definition with name '" + linkName + "' - it is recommended to use the same name for both." , intermediateEventElement);
      }

      // now we remember the link in order to replace the sequence flow later on
      eventLinkTargets.put(linkName, elementId);
    }
  }

  protected void parseIntermediateMessageEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity, boolean isAfterEventBasedGateway) {

    nestedActivity.setProperty("type", "intermediateMessageCatch");

    EventSubscriptionDeclaration messageDefinition =  parseMessageEventDefinition(messageEventDefinition);
    if(isAfterEventBasedGateway) {
      messageDefinition.setActivityId(nestedActivity.getId());
      addEventSubscriptionDeclaration(messageDefinition, nestedActivity.getParent(), messageEventDefinition);
    }else {
      nestedActivity.setScope(true);
      addEventSubscriptionDeclaration(messageDefinition, nestedActivity, messageEventDefinition);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateMessageCatchEventDefinition(messageEventDefinition, nestedActivity);
    }
  }

  public ActivityImpl parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scopeElement) {
    Element signalEventDefinitionElement = intermediateEventElement.element("signalEventDefinition");
    Element compensateEventDefinitionElement = intermediateEventElement.element("compensateEventDefinition");
    Element linkEventDefinitionElement = intermediateEventElement.element("linkEventDefinition");
    Element messageEventDefinitionElement = intermediateEventElement.element("messageEventDefinition");

    // the link event gets a special treatment as a throwing link event (event source)
    // will not create any activity instance but serves as a "redirection" to the catching link
    // event (event target)
    if (linkEventDefinitionElement!=null) {
      String linkName = linkEventDefinitionElement.attribute("name");
      String elementId = intermediateEventElement.attribute("id");

      // now we remember the link in order to replace the sequence flow later on
      eventLinkSources.put(elementId, linkName);
      // and done - no activity created
      return null;
    }

    boolean otherUnsupportedThrowingIntermediateEvent =
      (intermediateEventElement.element("escalationEventDefinition") != null); //
    // All other event definition types cannot be intermediate throwing (cancelEventDefinition, conditionalEventDefinition, errorEventDefinition, terminateEventDefinition, timerEventDefinition

    ActivityImpl nestedActivityImpl = createActivityOnScope(intermediateEventElement, scopeElement);
    ActivityBehavior activityBehavior = null;

    if(signalEventDefinitionElement != null) {
      nestedActivityImpl.setProperty("type", "intermediateSignalThrow");

      EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(signalEventDefinitionElement);
      activityBehavior = new IntermediateThrowSignalEventActivityBehavior(signalDefinition);
    } else if(compensateEventDefinitionElement != null) {
      CompensateEventDefinition compensateEventDefinition = parseCompensateEventDefinition(compensateEventDefinitionElement, scopeElement);
      activityBehavior = new IntermediateThrowCompensationEventActivityBehavior(compensateEventDefinition);
    } else if (messageEventDefinitionElement != null) {
      if (isServiceTaskLike(messageEventDefinitionElement)) {

        // CAM-436 same behaviour as service task
        activityBehavior = parseServiceTaskLike("intermediateMessageThrowEvent", messageEventDefinitionElement, scopeElement).getActivityBehavior();
      } else {
        // default to non behavior if no service task
        // properties have been specified
        activityBehavior = new IntermediateThrowNoneEventActivityBehavior();
      }
    } else if (otherUnsupportedThrowingIntermediateEvent) {
      addError("Unsupported intermediate throw event type", intermediateEventElement);
    } else { // None intermediate event
      activityBehavior = new IntermediateThrowNoneEventActivityBehavior();
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateThrowEvent(intermediateEventElement, scopeElement, nestedActivityImpl);
    }

    nestedActivityImpl.setActivityBehavior(activityBehavior);

    parseExecutionListenersOnScope(intermediateEventElement, nestedActivityImpl);

    return nestedActivityImpl;
  }


  protected CompensateEventDefinition parseCompensateEventDefinition(Element compensateEventDefinitionElement, ScopeImpl scopeElement) {
    String activityRef = compensateEventDefinitionElement.attribute("activityRef");
    boolean waitForCompletion = "true".equals(compensateEventDefinitionElement.attribute("waitForCompletion", "true"));

    if(activityRef != null) {
      if(scopeElement.getChildActivity(activityRef) == null) {
        Boolean isTriggeredByEvent = (Boolean) scopeElement.getProperty(PROPERTYNAME_TRIGGERED_BY_EVENT);
        String type = (String) scopeElement.getProperty(PROPERTYNAME_TYPE);
        if (Boolean.TRUE == isTriggeredByEvent && "subProcess".equals(type)) {
          scopeElement = scopeElement.getParentScope();
        }
        if (scopeElement.getChildActivity(activityRef) == null) {
          addError("Invalid attribute value for 'activityRef': no activity with id '"+activityRef+"' in scope '"+scopeElement.getId()+"'", compensateEventDefinitionElement);
        }
      }
    }

    CompensateEventDefinition compensateEventDefinition =  new CompensateEventDefinition();
    compensateEventDefinition.setActivityRef(activityRef);
    compensateEventDefinition.setWaitForCompletion(waitForCompletion);

    return compensateEventDefinition;
  }

  protected void parseCatchCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl activity) {
    activity.setProperty("type", "compensationBoundaryCatch");

    ScopeImpl parent = activity.getParent();
    for (ActivityImpl child : parent.getActivities()) {
      if(child.getProperty("type").equals("compensationBoundaryCatch")
        && child != activity ) {
        addError("multiple boundary events with compensateEventDefinition not supported on same activity", compensateEventDefinition);
      }
    }
  }

  protected ActivityBehavior parseBoundaryCancelEventDefinition(Element cancelEventDefinition, ActivityImpl activity) {
    activity.setProperty("type", "cancelBoundaryCatch");

    ActivityImpl parent = (ActivityImpl) activity.getParent();
    if(!parent.getProperty("type").equals("transaction")) {
      addError("boundary event with cancelEventDefinition only supported on transaction subprocesses", cancelEventDefinition);
    }

    for (ActivityImpl child : parent.getActivities()) {
      if(child.getProperty("type").equals("cancelBoundaryCatch")
        && child != activity ) {
        addError("multiple boundary events with cancelEventDefinition not supported on same transaction subprocess", cancelEventDefinition);
      }
    }

    return new CancelBoundaryEventActivityBehavior();
  }

  /**
   * Parses loopCharacteristics (standardLoop/Multi-instance) of an activity, if
   * any is defined.
   */
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, ActivityImpl activity) {

    // Only 'activities' (in the BPMN 2.0 spec meaning) can have mi
    // characteristics
    if (!(activity.getActivityBehavior() instanceof AbstractBpmnActivityBehavior)) {
      return;
    }

    Element miLoopCharacteristics = activityElement.element("multiInstanceLoopCharacteristics");
    if (miLoopCharacteristics != null) {

      MultiInstanceActivityBehavior miActivityBehavior = null;
      boolean isSequential = parseBooleanAttribute(miLoopCharacteristics.attribute("isSequential"), false);
      if (isSequential) {
        miActivityBehavior = new SequentialMultiInstanceBehavior(activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior());
      } else {
        miActivityBehavior = new ParallelMultiInstanceBehavior(activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior());
      }
      activity.setScope(true);
      activity.setProperty("multiInstance", isSequential ? "sequential" : "parallel");
      activity.setActivityBehavior(miActivityBehavior);

      // loopCardinality
      Element loopCardinality = miLoopCharacteristics.element("loopCardinality");
      if (loopCardinality != null) {
        String loopCardinalityText = loopCardinality.getText();
        if (loopCardinalityText == null || "".equals(loopCardinalityText)) {
          addError("loopCardinality must be defined for a multiInstanceLoopCharacteristics definition ", miLoopCharacteristics);
        }
        miActivityBehavior.setLoopCardinalityExpression(expressionManager.createExpression(loopCardinalityText));
      }

      // completionCondition
      Element completionCondition = miLoopCharacteristics.element("completionCondition");
      if (completionCondition != null) {
        String completionConditionText = completionCondition.getText();
        miActivityBehavior.setCompletionConditionExpression(expressionManager.createExpression(completionConditionText));
      }

      // activiti:collection
      String collection = miLoopCharacteristics.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "collection");
      if (collection != null) {
        if (collection.contains("{")) {
          miActivityBehavior.setCollectionExpression(expressionManager.createExpression(collection));
        } else {
          miActivityBehavior.setCollectionVariable(collection);
        }
      }

      // loopDataInputRef
      Element loopDataInputRef = miLoopCharacteristics.element("loopDataInputRef");
      if (loopDataInputRef != null) {
        String loopDataInputRefText = loopDataInputRef.getText();
        if (loopDataInputRefText != null) {
          if (loopDataInputRefText.contains("{")) {
            miActivityBehavior.setCollectionExpression(expressionManager.createExpression(loopDataInputRefText));
          } else {
            miActivityBehavior.setCollectionVariable(loopDataInputRefText);
          }
        }
      }

      // activiti:elementVariable
      String elementVariable = miLoopCharacteristics.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "elementVariable");
      if (elementVariable != null) {
        miActivityBehavior.setCollectionElementVariable(elementVariable);
      }

      // dataInputItem
      Element inputDataItem = miLoopCharacteristics.element("inputDataItem");
      if (inputDataItem != null) {
        String inputDataItemName = inputDataItem.attribute("name");
        miActivityBehavior.setCollectionElementVariable(inputDataItemName);
      }

      // Validation
      if (miActivityBehavior.getLoopCardinalityExpression() == null && miActivityBehavior.getCollectionExpression() == null
              && miActivityBehavior.getCollectionVariable() == null) {
        addError("Either loopCardinality or loopDataInputRef/activiti:collection must been set", miLoopCharacteristics);
      }

      // Validation
      if (miActivityBehavior.getCollectionExpression() == null && miActivityBehavior.getCollectionVariable() == null
              && miActivityBehavior.getCollectionElementVariable() != null) {
        addError("LoopDataInputRef/activiti:collection must be set when using inputDataItem or activiti:elementVariable", miLoopCharacteristics);
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseMultiInstanceLoopCharacteristics(activityElement, miLoopCharacteristics, activity);
      }

    }
  }

  /**
   * Parses the generic information of an activity element (id, name,
   * documentation, etc.), and creates a new {@link ActivityImpl} on the given
   * scope element.
   */
  public ActivityImpl createActivityOnScope(Element activityElement, ScopeImpl scopeElement) {
    String id = activityElement.attribute("id");
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Parsing activity " + id);
    }
    ActivityImpl activity = scopeElement.createActivity(id);

    activity.setProperty("name", activityElement.attribute("name"));
    activity.setProperty("documentation", parseDocumentation(activityElement));
    activity.setProperty("default", activityElement.attribute("default"));
    activity.setProperty("type", activityElement.getTagName());
    activity.setProperty("line", activityElement.getLine());

    String isForCompensation = activityElement.attribute("isForCompensation");
    if(isForCompensation != null && (isForCompensation.equals("true")||isForCompensation.equals("TRUE"))) {
      activity.setProperty(PROPERTYNAME_IS_FOR_COMPENSATION, true);
    }

    return activity;
  }

  public String parseDocumentation(Element element) {
    List<Element> docElements = element.elements("documentation");
    if (docElements.isEmpty()) {
      return null;
    }


    StringBuilder builder = new StringBuilder();
    for (Element e: docElements) {
      if (builder.length() != 0) {
        builder.append("\n\n");
      }

      builder.append(e.getText().trim());
    }

    return builder.toString();
  }

  /**
   * Parses an exclusive gateway declaration.
   */
  public ActivityImpl parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(exclusiveGwElement, scope);
    activity.setActivityBehavior(new ExclusiveGatewayActivityBehavior());

    parseExecutionListenersOnScope(exclusiveGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseExclusiveGateway(exclusiveGwElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses an inclusive gateway declaration.
   */
  public ActivityImpl parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(inclusiveGwElement, scope);
    activity.setActivityBehavior(new InclusiveGatewayActivityBehavior());

    parseExecutionListenersOnScope(inclusiveGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseInclusiveGateway(inclusiveGwElement, scope, activity);
    }
    return activity;
  }

  public ActivityImpl parseEventBasedGateway(Element eventBasedGwElement, Element parentElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(eventBasedGwElement, scope);
    activity.setActivityBehavior(new EventBasedGatewayActivityBehavior());
    activity.setScope(true);

    parseExecutionListenersOnScope(eventBasedGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseEventBasedGateway(eventBasedGwElement, scope, activity);
    }

    // find all outgoing sequence flows:
    List<Element> sequenceFlows = parentElement.elements("sequenceFlow");

    // collect all siblings in a map
    Map<String, Element> siblingsMap = new HashMap<String, Element>();
    List<Element> siblings = parentElement.elements();
    for (Element sibling : siblings) {
      siblingsMap.put(sibling.attribute("id"), sibling);
    }

    for (Element sequenceFlow : sequenceFlows) {

      String sourceRef = sequenceFlow.attribute("sourceRef");
      String targetRef = sequenceFlow.attribute("targetRef");

      if (activity.getId().equals(sourceRef)) {
        Element sibling = siblingsMap.get(targetRef);
        if (sibling != null) {
          if (sibling.getTagName().equals("intermediateCatchEvent")) {
            parseIntermediateCatchEvent(sibling, activity, true);
          } else {
            addError("Event based gateway can only be connected to elements of type intermediateCatchEvent", sibling);
          }
        }
      }
    }

    return activity;
  }


  /**
   * Parses a parallel gateway declaration.
   */
  public ActivityImpl parseParallelGateway(Element parallelGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(parallelGwElement, scope);
    activity.setActivityBehavior(new ParallelGatewayActivityBehavior());

    parseExecutionListenersOnScope(parallelGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseParallelGateway(parallelGwElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a scriptTask declaration.
   */
  public ActivityImpl parseScriptTask(Element scriptTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(scriptTaskElement, scope);

    String script = null;
    String language = null;
    String resultVariableName = null;

    Element scriptElement = scriptTaskElement.element("script");
    if (scriptElement != null) {
      script = scriptElement.getText();

      if (language == null) {
        language = scriptTaskElement.attribute("scriptFormat");
      }

      if (language == null) {
        language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
      }

      resultVariableName = scriptTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariable");
      if (resultVariableName == null) {
        // for backwards compatible reasons
        resultVariableName = scriptTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariableName");
      }
    }

    parseAsynchronousContinuation(scriptTaskElement, activity);

    activity.setActivityBehavior(new ScriptTaskActivityBehavior(script, language, resultVariableName));

    parseExecutionListenersOnScope(scriptTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseScriptTask(scriptTaskElement, scope, activity);
    }
    return activity;
  }


  /**
   * Parses a serviceTask declaration.
   */
  public ActivityImpl parseServiceTask(Element serviceTaskElement, ScopeImpl scope) {
    return parseServiceTaskLike("serviceTask", serviceTaskElement, scope);
  }

  public ActivityImpl parseServiceTaskLike(String elementName, Element serviceTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(serviceTaskElement, scope);

    String type = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");
    String className = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "class");
    String expression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "expression");
    String delegateExpression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "delegateExpression");
    String resultVariableName = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariable");
    if (resultVariableName == null) {
      resultVariableName = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariableName");
    }

    parseAsynchronousContinuation(serviceTaskElement, activity);

    if (type != null) {
      if (type.equalsIgnoreCase("mail")) {
        parseEmailServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else if (type.equalsIgnoreCase("shell")) {
        parseShellServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else {
        addError("Invalid usage of type attribute on " + elementName + ": '" + type + "'", serviceTaskElement);
      }

    } else if (className != null && className.trim().length() > 0) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for " + elementName + " elements using 'class'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ClassDelegate(className, parseFieldDeclarations(serviceTaskElement)));

    } else if (delegateExpression != null) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for " + elementName + " elements using 'delegateExpression'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ServiceTaskDelegateExpressionActivityBehavior(expressionManager.createExpression(delegateExpression), parseFieldDeclarations(serviceTaskElement)));

    } else if (expression != null && expression.trim().length() > 0) {
      activity.setActivityBehavior(new ServiceTaskExpressionActivityBehavior(expressionManager.createExpression(expression), resultVariableName));

    } else {
      addError("One of the attributes 'class', 'delegateExpression', 'type', or 'expression' is mandatory on " + elementName + ".", serviceTaskElement);
    }

    parseExecutionListenersOnScope(serviceTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseServiceTask(serviceTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a businessRuleTask declaration.
   */
  public ActivityImpl parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope) {
      return parseServiceTaskLike("businessRuleTask", businessRuleTaskElement, scope);
  }

  protected void parseAsynchronousContinuation(Element element, ActivityImpl activity) {
    if(isAsync(element)) {
      activity.setAsync(true);
      // create message event declaration:
      MessageJobDeclaration messageJobDecl = new MessageJobDeclaration();
      boolean exclusive = isExclusive(element);
      activity.setExclusive(exclusive);
      messageJobDecl.setExclusive(exclusive);
      messageJobDecl.setActivityId(activity.getId());
      activity.setProperty(PROPERTYNAME_MESSAGE_JOB_DECLARATION, messageJobDecl);
      addJobDeclaration(messageJobDecl, activity.getProcessDefinition());
    }
  }

  protected void addJobDeclaration(JobDeclaration<?> jobDeclaration, ProcessDefinitionImpl processDefinition) {
    ProcessDefinition definition = (ProcessDefinition) processDefinition;
    String key = definition.getKey();

    List<JobDeclaration<?>> containingJobDeclarations = jobDeclarations.get(key);
    if (containingJobDeclarations == null) {
      containingJobDeclarations = new ArrayList<JobDeclaration<?>>();
      jobDeclarations.put(key, containingJobDeclarations);
    }

    containingJobDeclarations.add(jobDeclaration);
  }

  /**
   * Parses a sendTask declaration.
   */
  public ActivityImpl parseSendTask(Element sendTaskElement, ScopeImpl scope) {
    if (isServiceTaskLike(sendTaskElement)) {
      // CAM-942: If expression or class is set on a SendTask it behaves like a service task
      // to allow implementing the send handling yourself
      return parseServiceTaskLike("sendTask", sendTaskElement, scope);
    } else {
      ActivityImpl activity = createActivityOnScope(sendTaskElement, scope);

      parseAsynchronousContinuation(sendTaskElement, activity);

      // for e-mail
      String type = sendTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");

      // for e-mail
      if (type != null) {
        if (type.equalsIgnoreCase("mail")) {
          parseEmailServiceTask(activity, sendTaskElement, parseFieldDeclarations(sendTaskElement));
        } else {
          addError("Invalid usage of type attribute: '" + type + "'", sendTaskElement);
        }
      } else {
        addError("One of the attributes 'class', 'delegateExpression', 'type', or 'expression' is mandatory on sendTask.", sendTaskElement);
      }

      parseExecutionListenersOnScope(sendTaskElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseSendTask(sendTaskElement, scope, activity);
      }
      return activity;
    }
  }

  protected void parseEmailServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    validateFieldDeclarationsForEmail(serviceTaskElement, fieldDeclarations);
    activity.setActivityBehavior((MailActivityBehavior) ClassDelegate.instantiateDelegate(MailActivityBehavior.class, fieldDeclarations));
  }

  protected void parseShellServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    validateFieldDeclarationsForShell(serviceTaskElement, fieldDeclarations);
    activity.setActivityBehavior((ActivityBehavior) ClassDelegate.instantiateDelegate(ShellActivityBehavior.class, fieldDeclarations));
  }


  protected void validateFieldDeclarationsForEmail(Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;
    for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
      if (fieldDeclaration.getName().equals("to")) {
        toDefined = true;
      }
      if (fieldDeclaration.getName().equals("html")) {
        textOrHtmlDefined = true;
      }
      if (fieldDeclaration.getName().equals("text")) {
        textOrHtmlDefined = true;
      }
    }

    if (!toDefined) {
      addError("No recipient is defined on the mail activity", serviceTaskElement);
    }
    if (!textOrHtmlDefined) {
      addError("Text or html field should be provided", serviceTaskElement);
    }
  }

  protected void validateFieldDeclarationsForShell(Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    boolean shellCommandDefined = false;

    for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
      String fieldName = fieldDeclaration.getName();
      FixedValue fieldFixedValue = (FixedValue) fieldDeclaration.getValue();
      String fieldValue = fieldFixedValue.getExpressionText();

      shellCommandDefined |= fieldName.equals("command");

      if ((fieldName.equals("wait") || fieldName.equals("redirectError") || fieldName.equals("cleanEnv")) && !fieldValue.toLowerCase().equals("true")
              && !fieldValue.toLowerCase().equals("false")) {
        addError("undefined value for shell " + fieldName + " parameter :" + fieldValue.toString(), serviceTaskElement);
      }

    }

    if (!shellCommandDefined) {
      addError("No shell command is defined on the shell activity", serviceTaskElement);
    }
  }


  public List<FieldDeclaration> parseFieldDeclarations(Element element) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();

    Element elementWithFieldInjections = element.element("extensionElements");
    if (elementWithFieldInjections == null) { // Custom extensions will just
                                              // have the <field.. as a
                                              // subelement
      elementWithFieldInjections = element;
    }
    List<Element> fieldDeclarationElements = elementWithFieldInjections.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "field");
    if (fieldDeclarationElements != null && !fieldDeclarationElements.isEmpty()) {

      for (Element fieldDeclarationElement : fieldDeclarationElements) {
        FieldDeclaration fieldDeclaration = parseFieldDeclaration(element, fieldDeclarationElement);
        if (fieldDeclaration != null) {
          fieldDeclarations.add(fieldDeclaration);
        }
      }
    }

    return fieldDeclarations;
  }

  protected FieldDeclaration parseFieldDeclaration(Element serviceTaskElement, Element fieldDeclarationElement) {
    String fieldName = fieldDeclarationElement.attribute("name");

    FieldDeclaration fieldDeclaration = parseStringFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);
    if (fieldDeclaration == null) {
      fieldDeclaration = parseExpressionFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);
    }

    if (fieldDeclaration == null) {
      addError("One of the following is mandatory on a field declaration: one of attributes stringValue|expression "
              + "or one of child elements string|expression", serviceTaskElement);
    }
    return fieldDeclaration;
  }

  protected FieldDeclaration parseStringFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
    try {
      String fieldValue = getStringValueFromAttributeOrElement("stringValue", "string", fieldDeclarationElement);
      if (fieldValue != null) {
        return new FieldDeclaration(fieldName, Expression.class.getName(), new FixedValue(fieldValue));
      }
    } catch (ProcessEngineException ae) {
      if (ae.getMessage().contains("multiple elements with tag name")) {
        addError("Multiple string field declarations found", serviceTaskElement);
      } else {
        addError("Error when paring field declarations: " + ae.getMessage(), serviceTaskElement);
      }
    }
    return null;
  }

  protected FieldDeclaration parseExpressionFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
    try {
      String expression = getStringValueFromAttributeOrElement("expression", "expression", fieldDeclarationElement);
      if (expression != null && expression.trim().length() > 0) {
        return new FieldDeclaration(fieldName, Expression.class.getName(), expressionManager.createExpression(expression));
      }
    } catch (ProcessEngineException ae) {
      if (ae.getMessage().contains("multiple elements with tag name")) {
        addError("Multiple expression field declarations found", serviceTaskElement);
      } else {
        addError("Error when paring field declarations: " + ae.getMessage(), serviceTaskElement);
      }
    }
    return null;
  }

  protected String getStringValueFromAttributeOrElement(String attributeName, String elementName, Element element) {
    String value = null;

    String attributeValue = element.attribute(attributeName);
    Element childElement = element.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, elementName);
    String stringElementText = null;

    if (attributeValue != null && childElement != null) {
      addError("Can't use attribute '" + attributeName + "' and element '" + elementName + "' together, only use one", element);
    } else if (childElement != null) {
      stringElementText = childElement.getText();
      if (stringElementText == null || stringElementText.length() == 0) {
        addError("No valid value found in attribute '" + attributeName + "' nor element '" + elementName + "'", element);
      } else {
        // Use text of element
        value = stringElementText;
      }
    } else if (attributeValue != null && attributeValue.length() > 0) {
      // Using attribute
      value = attributeValue;
    }

    return value;
  }

  /**
   * Parses a task with no specific type (behaves as passthrough).
   */
  public ActivityImpl parseTask(Element taskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(taskElement, scope);
    activity.setActivityBehavior(new TaskActivityBehavior());

    parseAsynchronousContinuation(taskElement, activity);

    parseExecutionListenersOnScope(taskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseTask(taskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a manual task.
   */
  public ActivityImpl parseManualTask(Element manualTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(manualTaskElement, scope);
    activity.setActivityBehavior(new ManualTaskActivityBehavior());

    parseExecutionListenersOnScope(manualTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseManualTask(manualTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a receive task.
   */
  public ActivityImpl parseReceiveTask(Element receiveTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(receiveTaskElement, scope);
    activity.setActivityBehavior(new ReceiveTaskActivityBehavior());

    parseAsynchronousContinuation(receiveTaskElement, activity);

    parseExecutionListenersOnScope(receiveTaskElement, activity);

    if (receiveTaskElement.attribute("messageRef") != null) {
      activity.setScope(true);
      EventSubscriptionDeclaration declaration = parseMessageEventDefinition(receiveTaskElement);
      declaration.setActivityId(activity.getActivityId());
      addEventSubscriptionDeclaration(declaration, activity, receiveTaskElement);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseReceiveTask(receiveTaskElement, scope, activity);
    }
    return activity;
  }

  /* userTask specific finals */

  protected static final String HUMAN_PERFORMER = "humanPerformer";
  protected static final String POTENTIAL_OWNER = "potentialOwner";

  protected static final String RESOURCE_ASSIGNMENT_EXPR = "resourceAssignmentExpression";
  protected static final String FORMAL_EXPRESSION = "formalExpression";

  protected static final String USER_PREFIX = "user(";
  protected static final String GROUP_PREFIX = "group(";

  protected static final String ASSIGNEE_EXTENSION = "assignee";
  protected static final String CANDIDATE_USERS_EXTENSION = "candidateUsers";
  protected static final String CANDIDATE_GROUPS_EXTENSION = "candidateGroups";
  protected static final String DUE_DATE_EXTENSION = "dueDate";
  protected static final String PRIORITY_EXTENSION = "priority";

  /**
   * Parses a userTask declaration.
   */
  public ActivityImpl parseUserTask(Element userTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(userTaskElement, scope);

    parseAsynchronousContinuation(userTaskElement, activity);

    TaskDefinition taskDefinition = parseTaskDefinition(userTaskElement, activity.getId(), (ProcessDefinitionEntity) scope.getProcessDefinition());

    UserTaskActivityBehavior userTaskActivity = new UserTaskActivityBehavior(expressionManager, taskDefinition);
    activity.setActivityBehavior(userTaskActivity);

    parseProperties(userTaskElement, activity);
    parseExecutionListenersOnScope(userTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseUserTask(userTaskElement, scope, activity);
    }
    return activity;
  }

  public TaskDefinition parseTaskDefinition(Element taskElement, String taskDefinitionKey, ProcessDefinitionEntity processDefinition) {
    TaskFormHandler taskFormHandler;
    String taskFormHandlerClassName = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formHandlerClass");
    if (taskFormHandlerClassName != null) {
      taskFormHandler = (TaskFormHandler) ReflectUtil.instantiate(taskFormHandlerClassName);
    } else {
      taskFormHandler = new DefaultTaskFormHandler();
    }
    taskFormHandler.parseConfiguration(taskElement, deployment, processDefinition, this);

    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    taskDefinition.setKey(taskDefinitionKey);
    processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);

    String name = taskElement.attribute("name");
    if (name != null) {
      taskDefinition.setNameExpression(expressionManager.createExpression(name));
    }

    String descriptionStr = parseDocumentation(taskElement);
    if (descriptionStr != null) {
      taskDefinition.setDescriptionExpression(expressionManager.createExpression(descriptionStr));
    }

    parseHumanPerformer(taskElement, taskDefinition);
    parsePotentialOwner(taskElement, taskDefinition);

    // Activiti custom extension
    parseUserTaskCustomExtensions(taskElement, taskDefinition);

    return taskDefinition;
  }

  protected void parseHumanPerformer(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> humanPerformerElements = taskElement.elements(HUMAN_PERFORMER);

    if (humanPerformerElements.size() > 1) {
      addError("Invalid task definition: multiple " + HUMAN_PERFORMER + " sub elements defined for " + taskDefinition.getNameExpression(), taskElement);
    } else if (humanPerformerElements.size() == 1) {
      Element humanPerformerElement = humanPerformerElements.get(0);
      if (humanPerformerElement != null) {
        parseHumanPerformerResourceAssignment(humanPerformerElement, taskDefinition);
      }
    }
  }

  protected void parsePotentialOwner(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> potentialOwnerElements = taskElement.elements(POTENTIAL_OWNER);
    for (Element potentialOwnerElement : potentialOwnerElements) {
      parsePotentialOwnerResourceAssignment(potentialOwnerElement, taskDefinition);
    }
  }

  protected void parseHumanPerformerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        taskDefinition.setAssigneeExpression(expressionManager.createExpression(feElement.getText()));
      }
    }
  }

  protected void parsePotentialOwnerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        List<String> assignmentExpressions = parseCommaSeparatedList(feElement.getText());
        for (String assignmentExpression : assignmentExpressions) {
          assignmentExpression = assignmentExpression.trim();
          if (assignmentExpression.startsWith(USER_PREFIX)) {
            String userAssignementId = getAssignmentId(assignmentExpression, USER_PREFIX);
            taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(userAssignementId));
          } else if (assignmentExpression.startsWith(GROUP_PREFIX)) {
            String groupAssignementId = getAssignmentId(assignmentExpression, GROUP_PREFIX);
            taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(groupAssignementId));
          } else { // default: given string is a goupId, as-is.
            taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(assignmentExpression));
          }
        }
      }
    }
  }

  protected String getAssignmentId(String expression, String prefix) {
    return expression.substring(prefix.length(), expression.length() - 1).trim();
  }

  protected void parseUserTaskCustomExtensions(Element taskElement, TaskDefinition taskDefinition) {

    // assignee
    String assignee = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (assignee != null) {
      if (taskDefinition.getAssigneeExpression() == null) {
        taskDefinition.setAssigneeExpression(expressionManager.createExpression(assignee));
      } else {
        addError("Invalid usage: duplicate assignee declaration for task " + taskDefinition.getNameExpression(), taskElement);
      }
    }

    // Candidate users
    String candidateUsersString = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_USERS_EXTENSION);
    if (candidateUsersString != null) {
      List<String> candidateUsers = parseCommaSeparatedList(candidateUsersString);
      for (String candidateUser : candidateUsers) {
        taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser.trim()));
      }
    }

    // Candidate groups
    String candidateGroupsString = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      List<String> candidateGroups = parseCommaSeparatedList(candidateGroupsString);
      for (String candidateGroup : candidateGroups) {
        taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup.trim()));
      }
    }

    // Task listeners
    parseTaskListeners(taskElement, taskDefinition);

    // Due date
    String dueDateExpression = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, DUE_DATE_EXTENSION);
    if (dueDateExpression != null) {
      taskDefinition.setDueDateExpression(expressionManager.createExpression(dueDateExpression));
    }

    // Priority
    final String priorityExpression = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, PRIORITY_EXTENSION);
    if (priorityExpression != null) {
      taskDefinition.setPriorityExpression(expressionManager.createExpression(priorityExpression));
    }
  }

  /**
   * Parses the given String as a list of comma separated entries, where an
   * entry can possibly be an expression that has comma's.
   *
   * If somebody is smart enough to write a regex for this, please let us know.
   *
   * @return the entries of the comma separated list, trimmed.
   */
  protected List<String> parseCommaSeparatedList(String s) {
    List<String> result = new ArrayList<String>();
    if (s != null && !"".equals(s)) {

      StringCharacterIterator iterator = new StringCharacterIterator(s);
      char c = iterator.first();

      StringBuilder strb = new StringBuilder();
      boolean insideExpression = false;

      while (c != StringCharacterIterator.DONE) {
        if (c == '{' || c == '$') {
          insideExpression = true;
        } else if (c == '}') {
          insideExpression = false;
        } else if (c == ',' && !insideExpression) {
          result.add(strb.toString().trim());
          strb.delete(0, strb.length());
        }

        if (c != ',' || (insideExpression)) {
          strb.append(c);
        }

        c = iterator.next();
      }

      if (strb.length() > 0) {
        result.add(strb.toString().trim());
      }

    }
    return result;
  }

  protected void parseTaskListeners(Element userTaskElement, TaskDefinition taskDefinition) {
    Element extentionsElement = userTaskElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> taskListenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "taskListener");
      for (Element taskListenerElement : taskListenerElements) {
        String eventName = taskListenerElement.attribute("event");
        if (eventName != null) {
          if (TaskListener.EVENTNAME_CREATE.equals(eventName) || TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)
                  || TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
            TaskListener taskListener = parseTaskListener(taskListenerElement);
            taskDefinition.addTaskListener(eventName, taskListener);
          } else {
            addError("Attribute 'event' must be one of {create|assignment|complete}", userTaskElement);
          }
        } else {
          addError("Attribute 'event' is mandatory on taskListener", userTaskElement);
        }
      }
    }
  }

  protected TaskListener parseTaskListener(Element taskListenerElement) {
    TaskListener taskListener = null;

    String className = taskListenerElement.attribute("class");
    String expression = taskListenerElement.attribute("expression");
    String delegateExpression = taskListenerElement.attribute("delegateExpression");

    if (className != null) {
      taskListener = new ClassDelegate(className, parseFieldDeclarations(taskListenerElement));
    } else if (expression != null) {
      taskListener = new ExpressionTaskListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      taskListener = new DelegateExpressionTaskListener(expressionManager.createExpression(delegateExpression), parseFieldDeclarations(taskListenerElement));
    } else {
      addError("Element 'class', 'expression' or 'delegateExpression' is mandatory on taskListener", taskListenerElement);
    }
    return taskListener;
  }

  /**
   * Parses the end events of a certain level in the process (process,
   * subprocess or another scope).
   *
   * @param parentElement
   *          The 'parent' element that contains the end events (process,
   *          subprocess).
   * @param scope
   *          The {@link ScopeImpl} to which the end events must be added.
   */
  public void parseEndEvents(Element parentElement, ScopeImpl scope) {
    for (Element endEventElement : parentElement.elements("endEvent")) {
      ActivityImpl activity = createActivityOnScope(endEventElement, scope);

      Element errorEventDefinition = endEventElement.element("errorEventDefinition");
      Element cancelEventDefinition = endEventElement.element("cancelEventDefinition");
      Element terminateEventDefinition = endEventElement.element("terminateEventDefinition");
      Element messageEventDefinitionElement = endEventElement.element("messageEventDefinition");
      Element signalEventDefinition = endEventElement.element("signalEventDefinition");
      if (errorEventDefinition != null) { // error end event
        String errorRef = errorEventDefinition.attribute("errorRef");
        if (errorRef == null || "".equals(errorRef)) {
          addError("'errorRef' attribute is mandatory on error end event", errorEventDefinition);
        } else {
          Error error = errors.get(errorRef);
          if (error != null && (error.getErrorCode() == null || "".equals(error.getErrorCode()))) {
            addError("'errorCode' is mandatory on errors referenced by throwing error event definitions, but the error '" + error.getId() + "' does not define one.", errorEventDefinition);
          }
          activity.setProperty("type", "errorEndEvent");
          activity.setActivityBehavior(new ErrorEndEventActivityBehavior(error != null ? error.getErrorCode() : errorRef));
        }
      } else if (cancelEventDefinition != null) {
        if (scope.getProperty("type")==null || !scope.getProperty("type").equals("transaction")) {
          addError("end event with cancelEventDefinition only supported inside transaction subprocess", cancelEventDefinition);
        } else {
          activity.setProperty("type", "cancelEndEvent");
          activity.setActivityBehavior(new CancelEndEventActivityBehavior());
        }
      } else if (terminateEventDefinition != null) {
        activity.setActivityBehavior(new TerminateEndEventActivityBehavior());
      } else if (messageEventDefinitionElement != null) {
        if (isServiceTaskLike(messageEventDefinitionElement)) {

          // CAM-436 same behaviour as service task
          activity.setActivityBehavior(parseServiceTaskLike("messageEndEvent", messageEventDefinitionElement, scope).getActivityBehavior());
        } else {
          // default to non behavior if no service task
          // properties have been specified
          activity.setActivityBehavior(new IntermediateThrowNoneEventActivityBehavior());
        }
      } else if (signalEventDefinition != null) {
          activity.setProperty("type", "signalEndEvent");
          EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(signalEventDefinition);
          activity.setActivityBehavior(new SignalEndEventActivityBehavior(signalDefinition));
      } else { // default: none end event
        activity.setActivityBehavior(new NoneEndEventActivityBehavior());
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseEndEvent(endEventElement, scope, activity);
      }

      parseExecutionListenersOnScope(endEventElement, activity);
    }
  }

  /**
   * Parses the boundary events of a certain 'level' (process, subprocess or
   * other scope).
   *
   * Note that the boundary events are not parsed during the parsing of the bpmn
   * activities, since the semantics are different (boundaryEvent needs to be
   * added as nested activity to the reference activity on PVM level).
   *
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeImpl} to which the activities must be added.
   */
  public void parseBoundaryEvents(Element parentElement, ScopeImpl scopeElement) {
    for (Element boundaryEventElement : parentElement.elements("boundaryEvent")) {

      // The boundary event is attached to an activity, reference by the
      // 'attachedToRef' attribute
      String attachedToRef = boundaryEventElement.attribute("attachedToRef");
      if (attachedToRef == null || attachedToRef.equals("")) {
        addError("AttachedToRef is required when using a timerEventDefinition", boundaryEventElement);
      }

      // Representation structure-wise is a nested activity in the activity to
      // which its attached
      String id = boundaryEventElement.attribute("id");
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Parsing boundary event " + id);
      }

      ActivityImpl parentActivity = scopeElement.findActivity(attachedToRef);
      if (parentActivity == null) {
        addError("Invalid reference in boundary event. Make sure that the referenced activity is " + "defined in the same scope as the boundary event",
                boundaryEventElement);
      }

      ActivityImpl nestedActivity = createActivityOnScope(boundaryEventElement, parentActivity);

      String cancelActivity = boundaryEventElement.attribute("cancelActivity", "true");
      boolean interrupting = cancelActivity.equals("true") ? true : false;

      // Catch event behavior is the same for most types
      ActivityBehavior behavior = null;

      // Depending on the sub-element definition, the correct activityBehavior
      // parsing is selected
      Element timerEventDefinition = boundaryEventElement.element("timerEventDefinition");
      Element errorEventDefinition = boundaryEventElement.element("errorEventDefinition");
      Element signalEventDefinition = boundaryEventElement.element("signalEventDefinition");
      Element cancelEventDefinition = boundaryEventElement.element("cancelEventDefinition");
      Element compensateEventDefinition = boundaryEventElement.element("compensateEventDefinition");
      Element messageEventDefinition = boundaryEventElement.element("messageEventDefinition");
      if (timerEventDefinition != null) {
    	behavior = new BoundaryEventActivityBehavior(interrupting, nestedActivity.getId());
        parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, nestedActivity);
      } else if (errorEventDefinition != null) {
        interrupting = true; // non-interrupting not yet supported
        behavior = new BoundaryEventActivityBehavior(interrupting, nestedActivity.getId());
        parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, parentActivity, nestedActivity);
      } else if (signalEventDefinition != null) {
    	behavior = new BoundaryEventActivityBehavior(interrupting, nestedActivity.getId());
        parseBoundarySignalEventDefinition(signalEventDefinition, interrupting, nestedActivity);
      } else if (cancelEventDefinition != null) {
        // always interrupting
        behavior = parseBoundaryCancelEventDefinition(cancelEventDefinition, nestedActivity);
      } else if(compensateEventDefinition != null) {
        behavior = new BoundaryEventActivityBehavior(interrupting, nestedActivity.getId());
        parseCatchCompensateEventDefinition(compensateEventDefinition, nestedActivity);
      } else if(messageEventDefinition != null) {
        behavior = new BoundaryEventActivityBehavior(interrupting, nestedActivity.getId());
        parseBoundaryMessageEventDefinition(messageEventDefinition, interrupting, nestedActivity);
      } else {
        addError("Unsupported boundary event type", boundaryEventElement);
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseBoundaryEvent(boundaryEventElement, scopeElement, nestedActivity);
      }

      nestedActivity.setActivityBehavior(behavior);
    }
  }

  /**
   * Parses a boundary timer event. The end-result will be that the given nested
   * activity will get the appropriate {@link ActivityBehavior}.
   *
   * @param timerEventDefinition
   *          The XML element corresponding with the timer event details
   * @param interrupting
   *          Indicates whether this timer is interrupting.
   * @param timerActivity
   *          The activity which maps to the structure of the timer event on the
   *          boundary of another activity. Note that this is NOT the activity
   *          onto which the boundary event is attached, but a nested activity
   *          inside this activity, specifically created for this event.
   */
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    timerActivity.setProperty("type", "boundaryTimer");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerExecuteNestedActivityJobHandler.TYPE);

    // ACT-1427
    if (interrupting) {
      timerDeclaration.setInterruptingTimer(true);
    }

    addTimerDeclaration(timerActivity.getParent(), timerDeclaration);

    if (timerActivity.getParent() instanceof ActivityImpl) {
      ((ActivityImpl) timerActivity.getParent()).setScope(true);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, timerActivity);
    }
  }

  public void parseBoundarySignalEventDefinition(Element element, boolean interrupting, ActivityImpl signalActivity) {
    signalActivity.setProperty("type", "boundarySignal");

    EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(element);
    if(signalActivity.getId() == null) {
      addError("boundary event has no id", element);
    }
    signalDefinition.setActivityId(signalActivity.getId());
    addEventSubscriptionDeclaration(signalDefinition, signalActivity.getParent(), element);

    if (signalActivity.getParent() instanceof ActivityImpl) {
      ((ActivityImpl) signalActivity.getParent()).setScope(true);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundarySignalEventDefinition(element, interrupting, signalActivity);
    }

  }

  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
    messageActivity.setProperty("type", "boundaryMessage");

    EventSubscriptionDeclaration messageEventDefinition = parseMessageEventDefinition(element);
    if(messageActivity.getId() == null) {
      addError("boundary event has no id", element);
    }
    messageEventDefinition.setActivityId(messageActivity.getId());
    addEventSubscriptionDeclaration(messageEventDefinition, messageActivity.getParent(), element);

    if (messageActivity.getParent() instanceof ActivityImpl) {
      ((ActivityImpl) messageActivity.getParent()).setScope(true);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryMessageEventDefinition(element, interrupting, messageActivity);
    }

  }

  @SuppressWarnings("unchecked")
  protected void parseTimerStartEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity, ProcessDefinitionEntity processDefinition) {
    timerActivity.setProperty("type", "startTimerEvent");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);
    timerDeclaration.setJobHandlerConfiguration(processDefinition.getKey());

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);

  }

  protected void parseIntemediateSignalEventDefinition(Element element, ActivityImpl signalActivity, boolean isAfterEventBasedGateway) {
    signalActivity.setProperty("type", "intermediateSignalCatch");

    EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(element);
    if(isAfterEventBasedGateway) {
      signalDefinition.setActivityId(signalActivity.getId());
      addEventSubscriptionDeclaration(signalDefinition, signalActivity.getParent(), element);
    }else {
      signalActivity.setScope(true);
      addEventSubscriptionDeclaration(signalDefinition, signalActivity, element);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateSignalCatchEventDefinition(element, signalActivity);
    }
  }

  protected EventSubscriptionDeclaration parseSignalEventDefinition(Element signalEventDefinitionElement) {
    String signalRef = signalEventDefinitionElement.attribute("signalRef");
    if (signalRef == null) {
      addError("signalEventDefinition does not have required property 'signalRef'", signalEventDefinitionElement);
      return null;
    } else {
      SignalDefinition signalDefinition = signals.get(resolveName(signalRef));
      if (signalDefinition == null) {
        addError("Could not find signal with id '" + signalRef + "'", signalEventDefinitionElement);
      }
      EventSubscriptionDeclaration signalEventDefinition = new EventSubscriptionDeclaration(signalDefinition.getName(), "signal");
      boolean asynch = "true".equals(signalEventDefinitionElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async", "false"));
      signalEventDefinition.setAsync(asynch);

      return signalEventDefinition;
    }
  }

  protected void parseIntemediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity, boolean isAfterEventBasedGateway) {
    timerActivity.setProperty("type", "intermediateTimer");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);
    if(isAfterEventBasedGateway) {
      addTimerDeclaration(timerActivity.getParent(), timerDeclaration);
    }else {
      addTimerDeclaration(timerActivity, timerDeclaration);
      timerActivity.setScope(true);
    }
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateTimerEventDefinition(timerEventDefinition, timerActivity);
    }
  }

  protected TimerDeclarationImpl parseTimer(Element timerEventDefinition, ScopeImpl timerActivity, String jobHandlerType) {
    // TimeDate
    TimerDeclarationType type = TimerDeclarationType.DATE;
    Expression expression = parseExpression(timerEventDefinition, "timeDate");
    // TimeCycle
    if (expression == null) {
      type = TimerDeclarationType.CYCLE;
      expression = parseExpression(timerEventDefinition, "timeCycle");
    }
    // TimeDuration
    if (expression == null) {
      type = TimerDeclarationType.DURATION;
      expression = parseExpression(timerEventDefinition, "timeDuration");
    }
    // neither date, cycle or duration configured!
    if (expression==null) {
      addError("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed).", timerEventDefinition);
    }

    // Parse the timer declaration
    // TODO move the timer declaration into the bpmn activity or next to the
    // TimerSession
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(expression, type, jobHandlerType);
    timerDeclaration.setJobHandlerConfiguration(timerActivity.getId());
    timerDeclaration.setExclusive("true".equals(timerEventDefinition.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "exclusive", String.valueOf(JobEntity.DEFAULT_EXCLUSIVE))));
    if(timerActivity.getId() == null) {
      addError("Attribute \"id\" is required!",timerEventDefinition);
    }
    timerDeclaration.setActivityId(timerActivity.getId());
    timerDeclaration.setJobConfiguration(type.toString() + ": " +expression.getExpressionText());
    addJobDeclaration(timerDeclaration, timerActivity.getProcessDefinition());

    return timerDeclaration;
  }

  protected Expression parseExpression(Element parent, String name) {
    Element value = parent.element(name);
    if (value != null) {
      String expressionText = value.getText().trim();
      return expressionManager.createExpression(expressionText);
    }
    return null;
  }

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {

    nestedErrorEventActivity.setProperty("type", "boundaryError");
    ScopeImpl catchingScope = nestedErrorEventActivity.getParent();
    ((ActivityImpl) catchingScope).setScope(true);

    String errorRef = errorEventDefinition.attribute("errorRef");
    Error error = null;
    ErrorEventDefinition definition = new ErrorEventDefinition(nestedErrorEventActivity.getId());
    if (errorRef != null) {
      error = errors.get(errorRef);
      definition.setErrorCode(error == null ? errorRef : error.getErrorCode());
    }

    addErrorEventDefinition(definition, catchingScope);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, activity, nestedErrorEventActivity);
    }
  }

  protected void addErrorEventDefinition(ErrorEventDefinition errorEventDefinition, ScopeImpl catchingScope) {
    List<ErrorEventDefinition> errorEventDefinitions = (List<ErrorEventDefinition>) catchingScope.getProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
    if(errorEventDefinitions == null) {
      errorEventDefinitions = new ArrayList<ErrorEventDefinition>();
      catchingScope.setProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS, errorEventDefinitions);
    }
    errorEventDefinitions.add(errorEventDefinition);
    Collections.sort(errorEventDefinitions, ErrorEventDefinition.comparator);
  }

  protected List<ActivityImpl> getAllChildActivitiesOfType(String type, ScopeImpl scope) {
    List<ActivityImpl> children = new ArrayList<ActivityImpl>();
    for (ActivityImpl childActivity : scope.getActivities()) {
      if (type.equals(childActivity.getProperty("type"))) {
        children.add(childActivity);
      }
      children.addAll(getAllChildActivitiesOfType(type, childActivity));
    }
    return children;
  }

  /**
   * Checks if the given activity is a child activity of the
   * possibleParentActivity.
   */
  protected boolean isChildActivity(ActivityImpl activityToCheck, ActivityImpl possibleParentActivity) {
    for (ActivityImpl child : possibleParentActivity.getActivities()) {
      if (child.getId().equals(activityToCheck.getId()) || isChildActivity(activityToCheck, child)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclaration(ScopeImpl scope, TimerDeclarationImpl timerDeclaration) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      scope.setProperty(PROPERTYNAME_TIMER_DECLARATION, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);
  }

  @SuppressWarnings("unchecked")
  protected void addVariableDeclaration(ScopeImpl scope, VariableDeclaration variableDeclaration) {
    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations == null) {
      variableDeclarations = new ArrayList<VariableDeclaration>();
      scope.setProperty(PROPERTYNAME_VARIABLE_DECLARATIONS, variableDeclarations);
    }
    variableDeclarations.add(variableDeclaration);
  }

  /**
   * Parses a subprocess (formally known as an embedded subprocess): a subprocess
   * defined within another process definition.
   *
   * @param subProcessElement
   *          The XML element corresponding with the subprocess definition
   * @param scope
   *          The current scope on which the subprocess is defined.
   */
  public ActivityImpl parseSubProcess(Element subProcessElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(subProcessElement, scope);

    parseAsynchronousContinuation(subProcessElement, activity);

    Boolean isTriggeredByEvent = parseBooleanAttribute(subProcessElement.attribute("triggeredByEvent"), false);
    activity.setProperty("triggeredByEvent", isTriggeredByEvent);

    // event subprocesses are not scopes
    activity.setScope(!isTriggeredByEvent);
    activity.setActivityBehavior(new SubProcessActivityBehavior());
    parseScope(subProcessElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSubProcess(subProcessElement, scope, activity);
    }
    return activity;
  }

  protected ActivityImpl parseTransaction(Element transactionElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(transactionElement, scope);

    parseAsynchronousContinuation(transactionElement, activity);

    activity.setScope(true);
    activity.setActivityBehavior(new TransactionActivityBehavior());
    parseScope(transactionElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseTransaction(transactionElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a call activity (currently only supporting calling subprocesses).
   *
   * @param callActivityElement
   *          The XML element defining the call activity
   * @param scope
   *          The current scope on which the call activity is defined.
   */
  public ActivityImpl parseCallActivity(Element callActivityElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(callActivityElement, scope);

    parseAsynchronousContinuation(callActivityElement, activity);

    String calledElement = callActivityElement.attribute("calledElement");
    String calledElementBinding = callActivityElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "calledElementBinding");
    String calledElementVersion = callActivityElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "calledElementVersion");
    if (calledElement == null) {
      addError("Missing attribute 'calledElement'", callActivityElement);
    }
    if (calledElementBinding != null &&
        calledElementBinding.equals(CallActivityBehavior.CalledElementBinding.VERSION.getValue()) &&
        calledElementVersion == null) {
        addError("Missing attribute 'calledElementVersion' when calledElementBinding has value '" + CallActivityBehavior.CalledElementBinding.VERSION.getValue() + "'", callActivityElement);
    }

    Integer processDefinitionVersion = null;
    if (calledElementVersion != null) {
      processDefinitionVersion = Integer.parseInt(calledElementVersion);
    }

    CallActivityBehavior callActivityBehaviour = null;
    String expressionRegex = "(\\$|#)(\\{.+\\})";
    if (calledElement != null && calledElement.matches(expressionRegex)) {
      if (calledElementBinding == null) {
        callActivityBehaviour = new CallActivityBehavior(expressionManager.createExpression(calledElement));
      } else {
        callActivityBehaviour = new CallActivityBehavior(expressionManager.createExpression(calledElement), calledElementBinding, processDefinitionVersion);
      }
    } else {
      if (calledElementBinding == null) {
        callActivityBehaviour = new CallActivityBehavior(calledElement);
      } else {
        callActivityBehaviour = new CallActivityBehavior(calledElement, calledElementBinding, processDefinitionVersion);
      }
    }

    Element extentionsElement = callActivityElement.element("extensionElements");
    if (extentionsElement != null) {
      // input data elements
      for (Element listenerElement : extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "in")) {
        String sourceExpression = listenerElement.attribute("sourceExpression");
        String target = listenerElement.attribute("target");
        String variables = listenerElement.attribute("variables");
        String businessKeyExpression = listenerElement.attribute("businessKey");
        if (sourceExpression != null) {
          Expression expression = expressionManager.createExpression(sourceExpression.trim());
          callActivityBehaviour.addDataInputAssociation(new DataAssociation(expression, target));
        } else if (variables != null && ("all").equals(variables)) {
          callActivityBehaviour.addDataInputAssociation(new DataAssociation(variables));
        } else if (businessKeyExpression != null) {
          Expression expression = expressionManager.createExpression(businessKeyExpression.trim());
          callActivityBehaviour.addDataInputAssociation(new DataAssociation(expression));
        } else {
          String source = listenerElement.attribute("source");
          callActivityBehaviour.addDataInputAssociation(new DataAssociation(source, target));
        }
      }
      // output data elements
      for (Element listenerElement : extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "out")) {
        String sourceExpression = listenerElement.attribute("sourceExpression");
        String target = listenerElement.attribute("target");
        String variables = listenerElement.attribute("variables");
        if (sourceExpression != null) {
          Expression expression = expressionManager.createExpression(sourceExpression.trim());
          callActivityBehaviour.addDataOutputAssociation(new DataAssociation(expression, target));
        } else if (variables != null && ("all").equals(variables)) {
          callActivityBehaviour.addDataOutputAssociation(new DataAssociation(variables));
        } else {
          String source = listenerElement.attribute("source");
          callActivityBehaviour.addDataOutputAssociation(new DataAssociation(source, target));
        }
      }
    }

    activity.setScope(true);
    activity.setActivityBehavior(callActivityBehaviour);

    parseExecutionListenersOnScope(callActivityElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseCallActivity(callActivityElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses the properties of an element (if any) that can contain properties
   * (processes, activities, etc.)
   *
   * Returns true if property subelemens are found.
   *
   * @param element
   *          The element that can contain properties.
   * @param activity
   *          The activity where the property declaration is done.
   */
  public void parseProperties(Element element, ActivityImpl activity) {
    List<Element> propertyElements = element.elements("property");
    for (Element propertyElement : propertyElements) {
      parseProperty(propertyElement, activity);
    }
  }

  /**
   * Parses one property definition.
   *
   * @param propertyElement
   *          The 'property' element that defines how a property looks like and
   *          is handled.
   */
  public void parseProperty(Element propertyElement, ActivityImpl activity) {
    String id = propertyElement.attribute("id");
    String name = propertyElement.attribute("name");

    // If name isn't given, use the id as name
    if (name == null) {
      if (id == null) {
        addError("Invalid property usage on line " + propertyElement.getLine() + ": no id or name specified.", propertyElement);
      } else {
        name = id;
      }
    }

    String type = null;
    parsePropertyCustomExtensions(activity, propertyElement, name, type);
  }

  /**
   * Parses the custom extensions for properties.
   *
   * @param activity
   *          The activity where the property declaration is done.
   * @param propertyElement
   *          The 'property' element defining the property.
   * @param propertyName
   *          The name of the property.
   * @param propertyType
   *          The type of the property.
   */
  public void parsePropertyCustomExtensions(ActivityImpl activity, Element propertyElement, String propertyName, String propertyType) {

    if (propertyType == null) {
      String type = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");
      propertyType = type != null ? type : "string"; // default is string
    }

    VariableDeclaration variableDeclaration = new VariableDeclaration(propertyName, propertyType);
    addVariableDeclaration(activity, variableDeclaration);
    activity.setScope(true);

    String src = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "src");
    if (src != null) {
      variableDeclaration.setSourceVariableName(src);
    }

    String srcExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "srcExpr");
    if (srcExpr != null) {
      Expression sourceExpression = expressionManager.createExpression(srcExpr);
      variableDeclaration.setSourceExpression(sourceExpression);
    }

    String dst = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "dst");
    if (dst != null) {
      variableDeclaration.setDestinationVariableName(dst);
    }

    String destExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "dstExpr");
    if (destExpr != null) {
      Expression destinationExpression = expressionManager.createExpression(destExpr);
      variableDeclaration.setDestinationExpression(destinationExpression);
    }

    String link = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "link");
    if (link != null) {
      variableDeclaration.setLink(link);
    }

    String linkExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "linkExpr");
    if (linkExpr != null) {
      Expression linkExpression = expressionManager.createExpression(linkExpr);
      variableDeclaration.setLinkExpression(linkExpression);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProperty(propertyElement, variableDeclaration, activity);
    }
  }

  /**
   * Parses all sequence flow of a scope.
   *
   * @param processElement
   *          The 'process' element wherein the sequence flow are defined.
   * @param scope
   *          The scope to which the sequence flow must be added.
   */
  public void parseSequenceFlow(Element processElement, ScopeImpl scope) {
    for (Element sequenceFlowElement : processElement.elements("sequenceFlow")) {

      String id = sequenceFlowElement.attribute("id");
      String sourceRef = sequenceFlowElement.attribute("sourceRef");
      String destinationRef = sequenceFlowElement.attribute("targetRef");

      // check if destination is a throwing link event (event source) which mean we have
      // to target the catching link event (event target) here:
      if (eventLinkSources.containsKey(destinationRef)) {
        String linkName = eventLinkSources.get(destinationRef);
        destinationRef = eventLinkTargets.get(linkName);
        if (destinationRef == null) {
          addError("sequence flow points to link event source with name '"+linkName+"' but no event target with that name exists. Most probably your link events are not configured correctly.", sequenceFlowElement);
          // we cannot do anything useful now
          return;
        }
        // Reminder: Maybe we should log a warning if we use intermediate link events which are not used?
        // e.g. we have a catching event without the corresponding throwing one.
        // not done for the moment as it does not break executability
      }

      // Implicit check: sequence flow cannot cross (sub) process boundaries: we
      // don't do a processDefinition.findActivity here
      ActivityImpl sourceActivity = scope.findActivity(sourceRef);
      ActivityImpl destinationActivity = scope.findActivity(destinationRef);

      if (sourceActivity == null) {
        addError("Invalid source '" + sourceRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      } else if (destinationActivity == null) {
        addError("Invalid destination '" + destinationRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      } else if(sourceActivity.getActivityBehavior() instanceof EventBasedGatewayActivityBehavior) {
        // ignore
      } else if(destinationActivity.getActivityBehavior() instanceof IntermediateCatchEventActivitiBehaviour
              && (destinationActivity.getParentActivity() != null)
              && (destinationActivity.getParentActivity().getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)) {
        addError("Invalid incoming sequenceflow for intermediateCatchEvent with id '"+destinationActivity.getId()+"' connected to an event-based gateway.", sequenceFlowElement);
      } else {
        TransitionImpl transition = sourceActivity.createOutgoingTransition(id);
        sequenceFlows.put(id, transition);
        transition.setProperty("name", sequenceFlowElement.attribute("name"));
        transition.setProperty("documentation", parseDocumentation(sequenceFlowElement));
        transition.setDestination(destinationActivity);
        parseSequenceFlowConditionExpression(sequenceFlowElement, transition);
        parseExecutionListenersOnTransition(sequenceFlowElement, transition);

        for (BpmnParseListener parseListener : parseListeners) {
          parseListener.parseSequenceFlow(sequenceFlowElement, scope, transition);
        }
      }
    }
  }

  /**
   * Parses a condition expression on a sequence flow.
   *
   * @param seqFlowElement
   *          The 'sequenceFlow' element that can contain a condition.
   * @param seqFlow
   *          The sequenceFlow object representation to which the condition must
   *          be added.
   */
  public void parseSequenceFlowConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
    Element conditionExprElement = seqFlowElement.element("conditionExpression");
    if (conditionExprElement != null) {
      String expression = conditionExprElement.getText().trim();
      String type = conditionExprElement.attributeNS(BpmnParser.XSI_NS, "type");
      if (type != null) {
        String value = type.contains(":") ? resolveName(type) : BpmnParser.BPMN20_NS + ":" + type;
        if (!value.equals(ATTRIBUTEVALUE_T_FORMAL_EXPRESSION)) {
          addError("Invalid type, only tFormalExpression is currently supported", conditionExprElement);
        }
      }

      Condition expressionCondition = new UelExpressionCondition(expressionManager.createExpression(expression));
      seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
      seqFlow.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
    }
  }

  /**
   * Parses all execution-listeners on a scope.
   *
   * @param scopeElement
   *          the XML element containing the scope definition.
   * @param scope
   *          the scope to add the executionListeners to.
   */
  public void parseExecutionListenersOnScope(Element scopeElement, ScopeImpl scope) {
    Element extentionsElement = scopeElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> listenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        String eventName = listenerElement.attribute("event");
        if (isValidEventNameForScope(eventName, listenerElement)) {
          ExecutionListener listener = parseExecutionListener(listenerElement);
          if (listener != null) {
            scope.addExecutionListener(eventName, listener);
          }
        }
      }
    }
  }

  /**
   * Check if the given event name is valid. If not, an appropriate error is
   * added.
   */
  protected boolean isValidEventNameForScope(String eventName, Element listenerElement) {
    if (eventName != null && eventName.trim().length() > 0) {
      if ("start".equals(eventName) || "end".equals(eventName)) {
        return true;
      } else {
        addError("Attribute 'event' must be one of {start|end}", listenerElement);
      }
    } else {
      addError("Attribute 'event' is mandatory on listener", listenerElement);
    }
    return false;
  }

  public void parseExecutionListenersOnTransition(Element activitiElement, TransitionImpl activity) {
    Element extentionsElement = activitiElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> listenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        ExecutionListener listener = parseExecutionListener(listenerElement);
        if (listener != null) {
          // Since a transition only fires event 'take', we don't parse the
          // event attribute, it is ignored
          activity.addExecutionListener(listener);
        }
      }
    }
  }

  /**
   * Parses an {@link ExecutionListener} implementation for the given
   * executionListener element.
   *
   * @param executionListenerElement
   *          the XML element containing the executionListener definition.
   */
  public ExecutionListener parseExecutionListener(Element executionListenerElement) {
    ExecutionListener executionListener = null;

    String className = executionListenerElement.attribute("class");
    String expression = executionListenerElement.attribute("expression");
    String delegateExpression = executionListenerElement.attribute("delegateExpression");

    if (className != null) {
      executionListener = new ClassDelegate(className, parseFieldDeclarations(executionListenerElement));
    } else if (expression != null) {
      executionListener = new ExpressionExecutionListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      executionListener = new DelegateExpressionExecutionListener(expressionManager.createExpression(delegateExpression), parseFieldDeclarations(executionListenerElement));
    } else {
      addError("Element 'class' or 'expression' is mandatory on executionListener", executionListenerElement);
    }
    return executionListener;
  }

  // Diagram interchange
  // /////////////////////////////////////////////////////////////////

  public void parseDiagramInterchangeElements() {
    // Multiple BPMNDiagram possible
    List<Element> diagrams = rootElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNDiagram");
    if (!diagrams.isEmpty()) {
      for (Element diagramElement : diagrams) {
        parseBPMNDiagram(diagramElement);
      }
    }
  }

  public void parseBPMNDiagram(Element bpmndiagramElement) {
    // Each BPMNdiagram needs to have exactly one BPMNPlane
    Element bpmnPlane = bpmndiagramElement.elementNS(BpmnParser.BPMN_DI_NS, "BPMNPlane");
    if (bpmnPlane != null) {
      parseBPMNPlane(bpmnPlane);
    }
  }

  public void parseBPMNPlane(Element bpmnPlaneElement) {
    String bpmnElement = bpmnPlaneElement.attribute("bpmnElement");
    if (bpmnElement != null && !"".equals(bpmnElement)) {
      // there seems to be only on process without collaboration
      if (getProcessDefinition(bpmnElement) != null) {
        getProcessDefinition(bpmnElement).setGraphicalNotationDefined(true);
      }

      List<Element> shapes = bpmnPlaneElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNShape");
      for (Element shape : shapes) {
        parseBPMNShape(shape);
      }

      List<Element> edges = bpmnPlaneElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNEdge");
      for (Element edge : edges) {
        parseBPMNEdge(edge);
      }

    } else {
      addError("'bpmnElement' attribute is required on BPMNPlane ", bpmnPlaneElement);
    }
  }

  public void parseBPMNShape(Element bpmnShapeElement) {
    String bpmnElement = bpmnShapeElement.attribute("bpmnElement");

    if (bpmnElement != null && !"".equals(bpmnElement)) {
      // For collaborations, their are also shape definitions for the
      // participants / processes
      if (participantProcesses.get(bpmnElement) != null) {
        ProcessDefinitionEntity procDef = getProcessDefinition(participantProcesses.get(bpmnElement));
        procDef.setGraphicalNotationDefined(true);

        // The participation that references this process, has a bounds to be rendered + a name as wel
        parseDIBounds(bpmnShapeElement, procDef.getParticipantProcess());
        return;
      }

      for (ProcessDefinitionEntity processDefinition : getProcessDefinitions()) {
        ActivityImpl activity = processDefinition.findActivity(bpmnElement);
        if (activity != null) {
          parseDIBounds(bpmnShapeElement, activity);

          // collapsed or expanded
          String isExpanded = bpmnShapeElement.attribute("isExpanded");
          if (isExpanded != null) {
            activity.setProperty(PROPERTYNAME_ISEXPANDED, parseBooleanAttribute(isExpanded));
          }
        } else {
          Lane lane = processDefinition.getLaneForId(bpmnElement);

          if(lane != null) {
            // The shape represents a lane
            parseDIBounds(bpmnShapeElement, lane);
          } else if(!elementIds.contains(bpmnElement)) { // It might not be an activity nor a lane, but it might still reference 'something'
            addError("Invalid reference in 'bpmnElement' attribute, activity " + bpmnElement + "not found", bpmnShapeElement);
          }
        }
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNShape", bpmnShapeElement);
    }
  }

  protected void parseDIBounds(Element bpmnShapeElement, HasDIBounds target) {
    Element bounds = bpmnShapeElement.elementNS(BpmnParser.BPMN_DC_NS, "Bounds");
    if (bounds != null) {
      target.setX(parseDoubleAttribute(bpmnShapeElement, "x", bounds.attribute("x"), true).intValue());
      target.setY(parseDoubleAttribute(bpmnShapeElement, "y", bounds.attribute("y"), true).intValue());
      target.setWidth(parseDoubleAttribute(bpmnShapeElement, "width", bounds.attribute("width"), true).intValue());
      target.setHeight(parseDoubleAttribute(bpmnShapeElement, "height", bounds.attribute("height"), true).intValue());
    } else {
      addError("'Bounds' element is required", bpmnShapeElement);
    }
  }

  public void parseBPMNEdge(Element bpmnEdgeElement) {
    String sequenceFlowId = bpmnEdgeElement.attribute("bpmnElement");
    if (sequenceFlowId != null && !"".equals(sequenceFlowId)) {
      if (sequenceFlows != null && sequenceFlows.containsKey(sequenceFlowId)) {

        TransitionImpl sequenceFlow = sequenceFlows.get(sequenceFlowId);
        List<Element> waypointElements = bpmnEdgeElement.elementsNS(BpmnParser.OMG_DI_NS, "waypoint");
        if (waypointElements.size() >= 2) {
          List<Integer> waypoints = new ArrayList<Integer>();
          for (Element waypointElement : waypointElements) {
            waypoints.add(parseDoubleAttribute(waypointElement, "x", waypointElement.attribute("x"), true).intValue());
            waypoints.add(parseDoubleAttribute(waypointElement, "y", waypointElement.attribute("y"), true).intValue());
          }
          sequenceFlow.setWaypoints(waypoints);
        } else {
          addError("Minimum 2 waypoint elements must be definted for a 'BPMNEdge'", bpmnEdgeElement);
        }
      } else if(!elementIds.contains(sequenceFlowId)) { // it might not be a sequenceFlow but it might still reference 'something'
        addError("Invalid reference in 'bpmnElement' attribute, sequenceFlow " + sequenceFlowId + "not found", bpmnEdgeElement);
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNEdge", bpmnEdgeElement);
    }
  }

  // Getters, setters and Parser overridden operations
  // ////////////////////////////////////////

  public List<ProcessDefinitionEntity> getProcessDefinitions() {
    return processDefinitions;
  }

  public ProcessDefinitionEntity getProcessDefinition(String processDefinitionKey) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (processDefinition.getKey().equals(processDefinitionKey)) {
        return processDefinition;
      }
    }
    return null;
  }

  @Override
  public BpmnParse name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public BpmnParse sourceInputStream(InputStream inputStream) {
    super.sourceInputStream(inputStream);
    return this;
  }

  @Override
  public BpmnParse sourceResource(String resource, ClassLoader classLoader) {
    super.sourceResource(resource, classLoader);
    return this;
  }

  @Override
  public BpmnParse sourceResource(String resource) {
    super.sourceResource(resource);
    return this;
  }

  @Override
  public BpmnParse sourceString(String string) {
    super.sourceString(string);
    return this;
  }

  @Override
  public BpmnParse sourceUrl(String url) {
    super.sourceUrl(url);
    return this;
  }

  @Override
  public BpmnParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }

  public Boolean parseBooleanAttribute(String booleanText, boolean defaultValue) {
    if (booleanText == null) {
      return defaultValue;
    } else {
      return parseBooleanAttribute(booleanText);
    }
  }

  public Boolean parseBooleanAttribute(String booleanText) {
    if ("true".equals(booleanText) || "enabled".equals(booleanText) || "on".equals(booleanText) || "active".equals(booleanText) || "yes".equals(booleanText)) {
      return Boolean.TRUE;
    }
    if ("false".equals(booleanText) || "disabled".equals(booleanText) || "off".equals(booleanText) || "inactive".equals(booleanText)
            || "no".equals(booleanText)) {
      return Boolean.FALSE;
    }
    return null;
  }

  public Double parseDoubleAttribute(Element element, String attributename, String doubleText, boolean required) {
    if (required && (doubleText == null || "".equals(doubleText))) {
      addError(attributename + " is required", element);
    } else {
      try {
        return Double.parseDouble(doubleText);
      } catch (NumberFormatException e) {
        addError("Cannot parse " + attributename + ": " + e.getMessage(), element);
      }
    }
    return -1.0;
  }

  protected boolean isExclusive(Element element) {
    return "true".equals(element.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "exclusive", String.valueOf(JobEntity.DEFAULT_EXCLUSIVE)));
  }

  protected boolean isAsync(Element element) {
    return "true".equals(element.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async"));
  }

  private boolean isServiceTaskLike(Element element) {

    return element.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "class") != null
        || element.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "expression") != null
        || element.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "delegateExpression") != null;
  }

  public Map<String, List<JobDeclaration<?>>> getJobDeclarations() {
    return jobDeclarations;
  }

  public List<JobDeclaration<?>> getJobDeclarationsByKey(String processDefinitionKey) {
    return jobDeclarations.get(processDefinitionKey);
  }
}

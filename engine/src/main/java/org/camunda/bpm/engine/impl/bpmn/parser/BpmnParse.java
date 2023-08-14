/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.bpmn.parser;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.findCamundaExtensionElement;
import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.parseCamundaExtensionProperties;
import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.parseCamundaScript;
import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.parseInputOutput;
import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.instantiateDelegate;

import java.io.InputStream;
import java.net.URL;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.HistoryTimeToLiveParser;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallableElementActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelBoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CaseCallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.DmnBusinessRuleTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartConditionalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchLinkEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateConditionalEventBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ShellActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowEscalationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowSignalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.listener.ClassDelegateExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.ScriptExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.core.model.CallableElementParameter;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.NullValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.dmn.result.DecisionResultMapper;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.el.UelExpressionCondition;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.form.FormDefinition;
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DefaultTaskFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DelegateStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DelegateTaskFormHandler;
import org.camunda.bpm.engine.impl.form.handler.StartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.TaskFormHandler;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncAfterMessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncBeforeMessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.EventSubscriptionJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerTaskListenerJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.HasDIBounds;
import org.camunda.bpm.engine.impl.pvm.process.Lane;
import org.camunda.bpm.engine.impl.pvm.process.LaneSet;
import org.camunda.bpm.engine.impl.pvm.process.ParticipantProcess;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptCondition;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.task.listener.ClassDelegateTaskListener;
import org.camunda.bpm.engine.impl.task.listener.DelegateExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ScriptTaskListener;
import org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.util.ScriptUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Namespace;
import org.camunda.bpm.engine.impl.util.xml.Parse;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 *
 * Instances of this class should not be reused and are also not threadsafe.
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
 * @author Ronny Bräunlich
 * @author Christopher Zell
 * @author Deivarayan Azhagappan
 * @author Ingo Richtsmeier
 */
public class BpmnParse extends Parse {

  public static final String MULTI_INSTANCE_BODY_ID_SUFFIX = "#multiInstanceBody";

  protected static final BpmnParseLogger LOG = ProcessEngineLogger.BPMN_PARSE_LOGGER;

  public static final String PROPERTYNAME_DOCUMENTATION = "documentation";
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_HAS_CONDITIONAL_EVENTS = "hasConditionalEvents";
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";
  public static final String PROPERTYNAME_VARIABLE_DECLARATIONS = "variableDeclarations";
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  public static final String PROPERTYNAME_MESSAGE_JOB_DECLARATION = "messageJobDeclaration";
  public static final String PROPERTYNAME_ISEXPANDED = "isExpanded";
  public static final String PROPERTYNAME_START_TIMER = "timerStart";
  public static final String PROPERTYNAME_COMPENSATION_HANDLER_ID = "compensationHandler";
  public static final String PROPERTYNAME_IS_FOR_COMPENSATION = "isForCompensation";
  public static final String PROPERTYNAME_EVENT_SUBSCRIPTION_JOB_DECLARATION = "eventJobDeclarations";
  public static final String PROPERTYNAME_THROWS_COMPENSATION = "throwsCompensation";
  public static final String PROPERTYNAME_CONSUMES_COMPENSATION = "consumesCompensation";
  public static final String PROPERTYNAME_JOB_PRIORITY = "jobPriority";
  public static final String PROPERTYNAME_TASK_PRIORITY = "taskPriority";
  public static final String PROPERTYNAME_EXTERNAL_TASK_TOPIC = "topic";
  public static final String PROPERTYNAME_CLASS = "class";
  public static final String PROPERTYNAME_EXPRESSION = "expression";
  public static final String PROPERTYNAME_DELEGATE_EXPRESSION = "delegateExpression";
  public static final String PROPERTYNAME_VARIABLE_MAPPING_CLASS = "variableMappingClass";
  public static final String PROPERTYNAME_VARIABLE_MAPPING_DELEGATE_EXPRESSION = "variableMappingDelegateExpression";
  public static final String PROPERTYNAME_RESOURCE = "resource";
  public static final String PROPERTYNAME_LANGUAGE = "language";
  public static final String TYPE = "type";

  public static final String TRUE = "true";
  public static final String INTERRUPTING = "isInterrupting";

  public static final String CONDITIONAL_EVENT_DEFINITION = "conditionalEventDefinition";
  public static final String ESCALATION_EVENT_DEFINITION = "escalationEventDefinition";
  public static final String COMPENSATE_EVENT_DEFINITION = "compensateEventDefinition";
  public static final String TIMER_EVENT_DEFINITION = "timerEventDefinition";
  public static final String SIGNAL_EVENT_DEFINITION = "signalEventDefinition";
  public static final String MESSAGE_EVENT_DEFINITION = "messageEventDefinition";
  public static final String ERROR_EVENT_DEFINITION = "errorEventDefinition";
  public static final String CANCEL_EVENT_DEFINITION = "cancelEventDefinition";
  public static final String LINK_EVENT_DEFINITION = "linkEventDefinition";
  public static final String CONDITION_EXPRESSION = "conditionExpression";
  public static final String CONDITION = "condition";

  public static final List<String> VARIABLE_EVENTS = Arrays.asList(
      VariableListener.CREATE,
      VariableListener.DELETE,
      VariableListener.UPDATE
  );

  /**
   * @deprecated use {@link BpmnProperties#TYPE}
   */
  @Deprecated
  public static final String PROPERTYNAME_TYPE = BpmnProperties.TYPE.getName();

  /**
   * @deprecated use {@link BpmnProperties#ERROR_EVENT_DEFINITIONS}
   */
  @Deprecated
  public static final String PROPERTYNAME_ERROR_EVENT_DEFINITIONS = BpmnProperties.ERROR_EVENT_DEFINITIONS.getName();

  /* process start authorization specific finals */
  protected static final String POTENTIAL_STARTER = "potentialStarter";
  protected static final String CANDIDATE_STARTER_USERS_EXTENSION = "candidateStarterUsers";
  protected static final String CANDIDATE_STARTER_GROUPS_EXTENSION = "candidateStarterGroups";

  protected static final String ATTRIBUTEVALUE_T_FORMAL_EXPRESSION = BpmnParser.BPMN20_NS + ":tFormalExpression";

  public static final String PROPERTYNAME_IS_MULTI_INSTANCE = "isMultiInstance";

  public static final Namespace CAMUNDA_BPMN_EXTENSIONS_NS = new Namespace(BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS);
  public static final Namespace XSI_NS = new Namespace(BpmnParser.XSI_NS);
  public static final Namespace BPMN_DI_NS = new Namespace(BpmnParser.BPMN_DI_NS);
  public static final Namespace OMG_DI_NS = new Namespace(BpmnParser.OMG_DI_NS);
  public static final Namespace BPMN_DC_NS = new Namespace(BpmnParser.BPMN_DC_NS);
  public static final String ALL = "all";

  /** The deployment to which the parsed process definitions will be added. */
  protected DeploymentEntity deployment;

  /** The end result of the parsing: a list of process definition. */
  protected List<ProcessDefinitionEntity> processDefinitions = new ArrayList<>();

  /** Mapping of found errors in BPMN 2.0 file */
  protected Map<String, Error> errors = new HashMap<>();

  /** Mapping of found escalation elements */
  protected Map<String, Escalation> escalations = new HashMap<>();

  /**
   * Mapping from a process definition key to his containing list of job
   * declarations
   **/
  protected Map<String, List<JobDeclaration<?, ?>>> jobDeclarations = new HashMap<>();

  /** A map for storing sequence flow based on their id during parsing. */
  protected Map<String, TransitionImpl> sequenceFlows;

  /**
   * A list of all element IDs. This allows us to parse only what we actually
   * support but still validate the references among elements we do not support.
   */
  protected List<String> elementIds = new ArrayList<>();

  /** A map for storing the process references of participants */
  protected Map<String, String> participantProcesses = new HashMap<>();

  /**
   * Mapping containing values stored during the first phase of parsing since
   * other elements can reference these messages.
   *
   * All the map's elements are defined outside the process definition(s), which
   * means that this map doesn't need to be re-initialized for each new process
   * definition.
   */
  protected Map<String, MessageDefinition> messages = new HashMap<>();
  protected Map<String, SignalDefinition> signals = new HashMap<>();

  // Members
  protected ExpressionManager expressionManager;
  protected List<BpmnParseListener> parseListeners;
  protected Map<String, XMLImporter> importers = new HashMap<>();
  protected Map<String, String> prefixs = new HashMap<>();
  protected String targetNamespace;

  private Map<String, String> eventLinkTargets = new HashMap<>();
  private Map<String, String> eventLinkSources = new HashMap<>();

  /**
   * Constructor to be called by the {@link BpmnParser}.
   */
  public BpmnParse(BpmnParser parser) {
    super(parser);
    expressionManager = parser.getExpressionManager();
    parseListeners = parser.getParseListeners();

    setSchemaResource(ReflectUtil.getResourceUrlAsString(BpmnParser.BPMN_20_SCHEMA_LOCATION));
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
    } catch (BpmnParseException e) {
      addError(e);

    } catch (Exception e) {
      LOG.parsingFailure(e);

      // ALL unexpected exceptions should bubble up since they are not handled
      // accordingly by underlying parse-methods and the process can't be
      // deployed
      throw LOG.parsingProcessException(e);

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
    parseSignals();
    parseErrors();
    parseEscalations();
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
        Class<?> wsdlImporterClass;
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
      String messageName = messageElement.attribute("name");

      Expression messageExpression = null;
      if (messageName != null) {
        messageExpression = expressionManager.createExpression(messageName);
      }

      MessageDefinition messageDefinition = new MessageDefinition(this.targetNamespace + ":" + id, messageExpression);
      this.messages.put(messageDefinition.getId(), messageDefinition);
    }
  }

  /**
   * Parses the signals of the given definitions file. Signals are not contained
   * within a process element, but they can be referenced from inner process
   * elements.
   */
  protected void parseSignals() {
    for (Element signalElement : rootElement.elements("signal")) {
      String id = signalElement.attribute("id");
      String signalName = signalElement.attribute("name");

      for (SignalDefinition signalDefinition : signals.values()) {
        if (signalDefinition.getName().equals(signalName)) {
          addError("duplicate signal name '" + signalName + "'.", signalElement);
        }
      }

      if (id == null) {
        addError("signal must have an id", signalElement);
      } else if (signalName == null) {
        addError("signal with id '" + id + "' has no name", signalElement);
      } else {
        Expression signalExpression = expressionManager.createExpression(signalName);
        SignalDefinition signal = new SignalDefinition();
        signal.setId(this.targetNamespace + ":" + id);
        signal.setExpression(signalExpression);

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

      String errorMessage = errorElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "errorMessage");
      if(errorMessage != null) {
        error.setErrorMessageExpression(createParameterValueProvider(errorMessage, expressionManager));
      }

      errors.put(id, error);
    }
  }

  protected void parseEscalations() {
    for (Element element : rootElement.elements("escalation")) {

      String id = element.attribute("id");
      if (id == null) {
        addError("escalation must have an id", element);
      } else {

        Escalation escalation = createEscalation(id, element);
        escalations.put(id, escalation);
      }
    }
  }

  protected Escalation createEscalation(String id, Element element) {

    Escalation escalation = new Escalation(id);

    String name = element.attribute("name");
    if (name != null) {
      escalation.setName(name);
    }

    String escalationCode = element.attribute("escalationCode");
    if (escalationCode != null && !escalationCode.isEmpty()) {
      escalation.setEscalationCode(escalationCode);
    }
    return escalation;
  }

  /**
   * Parses all the process definitions defined within the 'definitions' root
   * element.
   */
  public void parseProcessDefinitions() {
    for (Element processElement : rootElement.elements("process")) {
      boolean isExecutable = !deployment.isNew();
      String isExecutableStr = processElement.attribute("isExecutable");
      if (isExecutableStr != null) {
        isExecutable = Boolean.parseBoolean(isExecutableStr);
        if (!isExecutable) {
          LOG.ignoringNonExecutableProcess(processElement.attribute("id"));
        }
      } else {
        LOG.missingIsExecutableAttribute(processElement.attribute("id"));
      }

      // Only process executable processes
      if (isExecutable) {
        processDefinitions.add(parseProcess(processElement));
      }
    }
  }

  /**
   * Parses the collaboration definition defined within the 'definitions' root
   * element and get all participants to lookup their process references during
   * DI parsing.
   */
  public void parseCollaboration() {
    Element collaboration = rootElement.element("collaboration");
    if (collaboration != null) {
      for (Element participant : collaboration.elements("participant")) {
        String processRef = participant.attribute("processRef");
        if (processRef != null) {
          ProcessDefinitionImpl procDef = getProcessDefinition(processRef);
          if (procDef != null) {
            // Set participant process on the procDef, so it can get rendered
            // later on if needed
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
    sequenceFlows = new HashMap<>();

    ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntity();

    /*
     * Mapping object model - bpmn xml: processDefinition.id -> generated by
     * processDefinition.key -> bpmn id (required) processDefinition.name ->
     * bpmn name (optional)
     */
    processDefinition.setKey(processElement.attribute("id"));
    processDefinition.setName(processElement.attribute("name"));
    processDefinition.setCategory(rootElement.attribute("targetNamespace"));
    processDefinition.setProperty(PROPERTYNAME_DOCUMENTATION, parseDocumentation(processElement));
    processDefinition.setTaskDefinitions(new HashMap<String, TaskDefinition>());
    processDefinition.setDeploymentId(deployment.getId());
    processDefinition.setTenantId(deployment.getTenantId());
    processDefinition.setProperty(PROPERTYNAME_JOB_PRIORITY, parsePriority(processElement, PROPERTYNAME_JOB_PRIORITY));
    processDefinition.setProperty(PROPERTYNAME_TASK_PRIORITY, parsePriority(processElement, PROPERTYNAME_TASK_PRIORITY));
    processDefinition.setVersionTag(processElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "versionTag"));

    validateAndSetHTTL(processElement, processDefinition);

    boolean isStartableInTasklist = isStartable(processElement);
    processDefinition.setStartableInTasklist(isStartableInTasklist);

    LOG.parsingElement("process", processDefinition.getKey());

    parseScope(processElement, processDefinition);

    // Parse any laneSets defined for this process
    parseLaneSets(processElement, processDefinition);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProcess(processElement, processDefinition);
    }

    // now we have parsed anything we can validate some stuff
    validateActivities(processDefinition.getActivities());

    //unregister delegates
    for (ActivityImpl activity : processDefinition.getActivities()) {
      activity.setDelegateAsyncAfterUpdate(null);
      activity.setDelegateAsyncBeforeUpdate(null);
    }
    return processDefinition;
  }

  protected void validateAndSetHTTL(Element processElement, ProcessDefinitionEntity processDefinition) {
    try {
      Integer historyTimeToLive = HistoryTimeToLiveParser.create().parse(processElement);
      processDefinition.setHistoryTimeToLive(historyTimeToLive);
    }
    catch (Exception e) {
      addError(new BpmnParseException(e.getMessage(), processElement, e));
    }
  }

  protected void parseLaneSets(Element parentElement, ProcessDefinitionEntity processDefinition) {
    List<Element> laneSets = parentElement.elements("laneSet");

    if (laneSets != null && laneSets.size() > 0) {
      for (Element laneSetElement : laneSets) {
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
    if (lanes != null && lanes.size() > 0) {
      for (Element laneElement : lanes) {
        // Parse basic attributes
        Lane lane = new Lane();
        lane.setId(laneElement.attribute("id"));
        lane.setName(laneElement.attribute("name"));

        // Parse ID's of flow-nodes that live inside this lane
        List<Element> flowNodeElements = laneElement.elements("flowNodeRef");
        if (flowNodeElements != null && flowNodeElements.size() > 0) {
          for (Element flowNodeElement : flowNodeElements) {
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

    // filter activities that must be parsed separately
    List<Element> activityElements = new ArrayList<>(scopeElement.elements());
    Map<String, Element> intermediateCatchEvents = filterIntermediateCatchEvents(activityElements);
    activityElements.removeAll(intermediateCatchEvents.values());
    Map<String, Element> compensationHandlers = filterCompensationHandlers(activityElements);
    activityElements.removeAll(compensationHandlers.values());

    parseStartEvents(scopeElement, parentScope);
    parseActivities(activityElements, scopeElement, parentScope);
    parseIntermediateCatchEvents(scopeElement, parentScope, intermediateCatchEvents);
    parseEndEvents(scopeElement, parentScope);
    parseBoundaryEvents(scopeElement, parentScope);
    parseSequenceFlow(scopeElement, parentScope, compensationHandlers);
    parseExecutionListenersOnScope(scopeElement, parentScope);
    parseAssociations(scopeElement, parentScope, compensationHandlers);
    parseCompensationHandlers(parentScope, compensationHandlers);

    for (ScopeImpl.BacklogErrorCallback callback : parentScope.getBacklogErrorCallbacks()) {
      callback.callback();
    }

    if (parentScope instanceof ProcessDefinition) {
      parseProcessDefinitionCustomExtensions(scopeElement, (ProcessDefinition) parentScope);
    }
  }

  protected HashMap<String, Element> filterIntermediateCatchEvents(List<Element> activityElements) {
    HashMap<String, Element> intermediateCatchEvents = new HashMap<>();
    for(Element activityElement : activityElements) {
      if (activityElement.getTagName().equals(ActivityTypes.INTERMEDIATE_EVENT_CATCH)) {
        intermediateCatchEvents.put(activityElement.attribute("id"), activityElement);
      }
    }
    return intermediateCatchEvents;
  }

  protected HashMap<String, Element> filterCompensationHandlers(List<Element> activityElements) {
    HashMap<String, Element> compensationHandlers = new HashMap<>();
    for(Element activityElement : activityElements) {
      if (isCompensationHandler(activityElement)) {
        compensationHandlers.put(activityElement.attribute("id"), activityElement);
      }
    }
    return compensationHandlers;
  }

  protected void parseIntermediateCatchEvents(Element scopeElement, ScopeImpl parentScope, Map<String, Element> intermediateCatchEventElements) {
    for (Element intermediateCatchEventElement : intermediateCatchEventElements.values()) {

      if (parentScope.findActivity(intermediateCatchEventElement.attribute("id")) == null) {
        // check whether activity is already parsed
        ActivityImpl activity = parseIntermediateCatchEvent(intermediateCatchEventElement, parentScope, null);

        if (activity != null) {
          parseActivityInputOutput(intermediateCatchEventElement, activity);
        }
      }
    }
    intermediateCatchEventElements.clear();
  }

  protected void parseProcessDefinitionCustomExtensions(Element scopeElement, ProcessDefinition definition) {
    parseStartAuthorization(scopeElement, definition);
  }

  protected void parseStartAuthorization(Element scopeElement, ProcessDefinition definition) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) definition;

    // parse activiti:potentialStarters
    Element extentionsElement = scopeElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> potentialStarterElements = extentionsElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, POTENTIAL_STARTER);

      for (Element potentialStarterElement : potentialStarterElements) {
        parsePotentialStarterResourceAssignment(potentialStarterElement, processDefinition);
      }
    }

    // parse activiti:candidateStarterUsers
    String candidateUsersString = scopeElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, CANDIDATE_STARTER_USERS_EXTENSION);
    if (candidateUsersString != null) {
      List<String> candidateUsers = parseCommaSeparatedList(candidateUsersString);
      for (String candidateUser : candidateUsers) {
        processDefinition.addCandidateStarterUserIdExpression(expressionManager.createExpression(candidateUser.trim()));
      }
    }

    // Candidate activiti:candidateStarterGroups
    String candidateGroupsString = scopeElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, CANDIDATE_STARTER_GROUPS_EXTENSION);
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

  protected void parseAssociations(Element scopeElement, ScopeImpl parentScope, Map<String, Element> compensationHandlers) {
    for (Element associationElement : scopeElement.elements("association")) {
      String sourceRef = associationElement.attribute("sourceRef");
      if (sourceRef == null) {
        addError("association element missing attribute 'sourceRef'", associationElement);
      }
      String targetRef = associationElement.attribute("targetRef");
      if (targetRef == null) {
        addError("association element missing attribute 'targetRef'", associationElement);
      }
      ActivityImpl sourceActivity = parentScope.findActivity(sourceRef);
      ActivityImpl targetActivity = parentScope.findActivity(targetRef);

      // an association may reference elements that are not parsed as activities
      // (like for instance text annotations so do not throw an exception if sourceActivity or targetActivity are null)
      // However, we make sure they reference 'something':
      if (sourceActivity == null && !elementIds.contains(sourceRef)) {
        addError("Invalid reference sourceRef '" + sourceRef + "' of association element ", associationElement);
      } else if (targetActivity == null && !elementIds.contains(targetRef)) {
        addError("Invalid reference targetRef '" + targetRef + "' of association element ", associationElement);
      } else {

        if (sourceActivity != null && ActivityTypes.BOUNDARY_COMPENSATION.equals(sourceActivity.getProperty(BpmnProperties.TYPE.getName()))) {

          if (targetActivity == null && compensationHandlers.containsKey(targetRef)) {
            targetActivity = parseCompensationHandlerForCompensationBoundaryEvent(parentScope, sourceActivity, targetRef, compensationHandlers);

            compensationHandlers.remove(targetActivity.getId());
          }

          if (targetActivity != null) {
            parseAssociationOfCompensationBoundaryEvent(associationElement, sourceActivity, targetActivity);
          }
        }
      }
    }
  }

  protected ActivityImpl parseCompensationHandlerForCompensationBoundaryEvent(ScopeImpl parentScope, ActivityImpl sourceActivity, String targetRef,
      Map<String, Element> compensationHandlers) {

    Element compensationHandler = compensationHandlers.get(targetRef);

    ActivityImpl eventScope = (ActivityImpl) sourceActivity.getEventScope();
    ActivityImpl compensationHandlerActivity = null;
    if (eventScope.isMultiInstance()) {
      ScopeImpl miBody = eventScope.getFlowScope();
      compensationHandlerActivity = parseActivity(compensationHandler, null, miBody);
    } else {
      compensationHandlerActivity = parseActivity(compensationHandler, null, parentScope);
    }

    compensationHandlerActivity.getProperties().set(BpmnProperties.COMPENSATION_BOUNDARY_EVENT, sourceActivity);
    return compensationHandlerActivity;
  }

  protected void parseAssociationOfCompensationBoundaryEvent(Element associationElement, ActivityImpl sourceActivity, ActivityImpl targetActivity) {
    if (!targetActivity.isCompensationHandler()) {
      addError("compensation boundary catch must be connected to element with isForCompensation=true",
          associationElement,
          sourceActivity.getId(),
          targetActivity.getId());

    } else {
      ActivityImpl compensatedActivity = (ActivityImpl) sourceActivity.getEventScope();

      ActivityImpl compensationHandler = compensatedActivity.findCompensationHandler();
      if (compensationHandler != null && compensationHandler.isSubProcessScope()) {
        addError("compensation boundary event and event subprocess with compensation start event are not supported on the same scope",
            associationElement,
            compensatedActivity.getId(),
            sourceActivity.getId()
            );
      } else {

        compensatedActivity.setProperty(PROPERTYNAME_COMPENSATION_HANDLER_ID, targetActivity.getId());
      }
    }
  }

  protected void parseCompensationHandlers(ScopeImpl parentScope, Map<String, Element> compensationHandlers) {
    // compensation handlers attached to compensation boundary events should be already parsed
    for (Element compensationHandler : new HashSet<>(compensationHandlers.values())) {
      parseActivity(compensationHandler, null, parentScope);
    }
    compensationHandlers.clear();
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
    List<ActivityImpl> startEventActivities = new ArrayList<>();
    if (startEventElements.size() > 0) {
      for (Element startEventElement : startEventElements) {

        ActivityImpl startEventActivity = createActivityOnScope(startEventElement, scope);
        parseAsynchronousContinuationForActivity(startEventElement, startEventActivity);

        if (scope instanceof ProcessDefinitionEntity) {
          parseProcessDefinitionStartEvent(startEventActivity, startEventElement, parentElement, scope);
          startEventActivities.add(startEventActivity);
        } else {
          parseScopeStartEvent(startEventActivity, startEventElement, parentElement, (ActivityImpl) scope);
        }

        ensureNoIoMappingDefined(startEventElement);

        parseExecutionListenersOnScope(startEventElement, startEventActivity);
      }
    } else {
      if (Arrays.asList("process", "subProcess").contains(parentElement.getTagName())) {
        addError(parentElement.getTagName() + " must define a startEvent element", parentElement);
      }
    }
    if (scope instanceof ProcessDefinitionEntity) {
      selectInitial(startEventActivities, (ProcessDefinitionEntity) scope, parentElement);
      parseStartFormHandlers(startEventElements, (ProcessDefinitionEntity) scope);
    }

    // invoke parse listeners
    for (Element startEventElement : startEventElements) {
      ActivityImpl startEventActivity = scope.getChildActivity(startEventElement.attribute("id"));
      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseStartEvent(startEventElement, scope, startEventActivity);
      }
    }
  }

  protected void selectInitial(List<ActivityImpl> startEventActivities, ProcessDefinitionEntity processDefinition, Element parentElement) {
    ActivityImpl initial = null;
    // validate that there is s single none start event / timer start event:
    List<String> exclusiveStartEventTypes = Arrays.asList("startEvent", "startTimerEvent");

    for (ActivityImpl activityImpl : startEventActivities) {
      if (exclusiveStartEventTypes.contains(activityImpl.getProperty(BpmnProperties.TYPE.getName()))) {
        if (initial == null) {
          initial = activityImpl;
        } else {
          addError("multiple none start events or timer start events not supported on process definition", parentElement, activityImpl.getId());
        }
      }
    }
    // if there is a single start event, select it as initial, regardless of its type:
    if (initial == null && startEventActivities.size() == 1) {
      initial = startEventActivities.get(0);
    }
    processDefinition.setInitial(initial);
  }

  protected void parseProcessDefinitionStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ScopeImpl scope) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;

    String initiatorVariableName = startEventElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "initiator");
    if (initiatorVariableName != null) {
      processDefinition.setProperty(PROPERTYNAME_INITIATOR_VARIABLE_NAME, initiatorVariableName);
    }

    // all start events share the same behavior:
    startEventActivity.setActivityBehavior(new NoneStartEventActivityBehavior());

    Element timerEventDefinition = startEventElement.element(TIMER_EVENT_DEFINITION);
    Element messageEventDefinition = startEventElement.element(MESSAGE_EVENT_DEFINITION);
    Element signalEventDefinition = startEventElement.element(SIGNAL_EVENT_DEFINITION);
    Element conditionEventDefinition = startEventElement.element(CONDITIONAL_EVENT_DEFINITION);
    if (timerEventDefinition != null) {
      parseTimerStartEventDefinition(timerEventDefinition, startEventActivity, processDefinition);
    } else if (messageEventDefinition != null) {
      startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_MESSAGE);

      EventSubscriptionDeclaration messageStartEventSubscriptionDeclaration =
          parseMessageEventDefinition(messageEventDefinition, startEventElement.attribute("id"));
      messageStartEventSubscriptionDeclaration.setActivityId(startEventActivity.getId());
      messageStartEventSubscriptionDeclaration.setStartEvent(true);

      ensureNoExpressionInMessageStartEvent(messageEventDefinition, messageStartEventSubscriptionDeclaration, startEventElement.attribute("id"));
      addEventSubscriptionDeclaration(messageStartEventSubscriptionDeclaration, processDefinition, startEventElement);
    } else if (signalEventDefinition != null) {
      startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_SIGNAL);
      startEventActivity.setEventScope(scope);

      parseSignalCatchEventDefinition(signalEventDefinition, startEventActivity, true);
    } else if (conditionEventDefinition != null) {
      startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_CONDITIONAL);

      ConditionalEventDefinition conditionalEventDefinition = parseConditionalEventDefinition(conditionEventDefinition, startEventActivity);
      conditionalEventDefinition.setStartEvent(true);
      conditionalEventDefinition.setActivityId(startEventActivity.getId());
      startEventActivity.getProperties().set(BpmnProperties.CONDITIONAL_EVENT_DEFINITION, conditionalEventDefinition);

      addEventSubscriptionDeclaration(conditionalEventDefinition, processDefinition, startEventElement);
    }
  }

  protected void parseStartFormHandlers(List<Element> startEventElements, ProcessDefinitionEntity processDefinition) {
    if (processDefinition.getInitial() != null) {
      for (Element startEventElement : startEventElements) {

        if (startEventElement.attribute("id").equals(processDefinition.getInitial().getId())) {

          StartFormHandler startFormHandler;
          String startFormHandlerClassName = startEventElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "formHandlerClass");
          if (startFormHandlerClassName != null) {
            startFormHandler = (StartFormHandler) ReflectUtil.instantiate(startFormHandlerClassName);
          } else {
            startFormHandler = new DefaultStartFormHandler();
          }

          startFormHandler.parseConfiguration(startEventElement, deployment, processDefinition, this);

          processDefinition.setStartFormHandler(new DelegateStartFormHandler(startFormHandler, deployment));

          FormDefinition formDefinition = parseFormDefinition(startEventElement);
          processDefinition.setStartFormDefinition(formDefinition);

          processDefinition.setHasStartFormKey(formDefinition.getFormKey() != null);
        }

      }
    }
  }

  protected void parseScopeStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ActivityImpl scopeActivity) {

    Properties scopeProperties = scopeActivity.getProperties();

    // set this as the scope's initial
    if (!scopeProperties.contains(BpmnProperties.INITIAL_ACTIVITY)) {
      scopeProperties.set(BpmnProperties.INITIAL_ACTIVITY, startEventActivity);
    } else {
      addError("multiple start events not supported for subprocess", parentElement, startEventActivity.getId());
    }

    Element errorEventDefinition = startEventElement.element(ERROR_EVENT_DEFINITION);
    Element messageEventDefinition = startEventElement.element(MESSAGE_EVENT_DEFINITION);
    Element signalEventDefinition = startEventElement.element(SIGNAL_EVENT_DEFINITION);
    Element timerEventDefinition = startEventElement.element(TIMER_EVENT_DEFINITION);
    Element compensateEventDefinition = startEventElement.element(COMPENSATE_EVENT_DEFINITION);
    Element escalationEventDefinitionElement = startEventElement.element(ESCALATION_EVENT_DEFINITION);
    Element conditionalEventDefinitionElement = startEventElement.element(CONDITIONAL_EVENT_DEFINITION);

    if (scopeActivity.isTriggeredByEvent()) {
      // event subprocess
      EventSubProcessStartEventActivityBehavior behavior = new EventSubProcessStartEventActivityBehavior();

      // parse isInterrupting
      String isInterruptingAttr = startEventElement.attribute(INTERRUPTING);
      boolean isInterrupting = isInterruptingAttr.equalsIgnoreCase(TRUE);

      if (isInterrupting) {
        scopeActivity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_EVENT_SCOPE);
      } else {
        scopeActivity.setActivityStartBehavior(ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE);
      }

      // the event scope of the start event is the flow scope of the event subprocess
      startEventActivity.setEventScope(scopeActivity.getFlowScope());

      if (errorEventDefinition != null) {
        if (!isInterrupting) {
          addError("error start event of event subprocess must be interrupting", startEventElement);
        }
        parseErrorStartEventDefinition(errorEventDefinition, startEventActivity);

      } else if (messageEventDefinition != null) {
        startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_MESSAGE);

        EventSubscriptionDeclaration messageStartEventSubscriptionDeclaration =
            parseMessageEventDefinition(messageEventDefinition, startEventActivity.getId());
        parseEventDefinitionForSubprocess(messageStartEventSubscriptionDeclaration, startEventActivity, messageEventDefinition);

      } else if (signalEventDefinition != null) {
        startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_SIGNAL);

        EventSubscriptionDeclaration eventSubscriptionDeclaration = parseSignalEventDefinition(signalEventDefinition, false, startEventActivity.getId());
        parseEventDefinitionForSubprocess(eventSubscriptionDeclaration, startEventActivity, signalEventDefinition);

      } else if (timerEventDefinition != null) {
        parseTimerStartEventDefinitionForEventSubprocess(timerEventDefinition, startEventActivity, isInterrupting);

      } else if (compensateEventDefinition != null) {
        parseCompensationEventSubprocess(startEventActivity, startEventElement, scopeActivity, compensateEventDefinition);

      } else if (escalationEventDefinitionElement != null) {
        startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_ESCALATION);

        EscalationEventDefinition escalationEventDefinition = createEscalationEventDefinitionForEscalationHandler(escalationEventDefinitionElement, scopeActivity, isInterrupting, startEventActivity.getId());
        addEscalationEventDefinition(startEventActivity.getEventScope(), escalationEventDefinition, escalationEventDefinitionElement, startEventActivity.getId());
      } else if (conditionalEventDefinitionElement != null) {

        final ConditionalEventDefinition conditionalEventDef = parseConditionalStartEventForEventSubprocess(conditionalEventDefinitionElement, startEventActivity, isInterrupting);
        behavior = new EventSubProcessStartConditionalEventActivityBehavior(conditionalEventDef);
      } else {
        addError("start event of event subprocess must be of type 'error', 'message', 'timer', 'signal', 'compensation' or 'escalation'", startEventElement);
      }

     startEventActivity.setActivityBehavior(behavior);
    } else { // "regular" subprocess
      Element conditionalEventDefinition = startEventElement.element(CONDITIONAL_EVENT_DEFINITION);

      if (conditionalEventDefinition != null) {
        addError("conditionalEventDefinition is not allowed on start event within a subprocess", conditionalEventDefinition, startEventActivity.getId());
      }
      if (timerEventDefinition != null) {
        addError("timerEventDefinition is not allowed on start event within a subprocess", timerEventDefinition, startEventActivity.getId());
      }
      if (escalationEventDefinitionElement != null) {
        addError("escalationEventDefinition is not allowed on start event within a subprocess", escalationEventDefinitionElement, startEventActivity.getId());
      }
      if (compensateEventDefinition != null) {
        addError("compensateEventDefinition is not allowed on start event within a subprocess", compensateEventDefinition, startEventActivity.getId());
      }
      if (errorEventDefinition != null) {
        addError("errorEventDefinition only allowed on start event if subprocess is an event subprocess", errorEventDefinition, startEventActivity.getId());
      }
      if (messageEventDefinition != null) {
        addError("messageEventDefinition only allowed on start event if subprocess is an event subprocess", messageEventDefinition, startEventActivity.getId());
      }
      if (signalEventDefinition != null) {
        addError("signalEventDefintion only allowed on start event if subprocess is an event subprocess", signalEventDefinition, startEventActivity.getId());
      }

      startEventActivity.setActivityBehavior(new NoneStartEventActivityBehavior());
    }
  }

  protected void parseCompensationEventSubprocess(ActivityImpl startEventActivity, Element startEventElement, ActivityImpl scopeActivity, Element compensateEventDefinition) {
    startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_COMPENSATION);
    scopeActivity.setProperty(PROPERTYNAME_IS_FOR_COMPENSATION, Boolean.TRUE);

    if (scopeActivity.getFlowScope() instanceof ProcessDefinitionEntity) {
      addError("event subprocess with compensation start event is only supported for embedded subprocess "
          + "(since throwing compensation through a call activity-induced process hierarchy is not supported)", startEventElement);
    }

    ScopeImpl subprocess = scopeActivity.getFlowScope();
    ActivityImpl compensationHandler = ((ActivityImpl) subprocess).findCompensationHandler();
    if (compensationHandler == null) {
      // add property to subprocess
      subprocess.setProperty(PROPERTYNAME_COMPENSATION_HANDLER_ID, scopeActivity.getActivityId());
    } else {

      if (compensationHandler.isSubProcessScope()) {
        addError("multiple event subprocesses with compensation start event are not supported on the same scope", startEventElement);
      } else {
        addError("compensation boundary event and event subprocess with compensation start event are not supported on the same scope", startEventElement);
      }
    }

    validateCatchCompensateEventDefinition(compensateEventDefinition, startEventActivity.getId());
  }

  protected void parseErrorStartEventDefinition(Element errorEventDefinition, ActivityImpl startEventActivity) {
    startEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_ERROR);
    String errorRef = errorEventDefinition.attribute("errorRef");
    Error error = null;
    // the error event definition executes the event subprocess activity which
    // hosts the start event
    String eventSubProcessActivity = startEventActivity.getFlowScope().getId();
    ErrorEventDefinition definition = new ErrorEventDefinition(eventSubProcessActivity);
    if (errorRef != null) {
      error = errors.get(errorRef);
      String errorCode = error == null ? errorRef : error.getErrorCode();
      definition.setErrorCode(errorCode);
    }
    definition.setPrecedence(10);
    setErrorCodeVariableOnErrorEventDefinition(errorEventDefinition, definition);
    setErrorMessageVariableOnErrorEventDefinition(errorEventDefinition, definition);
    addErrorEventDefinition(definition, startEventActivity.getEventScope());
  }

  /**
   * Sets the value for "camunda:errorCodeVariable" on the passed definition if
   * it's present.
   *
   * @param errorEventDefinition
   *          the XML errorEventDefinition tag
   * @param definition
   *          the errorEventDefintion that can get the errorCodeVariable value
   */
  protected void setErrorCodeVariableOnErrorEventDefinition(Element errorEventDefinition, ErrorEventDefinition definition) {
    String errorCodeVar = errorEventDefinition.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "errorCodeVariable");
    if (errorCodeVar != null) {
      definition.setErrorCodeVariable(errorCodeVar);
    }
  }

  /**
   * Sets the value for "camunda:errorMessageVariable" on the passed definition if
   * it's present.
   *
   * @param errorEventDefinition
   *          the XML errorEventDefinition tag
   * @param definition
   *          the errorEventDefintion that can get the errorMessageVariable value
   */
  protected void setErrorMessageVariableOnErrorEventDefinition(Element errorEventDefinition, ErrorEventDefinition definition) {
    String errorMessageVariable = errorEventDefinition.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "errorMessageVariable");
    if (errorMessageVariable != null) {
      definition.setErrorMessageVariable(errorMessageVariable);
    }
  }

  protected EventSubscriptionDeclaration parseMessageEventDefinition(Element messageEventDefinition, String messageElementId) {
    String messageRef = messageEventDefinition.attribute("messageRef");
    if (messageRef == null) {
      addError("attribute 'messageRef' is required", messageEventDefinition, messageElementId);
    }
    MessageDefinition messageDefinition = messages.get(resolveName(messageRef));
    if (messageDefinition == null) {
      addError("Invalid 'messageRef': no message with id '" + messageRef + "' found.", messageEventDefinition, messageElementId);
    }
    return new EventSubscriptionDeclaration(messageDefinition.getExpression(), EventType.MESSAGE);
  }

  protected void addEventSubscriptionDeclaration(EventSubscriptionDeclaration subscription, ScopeImpl scope, Element element) {
    if (subscription.getEventType().equals(EventType.MESSAGE.name()) && (!subscription.hasEventName())) {
      addError("Cannot have a message event subscription with an empty or missing name", element, subscription.getActivityId());
    }

    Map<String, EventSubscriptionDeclaration> eventDefinitions = scope.getProperties().get(BpmnProperties.EVENT_SUBSCRIPTION_DECLARATIONS);

    // if this is a message event, validate that it is the only one with the provided name for this scope
    if (hasMultipleMessageEventDefinitionsWithSameName(subscription, eventDefinitions.values())){
      addError("Cannot have more than one message event subscription with name '" + subscription.getUnresolvedEventName() + "' for scope '" + scope.getId() + "'",
          element, subscription.getActivityId());
    }

    // if this is a signal event, validate that it is the only one with the provided name for this scope
    if (hasMultipleSignalEventDefinitionsWithSameName(subscription, eventDefinitions.values())){
      addError("Cannot have more than one signal event subscription with name '" + subscription.getUnresolvedEventName() + "' for scope '" + scope.getId() + "'",
          element, subscription.getActivityId());
    }
    // if this is a conditional event, validate that it is the only one with the provided condition
    if (subscription.isStartEvent() && hasMultipleConditionalEventDefinitionsWithSameCondition(subscription, eventDefinitions.values())) {
      addError("Cannot have more than one conditional event subscription with the same condition '" + ((ConditionalEventDefinition) subscription).getConditionAsString() + "'", element, subscription.getActivityId());
    }

    scope.getProperties().putMapEntry(BpmnProperties.EVENT_SUBSCRIPTION_DECLARATIONS, subscription.getActivityId(), subscription);
  }

  protected boolean hasMultipleMessageEventDefinitionsWithSameName(EventSubscriptionDeclaration subscription, Collection<EventSubscriptionDeclaration> eventDefinitions) {
    return hasMultipleEventDefinitionsWithSameName(subscription, eventDefinitions, EventType.MESSAGE.name());
  }

  protected boolean hasMultipleSignalEventDefinitionsWithSameName(EventSubscriptionDeclaration subscription, Collection<EventSubscriptionDeclaration> eventDefinitions) {
    return hasMultipleEventDefinitionsWithSameName(subscription, eventDefinitions, EventType.SIGNAL.name());
  }

  protected boolean hasMultipleConditionalEventDefinitionsWithSameCondition(EventSubscriptionDeclaration subscription, Collection<EventSubscriptionDeclaration> eventDefinitions) {
    if (subscription.getEventType().equals(EventType.CONDITONAL.name())) {
      for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
        if (eventDefinition.getEventType().equals(EventType.CONDITONAL.name()) && eventDefinition.isStartEvent() == subscription.isStartEvent()
            && ((ConditionalEventDefinition) eventDefinition).getConditionAsString().equals(((ConditionalEventDefinition) subscription).getConditionAsString())) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean hasMultipleEventDefinitionsWithSameName(EventSubscriptionDeclaration subscription, Collection<EventSubscriptionDeclaration> eventDefinitions, String eventType) {
    if (subscription.getEventType().equals(eventType)) {
      for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
        if (eventDefinition.getEventType().equals(eventType) && eventDefinition.getUnresolvedEventName().equals(subscription.getUnresolvedEventName())
            && eventDefinition.isStartEvent() == subscription.isStartEvent()) {
         return true;
        }
      }
    }
    return false;
  }

  protected void addEventSubscriptionJobDeclaration(EventSubscriptionJobDeclaration jobDeclaration, ActivityImpl activity, Element element) {
    List<EventSubscriptionJobDeclaration> jobDeclarationsForActivity = (List<EventSubscriptionJobDeclaration>) activity.getProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_JOB_DECLARATION);

    if (jobDeclarationsForActivity == null) {
      jobDeclarationsForActivity = new ArrayList<>();
      activity.setProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_JOB_DECLARATION, jobDeclarationsForActivity);
    }

    if(activityAlreadyContainsJobDeclarationEventType(jobDeclarationsForActivity, jobDeclaration)){
      addError("Activity contains already job declaration with type " + jobDeclaration.getEventType(), element, activity.getId());
    }

    jobDeclarationsForActivity.add(jobDeclaration);
  }

  /**
   * Assumes that an activity has at most one declaration of a certain eventType.
   */
  protected boolean activityAlreadyContainsJobDeclarationEventType(List<EventSubscriptionJobDeclaration> jobDeclarationsForActivity,
                                                                   EventSubscriptionJobDeclaration jobDeclaration){
    for(EventSubscriptionJobDeclaration declaration: jobDeclarationsForActivity){
      if(declaration.getEventType().equals(jobDeclaration.getEventType())){
        return true;
      }
    }
    return false;
  }

  /**
   * Parses the activities of a certain level in the process (process,
   * subprocess or another scope).
   *
   * @param activityElements
   *          The list of activities to be parsed. This list may be filtered before.
   * @param parentElement
   *          The 'parent' element that contains the activities (process, subprocess).
   * @param scopeElement
   *          The {@link ScopeImpl} to which the activities must be added.
   */
  public void parseActivities(List<Element> activityElements, Element parentElement, ScopeImpl scopeElement) {
    for (Element activityElement : activityElements) {
      parseActivity(activityElement, parentElement, scopeElement);
    }
  }

  protected ActivityImpl parseActivity(Element activityElement, Element parentElement, ScopeImpl scopeElement) {
    ActivityImpl activity = null;

    boolean isMultiInstance = false;
    ScopeImpl miBody = parseMultiInstanceLoopCharacteristics(activityElement, scopeElement);
    if (miBody != null) {
      scopeElement = miBody;
      isMultiInstance = true;
    }

    if (activityElement.getTagName().equals(ActivityTypes.GATEWAY_EXCLUSIVE)) {
      activity = parseExclusiveGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.GATEWAY_INCLUSIVE)) {
      activity = parseInclusiveGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.GATEWAY_PARALLEL)) {
      activity = parseParallelGateway(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_SCRIPT)) {
      activity = parseScriptTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_SERVICE)) {
      activity = parseServiceTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_BUSINESS_RULE)) {
      activity = parseBusinessRuleTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK)) {
      activity = parseTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_MANUAL_TASK)) {
      activity = parseManualTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_USER_TASK)) {
      activity = parseUserTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_SEND_TASK)) {
      activity = parseSendTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TASK_RECEIVE_TASK)) {
      activity = parseReceiveTask(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.SUB_PROCESS)) {
      activity = parseSubProcess(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.CALL_ACTIVITY)) {
      activity = parseCallActivity(activityElement, scopeElement, isMultiInstance);
    } else if (activityElement.getTagName().equals(ActivityTypes.INTERMEDIATE_EVENT_THROW)) {
      activity = parseIntermediateThrowEvent(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.GATEWAY_EVENT_BASED)) {
      activity = parseEventBasedGateway(activityElement, parentElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.TRANSACTION)) {
      activity = parseTransaction(activityElement, scopeElement);
    } else if (activityElement.getTagName().equals(ActivityTypes.SUB_PROCESS_AD_HOC) || activityElement.getTagName().equals(ActivityTypes.GATEWAY_COMPLEX)) {
      addWarning("Ignoring unsupported activity type", activityElement);
    }

    if (isMultiInstance) {
      activity.setProperty(PROPERTYNAME_IS_MULTI_INSTANCE, true);
    }

    if (activity != null) {
      activity.setName(activityElement.attribute("name"));
      parseActivityInputOutput(activityElement, activity);
    }

    return activity;
  }

  public void validateActivities(List<ActivityImpl> activities) {
    for (ActivityImpl activity : activities) {
      validateActivity(activity);
      // check children if it is an own scope / subprocess / ...
      if (activity.getActivities().size() > 0) {
        validateActivities(activity.getActivities());
      }
    }
  }

  protected void validateActivity(ActivityImpl activity) {
    if (activity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior) {
      validateExclusiveGateway(activity);
    }
    validateOutgoingFlows(activity);
  }

  protected void validateOutgoingFlows(ActivityImpl activity) {
    if (activity.isAsyncAfter()) {
      for (PvmTransition transition : activity.getOutgoingTransitions()) {
        if (transition.getId() == null) {
          addError("Sequence flow with sourceRef='" + activity.getId() + "' must have an id, activity with id '" + activity.getId() + "' uses 'asyncAfter'.",
              null, activity.getId());
        }
      }
    }
  }

  public void validateExclusiveGateway(ActivityImpl activity) {
    if (activity.getOutgoingTransitions().size() == 0) {
      // TODO: double check if this is valid (I think in Activiti yes, since we
      // need start events we will need an end event as well)
      addError("Exclusive Gateway '" + activity.getId() + "' has no outgoing sequence flows.", null, activity.getId());
    } else if (activity.getOutgoingTransitions().size() == 1) {
      PvmTransition flow = activity.getOutgoingTransitions().get(0);
      Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
      if (condition != null) {
        addError("Exclusive Gateway '" + activity.getId() + "' has only one outgoing sequence flow ('" + flow.getId()
            + "'). This is not allowed to have a condition.", null, activity.getId(), flow.getId());
      }
    } else {
      String defaultSequenceFlow = (String) activity.getProperty("default");
      boolean hasDefaultFlow = defaultSequenceFlow != null && defaultSequenceFlow.length() > 0;

      ArrayList<PvmTransition> flowsWithoutCondition = new ArrayList<>();
      for (PvmTransition flow : activity.getOutgoingTransitions()) {
        Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        boolean isDefaultFlow = flow.getId() != null && flow.getId().equals(defaultSequenceFlow);
        boolean hasConditon = condition != null;

        if (!hasConditon && !isDefaultFlow) {
          flowsWithoutCondition.add(flow);
        }
        if (hasConditon && isDefaultFlow) {
          addError("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId()
              + "' which is the default flow but has a condition too.", null, activity.getId(), flow.getId());
        }
      }
      if (hasDefaultFlow || flowsWithoutCondition.size() > 1) {
        // if we either have a default flow (then no flows without conditions
        // are valid at all) or if we have more than one flow without condition
        // this is an error
        for (PvmTransition flow : flowsWithoutCondition) {
          addError(
              "Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' without condition which is not the default flow.",
              null, activity.getId(), flow.getId());
        }
      } else if (flowsWithoutCondition.size() == 1) {
        // Havinf no default and exactly one flow without condition this is
        // considered the default one now (to not break backward compatibility)
        PvmTransition flow = flowsWithoutCondition.get(0);
        addWarning(
            "Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId()
                + "' without condition which is not the default flow. We assume it to be the default flow, but it is bad modeling practice, better set the default flow in your gateway.",
             null, activity.getId(), flow.getId());
      }
    }
  }

  public ActivityImpl parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scopeElement, ActivityImpl eventBasedGateway) {
    ActivityImpl nestedActivity = createActivityOnScope(intermediateEventElement, scopeElement);

    Element timerEventDefinition = intermediateEventElement.element(TIMER_EVENT_DEFINITION);
    Element signalEventDefinition = intermediateEventElement.element(SIGNAL_EVENT_DEFINITION);
    Element messageEventDefinition = intermediateEventElement.element(MESSAGE_EVENT_DEFINITION);
    Element linkEventDefinitionElement = intermediateEventElement.element(LINK_EVENT_DEFINITION);
    Element conditionalEventDefinitionElement = intermediateEventElement.element(CONDITIONAL_EVENT_DEFINITION);

    // shared by all events except for link event
    IntermediateCatchEventActivityBehavior defaultCatchBehaviour = new IntermediateCatchEventActivityBehavior(eventBasedGateway != null);

    parseAsynchronousContinuationForActivity(intermediateEventElement, nestedActivity);
    boolean isEventBaseGatewayPresent = eventBasedGateway != null;

    if (isEventBaseGatewayPresent) {
      nestedActivity.setEventScope(eventBasedGateway);
      nestedActivity.setActivityStartBehavior(ActivityStartBehavior.CANCEL_EVENT_SCOPE);
    } else {
      nestedActivity.setEventScope(nestedActivity);
      nestedActivity.setScope(true);
    }

    nestedActivity.setActivityBehavior(defaultCatchBehaviour);
    if (timerEventDefinition != null) {
      parseIntermediateTimerEventDefinition(timerEventDefinition, nestedActivity);

    } else if (signalEventDefinition != null) {
      parseIntermediateSignalEventDefinition(signalEventDefinition, nestedActivity);

    } else if (messageEventDefinition != null) {
      parseIntermediateMessageEventDefinition(messageEventDefinition, nestedActivity);

    } else if (linkEventDefinitionElement != null) {
      if (isEventBaseGatewayPresent) {
        addError("IntermediateCatchLinkEvent is not allowed after an EventBasedGateway.", intermediateEventElement);
      }
      nestedActivity.setActivityBehavior(new IntermediateCatchLinkEventActivityBehavior());
      parseIntermediateLinkEventCatchBehavior(intermediateEventElement, nestedActivity, linkEventDefinitionElement);

    } else if (conditionalEventDefinitionElement != null) {
      ConditionalEventDefinition conditionalEvent = parseIntermediateConditionalEventDefinition(conditionalEventDefinitionElement, nestedActivity);
      nestedActivity.setActivityBehavior(new IntermediateConditionalEventBehavior(conditionalEvent, isEventBaseGatewayPresent));
    } else {
      addError("Unsupported intermediate catch event type", intermediateEventElement);
    }

    parseExecutionListenersOnScope(intermediateEventElement, nestedActivity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateCatchEvent(intermediateEventElement, scopeElement, nestedActivity);
    }

    return nestedActivity;
  }

  protected void parseIntermediateLinkEventCatchBehavior(Element intermediateEventElement, ActivityImpl activity, Element linkEventDefinitionElement) {

    activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_LINK);

    String linkName = linkEventDefinitionElement.attribute("name");
    String elementName = intermediateEventElement.attribute("name");
    String elementId = intermediateEventElement.attribute("id");

    if (eventLinkTargets.containsKey(linkName)) {
      addError("Multiple Intermediate Catch Events with the same link event name ('" + linkName + "') are not allowed.", intermediateEventElement);
    } else {
      if (!linkName.equals(elementName)) {
        // this is valid - but not a good practice (as it is really confusing
        // for the reader of the process model) - hence we log a warning
        addWarning("Link Event named '" + elementName + "' contains link event definition with name '" + linkName
            + "' - it is recommended to use the same name for both.", intermediateEventElement);
      }

      // now we remember the link in order to replace the sequence flow later on
      eventLinkTargets.put(linkName, elementId);
    }
  }

  protected void parseIntermediateMessageEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {

    nestedActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_MESSAGE);

    EventSubscriptionDeclaration messageDefinition = parseMessageEventDefinition(messageEventDefinition, nestedActivity.getId());
    messageDefinition.setActivityId(nestedActivity.getId());
    addEventSubscriptionDeclaration(messageDefinition, nestedActivity.getEventScope(), messageEventDefinition);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateMessageCatchEventDefinition(messageEventDefinition, nestedActivity);
    }
  }

  public ActivityImpl parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scopeElement) {
    Element signalEventDefinitionElement = intermediateEventElement.element(SIGNAL_EVENT_DEFINITION);
    Element compensateEventDefinitionElement = intermediateEventElement.element(COMPENSATE_EVENT_DEFINITION);
    Element linkEventDefinitionElement = intermediateEventElement.element(LINK_EVENT_DEFINITION);
    Element messageEventDefinitionElement = intermediateEventElement.element(MESSAGE_EVENT_DEFINITION);
    Element escalationEventDefinition = intermediateEventElement.element(ESCALATION_EVENT_DEFINITION);
    String elementId = intermediateEventElement.attribute("id");

    // the link event gets a special treatment as a throwing link event (event
    // source)
    // will not create any activity instance but serves as a "redirection" to
    // the catching link
    // event (event target)
    if (linkEventDefinitionElement != null) {
      String linkName = linkEventDefinitionElement.attribute("name");

      // now we remember the link in order to replace the sequence flow later on
      eventLinkSources.put(elementId, linkName);
      // and done - no activity created
      return null;
    }

    ActivityImpl nestedActivityImpl = createActivityOnScope(intermediateEventElement, scopeElement);
    ActivityBehavior activityBehavior = null;

    parseAsynchronousContinuationForActivity(intermediateEventElement, nestedActivityImpl);

    boolean isServiceTaskLike = isServiceTaskLike(messageEventDefinitionElement);

    if (signalEventDefinitionElement != null) {
      nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_SIGNAL_THROW);

      EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(signalEventDefinitionElement, true, nestedActivityImpl.getId());
      activityBehavior = new ThrowSignalEventActivityBehavior(signalDefinition);
    } else if (compensateEventDefinitionElement != null) {
      nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_COMPENSATION_THROW);
      CompensateEventDefinition compensateEventDefinition = parseThrowCompensateEventDefinition(compensateEventDefinitionElement, scopeElement, elementId);
      activityBehavior = new CompensationEventActivityBehavior(compensateEventDefinition);
      nestedActivityImpl.setProperty(PROPERTYNAME_THROWS_COMPENSATION, true);
      nestedActivityImpl.setScope(true);
    } else if (messageEventDefinitionElement != null) {
      if (isServiceTaskLike) {

        // CAM-436 same behavior as service task
        nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_MESSAGE_THROW);
        parseServiceTaskLike(
            nestedActivityImpl,
            ActivityTypes.INTERMEDIATE_EVENT_MESSAGE_THROW,
            messageEventDefinitionElement,
            intermediateEventElement,
            scopeElement);
      } else {
        // default to non behavior if no service task
        // properties have been specified
        nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_NONE_THROW);
        activityBehavior = new IntermediateThrowNoneEventActivityBehavior();
      }
    } else if (escalationEventDefinition != null) {
      nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_ESCALATION_THROW);

      Escalation escalation = findEscalationForEscalationEventDefinition(escalationEventDefinition, nestedActivityImpl.getId());
      if (escalation != null && escalation.getEscalationCode() == null) {
        addError("throwing escalation event must have an 'escalationCode'", escalationEventDefinition, nestedActivityImpl.getId());
      }

      activityBehavior = new ThrowEscalationEventActivityBehavior(escalation);

    } else { // None intermediate event
      nestedActivityImpl.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_NONE_THROW);
      activityBehavior = new IntermediateThrowNoneEventActivityBehavior();
    }

    if (activityBehavior != null) {
      nestedActivityImpl.setActivityBehavior(activityBehavior);
    }

    parseExecutionListenersOnScope(intermediateEventElement, nestedActivityImpl);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateThrowEvent(intermediateEventElement, scopeElement, nestedActivityImpl);
    }

    if (isServiceTaskLike) {
      // activity behavior could be set by a listener (e.g. connector); thus,
      // check is after listener invocation
      validateServiceTaskLike(nestedActivityImpl,
          ActivityTypes.INTERMEDIATE_EVENT_MESSAGE_THROW,
          messageEventDefinitionElement);
    }

    return nestedActivityImpl;
  }

  protected CompensateEventDefinition parseThrowCompensateEventDefinition(final Element compensateEventDefinitionElement, ScopeImpl scopeElement, final String parentElementId) {
    final String activityRef = compensateEventDefinitionElement.attribute("activityRef");
    boolean waitForCompletion = TRUE.equals(compensateEventDefinitionElement.attribute("waitForCompletion", TRUE));

    if (activityRef != null) {
      if (scopeElement.findActivityAtLevelOfSubprocess(activityRef) == null) {
        Boolean isTriggeredByEvent = scopeElement.getProperties().get(BpmnProperties.TRIGGERED_BY_EVENT);
        String type = (String) scopeElement.getProperty(PROPERTYNAME_TYPE);
        if (Boolean.TRUE == isTriggeredByEvent && "subProcess".equals(type)) {
          scopeElement = scopeElement.getFlowScope();
        }
        if (scopeElement.findActivityAtLevelOfSubprocess(activityRef) == null) {
          final String scopeId = scopeElement.getId();
          scopeElement.addToBacklog(activityRef, new ScopeImpl.BacklogErrorCallback() {

            @Override
            public void callback() {
              addError("Invalid attribute value for 'activityRef': no activity with id '" + activityRef + "' in scope '" + scopeId + "'",
              compensateEventDefinitionElement,
              parentElementId);
            }
          });
        }
      }
    }

    CompensateEventDefinition compensateEventDefinition = new CompensateEventDefinition();
    compensateEventDefinition.setActivityRef(activityRef);

    compensateEventDefinition.setWaitForCompletion(waitForCompletion);
    if (!waitForCompletion) {
      addWarning(
          "Unsupported attribute value for 'waitForCompletion': 'waitForCompletion=false' is not supported. Compensation event will wait for compensation to join.",
          compensateEventDefinitionElement, parentElementId);
    }

    return compensateEventDefinition;
  }

  protected void validateCatchCompensateEventDefinition(Element compensateEventDefinitionElement, String parentElementId) {
    String activityRef = compensateEventDefinitionElement.attribute("activityRef");
    if (activityRef != null) {
      addWarning("attribute 'activityRef' is not supported on catching compensation event. attribute will be ignored",
          compensateEventDefinitionElement, parentElementId);
    }

    String waitForCompletion = compensateEventDefinitionElement.attribute("waitForCompletion");
    if (waitForCompletion != null) {
      addWarning("attribute 'waitForCompletion' is not supported on catching compensation event. attribute will be ignored", compensateEventDefinitionElement, parentElementId);
    }
  }

  protected void parseBoundaryCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl activity) {
    activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_COMPENSATION);

    ScopeImpl hostActivity = activity.getEventScope();
    for (ActivityImpl sibling : activity.getFlowScope().getActivities()) {
      if (sibling.getProperty(BpmnProperties.TYPE.getName()).equals("compensationBoundaryCatch") && sibling.getEventScope().equals(hostActivity) && sibling != activity) {
        addError("multiple boundary events with compensateEventDefinition not supported on same activity", compensateEventDefinition, activity.getId());
      }
    }

    validateCatchCompensateEventDefinition(compensateEventDefinition, activity.getId());
  }

  protected ActivityBehavior parseBoundaryCancelEventDefinition(Element cancelEventDefinition, ActivityImpl activity) {
    activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_CANCEL);

    LegacyBehavior.parseCancelBoundaryEvent(activity);

    ActivityImpl transaction = (ActivityImpl) activity.getEventScope();
    if (transaction.getActivityBehavior() != null && transaction.getActivityBehavior() instanceof MultiInstanceActivityBehavior) {
      transaction = transaction.getActivities().get(0);
    }

    if (!"transaction".equals(transaction.getProperty(BpmnProperties.TYPE.getName()))) {
      addError("boundary event with cancelEventDefinition only supported on transaction subprocesses", cancelEventDefinition, activity.getId());
    }

    // ensure there is only one cancel boundary event
    for (ActivityImpl sibling : activity.getFlowScope().getActivities()) {
      if ("cancelBoundaryCatch".equals(sibling.getProperty(BpmnProperties.TYPE.getName())) && sibling != activity && sibling.getEventScope() == transaction) {
        addError("multiple boundary events with cancelEventDefinition not supported on same transaction subprocess", cancelEventDefinition, activity.getId());
      }
    }

    // find all cancel end events
    for (ActivityImpl childActivity : transaction.getActivities()) {
      ActivityBehavior activityBehavior = childActivity.getActivityBehavior();
      if (activityBehavior != null && activityBehavior instanceof CancelEndEventActivityBehavior) {
        ((CancelEndEventActivityBehavior) activityBehavior).setCancelBoundaryEvent(activity);
      }
    }

    return new CancelBoundaryEventActivityBehavior();
  }

  /**
   * Parses loopCharacteristics (standardLoop/Multi-instance) of an activity, if
   * any is defined.
   */
  public ScopeImpl parseMultiInstanceLoopCharacteristics(Element activityElement, ScopeImpl scope) {

    Element miLoopCharacteristics = activityElement.element("multiInstanceLoopCharacteristics");
    if (miLoopCharacteristics == null) {
      return null;
    } else {
      String id = activityElement.attribute("id");

      LOG.parsingElement("mi body for activity", id);

      id = getIdForMiBody(id);
      ActivityImpl miBodyScope = scope.createActivity(id);
      setActivityAsyncDelegates(miBodyScope);
      miBodyScope.setProperty(PROPERTYNAME_TYPE, ActivityTypes.MULTI_INSTANCE_BODY);
      miBodyScope.setScope(true);

      boolean isSequential = parseBooleanAttribute(miLoopCharacteristics.attribute("isSequential"), false);

      MultiInstanceActivityBehavior behavior = null;
      if (isSequential) {
        behavior = new SequentialMultiInstanceActivityBehavior();
      } else {
        behavior = new ParallelMultiInstanceActivityBehavior();
      }
      miBodyScope.setActivityBehavior(behavior);

      // loopCardinality
      Element loopCardinality = miLoopCharacteristics.element("loopCardinality");
      if (loopCardinality != null) {
        String loopCardinalityText = loopCardinality.getText();
        if (loopCardinalityText == null || "".equals(loopCardinalityText)) {
          addError("loopCardinality must be defined for a multiInstanceLoopCharacteristics definition ", miLoopCharacteristics, id);
        }
        behavior.setLoopCardinalityExpression(expressionManager.createExpression(loopCardinalityText));
      }

      // completionCondition
      Element completionCondition = miLoopCharacteristics.element("completionCondition");
      if (completionCondition != null) {
        String completionConditionText = completionCondition.getText();
        behavior.setCompletionConditionExpression(expressionManager.createExpression(completionConditionText));
      }

      // activiti:collection
      String collection = miLoopCharacteristics.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "collection");
      if (collection != null) {
        if (collection.contains("{")) {
          behavior.setCollectionExpression(expressionManager.createExpression(collection));
        } else {
          behavior.setCollectionVariable(collection);
        }
      }

      // loopDataInputRef
      Element loopDataInputRef = miLoopCharacteristics.element("loopDataInputRef");
      if (loopDataInputRef != null) {
        String loopDataInputRefText = loopDataInputRef.getText();
        if (loopDataInputRefText != null) {
          if (loopDataInputRefText.contains("{")) {
            behavior.setCollectionExpression(expressionManager.createExpression(loopDataInputRefText));
          } else {
            behavior.setCollectionVariable(loopDataInputRefText);
          }
        }
      }

      // activiti:elementVariable
      String elementVariable = miLoopCharacteristics.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "elementVariable");
      if (elementVariable != null) {
        behavior.setCollectionElementVariable(elementVariable);
      }

      // dataInputItem
      Element inputDataItem = miLoopCharacteristics.element("inputDataItem");
      if (inputDataItem != null) {
        String inputDataItemName = inputDataItem.attribute("name");
        behavior.setCollectionElementVariable(inputDataItemName);
      }

      // Validation
      if (behavior.getLoopCardinalityExpression() == null && behavior.getCollectionExpression() == null && behavior.getCollectionVariable() == null) {
        addError("Either loopCardinality or loopDataInputRef/activiti:collection must be set", miLoopCharacteristics, id);
      }

      // Validation
      if (behavior.getCollectionExpression() == null && behavior.getCollectionVariable() == null && behavior.getCollectionElementVariable() != null) {
        addError("LoopDataInputRef/activiti:collection must be set when using inputDataItem or activiti:elementVariable", miLoopCharacteristics, id);
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseMultiInstanceLoopCharacteristics(activityElement, miLoopCharacteristics, miBodyScope);
      }

      return miBodyScope;
    }
  }

  public static String getIdForMiBody(String id) {
    return id + MULTI_INSTANCE_BODY_ID_SUFFIX;
  }

  /**
   * Parses the generic information of an activity element (id, name,
   * documentation, etc.), and creates a new {@link ActivityImpl} on the given
   * scope element.
   */
  public ActivityImpl createActivityOnScope(Element activityElement, ScopeImpl scopeElement) {
    String id = activityElement.attribute("id");

    LOG.parsingElement("activity", id);
    ActivityImpl activity = scopeElement.createActivity(id);

    activity.setProperty("name", activityElement.attribute("name"));
    activity.setProperty("documentation", parseDocumentation(activityElement));
    activity.setProperty("default", activityElement.attribute("default"));
    activity.getProperties().set(BpmnProperties.TYPE, activityElement.getTagName());
    activity.setProperty("line", activityElement.getLine());
    setActivityAsyncDelegates(activity);
    activity.setProperty(PROPERTYNAME_JOB_PRIORITY, parsePriority(activityElement, PROPERTYNAME_JOB_PRIORITY));

    if (isCompensationHandler(activityElement)) {
      activity.setProperty(PROPERTYNAME_IS_FOR_COMPENSATION, true);
    }

    return activity;
  }

  /**
   * Sets the delegates for the activity, which will be called
   * if the attribute asyncAfter or asyncBefore was changed.
   *
   * @param activity the activity which gets the delegates
   */
  protected void setActivityAsyncDelegates(final ActivityImpl activity) {
    activity.setDelegateAsyncAfterUpdate(new ActivityImpl.AsyncAfterUpdate() {
      @Override
      public void updateAsyncAfter(boolean asyncAfter, boolean exclusive) {
        if (asyncAfter) {
          addMessageJobDeclaration(new AsyncAfterMessageJobDeclaration(), activity, exclusive);
        } else {
          removeMessageJobDeclarationWithJobConfiguration(activity, MessageJobDeclaration.ASYNC_AFTER);
        }
      }
    });

    activity.setDelegateAsyncBeforeUpdate(new ActivityImpl.AsyncBeforeUpdate() {
      @Override
      public void updateAsyncBefore(boolean asyncBefore, boolean exclusive) {
        if (asyncBefore) {
          addMessageJobDeclaration(new AsyncBeforeMessageJobDeclaration(), activity, exclusive);
        } else {
          removeMessageJobDeclarationWithJobConfiguration(activity, MessageJobDeclaration.ASYNC_BEFORE);
        }
      }
    });
  }

  /**
   * Adds the new message job declaration to existing declarations.
   * There will be executed an existing check before the adding is executed.
   *
   * @param messageJobDeclaration the new message job declaration
   * @param activity the corresponding activity
   * @param exclusive the flag which indicates if the async should be exclusive
   */
  protected void addMessageJobDeclaration(MessageJobDeclaration messageJobDeclaration, ActivityImpl activity, boolean exclusive) {
    ProcessDefinition procDef = (ProcessDefinition) activity.getProcessDefinition();
    if (!exists(messageJobDeclaration, procDef.getKey(), activity.getActivityId())) {
      messageJobDeclaration.setExclusive(exclusive);
      messageJobDeclaration.setActivity(activity);
      messageJobDeclaration.setJobPriorityProvider((ParameterValueProvider) activity.getProperty(PROPERTYNAME_JOB_PRIORITY));

      addMessageJobDeclarationToActivity(messageJobDeclaration, activity);
      addJobDeclarationToProcessDefinition(messageJobDeclaration, procDef);
    }
  }

  /**
   * Checks whether the message declaration already exists.
   *
   * @param msgJobdecl the message job declaration which is searched
   * @param procDefKey the corresponding process definition key
   * @param activityId the corresponding activity id
   * @return true if the message job declaration exists, false otherwise
   */
  protected boolean exists(MessageJobDeclaration msgJobdecl, String procDefKey, String activityId) {
    boolean exist = false;
    List<JobDeclaration<?, ?>> declarations = jobDeclarations.get(procDefKey);
    if (declarations != null) {
      for (int i = 0; i < declarations.size() && !exist; i++) {
        JobDeclaration<?, ?> decl = declarations.get(i);
        if (decl.getActivityId().equals(activityId) &&
            decl.getJobConfiguration().equalsIgnoreCase(msgJobdecl.getJobConfiguration())) {
          exist = true;
        }
      }
    }
    return exist;
  }

  /**
   * Removes a job declaration which belongs to the given activity and has the given job configuration.
   *
   * @param activity the activity of the job declaration
   * @param jobConfiguration  the job configuration of the declaration
   */
  protected void removeMessageJobDeclarationWithJobConfiguration(ActivityImpl activity, String jobConfiguration) {
    List<MessageJobDeclaration> messageJobDeclarations = (List<MessageJobDeclaration>) activity.getProperty(PROPERTYNAME_MESSAGE_JOB_DECLARATION);
    if (messageJobDeclarations != null) {
      Iterator<MessageJobDeclaration> iter = messageJobDeclarations.iterator();
      while (iter.hasNext()) {
        MessageJobDeclaration msgDecl = iter.next();
        if (msgDecl.getJobConfiguration().equalsIgnoreCase(jobConfiguration)
          && msgDecl.getActivityId().equalsIgnoreCase(activity.getActivityId())) {
          iter.remove();
        }
      }
    }

    ProcessDefinition procDef = (ProcessDefinition) activity.getProcessDefinition();
    List<JobDeclaration<?, ?>> declarations = jobDeclarations.get(procDef.getKey());
    if (declarations != null) {
      Iterator<JobDeclaration<?, ?>> iter = declarations.iterator();
      while (iter.hasNext()) {
        JobDeclaration<?, ?> jobDcl = iter.next();
        if (jobDcl.getJobConfiguration().equalsIgnoreCase(jobConfiguration)
            && jobDcl.getActivityId().equalsIgnoreCase(activity.getActivityId())) {
          iter.remove();
        }
      }
    }
  }

  public String parseDocumentation(Element element) {
    List<Element> docElements = element.elements("documentation");
    List<String> docStrings = new ArrayList<>();
    for (Element e : docElements) {
      docStrings.add(e.getText());
    }

    return parseDocumentation(docStrings);
  }

  public static String parseDocumentation(List<String> docStrings) {
    if (docStrings.isEmpty()) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    for (String e : docStrings) {
      if (builder.length() != 0) {
        builder.append("\n\n");
      }

      builder.append(e.trim());
    }

    return builder.toString();
  }

  protected boolean isCompensationHandler(Element activityElement) {
    String isForCompensation = activityElement.attribute("isForCompensation");
    return isForCompensation != null && isForCompensation.equalsIgnoreCase(TRUE);
  }

  /**
   * Parses an exclusive gateway declaration.
   */
  public ActivityImpl parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(exclusiveGwElement, scope);
    activity.setActivityBehavior(new ExclusiveGatewayActivityBehavior());

    parseAsynchronousContinuationForActivity(exclusiveGwElement, activity);

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

    parseAsynchronousContinuationForActivity(inclusiveGwElement, activity);

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

    parseAsynchronousContinuationForActivity(eventBasedGwElement, activity);

    if (activity.isAsyncAfter()) {
      addError("'asyncAfter' not supported for " + eventBasedGwElement.getTagName() + " elements.", eventBasedGwElement);
    }

    parseExecutionListenersOnScope(eventBasedGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseEventBasedGateway(eventBasedGwElement, scope, activity);
    }

    // find all outgoing sequence flows:
    List<Element> sequenceFlows = parentElement.elements("sequenceFlow");

    // collect all siblings in a map
    Map<String, Element> siblingsMap = new HashMap<>();
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
          if (sibling.getTagName().equals(ActivityTypes.INTERMEDIATE_EVENT_CATCH)) {
            ActivityImpl catchEventActivity = parseIntermediateCatchEvent(sibling, scope, activity);

            if (catchEventActivity != null) {
              parseActivityInputOutput(sibling, catchEventActivity);
            }

          } else {
            addError("Event based gateway can only be connected to elements of type intermediateCatchEvent", eventBasedGwElement);
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

    parseAsynchronousContinuationForActivity(parallelGwElement, activity);

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

    ScriptTaskActivityBehavior activityBehavior = parseScriptTaskElement(scriptTaskElement);

    if (activityBehavior != null) {
      parseAsynchronousContinuationForActivity(scriptTaskElement, activity);

      activity.setActivityBehavior(activityBehavior);

      parseExecutionListenersOnScope(scriptTaskElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseScriptTask(scriptTaskElement, scope, activity);
      }
    }

    return activity;
  }

  /**
   * Returns a {@link ScriptTaskActivityBehavior} for the script task element
   * corresponding to the script source or resource specified.
   *
   * @param scriptTaskElement
   *          the script task element
   * @return the corresponding {@link ScriptTaskActivityBehavior}
   */
  protected ScriptTaskActivityBehavior parseScriptTaskElement(Element scriptTaskElement) {
    // determine script language
    String language = scriptTaskElement.attribute("scriptFormat");
    if (language == null) {
      language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
    }
    String resultVariableName = parseResultVariable(scriptTaskElement);

    // determine script source
    String scriptSource = null;
    Element scriptElement = scriptTaskElement.element("script");
    if (scriptElement != null) {
      scriptSource = scriptElement.getText();
    }
    String scriptResource = scriptTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_RESOURCE);

    try {
      ExecutableScript script = ScriptUtil.getScript(language, scriptSource, scriptResource, expressionManager);
      return new ScriptTaskActivityBehavior(script, resultVariableName);
    } catch (ProcessEngineException e) {
      addError("Unable to process ScriptTask: " + e.getMessage(), scriptElement);
      return null;
    }
  }

  protected String parseResultVariable(Element element) {
    // determine if result variable exists
    String resultVariableName = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "resultVariable");
    if (resultVariableName == null) {
      // for backwards compatible reasons
      resultVariableName = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "resultVariableName");
    }
    return resultVariableName;
  }

  /**
   * Parses a serviceTask declaration.
   */
  public ActivityImpl parseServiceTask(Element serviceTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(serviceTaskElement, scope);

    parseAsynchronousContinuationForActivity(serviceTaskElement, activity);

    String elementName = "serviceTask";
    parseServiceTaskLike(activity, elementName, serviceTaskElement, serviceTaskElement, scope);

    parseExecutionListenersOnScope(serviceTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseServiceTask(serviceTaskElement, scope, activity);
    }

    // activity behavior could be set by a listener (e.g. connector); thus,
    // check is after listener invocation
    validateServiceTaskLike(activity, elementName, serviceTaskElement);

    return activity;
  }

  /**
   * @param elementName
   * @param serviceTaskElement the element that contains the camunda service task definition
   *   (e.g. camunda:class attributes)
   * @param camundaPropertiesElement the element that contains the camunda:properties extension elements
   *   that apply to this service task. Usually, but not always, this is the same as serviceTaskElement
   * @param scope
   * @return
   */
  public void parseServiceTaskLike(
      ActivityImpl activity,
      String elementName,
      Element serviceTaskElement,
      Element camundaPropertiesElement,
      ScopeImpl scope) {

    String type = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE);
    String className = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_CLASS);
    String expression = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_EXPRESSION);
    String delegateExpression = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_DELEGATE_EXPRESSION);
    String resultVariableName = parseResultVariable(serviceTaskElement);

    if (type != null) {
      if (type.equalsIgnoreCase("mail")) {
        parseEmailServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else if (type.equalsIgnoreCase("shell")) {
        parseShellServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else if (type.equalsIgnoreCase("external")) {
        parseExternalServiceTask(activity, serviceTaskElement, camundaPropertiesElement);
      } else {
        addError("Invalid usage of type attribute on " + elementName + ": '" + type + "'", serviceTaskElement);
      }
    } else if (className != null && className.trim().length() > 0) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for " + elementName + " elements using 'class'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ClassDelegateActivityBehavior(className, parseFieldDeclarations(serviceTaskElement)));

    } else if (delegateExpression != null) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for " + elementName + " elements using 'delegateExpression'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ServiceTaskDelegateExpressionActivityBehavior(expressionManager.createExpression(delegateExpression),
          parseFieldDeclarations(serviceTaskElement)));

    } else if (expression != null && expression.trim().length() > 0) {
      activity.setActivityBehavior(new ServiceTaskExpressionActivityBehavior(expressionManager.createExpression(expression), resultVariableName));

    }
  }

  protected void validateServiceTaskLike(
      ActivityImpl activity,
      String elementName,
      Element serviceTaskElement
      ) {
    if (activity.getActivityBehavior() == null) {
      addError("One of the attributes 'class', 'delegateExpression', 'type', "
          + "or 'expression' is mandatory on " + elementName + ". If you are using a connector, make sure the"
          + "connect process engine plugin is registered with the process engine.", serviceTaskElement);
    }
  }

  /**
   * Parses a businessRuleTask declaration.
   */
  public ActivityImpl parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope) {
    String decisionRef = businessRuleTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "decisionRef");
    if (decisionRef != null) {
      return parseDmnBusinessRuleTask(businessRuleTaskElement, scope);
    }
    else {
      ActivityImpl activity = createActivityOnScope(businessRuleTaskElement, scope);
      parseAsynchronousContinuationForActivity(businessRuleTaskElement, activity);

      String elementName = "businessRuleTask";
      parseServiceTaskLike(
          activity,
          elementName,
          businessRuleTaskElement,
          businessRuleTaskElement,
          scope);

      parseExecutionListenersOnScope(businessRuleTaskElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseBusinessRuleTask(businessRuleTaskElement, scope, activity);
      }

      // activity behavior could be set by a listener (e.g. connector); thus,
      // check is after listener invocation
      validateServiceTaskLike(activity,
          elementName,
          businessRuleTaskElement);

      return activity;
    }
  }

  /**
   * Parse a Business Rule Task which references a decision.
   */
  protected ActivityImpl parseDmnBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(businessRuleTaskElement, scope);
    // the activity is a scope since the result variable is stored as local variable
    activity.setScope(true);

    parseAsynchronousContinuationForActivity(businessRuleTaskElement, activity);

    String decisionRef = businessRuleTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "decisionRef");

    BaseCallableElement callableElement = new BaseCallableElement();
    callableElement.setDeploymentId(deployment.getId());

    ParameterValueProvider definitionKeyProvider = createParameterValueProvider(decisionRef, expressionManager);
    callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);

    parseBinding(businessRuleTaskElement, activity, callableElement, "decisionRefBinding");
    parseVersion(businessRuleTaskElement, activity, callableElement, "decisionRefBinding", "decisionRefVersion");
    parseVersionTag(businessRuleTaskElement, activity, callableElement, "decisionRefBinding", "decisionRefVersionTag");
    parseTenantId(businessRuleTaskElement, activity, callableElement, "decisionRefTenantId");

    String resultVariable = parseResultVariable(businessRuleTaskElement);
    DecisionResultMapper decisionResultMapper = parseDecisionResultMapper(businessRuleTaskElement);

    DmnBusinessRuleTaskActivityBehavior behavior = new DmnBusinessRuleTaskActivityBehavior(callableElement, resultVariable, decisionResultMapper);
    activity.setActivityBehavior(behavior);

    parseExecutionListenersOnScope(businessRuleTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBusinessRuleTask(businessRuleTaskElement, scope, activity);
    }

    return activity;
  }

  protected DecisionResultMapper parseDecisionResultMapper(Element businessRuleTaskElement) {
    // default mapper is 'resultList'
    String decisionResultMapper = businessRuleTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "mapDecisionResult");
    DecisionResultMapper mapper = DecisionEvaluationUtil.getDecisionResultMapperForName(decisionResultMapper);

    if (mapper == null) {
      addError("No decision result mapper found for name '" + decisionResultMapper
          + "'. Supported mappers are 'singleEntry', 'singleResult', 'collectEntries' and 'resultList'.", businessRuleTaskElement);
    }

    return mapper;
  }

  /**
   * Parse async continuation of an activity and create async jobs for the activity.
   * <br/> <br/>
   * When the activity is marked as multi instance, then async jobs create instead for the multi instance body.
   * When the wrapped activity has async characteristics in 'multiInstanceLoopCharacteristics' element,
   * then async jobs create additionally for the wrapped activity.
   */
  protected void parseAsynchronousContinuationForActivity(Element activityElement, ActivityImpl activity) {
    // can't use #getMultiInstanceScope here to determine whether the task is multi-instance,
    // since the property hasn't been set yet (cf parseActivity)
    ActivityImpl parentFlowScopeActivity = activity.getParentFlowScopeActivity();
    if (parentFlowScopeActivity != null && parentFlowScopeActivity.getActivityBehavior() instanceof MultiInstanceActivityBehavior
        && !activity.isCompensationHandler()) {

      parseAsynchronousContinuation(activityElement, parentFlowScopeActivity);

      Element miLoopCharacteristics = activityElement.element("multiInstanceLoopCharacteristics");
      parseAsynchronousContinuation(miLoopCharacteristics, activity);

    } else {
      parseAsynchronousContinuation(activityElement, activity);
    }
  }

  /**
   * Parse async continuation of the given element and create async jobs for the activity.
   *
   * @param element with async characteristics
   * @param activity
   */
  protected void parseAsynchronousContinuation(Element element, ActivityImpl activity) {

    boolean isAsyncBefore = isAsyncBefore(element);
    boolean isAsyncAfter = isAsyncAfter(element);
    boolean exclusive = isExclusive(element);

    // set properties on activity
    activity.setAsyncBefore(isAsyncBefore, exclusive);
    activity.setAsyncAfter(isAsyncAfter, exclusive);
  }

  protected ParameterValueProvider parsePriority(Element element, String priorityAttribute) {
    String priorityAttributeValue = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, priorityAttribute);

    if (priorityAttributeValue == null) {
      return null;

    } else {
      Object value = priorityAttributeValue;
      if (!StringUtil.isExpression(priorityAttributeValue)) {
        // constant values must be valid integers
        try {
          value = Integer.parseInt(priorityAttributeValue);

        } catch (NumberFormatException e) {
          addError("Value '" + priorityAttributeValue + "' for attribute '" + priorityAttribute + "' is not a valid number", element);
        }
      }

      return createParameterValueProvider(value, expressionManager);
    }
  }

  protected ParameterValueProvider parseTopic(Element element, String topicAttribute) {
    String topicAttributeValue = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, topicAttribute);

    if (topicAttributeValue == null) {
      addError("External tasks must specify a 'topic' attribute in the camunda namespace", element);
      return null;

    } else {
      return createParameterValueProvider(topicAttributeValue, expressionManager);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addMessageJobDeclarationToActivity(MessageJobDeclaration messageJobDeclaration, ActivityImpl activity) {
    List<MessageJobDeclaration> messageJobDeclarations = (List<MessageJobDeclaration>) activity.getProperty(PROPERTYNAME_MESSAGE_JOB_DECLARATION);
    if (messageJobDeclarations == null) {
      messageJobDeclarations = new ArrayList<>();
      activity.setProperty(PROPERTYNAME_MESSAGE_JOB_DECLARATION, messageJobDeclarations);
    }
    messageJobDeclarations.add(messageJobDeclaration);
  }

  protected void addJobDeclarationToProcessDefinition(JobDeclaration<?, ?> jobDeclaration, ProcessDefinition processDefinition) {
    String key = processDefinition.getKey();

    List<JobDeclaration<?, ?>> containingJobDeclarations = jobDeclarations.get(key);
    if (containingJobDeclarations == null) {
      containingJobDeclarations = new ArrayList<>();
      jobDeclarations.put(key, containingJobDeclarations);
    }

    containingJobDeclarations.add(jobDeclaration);
  }

  /**
   * Parses a sendTask declaration.
   */
  public ActivityImpl parseSendTask(Element sendTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(sendTaskElement, scope);

    if (isServiceTaskLike(sendTaskElement)) {
      // CAM-942: If expression or class is set on a SendTask it behaves like a service task
      // to allow implementing the send handling yourself
      String elementName = "sendTask";
      parseAsynchronousContinuationForActivity(sendTaskElement, activity);

      parseServiceTaskLike(activity, elementName, sendTaskElement, sendTaskElement, scope);

      parseExecutionListenersOnScope(sendTaskElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseSendTask(sendTaskElement, scope, activity);
      }

      // activity behavior could be set by a listener (e.g. connector); thus,
      // check is after listener invocation
      validateServiceTaskLike(activity, elementName, sendTaskElement);

    } else {
      parseAsynchronousContinuationForActivity(sendTaskElement, activity);
      parseExecutionListenersOnScope(sendTaskElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseSendTask(sendTaskElement, scope, activity);
      }

      // activity behavior could be set by a listener; thus, check is after listener invocation
      if (activity.getActivityBehavior() == null) {
        addError("One of the attributes 'class', 'delegateExpression', 'type', or 'expression' is mandatory on sendTask.", sendTaskElement);
      }
    }

    return activity;
  }

  protected void parseEmailServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    validateFieldDeclarationsForEmail(serviceTaskElement, fieldDeclarations);
    activity.setActivityBehavior((MailActivityBehavior) instantiateDelegate(MailActivityBehavior.class, fieldDeclarations));
  }

  protected void parseShellServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    validateFieldDeclarationsForShell(serviceTaskElement, fieldDeclarations);
    activity.setActivityBehavior((ActivityBehavior) instantiateDelegate(ShellActivityBehavior.class, fieldDeclarations));
  }

  protected void parseExternalServiceTask(ActivityImpl activity,
      Element serviceTaskElement,
      Element camundaPropertiesElement) {
    activity.setScope(true);

    ParameterValueProvider topicNameProvider = parseTopic(serviceTaskElement, PROPERTYNAME_EXTERNAL_TASK_TOPIC);
    ParameterValueProvider priorityProvider = parsePriority(serviceTaskElement, PROPERTYNAME_TASK_PRIORITY);
    Map<String, String> properties = parseCamundaExtensionProperties(camundaPropertiesElement);
    activity.getProperties().set(BpmnProperties.EXTENSION_PROPERTIES, properties);
    List<CamundaErrorEventDefinition> camundaErrorEventDefinitions = parseCamundaErrorEventDefinitions(activity, serviceTaskElement);
    activity.getProperties().set(BpmnProperties.CAMUNDA_ERROR_EVENT_DEFINITION, camundaErrorEventDefinitions);
    activity.setActivityBehavior(new ExternalTaskActivityBehavior(topicNameProvider, priorityProvider));
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

      if ((fieldName.equals("wait") || fieldName.equals("redirectError") || fieldName.equals("cleanEnv")) && !fieldValue.toLowerCase().equals(TRUE)
          && !fieldValue.toLowerCase().equals("false")) {
        addError("undefined value for shell " + fieldName + " parameter :" + fieldValue.toString(), serviceTaskElement);
      }

    }

    if (!shellCommandDefined) {
      addError("No shell command is defined on the shell activity", serviceTaskElement);
    }
  }

  public List<FieldDeclaration> parseFieldDeclarations(Element element) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    Element elementWithFieldInjections = element.element("extensionElements");
    if (elementWithFieldInjections == null) { // Custom extensions will just
                                              // have the <field.. as a
                                              // subelement
      elementWithFieldInjections = element;
    }
    List<Element> fieldDeclarationElements = elementWithFieldInjections.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "field");
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
      addError(
          "One of the following is mandatory on a field declaration: one of attributes stringValue|expression " + "or one of child elements string|expression",
          serviceTaskElement);
    }
    return fieldDeclaration;
  }

  protected FieldDeclaration parseStringFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
    try {
      String fieldValue = getStringValueFromAttributeOrElement("stringValue", "string", fieldDeclarationElement, serviceTaskElement.attribute("id"));
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
      String expression = getStringValueFromAttributeOrElement(PROPERTYNAME_EXPRESSION, PROPERTYNAME_EXPRESSION, fieldDeclarationElement, serviceTaskElement.attribute("id"));
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

  protected String getStringValueFromAttributeOrElement(String attributeName, String elementName, Element element, String ancestorElementId) {
    String value = null;

    String attributeValue = element.attribute(attributeName);
    Element childElement = element.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, elementName);
    String stringElementText = null;

    if (attributeValue != null && childElement != null) {
      addError("Can't use attribute '" + attributeName + "' and element '" + elementName + "' together, only use one", element, ancestorElementId);
    } else if (childElement != null) {
      stringElementText = childElement.getText();
      if (stringElementText == null || stringElementText.length() == 0) {
        addError("No valid value found in attribute '" + attributeName + "' nor element '" + elementName + "'", element, ancestorElementId);
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

    parseAsynchronousContinuationForActivity(taskElement, activity);

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

    parseAsynchronousContinuationForActivity(manualTaskElement, activity);

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

    parseAsynchronousContinuationForActivity(receiveTaskElement, activity);

    parseExecutionListenersOnScope(receiveTaskElement, activity);

    // please check https://app.camunda.com/jira/browse/CAM-10989
    if (receiveTaskElement.attribute("messageRef") != null) {
      activity.setScope(true);
      activity.setEventScope(activity);
      EventSubscriptionDeclaration declaration = parseMessageEventDefinition(receiveTaskElement, activity.getId());
      declaration.setActivityId(activity.getActivityId());
      declaration.setEventScopeActivityId(activity.getActivityId());
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
  protected static final String FOLLOW_UP_DATE_EXTENSION = "followUpDate";
  protected static final String PRIORITY_EXTENSION = "priority";

  /**
   * Parses a userTask declaration.
   */
  public ActivityImpl parseUserTask(Element userTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(userTaskElement, scope);

    parseAsynchronousContinuationForActivity(userTaskElement, activity);

    TaskDefinition taskDefinition = parseTaskDefinition(userTaskElement, activity.getId(), activity, (ProcessDefinitionEntity) scope.getProcessDefinition());
    TaskDecorator taskDecorator = new TaskDecorator(taskDefinition, expressionManager);

    UserTaskActivityBehavior userTaskActivity = new UserTaskActivityBehavior(taskDecorator);
    activity.setActivityBehavior(userTaskActivity);

    parseProperties(userTaskElement, activity);
    parseExecutionListenersOnScope(userTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseUserTask(userTaskElement, scope, activity);
    }
    return activity;
  }

  public TaskDefinition parseTaskDefinition(Element taskElement, String taskDefinitionKey, ActivityImpl activity, ProcessDefinitionEntity processDefinition) {
    TaskFormHandler taskFormHandler;
    String taskFormHandlerClassName = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "formHandlerClass");
    if (taskFormHandlerClassName != null) {
      taskFormHandler = (TaskFormHandler) ReflectUtil.instantiate(taskFormHandlerClassName);
    } else {
      taskFormHandler = new DefaultTaskFormHandler();
    }
    taskFormHandler.parseConfiguration(taskElement, deployment, processDefinition, this);

    TaskDefinition taskDefinition = new TaskDefinition(new DelegateTaskFormHandler(taskFormHandler, deployment));

    taskDefinition.setKey(taskDefinitionKey);
    processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);

    FormDefinition formDefinition = parseFormDefinition(taskElement);
    taskDefinition.setFormDefinition(formDefinition);

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
    parseUserTaskCustomExtensions(taskElement, activity, taskDefinition);

    return taskDefinition;
  }

  protected FormDefinition parseFormDefinition(Element flowNodeElement) {
    FormDefinition formDefinition = new FormDefinition();

    String formKeyAttribute = flowNodeElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "formKey");
    String formRefAttribute = flowNodeElement.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "formRef");

    if(formKeyAttribute != null && formRefAttribute != null) {
      addError("Invalid element definition: only one of the attributes formKey and formRef is allowed.", flowNodeElement);
    }

    if (formKeyAttribute != null) {
      formDefinition.setFormKey(expressionManager.createExpression(formKeyAttribute));
    }

    if(formRefAttribute != null) {
      formDefinition.setCamundaFormDefinitionKey(expressionManager.createExpression(formRefAttribute));

      String formRefBindingAttribute = flowNodeElement.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "formRefBinding");

      if (formRefBindingAttribute == null || !DefaultTaskFormHandler.ALLOWED_FORM_REF_BINDINGS.contains(formRefBindingAttribute)) {
        addError("Invalid element definition: value for formRefBinding attribute has to be one of "
            + DefaultTaskFormHandler.ALLOWED_FORM_REF_BINDINGS + " but was " + formRefBindingAttribute, flowNodeElement);
      }


      if(formRefBindingAttribute != null) {
        formDefinition.setCamundaFormDefinitionBinding(formRefBindingAttribute);
      }

      if(DefaultTaskFormHandler.FORM_REF_BINDING_VERSION.equals(formRefBindingAttribute)) {
        String formRefVersionAttribute = flowNodeElement.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "formRefVersion");

        Expression camundaFormDefinitionVersion = expressionManager.createExpression(formRefVersionAttribute);

        if(formRefVersionAttribute != null) {
          formDefinition.setCamundaFormDefinitionVersion(camundaFormDefinitionVersion);
        }
      }
    }

    return formDefinition;
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

  protected void parseUserTaskCustomExtensions(Element taskElement, ActivityImpl activity, TaskDefinition taskDefinition) {

    // assignee
    String assignee = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (assignee != null) {
      if (taskDefinition.getAssigneeExpression() == null) {
        taskDefinition.setAssigneeExpression(expressionManager.createExpression(assignee));
      } else {
        addError("Invalid usage: duplicate assignee declaration for task " + taskDefinition.getNameExpression(), taskElement);
      }
    }

    // Candidate users
    String candidateUsersString = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, CANDIDATE_USERS_EXTENSION);
    if (candidateUsersString != null) {
      List<String> candidateUsers = parseCommaSeparatedList(candidateUsersString);
      for (String candidateUser : candidateUsers) {
        taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser.trim()));
      }
    }

    // Candidate groups
    String candidateGroupsString = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, CANDIDATE_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      List<String> candidateGroups = parseCommaSeparatedList(candidateGroupsString);
      for (String candidateGroup : candidateGroups) {
        taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup.trim()));
      }
    }

    // Task listeners
    parseTaskListeners(taskElement, activity, taskDefinition);

    // Due date
    String dueDateExpression = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, DUE_DATE_EXTENSION);
    if (dueDateExpression != null) {
      taskDefinition.setDueDateExpression(expressionManager.createExpression(dueDateExpression));
    }

    // follow up date
    String followUpDateExpression = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, FOLLOW_UP_DATE_EXTENSION);
    if (followUpDateExpression != null) {
      taskDefinition.setFollowUpDateExpression(expressionManager.createExpression(followUpDateExpression));
    }

    // Priority
    final String priorityExpression = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PRIORITY_EXTENSION);
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
    List<String> result = new ArrayList<>();
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

  protected void parseTaskListeners(Element userTaskElement, ActivityImpl activity, TaskDefinition taskDefinition) {
    Element extentionsElement = userTaskElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> taskListenerElements = extentionsElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "taskListener");
      for (Element taskListenerElement : taskListenerElements) {
        String eventName = taskListenerElement.attribute("event");
        if (eventName != null) {
          if (TaskListener.EVENTNAME_CREATE.equals(eventName) || TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)
              || TaskListener.EVENTNAME_COMPLETE.equals(eventName) || TaskListener.EVENTNAME_UPDATE.equals(eventName)
              || TaskListener.EVENTNAME_DELETE.equals(eventName)) {
            TaskListener taskListener = parseTaskListener(taskListenerElement, activity.getId());
            taskDefinition.addTaskListener(eventName, taskListener);
          } else if (TaskListener.EVENTNAME_TIMEOUT.equals(eventName)) {
            TaskListener taskListener = parseTimeoutTaskListener(taskListenerElement, activity, taskDefinition);
            taskDefinition.addTimeoutTaskListener(taskListenerElement.attribute("id"), taskListener);
          } else {
            addError("Attribute 'event' must be one of {create|assignment|complete|update|delete|timeout}", userTaskElement);
          }
        } else {
          addError("Attribute 'event' is mandatory on taskListener", userTaskElement);
        }
      }
    }
  }

  protected TaskListener parseTaskListener(Element taskListenerElement, String taskElementId) {
    TaskListener taskListener = null;

    String className = taskListenerElement.attribute(PROPERTYNAME_CLASS);
    String expression = taskListenerElement.attribute(PROPERTYNAME_EXPRESSION);
    String delegateExpression = taskListenerElement.attribute(PROPERTYNAME_DELEGATE_EXPRESSION);
    Element scriptElement = taskListenerElement.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, "script");

    if (className != null) {
      taskListener = new ClassDelegateTaskListener(className, parseFieldDeclarations(taskListenerElement));
    } else if (expression != null) {
      taskListener = new ExpressionTaskListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      taskListener = new DelegateExpressionTaskListener(expressionManager.createExpression(delegateExpression), parseFieldDeclarations(taskListenerElement));
    } else if (scriptElement != null) {
      try {
        ExecutableScript executableScript = parseCamundaScript(scriptElement);
        if (executableScript != null) {
          taskListener = new ScriptTaskListener(executableScript);
        }
      } catch (BpmnParseException e) {
        addError(e, taskElementId);
      }
    } else {
      addError("Element 'class', 'expression', 'delegateExpression' or 'script' is mandatory on taskListener", taskListenerElement, taskElementId);
    }
    return taskListener;
  }

  protected TaskListener parseTimeoutTaskListener(Element taskListenerElement, ActivityImpl timerActivity, TaskDefinition taskDefinition) {
    String listenerId = taskListenerElement.attribute("id");
    String timerActivityId = timerActivity.getId();
    if (listenerId == null) {
      addError("Element 'id' is mandatory on taskListener of type 'timeout'", taskListenerElement, timerActivityId);
    }
    Element timerEventDefinition = taskListenerElement.element(TIMER_EVENT_DEFINITION);
    if (timerEventDefinition == null) {
      addError("Element 'timerEventDefinition' is mandatory on taskListener of type 'timeout'", taskListenerElement, timerActivityId);
    }
    timerActivity.setScope(true);
    timerActivity.setEventScope(timerActivity);
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerTaskListenerJobHandler.TYPE);
    timerDeclaration.setRawJobHandlerConfiguration(timerActivityId + TimerEventJobHandler.JOB_HANDLER_CONFIG_PROPERTY_DELIMITER +
        TimerEventJobHandler.JOB_HANDLER_CONFIG_TASK_LISTENER_PREFIX + listenerId);
    addTimerListenerDeclaration(listenerId, timerActivity, timerDeclaration);

    return parseTaskListener(taskListenerElement, timerActivityId);
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

      Element errorEventDefinition = endEventElement.element(ERROR_EVENT_DEFINITION);
      Element cancelEventDefinition = endEventElement.element(CANCEL_EVENT_DEFINITION);
      Element terminateEventDefinition = endEventElement.element("terminateEventDefinition");
      Element messageEventDefinitionElement = endEventElement.element(MESSAGE_EVENT_DEFINITION);
      Element signalEventDefinition = endEventElement.element(SIGNAL_EVENT_DEFINITION);
      Element compensateEventDefinitionElement = endEventElement.element(COMPENSATE_EVENT_DEFINITION);
      Element escalationEventDefinition = endEventElement.element(ESCALATION_EVENT_DEFINITION);

      boolean isServiceTaskLike = isServiceTaskLike(messageEventDefinitionElement);

      String activityId = activity.getId();
      if (errorEventDefinition != null) { // error end event
        String errorRef = errorEventDefinition.attribute("errorRef");

        if (errorRef == null || "".equals(errorRef)) {
          addError("'errorRef' attribute is mandatory on error end event", errorEventDefinition, activityId);
        } else {
          Error error = errors.get(errorRef);
          if (error != null && (error.getErrorCode() == null || "".equals(error.getErrorCode()))) {
            addError(
                "'errorCode' is mandatory on errors referenced by throwing error event definitions, but the error '" + error.getId() + "' does not define one.",
                errorEventDefinition,
                activityId);
          }
          activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_ERROR);
          if(error != null) {
            activity.setActivityBehavior(new ErrorEndEventActivityBehavior(error.getErrorCode(), error.getErrorMessageExpression()));
          } else {
            activity.setActivityBehavior(new ErrorEndEventActivityBehavior(errorRef, null));
          }
        }
      } else if (cancelEventDefinition != null) {
        if (scope.getProperty(BpmnProperties.TYPE.getName()) == null || !scope.getProperty(BpmnProperties.TYPE.getName()).equals("transaction")) {
          addError("end event with cancelEventDefinition only supported inside transaction subprocess", cancelEventDefinition, activityId);
        } else {
          activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_CANCEL);
          activity.setActivityBehavior(new CancelEndEventActivityBehavior());
          activity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_FLOW_SCOPE);
          activity.setProperty(PROPERTYNAME_THROWS_COMPENSATION, true);
          activity.setScope(true);
        }
      } else if (terminateEventDefinition != null) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_TERMINATE);
        activity.setActivityBehavior(new TerminateEndEventActivityBehavior());
        activity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_FLOW_SCOPE);
      } else if (messageEventDefinitionElement != null) {
        if (isServiceTaskLike) {

          // CAM-436 same behaviour as service task
          parseServiceTaskLike(
              activity,
              ActivityTypes.END_EVENT_MESSAGE,
              messageEventDefinitionElement,
              endEventElement,
              scope);
          activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_MESSAGE);
        } else {
          // default to non behavior if no service task
          // properties have been specified
          activity.setActivityBehavior(new IntermediateThrowNoneEventActivityBehavior());
        }
      } else if (signalEventDefinition != null) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_SIGNAL);
        EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(signalEventDefinition, true, activityId);
        activity.setActivityBehavior(new ThrowSignalEventActivityBehavior(signalDefinition));

      } else if (compensateEventDefinitionElement != null) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_COMPENSATION);
        CompensateEventDefinition compensateEventDefinition = parseThrowCompensateEventDefinition(compensateEventDefinitionElement, scope, endEventElement.attribute("id"));
        activity.setActivityBehavior(new CompensationEventActivityBehavior(compensateEventDefinition));
        activity.setProperty(PROPERTYNAME_THROWS_COMPENSATION, true);
        activity.setScope(true);

      } else if(escalationEventDefinition != null) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_ESCALATION);

        Escalation escalation = findEscalationForEscalationEventDefinition(escalationEventDefinition, activityId);
        if (escalation != null && escalation.getEscalationCode() == null) {
          addError("escalation end event must have an 'escalationCode'", escalationEventDefinition, activityId);
        }
        activity.setActivityBehavior(new ThrowEscalationEventActivityBehavior(escalation));

      } else { // default: none end event
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_NONE);
        activity.setActivityBehavior(new NoneEndEventActivityBehavior());
      }

      if (activity != null) {
        parseActivityInputOutput(endEventElement, activity);
      }

      parseAsynchronousContinuationForActivity(endEventElement, activity);

      parseExecutionListenersOnScope(endEventElement, activity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseEndEvent(endEventElement, scope, activity);
      }

      if (isServiceTaskLike) {
        // activity behavior could be set by a listener (e.g. connector); thus,
        // check is after listener invocation
        validateServiceTaskLike(activity,
            ActivityTypes.END_EVENT_MESSAGE,
            messageEventDefinitionElement);
      }
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
   * @param flowScope
   *          The {@link ScopeImpl} to which the activities must be added.
   */
  public void parseBoundaryEvents(Element parentElement, ScopeImpl flowScope) {
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

      LOG.parsingElement("boundary event", id);

      // Depending on the sub-element definition, the correct activityBehavior
      // parsing is selected
      Element timerEventDefinition = boundaryEventElement.element(TIMER_EVENT_DEFINITION);
      Element errorEventDefinition = boundaryEventElement.element(ERROR_EVENT_DEFINITION);
      Element signalEventDefinition = boundaryEventElement.element(SIGNAL_EVENT_DEFINITION);
      Element cancelEventDefinition = boundaryEventElement.element(CANCEL_EVENT_DEFINITION);
      Element compensateEventDefinition = boundaryEventElement.element(COMPENSATE_EVENT_DEFINITION);
      Element messageEventDefinition = boundaryEventElement.element(MESSAGE_EVENT_DEFINITION);
      Element escalationEventDefinition = boundaryEventElement.element(ESCALATION_EVENT_DEFINITION);
      Element conditionalEventDefinition = boundaryEventElement.element(CONDITIONAL_EVENT_DEFINITION);

      // create the boundary event activity
      ActivityImpl boundaryEventActivity = createActivityOnScope(boundaryEventElement, flowScope);
      parseAsynchronousContinuation(boundaryEventElement, boundaryEventActivity);

      ActivityImpl attachedActivity = flowScope.findActivityAtLevelOfSubprocess(attachedToRef);
      if (attachedActivity == null) {
        addError("Invalid reference in boundary event. Make sure that the referenced activity is defined in the same scope as the boundary event",
            boundaryEventElement);
      }

      // determine the correct event scope (the scope in which the boundary event catches events)
      if (compensateEventDefinition == null) {
        ActivityImpl multiInstanceScope = getMultiInstanceScope(attachedActivity);
        if (multiInstanceScope != null) {
          // if the boundary event is attached to a multi instance activity,
          // then the scope of the boundary event is the multi instance body.
          boundaryEventActivity.setEventScope(multiInstanceScope);
        } else {
          attachedActivity.setScope(true);
          boundaryEventActivity.setEventScope(attachedActivity);
        }
      } else {
        boundaryEventActivity.setEventScope(attachedActivity);
      }

      // except escalation, by default is assumed to abort the activity
      String cancelActivityAttr = boundaryEventElement.attribute("cancelActivity", TRUE);
      boolean isCancelActivity = Boolean.valueOf(cancelActivityAttr);

      // determine start behavior
      if (isCancelActivity) {
        boundaryEventActivity.setActivityStartBehavior(ActivityStartBehavior.CANCEL_EVENT_SCOPE);
      } else {
        boundaryEventActivity.setActivityStartBehavior(ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE);
      }

      // Catch event behavior is the same for most types
      ActivityBehavior behavior = new BoundaryEventActivityBehavior();
      if (timerEventDefinition != null) {
        parseBoundaryTimerEventDefinition(timerEventDefinition, isCancelActivity, boundaryEventActivity);

      } else if (errorEventDefinition != null) {
        parseBoundaryErrorEventDefinition(errorEventDefinition, boundaryEventActivity);

      } else if (signalEventDefinition != null) {
        parseBoundarySignalEventDefinition(signalEventDefinition, isCancelActivity, boundaryEventActivity);

      } else if (cancelEventDefinition != null) {
        behavior = parseBoundaryCancelEventDefinition(cancelEventDefinition, boundaryEventActivity);

      } else if (compensateEventDefinition != null) {
        parseBoundaryCompensateEventDefinition(compensateEventDefinition, boundaryEventActivity);

      } else if (messageEventDefinition != null) {
        parseBoundaryMessageEventDefinition(messageEventDefinition, isCancelActivity, boundaryEventActivity);

      } else if (escalationEventDefinition != null) {

        if (attachedActivity.isSubProcessScope() || attachedActivity.getActivityBehavior() instanceof CallActivityBehavior ||
            attachedActivity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
          parseBoundaryEscalationEventDefinition(escalationEventDefinition, isCancelActivity, boundaryEventActivity);
        } else {
          addError("An escalation boundary event should only be attached to a subprocess, a call activity or an user task", boundaryEventElement);
        }

      } else if (conditionalEventDefinition != null) {
        behavior = parseBoundaryConditionalEventDefinition(conditionalEventDefinition, isCancelActivity, boundaryEventActivity);
      } else {
        addError("Unsupported boundary event type", boundaryEventElement);

      }

      ensureNoIoMappingDefined(boundaryEventElement);

      boundaryEventActivity.setActivityBehavior(behavior);

      parseExecutionListenersOnScope(boundaryEventElement, boundaryEventActivity);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseBoundaryEvent(boundaryEventElement, flowScope, boundaryEventActivity);
      }

    }

  }

  public List<CamundaErrorEventDefinition> parseCamundaErrorEventDefinitions(ActivityImpl activity, Element scopeElement) {
    List<CamundaErrorEventDefinition> errorEventDefinitions = new ArrayList<>();
    Element extensionElements = scopeElement.element("extensionElements");
    if (extensionElements != null) {
      List<Element> errorEventDefinitionElements = extensionElements.elements("errorEventDefinition");
      for (Element errorEventDefinitionElement : errorEventDefinitionElements) {
        String errorRef = errorEventDefinitionElement.attribute("errorRef");
        Error error = null;
        if (errorRef != null) {
          String camundaExpression = errorEventDefinitionElement.attribute("expression");
          error = errors.get(errorRef);
          CamundaErrorEventDefinition definition = new CamundaErrorEventDefinition(activity.getId(), expressionManager.createExpression(camundaExpression));
          definition.setErrorCode(error == null ? errorRef : error.getErrorCode());
          setErrorCodeVariableOnErrorEventDefinition(errorEventDefinitionElement, definition);
          setErrorMessageVariableOnErrorEventDefinition(errorEventDefinitionElement, definition);

          errorEventDefinitions.add(definition);
        }
      }
    }
    return errorEventDefinitions;
  }

  protected ActivityImpl getMultiInstanceScope(ActivityImpl activity) {
    if (activity.isMultiInstance()) {
      return activity.getParentFlowScopeActivity();
    } else {
      return null;
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
   * @param boundaryActivity
   *          The activity which maps to the structure of the timer event on the
   *          boundary of another activity. Note that this is NOT the activity
   *          onto which the boundary event is attached, but a nested activity
   *          inside this activity, specifically created for this event.
   */
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl boundaryActivity) {
    boundaryActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_TIMER);
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, boundaryActivity, TimerExecuteNestedActivityJobHandler.TYPE);

    // ACT-1427
    if (interrupting) {
      timerDeclaration.setInterruptingTimer(true);

      Element timeCycleElement = timerEventDefinition.element("timeCycle");
      if (timeCycleElement != null) {
        addTimeCycleWarning(timeCycleElement, "cancelling boundary", boundaryActivity.getId());
      }
    }

    addTimerDeclaration(boundaryActivity.getEventScope(), timerDeclaration);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, boundaryActivity);
    }
  }

  public void parseBoundarySignalEventDefinition(Element element, boolean interrupting, ActivityImpl signalActivity) {
    signalActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_SIGNAL);

    EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(element, false, signalActivity.getId());
    if (signalActivity.getId() == null) {
      addError("boundary event has no id", element);
    }
    signalDefinition.setActivityId(signalActivity.getId());
    addEventSubscriptionDeclaration(signalDefinition, signalActivity.getEventScope(), element);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundarySignalEventDefinition(element, interrupting, signalActivity);
    }

  }

  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
    messageActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_MESSAGE);

    EventSubscriptionDeclaration messageEventDefinition = parseMessageEventDefinition(element, messageActivity.getId());
    if (messageActivity.getId() == null) {
      addError("boundary event has no id", element);
    }
    messageEventDefinition.setActivityId(messageActivity.getId());
    addEventSubscriptionDeclaration(messageEventDefinition, messageActivity.getEventScope(), element);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryMessageEventDefinition(element, interrupting, messageActivity);
    }

  }

  @SuppressWarnings("unchecked")
  protected void parseTimerStartEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity, ProcessDefinitionEntity processDefinition) {
    timerActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_TIMER);
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);
    timerDeclaration.setRawJobHandlerConfiguration(processDefinition.getKey());

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<>();
      processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);

  }

  protected void parseTimerStartEventDefinitionForEventSubprocess(Element timerEventDefinition, ActivityImpl timerActivity, boolean interrupting) {
    timerActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_TIMER);

    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerStartEventSubprocessJobHandler.TYPE);

    timerDeclaration.setActivity(timerActivity);
    timerDeclaration.setEventScopeActivityId(timerActivity.getEventScope().getId());
    timerDeclaration.setRawJobHandlerConfiguration(timerActivity.getFlowScope().getId());
    timerDeclaration.setInterruptingTimer(interrupting);

    if (interrupting) {
      Element timeCycleElement = timerEventDefinition.element("timeCycle");
      if (timeCycleElement != null) {
        addTimeCycleWarning(timeCycleElement, "interrupting start", timerActivity.getId());
      }

    }

    addTimerDeclaration(timerActivity.getEventScope(), timerDeclaration);
  }

  protected void parseEventDefinitionForSubprocess(EventSubscriptionDeclaration subscriptionDeclaration, ActivityImpl activity, Element element) {
    subscriptionDeclaration.setActivityId(activity.getId());
    subscriptionDeclaration.setEventScopeActivityId(activity.getEventScope().getId());
    subscriptionDeclaration.setStartEvent(false);
    addEventSubscriptionDeclaration(subscriptionDeclaration, activity.getEventScope(), element);
  }

  protected void parseIntermediateSignalEventDefinition(Element element, ActivityImpl signalActivity) {
    signalActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_SIGNAL);

    parseSignalCatchEventDefinition(element, signalActivity, false);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateSignalCatchEventDefinition(element, signalActivity);
    }
  }

  protected void parseSignalCatchEventDefinition(Element element, ActivityImpl signalActivity, boolean isStartEvent) {
    EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(element, false, signalActivity.getId());
    signalDefinition.setActivityId(signalActivity.getId());
    signalDefinition.setStartEvent(isStartEvent);
    addEventSubscriptionDeclaration(signalDefinition, signalActivity.getEventScope(), element);

    EventSubscriptionJobDeclaration catchingAsyncDeclaration = new EventSubscriptionJobDeclaration(signalDefinition);
    catchingAsyncDeclaration.setJobPriorityProvider((ParameterValueProvider) signalActivity.getProperty(PROPERTYNAME_JOB_PRIORITY));
    catchingAsyncDeclaration.setActivity(signalActivity);
    signalDefinition.setJobDeclaration(catchingAsyncDeclaration);
    addEventSubscriptionJobDeclaration(catchingAsyncDeclaration, signalActivity, element);
  }

  /**
   * Parses the Signal Event Definition XML including payload definition.
   *
   * @param signalEventDefinitionElement the Signal Event Definition element
   * @param isThrowing true if a Throwing signal event is being parsed
   * @return
   */
  protected EventSubscriptionDeclaration parseSignalEventDefinition(Element signalEventDefinitionElement, boolean isThrowing, String signalElementId) {
    String signalRef = signalEventDefinitionElement.attribute("signalRef");
    if (signalRef == null) {
      addError("signalEventDefinition does not have required property 'signalRef'", signalEventDefinitionElement, signalElementId);
      return null;
    } else {
      SignalDefinition signalDefinition = signals.get(resolveName(signalRef));
      if (signalDefinition == null) {
        addError("Could not find signal with id '" + signalRef + "'", signalEventDefinitionElement, signalElementId);
      }

      EventSubscriptionDeclaration signalEventDefinition;
      if (isThrowing) {
        CallableElement payload = new CallableElement();
        parseInputParameter(signalEventDefinitionElement, payload);
        signalEventDefinition = new EventSubscriptionDeclaration(signalDefinition.getExpression(), EventType.SIGNAL, payload);
      } else {
        signalEventDefinition = new EventSubscriptionDeclaration(signalDefinition.getExpression(), EventType.SIGNAL);
      }

      boolean throwingAsync = TRUE.equals(signalEventDefinitionElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "async", "false"));
      signalEventDefinition.setAsync(throwingAsync);

      return signalEventDefinition;
    }
  }

  protected void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
    timerActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_TIMER);
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);

    Element timeCycleElement = timerEventDefinition.element("timeCycle");
    if (timeCycleElement != null) {
      addTimeCycleWarning(timeCycleElement, "intermediate catch", timerActivity.getId());
    }

    addTimerDeclaration(timerActivity.getEventScope(), timerDeclaration);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateTimerEventDefinition(timerEventDefinition, timerActivity);
    }
  }

  protected TimerDeclarationImpl parseTimer(Element timerEventDefinition, ActivityImpl timerActivity, String jobHandlerType) {
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
    if (expression == null) {
      addError("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed).", timerEventDefinition, timerActivity.getId());
    }

    // Parse the timer declaration
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(expression, type, jobHandlerType);
    timerDeclaration.setRawJobHandlerConfiguration(timerActivity.getId());
    timerDeclaration.setExclusive(TRUE.equals(timerEventDefinition.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "exclusive", String.valueOf(JobEntity.DEFAULT_EXCLUSIVE))));
    if (timerActivity.getId() == null) {
      addError("Attribute \"id\" is required!", timerEventDefinition);
    }
    timerDeclaration.setActivity(timerActivity);
    timerDeclaration.setJobConfiguration(type.toString() + ": " + expression.getExpressionText());
    addJobDeclarationToProcessDefinition(timerDeclaration, (ProcessDefinition) timerActivity.getProcessDefinition());

    timerDeclaration.setJobPriorityProvider((ParameterValueProvider) timerActivity.getProperty(PROPERTYNAME_JOB_PRIORITY));

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

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, ActivityImpl boundaryEventActivity) {

    boundaryEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_ERROR);

    String errorRef = errorEventDefinition.attribute("errorRef");
    Error error = null;
    ErrorEventDefinition definition = new ErrorEventDefinition(boundaryEventActivity.getId());
    if (errorRef != null) {
      error = errors.get(errorRef);
      definition.setErrorCode(error == null ? errorRef : error.getErrorCode());
    }
    setErrorCodeVariableOnErrorEventDefinition(errorEventDefinition, definition);
    setErrorMessageVariableOnErrorEventDefinition(errorEventDefinition, definition);

    addErrorEventDefinition(definition, boundaryEventActivity.getEventScope());

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryErrorEventDefinition(errorEventDefinition, true, (ActivityImpl) boundaryEventActivity.getEventScope(), boundaryEventActivity);
    }
  }

  protected void addErrorEventDefinition(ErrorEventDefinition errorEventDefinition, ScopeImpl catchingScope) {
    catchingScope.getProperties().addListItem(BpmnProperties.ERROR_EVENT_DEFINITIONS, errorEventDefinition);

    List<ErrorEventDefinition> errorEventDefinitions = catchingScope.getProperties().get(BpmnProperties.ERROR_EVENT_DEFINITIONS);
    Collections.sort(errorEventDefinitions, ErrorEventDefinition.comparator);
  }

  protected void parseBoundaryEscalationEventDefinition(Element escalationEventDefinitionElement, boolean cancelActivity, ActivityImpl boundaryEventActivity) {
    boundaryEventActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_ESCALATION);

    EscalationEventDefinition escalationEventDefinition = createEscalationEventDefinitionForEscalationHandler(escalationEventDefinitionElement, boundaryEventActivity, cancelActivity, boundaryEventActivity.getId());
    addEscalationEventDefinition(boundaryEventActivity.getEventScope(), escalationEventDefinition, escalationEventDefinitionElement, boundaryEventActivity.getId());

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryEscalationEventDefinition(escalationEventDefinitionElement, cancelActivity, boundaryEventActivity);
    }
  }

  /**
   * Find the referenced escalation of the given escalation event definition.
   * Add errors if the referenced escalation not found.
   *
   * @return referenced escalation or <code>null</code>, if referenced escalation not found
   */
  protected Escalation findEscalationForEscalationEventDefinition(Element escalationEventDefinition, String escalationElementId) {
    String escalationRef = escalationEventDefinition.attribute("escalationRef");
    if (escalationRef == null) {
      addError("escalationEventDefinition does not have required attribute 'escalationRef'", escalationEventDefinition, escalationElementId);
    } else if (!escalations.containsKey(escalationRef)) {
      addError("could not find escalation with id '" + escalationRef + "'", escalationEventDefinition, escalationElementId);
    } else {
      return escalations.get(escalationRef);
    }
    return null;
  }

  protected EscalationEventDefinition createEscalationEventDefinitionForEscalationHandler(Element escalationEventDefinitionElement, ActivityImpl escalationHandler, boolean cancelActivity, String parentElementId) {
    EscalationEventDefinition escalationEventDefinition = new EscalationEventDefinition(escalationHandler, cancelActivity);

    String escalationRef = escalationEventDefinitionElement.attribute("escalationRef");
    if (escalationRef != null) {
      if (!escalations.containsKey(escalationRef)) {
        addError("could not find escalation with id '" + escalationRef + "'", escalationEventDefinitionElement, parentElementId);
      } else {
        Escalation escalation = escalations.get(escalationRef);
        escalationEventDefinition.setEscalationCode(escalation.getEscalationCode());
      }
    }

    String escalationCodeVariable = escalationEventDefinitionElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "escalationCodeVariable");
    if(escalationCodeVariable != null) {
      escalationEventDefinition.setEscalationCodeVariable(escalationCodeVariable);
    }

    return escalationEventDefinition;
  }

  protected void addEscalationEventDefinition(ScopeImpl catchingScope, EscalationEventDefinition escalationEventDefinition, Element element, String escalationElementId) {
    // ensure there is only one escalation handler (e.g. escalation boundary event, escalation event subprocess) what can catch the escalation event
    for (EscalationEventDefinition existingEscalationEventDefinition : catchingScope.getProperties().get(BpmnProperties.ESCALATION_EVENT_DEFINITIONS)) {

      if (existingEscalationEventDefinition.getEscalationHandler().isSubProcessScope()
          && escalationEventDefinition.getEscalationHandler().isSubProcessScope()) {

        if (existingEscalationEventDefinition.getEscalationCode() == null && escalationEventDefinition.getEscalationCode() == null) {
          addError("The same scope can not contains more than one escalation event subprocess without escalation code. "
              + "An escalation event subprocess without escalation code catch all escalation events.", element, escalationElementId);
        } else if (existingEscalationEventDefinition.getEscalationCode() == null || escalationEventDefinition.getEscalationCode() == null) {
          addError("The same scope can not contains an escalation event subprocess without escalation code and another one with escalation code. "
              + "The escalation event subprocess without escalation code catch all escalation events.", element, escalationElementId);
        } else if (existingEscalationEventDefinition.getEscalationCode().equals(escalationEventDefinition.getEscalationCode())) {
          addError("multiple escalation event subprocesses with the same escalationCode '" + escalationEventDefinition.getEscalationCode()
              + "' are not supported on same scope", element, escalationElementId);
        }
      } else if (!existingEscalationEventDefinition.getEscalationHandler().isSubProcessScope()
          && !escalationEventDefinition.getEscalationHandler().isSubProcessScope()) {

        if (existingEscalationEventDefinition.getEscalationCode() == null && escalationEventDefinition.getEscalationCode() == null) {
          addError("The same scope can not contains more than one escalation boundary event without escalation code. "
              + "An escalation boundary event without escalation code catch all escalation events.", element, escalationElementId);
        } else if (existingEscalationEventDefinition.getEscalationCode() == null || escalationEventDefinition.getEscalationCode() == null) {
          addError("The same scope can not contains an escalation boundary event without escalation code and another one with escalation code. "
              + "The escalation boundary event without escalation code catch all escalation events.", element, escalationElementId);
        } else if (existingEscalationEventDefinition.getEscalationCode().equals(escalationEventDefinition.getEscalationCode())) {
          addError("multiple escalation boundary events with the same escalationCode '" + escalationEventDefinition.getEscalationCode()
              + "' are not supported on same scope", element, escalationElementId);
        }
      }
    }

    catchingScope.getProperties().addListItem(BpmnProperties.ESCALATION_EVENT_DEFINITIONS, escalationEventDefinition);
  }

  protected void addTimerDeclaration(ScopeImpl scope, TimerDeclarationImpl timerDeclaration) {
    scope.getProperties().putMapEntry(BpmnProperties.TIMER_DECLARATIONS, timerDeclaration.getActivityId(), timerDeclaration);
  }

  protected void addTimerListenerDeclaration(String listenerId, ScopeImpl scope, TimerDeclarationImpl timerDeclaration) {
    if (scope.getProperties().get(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS) != null && scope.getProperties().get(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS).get(timerDeclaration.getActivityId()) != null) {
      scope.getProperties().get(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS).get(timerDeclaration.getActivityId()).put(listenerId, timerDeclaration);
    } else {
      Map<String, TimerDeclarationImpl> activityDeclarations = new HashMap<>();
      activityDeclarations.put(listenerId, timerDeclaration);
      scope.getProperties().putMapEntry(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS, timerDeclaration.getActivityId(), activityDeclarations);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addVariableDeclaration(ScopeImpl scope, VariableDeclaration variableDeclaration) {
    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations == null) {
      variableDeclarations = new ArrayList<>();
      scope.setProperty(PROPERTYNAME_VARIABLE_DECLARATIONS, variableDeclarations);
    }
    variableDeclarations.add(variableDeclaration);
  }

  /**
   * Parses the given element as conditional boundary event.
   *
   * @param element the XML element which contains the conditional event information
   * @param interrupting indicates if the event is interrupting or not
   * @param conditionalActivity the conditional event activity
   * @return the boundary conditional event behavior which contains the condition
   */
  public BoundaryConditionalEventActivityBehavior parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
    conditionalActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.BOUNDARY_CONDITIONAL);

    ConditionalEventDefinition conditionalEventDefinition = parseConditionalEventDefinition(element, conditionalActivity);
    conditionalEventDefinition.setInterrupting(interrupting);
    addEventSubscriptionDeclaration(conditionalEventDefinition, conditionalActivity.getEventScope(), element);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryConditionalEventDefinition(element, interrupting, conditionalActivity);
    }

    return new BoundaryConditionalEventActivityBehavior(conditionalEventDefinition);
  }

  /**
   * Parses the given element as intermediate conditional event.
   *
   * @param element the XML element which contains the conditional event information
   * @param conditionalActivity the conditional event activity
   * @return returns the conditional activity with the parsed information
   */
  public ConditionalEventDefinition parseIntermediateConditionalEventDefinition(Element element, ActivityImpl conditionalActivity) {
    conditionalActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.INTERMEDIATE_EVENT_CONDITIONAL);

    ConditionalEventDefinition conditionalEventDefinition = parseConditionalEventDefinition(element, conditionalActivity);
    addEventSubscriptionDeclaration(conditionalEventDefinition, conditionalActivity.getEventScope(), element);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateConditionalEventDefinition(element, conditionalActivity);
    }

    return conditionalEventDefinition;
  }

  /**
   * Parses the given element as conditional start event of an event subprocess.
   *
   * @param element the XML element which contains the conditional event information
   * @param interrupting indicates if the event is interrupting or not
   * @param conditionalActivity the conditional event activity
   * @return
   */
  public ConditionalEventDefinition parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
    conditionalActivity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.START_EVENT_CONDITIONAL);

    ConditionalEventDefinition conditionalEventDefinition = parseConditionalEventDefinition(element, conditionalActivity);
    conditionalEventDefinition.setInterrupting(interrupting);
    addEventSubscriptionDeclaration(conditionalEventDefinition, conditionalActivity.getEventScope(), element);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseConditionalStartEventForEventSubprocess(element, conditionalActivity, interrupting);
    }

    return conditionalEventDefinition;
  }

  /**
   * Parses the given element and returns an ConditionalEventDefinition object.
   *
   * @param element the XML element which contains the conditional event information
   * @param conditionalActivity the conditional event activity
   * @return the conditional event definition which was parsed
   */
  protected ConditionalEventDefinition parseConditionalEventDefinition(Element element, ActivityImpl conditionalActivity) {
    ConditionalEventDefinition conditionalEventDefinition = null;

    Element conditionExprElement = element.element(CONDITION);
    String conditionalActivityId = conditionalActivity.getId();
    if (conditionExprElement != null) {
      Condition condition = parseConditionExpression(conditionExprElement, conditionalActivityId);
      conditionalEventDefinition = new ConditionalEventDefinition(condition, conditionalActivity);

      String expression = conditionExprElement.getText().trim();
      conditionalEventDefinition.setConditionAsString(expression);

      conditionalActivity.getProcessDefinition().getProperties().set(BpmnProperties.HAS_CONDITIONAL_EVENTS, true);

      final String variableName = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "variableName");
      conditionalEventDefinition.setVariableName(variableName);

      final String variableEvents = element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "variableEvents");
      final List<String> variableEventsList = parseCommaSeparatedList(variableEvents);
      conditionalEventDefinition.setVariableEvents(new HashSet<>(variableEventsList));

      for (String variableEvent : variableEventsList) {
        if (!VARIABLE_EVENTS.contains(variableEvent)) {
          addWarning("Variable event: " + variableEvent + " is not valid. Possible variable change events are: " + Arrays.toString(VARIABLE_EVENTS.toArray()),
              element, conditionalActivityId);
        }
      }

    } else {
      addError("Conditional event must contain an expression for evaluation.", element, conditionalActivityId);
    }

    return conditionalEventDefinition;
  }

  /**
   * Parses a subprocess (formally known as an embedded subprocess): a
   * subprocess defined within another process definition.
   *
   * @param subProcessElement
   *          The XML element corresponding with the subprocess definition
   * @param scope
   *          The current scope on which the subprocess is defined.
   */
  public ActivityImpl parseSubProcess(Element subProcessElement, ScopeImpl scope) {
    ActivityImpl subProcessActivity = createActivityOnScope(subProcessElement, scope);
    subProcessActivity.setSubProcessScope(true);

    parseAsynchronousContinuationForActivity(subProcessElement, subProcessActivity);

    Boolean isTriggeredByEvent = parseBooleanAttribute(subProcessElement.attribute("triggeredByEvent"), false);
    subProcessActivity.getProperties().set(BpmnProperties.TRIGGERED_BY_EVENT, isTriggeredByEvent);
    subProcessActivity.setProperty(PROPERTYNAME_CONSUMES_COMPENSATION, !isTriggeredByEvent);

    subProcessActivity.setScope(true);
    if (isTriggeredByEvent) {
      subProcessActivity.setActivityBehavior(new EventSubProcessActivityBehavior());
      subProcessActivity.setEventScope(scope);
    } else {
      subProcessActivity.setActivityBehavior(new SubProcessActivityBehavior());
    }
    parseScope(subProcessElement, subProcessActivity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSubProcess(subProcessElement, scope, subProcessActivity);
    }
    return subProcessActivity;
  }

  protected ActivityImpl parseTransaction(Element transactionElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(transactionElement, scope);

    parseAsynchronousContinuationForActivity(transactionElement, activity);

    activity.setScope(true);
    activity.setSubProcessScope(true);
    activity.setActivityBehavior(new SubProcessActivityBehavior());
    activity.getProperties().set(BpmnProperties.TRIGGERED_BY_EVENT, false);
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
  public ActivityImpl parseCallActivity(Element callActivityElement, ScopeImpl scope, boolean isMultiInstance) {
    ActivityImpl activity = createActivityOnScope(callActivityElement, scope);

    // parse async
    parseAsynchronousContinuationForActivity(callActivityElement, activity);

    // parse definition key (and behavior)
    String calledElement = callActivityElement.attribute("calledElement");
    String caseRef = callActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "caseRef");
    String className = callActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_VARIABLE_MAPPING_CLASS);
    String delegateExpression = callActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_VARIABLE_MAPPING_DELEGATE_EXPRESSION);

    if (calledElement == null && caseRef == null) {
      addError("Missing attribute 'calledElement' or 'caseRef'", callActivityElement);
    } else if (calledElement != null && caseRef != null) {
      addError("The attributes 'calledElement' or 'caseRef' cannot be used together: Use either 'calledElement' or 'caseRef'", callActivityElement);
    }

    String bindingAttributeName = "calledElementBinding";
    String versionAttributeName = "calledElementVersion";
    String versionTagAttributeName = "calledElementVersionTag";
    String tenantIdAttributeName = "calledElementTenantId";

    String deploymentId = deployment.getId();

    CallableElement callableElement = new CallableElement();
    callableElement.setDeploymentId(deploymentId);

    CallableElementActivityBehavior behavior = null;

    if (calledElement != null) {
      if (className != null) {
          behavior = new CallActivityBehavior(className);
      } else if (delegateExpression != null) {
         Expression exp = expressionManager.createExpression(delegateExpression);
         behavior = new CallActivityBehavior(exp);
      } else {
        behavior = new CallActivityBehavior();
      }
      ParameterValueProvider definitionKeyProvider = createParameterValueProvider(calledElement, expressionManager);
      callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);

    } else {
      behavior = new CaseCallActivityBehavior();
      ParameterValueProvider definitionKeyProvider = createParameterValueProvider(caseRef, expressionManager);
      callableElement.setDefinitionKeyValueProvider(definitionKeyProvider);
      bindingAttributeName = "caseBinding";
      versionAttributeName = "caseVersion";
      tenantIdAttributeName = "caseTenantId";
    }

    behavior.setCallableElement(callableElement);

    // parse binding
    parseBinding(callActivityElement, activity, callableElement, bindingAttributeName);

    // parse version
    parseVersion(callActivityElement, activity, callableElement, bindingAttributeName, versionAttributeName);

    // parse versionTag
    parseVersionTag(callActivityElement, activity, callableElement, bindingAttributeName, versionTagAttributeName);

    // parse tenant id
    parseTenantId(callActivityElement, activity, callableElement, tenantIdAttributeName);

    // parse input parameter
    parseInputParameter(callActivityElement, callableElement);

    // parse output parameter
    parseOutputParameter(callActivityElement, activity, callableElement);

    if (!isMultiInstance) {
      // turn activity into a scope unless it is a multi instance activity, in
      // that case this
      // is not necessary because there is already the multi instance body scope
      // and concurrent
      // child executions are sufficient
      activity.setScope(true);
    }
    activity.setActivityBehavior(behavior);

    parseExecutionListenersOnScope(callActivityElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseCallActivity(callActivityElement, scope, activity);
    }
    return activity;
  }

  protected void parseBinding(Element callActivityElement, ActivityImpl activity, BaseCallableElement callableElement, String bindingAttributeName) {
    String binding = callActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, bindingAttributeName);

    if (CallableElementBinding.DEPLOYMENT.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.DEPLOYMENT);
    } else if (CallableElementBinding.LATEST.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.LATEST);
    } else if (CallableElementBinding.VERSION.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.VERSION);
    } else if (CallableElementBinding.VERSION_TAG.getValue().equals(binding)) {
      callableElement.setBinding(CallableElementBinding.VERSION_TAG);
    }
  }

  protected void parseTenantId(Element callingActivityElement, ActivityImpl activity, BaseCallableElement callableElement, String attrName) {
    ParameterValueProvider tenantIdValueProvider = null;

    String tenantId = callingActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, attrName);
    if (tenantId != null && tenantId.length() > 0) {
      tenantIdValueProvider = createParameterValueProvider(tenantId, expressionManager);
    }

    callableElement.setTenantIdProvider(tenantIdValueProvider);
  }

  protected void parseVersion(Element callingActivityElement, ActivityImpl activity, BaseCallableElement callableElement, String bindingAttributeName, String versionAttributeName) {
    String version = null;

    CallableElementBinding binding = callableElement.getBinding();
    version = callingActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, versionAttributeName);

    if (binding != null && binding.equals(CallableElementBinding.VERSION) && version == null) {
      addError("Missing attribute '" + versionAttributeName + "' when '" + bindingAttributeName + "' has value '" + CallableElementBinding.VERSION.getValue()
        + "'", callingActivityElement);
    }

    ParameterValueProvider versionProvider = createParameterValueProvider(version, expressionManager);
    callableElement.setVersionValueProvider(versionProvider);
  }

  protected void parseVersionTag(Element callingActivityElement, ActivityImpl activity, BaseCallableElement callableElement, String bindingAttributeName, String versionTagAttributeName) {
    String versionTag = null;

    CallableElementBinding binding = callableElement.getBinding();
    versionTag = callingActivityElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, versionTagAttributeName);

    if (binding != null && binding.equals(CallableElementBinding.VERSION_TAG) && versionTag == null) {
      addError("Missing attribute '" + versionTagAttributeName + "' when '" + bindingAttributeName + "' has value '" + CallableElementBinding.VERSION_TAG.getValue()
        + "'", callingActivityElement);
    }

    ParameterValueProvider versionTagProvider = createParameterValueProvider(versionTag, expressionManager);
    callableElement.setVersionTagValueProvider(versionTagProvider);
  }

  protected void parseInputParameter(Element elementWithParameters, CallableElement callableElement) {
    Element extensionsElement = elementWithParameters.element("extensionElements");

    if (extensionsElement != null) {
      // input data elements
      for (Element inElement : extensionsElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "in")) {

        String businessKey = inElement.attribute("businessKey");

        if (businessKey != null && !businessKey.isEmpty()) {
          ParameterValueProvider businessKeyValueProvider = createParameterValueProvider(businessKey, expressionManager);
          callableElement.setBusinessKeyValueProvider(businessKeyValueProvider);

        } else {

          CallableElementParameter parameter = parseCallableElementProvider(inElement, elementWithParameters.attribute("id"));

          if (attributeValueEquals(inElement, "local", TRUE)) {
            parameter.setReadLocal(true);
          }

          callableElement.addInput(parameter);
        }
      }
    }
  }

  protected void parseOutputParameter(Element callActivityElement, ActivityImpl activity, CallableElement callableElement) {
    Element extensionsElement = callActivityElement.element("extensionElements");

    if (extensionsElement != null) {
      // output data elements
      for (Element outElement : extensionsElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "out")) {

        CallableElementParameter parameter = parseCallableElementProvider(outElement, callActivityElement.attribute("id"));

        if (attributeValueEquals(outElement, "local", TRUE)) {
          callableElement.addOutputLocal(parameter);
        }
        else {
          callableElement.addOutput(parameter);
        }

      }
    }
  }

  protected boolean attributeValueEquals(Element element, String attribute, String comparisonValue) {
    String value = element.attribute(attribute);

    return comparisonValue.equals(value);
  }

  protected CallableElementParameter parseCallableElementProvider(Element parameterElement, String ancestorElementId) {
    CallableElementParameter parameter = new CallableElementParameter();

    String variables = parameterElement.attribute("variables");

    if (ALL.equals(variables)) {
      parameter.setAllVariables(true);
    } else {
      boolean strictValidation = !Context.getProcessEngineConfiguration().getDisableStrictCallActivityValidation();

      ParameterValueProvider sourceValueProvider = new NullValueProvider();

      String source = parameterElement.attribute("source");
      if (source != null) {
        if (!source.isEmpty()) {
          sourceValueProvider = new ConstantValueProvider(source);
        }
        else {
          if (strictValidation) {
            addError("Empty attribute 'source' when passing variables", parameterElement, ancestorElementId);
          }
          else {
            source = null;
          }
        }
      }

      if (source == null) {
        source = parameterElement.attribute("sourceExpression");

        if (source != null) {
          if (!source.isEmpty()) {
            Expression expression = expressionManager.createExpression(source);
            sourceValueProvider = new ElValueProvider(expression);
          }
          else if (strictValidation) {
            addError("Empty attribute 'sourceExpression' when passing variables", parameterElement, ancestorElementId);
          }
        }
      }

      if (strictValidation && source == null) {
        addError("Missing parameter 'source' or 'sourceExpression' when passing variables", parameterElement, ancestorElementId);
      }

      parameter.setSourceValueProvider(sourceValueProvider);

      String target = parameterElement.attribute("target");
      if ((strictValidation || source != null && !source.isEmpty()) && target == null) {
        addError("Missing attribute 'target' when attribute 'source' or 'sourceExpression' is set", parameterElement, ancestorElementId);
      }
      else if (strictValidation && target != null && target.isEmpty()) {
        addError("Empty attribute 'target' when attribute 'source' or 'sourceExpression' is set", parameterElement, ancestorElementId);
      }
      parameter.setTarget(target);
    }

    return parameter;
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
        addError("Invalid property usage on line " + propertyElement.getLine() + ": no id or name specified.", propertyElement, activity.getId());
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
      String type = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE);
      propertyType = type != null ? type : "string"; // default is string
    }

    VariableDeclaration variableDeclaration = new VariableDeclaration(propertyName, propertyType);
    addVariableDeclaration(activity, variableDeclaration);
    activity.setScope(true);

    String src = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "src");
    if (src != null) {
      variableDeclaration.setSourceVariableName(src);
    }

    String srcExpr = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "srcExpr");
    if (srcExpr != null) {
      Expression sourceExpression = expressionManager.createExpression(srcExpr);
      variableDeclaration.setSourceExpression(sourceExpression);
    }

    String dst = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "dst");
    if (dst != null) {
      variableDeclaration.setDestinationVariableName(dst);
    }

    String destExpr = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "dstExpr");
    if (destExpr != null) {
      Expression destinationExpression = expressionManager.createExpression(destExpr);
      variableDeclaration.setDestinationExpression(destinationExpression);
    }

    String link = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "link");
    if (link != null) {
      variableDeclaration.setLink(link);
    }

    String linkExpr = propertyElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "linkExpr");
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
   * @param compensationHandlers
   */
  public void parseSequenceFlow(Element processElement, ScopeImpl scope, Map<String, Element> compensationHandlers) {
    for (Element sequenceFlowElement : processElement.elements("sequenceFlow")) {

      String id = sequenceFlowElement.attribute("id");
      String sourceRef = sequenceFlowElement.attribute("sourceRef");
      String destinationRef = sequenceFlowElement.attribute("targetRef");

      // check if destination is a throwing link event (event source) which mean
      // we have
      // to target the catching link event (event target) here:
      if (eventLinkSources.containsKey(destinationRef)) {
        String linkName = eventLinkSources.get(destinationRef);
        destinationRef = eventLinkTargets.get(linkName);
        if (destinationRef == null) {
          addError("sequence flow points to link event source with name '" + linkName
              + "' but no event target with that name exists. Most probably your link events are not configured correctly.", sequenceFlowElement);
          // we cannot do anything useful now
          return;
        }
        // Reminder: Maybe we should log a warning if we use intermediate link
        // events which are not used?
        // e.g. we have a catching event without the corresponding throwing one.
        // not done for the moment as it does not break executability
      }

      // Implicit check: sequence flow cannot cross (sub) process boundaries: we
      // don't do a processDefinition.findActivity here
      ActivityImpl sourceActivity = scope.findActivityAtLevelOfSubprocess(sourceRef);
      ActivityImpl destinationActivity = scope.findActivityAtLevelOfSubprocess(destinationRef);

      if ((sourceActivity == null && compensationHandlers.containsKey(sourceRef))
          || (sourceActivity != null && sourceActivity.isCompensationHandler())) {
        addError("Invalid outgoing sequence flow of compensation activity '" + sourceRef
            + "'. A compensation activity should not have an incoming or outgoing sequence flow.",
            sequenceFlowElement,
            sourceRef,
            id);
      } else if ((destinationActivity == null && compensationHandlers.containsKey(destinationRef))
          || (destinationActivity != null && destinationActivity.isCompensationHandler())) {
        addError("Invalid incoming sequence flow of compensation activity '" + destinationRef
            + "'. A compensation activity should not have an incoming or outgoing sequence flow.",
            sequenceFlowElement,
            destinationRef,
            id);
      } else if (sourceActivity == null) {
        addError("Invalid source '" + sourceRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      } else if (destinationActivity == null) {
        addError("Invalid destination '" + destinationRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      } else if (sourceActivity.getActivityBehavior() instanceof EventBasedGatewayActivityBehavior) {
        // ignore
      } else if (destinationActivity.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior && (destinationActivity.getEventScope() != null)
          && (destinationActivity.getEventScope().getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)) {
        addError("Invalid incoming sequenceflow for intermediateCatchEvent with id '" + destinationActivity.getId() + "' connected to an event-based gateway.",
            sequenceFlowElement);
      } else if (sourceActivity.getActivityBehavior() instanceof SubProcessActivityBehavior
          && sourceActivity.isTriggeredByEvent()) {
        addError("Invalid outgoing sequence flow of event subprocess", sequenceFlowElement);
      } else if (destinationActivity.getActivityBehavior() instanceof SubProcessActivityBehavior
          && destinationActivity.isTriggeredByEvent()) {
        addError("Invalid incoming sequence flow of event subprocess", sequenceFlowElement);
      }
      else {

        if(getMultiInstanceScope(sourceActivity) != null) {
          sourceActivity = getMultiInstanceScope(sourceActivity);
        }
        if(getMultiInstanceScope(destinationActivity) != null) {
          destinationActivity = getMultiInstanceScope(destinationActivity);
        }

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
    Element conditionExprElement = seqFlowElement.element(CONDITION_EXPRESSION);
    if (conditionExprElement != null) {
      Condition condition = parseConditionExpression(conditionExprElement, seqFlow.getId());
      seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT, conditionExprElement.getText().trim());
      seqFlow.setProperty(PROPERTYNAME_CONDITION, condition);
    }
  }

  protected Condition parseConditionExpression(Element conditionExprElement, String ancestorElementId) {
    String expression = conditionExprElement.getText().trim();
    String type = conditionExprElement.attributeNS(XSI_NS, TYPE);
    String language = conditionExprElement.attribute(PROPERTYNAME_LANGUAGE);
    String resource = conditionExprElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_RESOURCE);
    if (type != null) {
      String value = type.contains(":") ? resolveName(type) : BpmnParser.BPMN20_NS + ":" + type;
      if (!value.equals(ATTRIBUTEVALUE_T_FORMAL_EXPRESSION)) {
        addError("Invalid type, only tFormalExpression is currently supported", conditionExprElement, ancestorElementId);
      }
    }
    Condition condition = null;
    if (language == null) {
      condition = new UelExpressionCondition(expressionManager.createExpression(expression));
    } else {
      try {
        ExecutableScript script = ScriptUtil.getScript(language, expression, resource, expressionManager);
        condition = new ScriptCondition(script);
      } catch (ProcessEngineException e) {
        addError("Unable to process condition expression:" + e.getMessage(), conditionExprElement, ancestorElementId);
      }
    }
    return condition;
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
    String scopeElementId = scopeElement.attribute("id");
    if (extentionsElement != null) {
      List<Element> listenerElements = extentionsElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        String eventName = listenerElement.attribute("event");
        if (isValidEventNameForScope(eventName, listenerElement, scopeElementId)) {
          ExecutionListener listener = parseExecutionListener(listenerElement, scopeElementId);
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
  protected boolean isValidEventNameForScope(String eventName, Element listenerElement, String ancestorElementId) {
    if (eventName != null && eventName.trim().length() > 0) {
      if ("start".equals(eventName) || "end".equals(eventName)) {
        return true;
      } else {
        addError("Attribute 'event' must be one of {start|end}", listenerElement, ancestorElementId);
      }
    } else {
      addError("Attribute 'event' is mandatory on listener", listenerElement, ancestorElementId);
    }
    return false;
  }

  public void parseExecutionListenersOnTransition(Element activitiElement, TransitionImpl activity) {
    Element extensionElements = activitiElement.element("extensionElements");
    if (extensionElements != null) {
      List<Element> listenerElements = extensionElements.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        ExecutionListener listener = parseExecutionListener(listenerElement, activity.getId());
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
  public ExecutionListener parseExecutionListener(Element executionListenerElement, String ancestorElementId) {
    ExecutionListener executionListener = null;

    String className = executionListenerElement.attribute(PROPERTYNAME_CLASS);
    String expression = executionListenerElement.attribute(PROPERTYNAME_EXPRESSION);
    String delegateExpression = executionListenerElement.attribute(PROPERTYNAME_DELEGATE_EXPRESSION);
    Element scriptElement = executionListenerElement.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, "script");

    if (className != null) {
      if (className.isEmpty()) {
        addError("Attribute 'class' cannot be empty", executionListenerElement, ancestorElementId);
      } else {
        executionListener = new ClassDelegateExecutionListener(className, parseFieldDeclarations(executionListenerElement));
      }
    } else if (expression != null) {
      executionListener = new ExpressionExecutionListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      if (delegateExpression.isEmpty()) {
        addError("Attribute 'delegateExpression' cannot be empty", executionListenerElement, ancestorElementId);
      } else {
        executionListener = new DelegateExpressionExecutionListener(expressionManager.createExpression(delegateExpression), parseFieldDeclarations(executionListenerElement));
      }
    } else if (scriptElement != null) {
      try {
        ExecutableScript executableScript = parseCamundaScript(scriptElement);
        if (executableScript != null) {
          executionListener = new ScriptExecutionListener(executableScript);
        }
      } catch (BpmnParseException e) {
        addError(e, ancestorElementId);
      }
    } else {
      addError("Element 'class', 'expression', 'delegateExpression' or 'script' is mandatory on executionListener", executionListenerElement, ancestorElementId);
    }
    return executionListener;
  }

  // Diagram interchange
  // /////////////////////////////////////////////////////////////////

  public void parseDiagramInterchangeElements() {
    // Multiple BPMNDiagram possible
    List<Element> diagrams = rootElement.elementsNS(BPMN_DI_NS, "BPMNDiagram");
    if (!diagrams.isEmpty()) {
      for (Element diagramElement : diagrams) {
        parseBPMNDiagram(diagramElement);
      }
    }
  }

  public void parseBPMNDiagram(Element bpmndiagramElement) {
    // Each BPMNdiagram needs to have exactly one BPMNPlane
    Element bpmnPlane = bpmndiagramElement.elementNS(BPMN_DI_NS, "BPMNPlane");
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

      List<Element> shapes = bpmnPlaneElement.elementsNS(BPMN_DI_NS, "BPMNShape");
      for (Element shape : shapes) {
        parseBPMNShape(shape);
      }

      List<Element> edges = bpmnPlaneElement.elementsNS(BPMN_DI_NS, "BPMNEdge");
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

        // The participation that references this process, has a bounds to be
        // rendered + a name as wel
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

          if (lane != null) {
            // The shape represents a lane
            parseDIBounds(bpmnShapeElement, lane);
          } else if (!elementIds.contains(bpmnElement)) { // It might not be an
                                                          // activity nor a
                                                          // lane, but it might
                                                          // still reference
                                                          // 'something'
            addError("Invalid reference in 'bpmnElement' attribute, activity " + bpmnElement + " not found", bpmnShapeElement);
          }
        }
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNShape", bpmnShapeElement);
    }
  }

  protected void parseDIBounds(Element bpmnShapeElement, HasDIBounds target) {
    Element bounds = bpmnShapeElement.elementNS(BPMN_DC_NS, "Bounds");
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
        List<Element> waypointElements = bpmnEdgeElement.elementsNS(OMG_DI_NS, "waypoint");
        if (waypointElements.size() >= 2) {
          List<Integer> waypoints = new ArrayList<>();
          for (Element waypointElement : waypointElements) {
            waypoints.add(parseDoubleAttribute(waypointElement, "x", waypointElement.attribute("x"), true).intValue());
            waypoints.add(parseDoubleAttribute(waypointElement, "y", waypointElement.attribute("y"), true).intValue());
          }
          sequenceFlow.setWaypoints(waypoints);
        } else {
          addError("Minimum 2 waypoint elements must be definted for a 'BPMNEdge'", bpmnEdgeElement);
        }
      } else if (!elementIds.contains(sequenceFlowId)) { // it might not be a
                                                         // sequenceFlow but it
                                                         // might still
                                                         // reference
                                                         // 'something'
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
    if (TRUE.equals(booleanText) || "enabled".equals(booleanText) || "on".equals(booleanText) || "active".equals(booleanText) || "yes".equals(booleanText)) {
      return Boolean.TRUE;
    }
    if ("false".equals(booleanText) || "disabled".equals(booleanText) || "off".equals(booleanText) || "inactive".equals(booleanText)
        || "no".equals(booleanText)) {
      return Boolean.FALSE;
    }
    return null;
  }

  public Double parseDoubleAttribute(Element element, String attributeName, String doubleText, boolean required) {
    if (required && (doubleText == null || "".equals(doubleText))) {
      addError(attributeName + " is required", element);
    } else {
      try {
        return Double.parseDouble(doubleText);
      } catch (NumberFormatException e) {
        addError("Cannot parse " + attributeName + ": " + e.getMessage(), element);
      }
    }
    return -1.0;
  }

  protected boolean isStartable(Element element) {
    return TRUE.equalsIgnoreCase(element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "isStartableInTasklist", TRUE));
  }

  protected boolean isExclusive(Element element) {
    return TRUE.equals(element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "exclusive", String.valueOf(JobEntity.DEFAULT_EXCLUSIVE)));
  }

  protected boolean isAsyncBefore(Element element) {
    return TRUE.equals(element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "async"))
        || TRUE.equals(element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "asyncBefore"));
  }

  protected boolean isAsyncAfter(Element element) {
    return TRUE.equals(element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "asyncAfter"));
  }

  protected boolean isServiceTaskLike(Element element) {

    return element != null && (
          element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_CLASS) != null
        || element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_EXPRESSION) != null
        || element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_DELEGATE_EXPRESSION) != null
        || element.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE) != null
        || hasConnector(element));
  }

  protected boolean hasConnector(Element element) {
    Element extensionElements = element.element("extensionElements");
    return extensionElements != null && extensionElements.element("connector") != null;
  }

  public Map<String, List<JobDeclaration<?, ?>>> getJobDeclarations() {
    return jobDeclarations;
  }

  public List<JobDeclaration<?, ?>> getJobDeclarationsByKey(String processDefinitionKey) {
    return jobDeclarations.get(processDefinitionKey);
  }

  // IoMappings ////////////////////////////////////////////////////////

  protected void parseActivityInputOutput(Element activityElement, ActivityImpl activity) {
    Element extensionElements = activityElement.element("extensionElements");
    if (extensionElements != null) {
      IoMapping inputOutput = null;
      try {
        inputOutput = parseInputOutput(extensionElements);
      } catch (BpmnParseException e) {
        addError(e, activity.getId());
      }

      if (inputOutput != null) {
        if (checkActivityInputOutputSupported(activityElement, activity, inputOutput)) {

          activity.setIoMapping(inputOutput);

          if (getMultiInstanceScope(activity) == null) {
            // turn activity into a scope (->local, isolated scope for
            // variables) unless it is a multi instance activity, in that case
            // this
            // is not necessary because:
            // A scope is already created for the multi instance body which
            // isolates the local variables from other executions in the same
            // scope, and
            // * parallel: the individual concurrent executions are isolated
            // even if they are not scope themselves
            // * sequential: after each iteration local variables are purged
            activity.setScope(true);
          }
        }

        for (BpmnParseListener parseListener : parseListeners) {
          parseListener.parseIoMapping(extensionElements, activity, inputOutput);
        }
      }
    }
  }

  protected boolean checkActivityInputOutputSupported(Element activityElement, ActivityImpl activity, IoMapping inputOutput) {
    String tagName = activityElement.getTagName();

    if (!(tagName.toLowerCase().contains("task")
        || tagName.contains("Event")
        || tagName.equals("transaction")
        || tagName.equals("subProcess")
        || tagName.equals("callActivity"))) {
      addError("camunda:inputOutput mapping unsupported for element type '" + tagName + "'.", activityElement);
      return false;
    }

    if (tagName.equals("subProcess") && TRUE.equals(activityElement.attribute("triggeredByEvent"))) {
      addError("camunda:inputOutput mapping unsupported for element type '" + tagName + "' with attribute 'triggeredByEvent = true'.", activityElement);
      return false;
    }

    if (!inputOutput.getOutputParameters().isEmpty()) {
      return checkActivityOutputParameterSupported(activityElement, activity);
    } else {
      return true;
    }
  }

  protected boolean checkActivityOutputParameterSupported(Element activityElement, ActivityImpl activity) {
    String tagName = activityElement.getTagName();

    if (tagName.equals("endEvent")) {
      addError("camunda:outputParameter not allowed for element type '" + tagName + "'.", activityElement);
      return true;
    } else if (getMultiInstanceScope(activity) != null) {
      addError("camunda:outputParameter not allowed for multi-instance constructs", activityElement);
      return false;
    } else {
      return true;
    }
  }

  protected void ensureNoIoMappingDefined(Element element) {
    Element inputOutput = findCamundaExtensionElement(element, "inputOutput");
    if (inputOutput != null) {
      addError("camunda:inputOutput mapping unsupported for element type '" + element.getTagName() + "'.", element);
    }
  }

  protected ParameterValueProvider createParameterValueProvider(Object value, ExpressionManager expressionManager) {
    if (value == null) {
      return new NullValueProvider();

    } else if (value instanceof String) {
      Expression expression = expressionManager.createExpression((String) value);
      return new ElValueProvider(expression);

    } else {
      return new ConstantValueProvider(value);
    }
  }

  protected void addTimeCycleWarning(Element timeCycleElement, String type, String timerElementId) {
    String warning = "It is not recommended to use a " + type + " timer event with a time cycle.";
    addWarning(warning, timeCycleElement, timerElementId);
  }

  protected void ensureNoExpressionInMessageStartEvent(Element element,
                                                       EventSubscriptionDeclaration messageStartEventSubscriptionDeclaration,
                                                       String parentElementId) {
    boolean eventNameContainsExpression = false;
    if(messageStartEventSubscriptionDeclaration.hasEventName()) {
      eventNameContainsExpression = !messageStartEventSubscriptionDeclaration.isEventNameLiteralText();
    }
    if (eventNameContainsExpression) {
      String messageStartName = messageStartEventSubscriptionDeclaration.getUnresolvedEventName();
      addError("Invalid message name '" + messageStartName + "' for element '" +
          element.getTagName() + "': expressions in the message start event name are not allowed!", element, parentElementId);
    }
  }

}

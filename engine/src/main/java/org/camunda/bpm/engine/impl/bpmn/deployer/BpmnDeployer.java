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
package org.camunda.bpm.engine.impl.bpmn.deployer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.IdentityLinkType;

/**
 * {@link Deployer} responsible to parse BPMN 2.0 XML files and create the proper
 * {@link ProcessDefinitionEntity}s. Overwrite this class if you want to gain some control over
 * this mechanism, e.g. setting different version numbers, or you want to use your own {@link BpmnParser}.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Bernd Ruecker
 */
public class BpmnDeployer implements Deployer {

  private static final Logger LOG = Logger.getLogger(BpmnDeployer.class.getName());;

  public static final String[] BPMN_RESOURCE_SUFFIXES = new String[] { "bpmn20.xml", "bpmn" };
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  protected ExpressionManager expressionManager;
  protected BpmnParser bpmnParser;
  protected IdGenerator idGenerator;

  public void deploy(DeploymentEntity deployment) {
    LOG.fine("Processing deployment " + deployment.getName());

    Map<String, List<JobDeclaration<?>>> jobDeclarations = new HashMap<String, List<JobDeclaration<?>>>();
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();

    for (String resourceName : resources.keySet()) {
      BpmnParse bpmnParse = null;

      LOG.info("Processing resource " + resourceName);
      if (isBpmnResource(resourceName)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);


        bpmnParse = bpmnParser
          .createParse()
          .sourceInputStream(inputStream)
          .deployment(deployment)
          .name(resourceName);

        if (!deployment.isValidatingSchema()) {
          bpmnParse.setSchemaResource(null);
        }

        bpmnParse.execute();

        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);

          String diagramResourceName = getDiagramResourceForProcess(resourceName, processDefinition.getKey(), resources);

          // Only generate the resource when deployment is new to prevent modification of deployment resources
          // after the process-definition is actually deployed. Also to prevent resource-generation failure every
          // time the process definition is added to the deployment-cache when diagram-generation has failed the first time.
          if(deployment.isNew()) {
            if (Context.getProcessEngineConfiguration().isCreateDiagramOnDeploy() &&
                  diagramResourceName==null && processDefinition.isGraphicalNotationDefined()) {
              try {
                  byte[] diagramBytes = IoUtil.readInputStream(ProcessDiagramGenerator.generatePngDiagram(processDefinition), null);
                  diagramResourceName = getProcessImageResourceName(resourceName, processDefinition.getKey(), "png");
                  createResource(diagramResourceName, diagramBytes, deployment);
              } catch (Throwable t) { // if anything goes wrong, we don't store the image (the process will still be executable).
                LOG.log(Level.WARNING, "Error while generating process diagram, image will not be stored in repository", t);
              }
            }
          }

          processDefinition.setDiagramResourceName(diagramResourceName);
          processDefinitions.add(processDefinition);
        }

        jobDeclarations.putAll(bpmnParse.getJobDeclarations());
      }
    }

    // check if there are process definitions with the same process key to prevent database unique index violation
    List<String> keyList = new ArrayList<String>();
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (keyList.contains(processDefinition.getKey())) {
        throw new ProcessEngineException("The deployment contains process definitions with the same key '" + processDefinition.getKey() + "' (process id attribute), this is not allowed");
      }
      keyList.add(processDefinition.getKey());
    }

    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {

      if (deployment.isNew()) {
        ProcessDefinitionEntity latestProcessDefinition = processDefinitionManager.findLatestProcessDefinitionByKey(processDefinition.getKey());

        processDefinition.setDeploymentId(deployment.getId());
        processDefinition.setVersion(getVersionForNewProcessDefinition(deployment, processDefinition, latestProcessDefinition));
        processDefinition.setId(getProcessDefinitionId(deployment, processDefinition));

        List<JobDeclaration<?>> declarations = jobDeclarations.get(processDefinition.getKey());
        updateJobDeclarations(declarations, processDefinition, deployment.isNew());
        adjustStartEventSubscriptions(processDefinition, latestProcessDefinition);

        dbSqlSession.insert(processDefinition);
        deploymentCache.addProcessDefinition(processDefinition);
        addAuthorizations(processDefinition);

      } else {

        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        ProcessDefinitionEntity persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        processDefinition.setSuspensionState(persistedProcessDefinition.getSuspensionState());

        List<JobDeclaration<?>> declarations = jobDeclarations.get(processDefinition.getKey());
        updateJobDeclarations(declarations, processDefinition, deployment.isNew());

        deploymentCache.addProcessDefinition(processDefinition);
        addAuthorizations(processDefinition);

      }

      // Add to cache
      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .addProcessDefinition(processDefinition);

      // Add to deployment for further usage
      deployment.addDeployedArtifact(processDefinition);
    }
  }

  protected void updateJobDeclarations(List<JobDeclaration<?>> jobDeclarations, ProcessDefinition processDefinition, boolean isNewDeployment) {

    if(jobDeclarations == null || jobDeclarations.isEmpty()) {
      return;
    }

    final JobDefinitionManager jobDefinitionManager = Context.getCommandContext().getJobDefinitionManager();

    if(isNewDeployment) {
      // create new job definitions:
      for (JobDeclaration<?> jobDeclaration : jobDeclarations) {
        createJobDefinition(processDefinition, jobDeclaration);
      }

    } else {
      // query all job definitions and update the declarations with their Ids
      List<JobDefinitionEntity> existingDefinitions = jobDefinitionManager.findByProcessDefinitionId(processDefinition.getId());
      for (JobDeclaration<?> jobDeclaration : jobDeclarations) {
        boolean jobDefinitionExists = false;
        for (JobDefinition jobDefinitionEntity : existingDefinitions) {

          // <!> Assumption: there can be only one job definition per activity and type
          if(jobDeclaration.getActivityId().equals(jobDefinitionEntity.getActivityId()) &&
              jobDeclaration.getJobHandlerType().equals(jobDefinitionEntity.getJobType())) {
            jobDeclaration.setJobDefinitionId(jobDefinitionEntity.getId());
            jobDefinitionExists = true;
            break;
          }
        }

        if(!jobDefinitionExists) {
          // not found: create new definition
          createJobDefinition(processDefinition, jobDeclaration);
        }

      }
    }

  }

  protected void createJobDefinition(ProcessDefinition processDefinition, JobDeclaration<?> jobDeclaration) {
    final JobDefinitionManager jobDefinitionManager = Context.getCommandContext().getJobDefinitionManager();

    JobDefinitionEntity jobDefinitionEntity = new JobDefinitionEntity(jobDeclaration);
    jobDefinitionEntity.setProcessDefinitionId(processDefinition.getId());
    jobDefinitionEntity.setProcessDefinitionKey(processDefinition.getKey());
    jobDefinitionManager.insert(jobDefinitionEntity);
    jobDeclaration.setJobDefinitionId(jobDefinitionEntity.getId());
  }

  /**
   * adjust all event subscriptions responsible to start process instances
   * (timer start event, message start event). The default behavior is to remove the old
   * subscriptions and add new ones for the new deployed process definitions.
   */
  protected void adjustStartEventSubscriptions(ProcessDefinitionEntity newLatestProcessDefinition, ProcessDefinitionEntity oldLatestProcessDefinition) {
  	removeObsoleteTimers(newLatestProcessDefinition);
  	addTimerDeclarations(newLatestProcessDefinition);

  	removeObsoleteMessageEventSubscriptions(newLatestProcessDefinition, oldLatestProcessDefinition);
  	addMessageEventSubscriptions(newLatestProcessDefinition);
  }

  /**
   * create an id for the process definition. The default is to ask the {@link IdGenerator}
   * and add the process definition key and version if that does not exceed 64 characters.
   * You might want to hook in your own implemenation here.
 * @param deployment
   */
  protected String getProcessDefinitionId(DeploymentEntity deployment, ProcessDefinitionEntity processDefinition) {
  	String nextId = idGenerator.getNextId();
  	String processDefinitionId = processDefinition.getKey()
  	  + ":" + processDefinition.getVersion()
  	  + ":" + nextId; // ACT-505

  	// ACT-115: maximum id length is 64 charcaters
  	if (processDefinitionId.length() > 64) {
  	  processDefinitionId = nextId;
  	}
  	return processDefinitionId;
  }

  /**
   * per default we increment the latest process definition by one - but you
   * might want to hook in some own logic here, e.g. to align process definition
   * versions with deployment / build versions.
   */
  protected int getVersionForNewProcessDefinition(DeploymentEntity deployment, ProcessDefinitionEntity newProcessDefinition, ProcessDefinitionEntity latestProcessDefinition) {
  	if (latestProcessDefinition != null) {
  	  return latestProcessDefinition.getVersion() + 1;
  	} else {
  	  return 1;
  	}
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclarations(ProcessDefinitionEntity processDefinition) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.createTimerInstance(null);
        timer.setDeploymentId(processDefinition.getDeploymentId());
      }
    }
  }

  protected void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<Job> jobsToDelete = Context
      .getCommandContext()
      .getJobManager()
      .findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());

    for (Job job :jobsToDelete) {
        new DeleteJobsCmd(job.getId()).execute(Context.getCommandContext());
    }
  }

  protected void removeObsoleteMessageEventSubscriptions(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
    // remove all subscriptions for the previous version
    if(latestProcessDefinition != null) {
      CommandContext commandContext = Context.getCommandContext();

      List<EventSubscriptionEntity> subscriptionsToDelete = commandContext
        .getEventSubscriptionManager()
        .findEventSubscriptionsByConfiguration(MessageEventHandler.EVENT_HANDLER_TYPE, latestProcessDefinition.getId());

      for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsToDelete) {
        eventSubscriptionEntity.delete();
      }

    }
  }

  @SuppressWarnings("unchecked")
  protected void addMessageEventSubscriptions(ProcessDefinitionEntity processDefinition) {
    CommandContext commandContext = Context.getCommandContext();
    List<EventSubscriptionDeclaration> messageEventDefinitions = (List<EventSubscriptionDeclaration>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(messageEventDefinitions != null) {
      for (EventSubscriptionDeclaration messageEventDefinition : messageEventDefinitions) {
        if(messageEventDefinition.isStartEvent()) {
          // look for subscriptions for the same name in db:
          List<EventSubscriptionEntity> subscriptionsForSameMessageName = commandContext
            .getEventSubscriptionManager()
            .findEventSubscriptionsByName(MessageEventHandler.EVENT_HANDLER_TYPE, messageEventDefinition.getEventName());
          // also look for subscriptions created in the session:
          List<MessageEventSubscriptionEntity> cachedSubscriptions = commandContext
            .getDbSqlSession()
            .findInCache(MessageEventSubscriptionEntity.class);
          for (MessageEventSubscriptionEntity cachedSubscription : cachedSubscriptions) {
            if(messageEventDefinition.getEventName().equals(cachedSubscription.getEventName())
                    && !subscriptionsForSameMessageName.contains(cachedSubscription)) {
              subscriptionsForSameMessageName.add(cachedSubscription);
            }
          }
          // remove subscriptions deleted in the same command
          subscriptionsForSameMessageName = commandContext
                  .getDbSqlSession()
                  .pruneDeletedEntities(subscriptionsForSameMessageName);

          if(!subscriptionsForSameMessageName.isEmpty()) {
            throw new ProcessEngineException("Cannot deploy process definition '" + processDefinition.getResourceName()
                    + "': there already is a message event subscription for the message with name '" + messageEventDefinition.getEventName() + "'.");
          }

          MessageEventSubscriptionEntity newSubscription = new MessageEventSubscriptionEntity();
          newSubscription.setEventName(messageEventDefinition.getEventName());
          newSubscription.setActivityId(messageEventDefinition.getActivityId());
          newSubscription.setConfiguration(processDefinition.getId());

          newSubscription.insert();
        }
      }
    }
  }

  enum ExprType {
	  USER, GROUP
  }

  private void addAuthorizationsFromIterator(Set<Expression> exprSet, ProcessDefinitionEntity processDefinition, ExprType exprType) {
    CommandContext commandContext = Context.getCommandContext();
    if (exprSet != null) {
      Iterator<Expression> iterator = exprSet.iterator();
      while (iterator.hasNext()) {
        Expression expr = (Expression) iterator.next();
        IdentityLinkEntity identityLink = new IdentityLinkEntity();
        identityLink.setProcessDef(processDefinition);
        if (exprType.equals(ExprType.USER)) {
           identityLink.setUserId(expr.toString());
        } else if (exprType.equals(ExprType.GROUP)) {
          identityLink.setGroupId(expr.toString());
        }
        identityLink.setType(IdentityLinkType.CANDIDATE);
        commandContext.getDbSqlSession().insert(identityLink);
      }
    }
  }

  protected void addAuthorizations(ProcessDefinitionEntity processDefinition) {
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterUserIdExpressions(), processDefinition, ExprType.USER);
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterGroupIdExpressions(), processDefinition, ExprType.GROUP);
  }

  /**
   * Returns the default name of the image resource for a certain process.
   *
   * It will first look for an image resource which matches the process
   * specifically, before resorting to an image resource which matches the BPMN
   * 2.0 xml file resource.
   *
   * Example: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing only one process with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   *
   * Example 2: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing three processes (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png'
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * processes will have the same image: abc.png.
   *
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForProcess(String bpmnFileResource, String processKey, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String diagramForBpmnFileResource = getBpmnFileImageResourceName(bpmnFileResource, diagramSuffix);
      String processDiagramResource = getProcessImageResourceName(bpmnFileResource, processKey, diagramSuffix);
      if (resources.containsKey(processDiagramResource)) {
        return processDiagramResource;
      } else if (resources.containsKey(diagramForBpmnFileResource)) {
        return diagramForBpmnFileResource;
      }
    }
    return null;
  }

  protected String getBpmnFileImageResourceName(String bpmnFileResource, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + diagramSuffix;
  }

  protected String getProcessImageResourceName(String bpmnFileResource, String processKey, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + processKey + "." + diagramSuffix;
  }

  protected String stripBpmnFileSuffix(String bpmnFileResource) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (bpmnFileResource.endsWith(suffix)) {
        return bpmnFileResource.substring(0, bpmnFileResource.length() - suffix.length());
      }
    }
    return bpmnFileResource;
  }

  protected void createResource(String name, byte[] bytes, DeploymentEntity deploymentEntity) {
    ResourceEntity resource = new ResourceEntity();
    resource.setName(name);
    resource.setBytes(bytes);
    resource.setDeploymentId(deploymentEntity.getId());

    // Mark the resource as 'generated'
    resource.setGenerated(true);

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(resource);
  }

  protected boolean isBpmnResource(String resourceName) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }

  public void setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

}

package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Deivarayan Azhagappan
 *
 */
public interface HistoricIdentityLinkLogQuery extends Query<HistoricIdentityLinkLogQuery, HistoricIdentityLinkLog>{
  
  /** Only select historic identity links which have the date before the give date. **/
	HistoricIdentityLinkLogQuery dateBefore(Date dateBefore);

	/** Only select historic identity links which have the date after the give date. **/
	HistoricIdentityLinkLogQuery dateAfter(Date dateAfter);
	
	/** Only select historic identity links which have the given identity link type. **/
	HistoricIdentityLinkLogQuery type(String type);
	
	/** Only select historic identity links which have the given user id. **/
	HistoricIdentityLinkLogQuery userId(String userId);
	
	/** Only select historic identity links which have the given group id. **/
	HistoricIdentityLinkLogQuery groupId(String groupId);
	
	/** Only select historic identity links which have the given task id. **/
	HistoricIdentityLinkLogQuery taskId(String taskId);
	
	/** Only select historic identity links which have the given process definition id. **/
	HistoricIdentityLinkLogQuery processDefinitionId(String processDefinitionId);
	
	/** Only select historic identity links which have the given process definition key. **/
  HistoricIdentityLinkLogQuery processDefinitionKey(String processDefinitionKey);

	/** Only select historic identity links which have the given operation type (add/delete). **/
	HistoricIdentityLinkLogQuery operationType(String operationType);
	
	/** Only select historic identity links which have the given assigner id. **/
	HistoricIdentityLinkLogQuery assignerId(String assignerId);
	
	/** Only select historic identity links which have the given tenant id. **/
	HistoricIdentityLinkLogQuery tenantIdIn(String... tenantId);
	
	/** Order by time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIdentityLinkLogQuery orderByTime();
  
	/** Order by type (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByType();
	
	/** Order by userId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByUserId();
	
	/** Order by groupId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByGroupId();
	
	/** Order by taskId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByTaskId();
	
	/** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByProcessDefinitionId();
	
	/** Order by processDefinitionKey (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIdentityLinkLogQuery orderByProcessDefinitionKey();

	/** Order by operationType (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByOperationType();
	
	/** Order by assignerId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkLogQuery orderByAssignerId();
	
	 /** Order by tenantId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIdentityLinkLogQuery orderByTenantId();
	
}

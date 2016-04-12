package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Deivarayan Azhagappan
 *
 */
public interface HistoricIdentityLinkQuery extends Query<HistoricIdentityLinkQuery, HistoricIdentityLink>{
  
  /** Only select historic identity links which have the date before the give date. **/
	HistoricIdentityLinkQuery dateBefore(Date dateBefore);

	/** Only select historic identity links which have the date after the give date. **/
	HistoricIdentityLinkQuery dateAfter(Date dateAfter);
	
	/** Only select historic identity links which have the given identity link type. **/
	HistoricIdentityLinkQuery type(String type);
	
	/** Only select historic identity links which have the given user id. **/
	HistoricIdentityLinkQuery userId(String userId);
	
	/** Only select historic identity links which have the given group id. **/
	HistoricIdentityLinkQuery groupId(String groupId);
	
	/** Only select historic identity links which have the given task id. **/
	HistoricIdentityLinkQuery taskId(String taskId);
	
	/** Only select historic identity links which have the given process definition id. **/
	HistoricIdentityLinkQuery processDefinitionId(String processDefinitionId);
	
	/** Only select historic identity links which have the given operation type (add/delete). **/
	HistoricIdentityLinkQuery operationType(String operationType);
	
	/** Only select historic identity links which have the given assigner id. **/
	HistoricIdentityLinkQuery assignerId(String assignerId);
	
	/** Order by time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIdentityLinkQuery orderByTime();
  
	/** Order by type (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByType();
	
	/** Order by userId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByUserId();
	
	/** Order by groupId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByGroupId();
	
	/** Order by taskId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByTaskId();
	
	/** Order by processDefId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByProcessDefinitionId();
	
	/** Order by operationType (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByOperationType();
	
	/** Order by assignerId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByAssignerId();
	
}

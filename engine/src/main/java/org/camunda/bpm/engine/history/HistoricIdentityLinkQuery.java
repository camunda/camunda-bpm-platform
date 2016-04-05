package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Deivarayan Azhagappan
 *
 */
public interface HistoricIdentityLinkQuery extends Query<HistoricIdentityLinkQuery, HistoricIdentityLink>{
  
	HistoricIdentityLinkQuery identityLinkId(String id);
	
	HistoricIdentityLinkQuery dateBefore(Date dateBefore);
	
	HistoricIdentityLinkQuery dateAfter(Date dateAfter);
	
	HistoricIdentityLinkQuery identityLinkType(String identityLinkType);
	
	HistoricIdentityLinkQuery userId(String userId);
	
	HistoricIdentityLinkQuery groupId(String groupId);
	
	HistoricIdentityLinkQuery taskId(String taskId);
	
	HistoricIdentityLinkQuery processDefId(String processDefId);
	
	HistoricIdentityLinkQuery operationType(String operationType);
	
	HistoricIdentityLinkQuery assignerId(String assignerId);
	
	/** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByIdentityLinkId();
  
	/** Order by identityLinkType (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByIdentityLinkType();
	
	/** Order by userId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByUserId();
	
	/** Order by groupId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByGroupId();
	
	/** Order by taskId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByTaskId();
	
	/** Order by processDefId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByProcessDefId();
	
	/** Order by operationType (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByOperationType();
	
	/** Order by assignerId (needs to be followed by {@link #asc()} or {@link #desc()}). */
	HistoricIdentityLinkQuery orderByAssignerId();
	
}

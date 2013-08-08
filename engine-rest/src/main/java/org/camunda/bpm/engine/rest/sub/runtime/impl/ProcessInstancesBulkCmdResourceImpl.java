package org.camunda.bpm.engine.rest.sub.runtime.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.BulkCommandDto;
import org.camunda.bpm.engine.rest.dto.BulkCommandExceptionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.ProcessInstancesBulkCmdResource;

public class ProcessInstancesBulkCmdResourceImpl implements
		ProcessInstancesBulkCmdResource {
	
	private ProcessEngine engine;
	  
	public ProcessInstancesBulkCmdResourceImpl(ProcessEngine engine) {
	    this.engine = engine;	  
	}
	
	@Override
	public  List<BulkCommandExceptionDto> updateSuspensionStates(BulkCommandDto commandDto) {
		RuntimeService runtimeService = engine.getRuntimeService();
		 List<BulkCommandExceptionDto> bulkCommandExceptions  = new ArrayList<BulkCommandExceptionDto>();
		 boolean suspensionState = false;
		 String variableName = "suspended";
		 
		 if(!commandDto.getVariables().containsKey(variableName)){		
			 throw new InvalidRequestException(Status.NOT_FOUND,"No variable 'suspended' found for update suspension state operation");
		 }	
		 
		 try {		 
		 suspensionState = (Boolean)commandDto.getVariables().get(variableName);
		 } catch(ClassCastException ce) {
			 throw new InvalidRequestException(Status.BAD_REQUEST,"The value for the variable '" + variableName + "' is not the correct type");
		 }
		 
		 for(String id : commandDto.getIds()){
			try {
				if(suspensionState){
					runtimeService.suspendProcessInstanceById(id);	
				} else {
					runtimeService.activateProcessInstanceById(id);
				}			  
			} catch(RuntimeException e) {
				bulkCommandExceptions.add(BulkCommandExceptionDto.fromExceptionDetails(id, e.getMessage()));
			}
		}		
		return bulkCommandExceptions;
	}	

	@Override
	public  List<BulkCommandExceptionDto> deleteProcessInstances(BulkCommandDto commandDto) {
		RuntimeService runtimeService = engine.getRuntimeService();
		List<BulkCommandExceptionDto> bulkCommandExceptions  = new ArrayList<BulkCommandExceptionDto>();
		 String deleteReason = null;
		
		 if(commandDto.getVariables().containsKey("deleteReason")){
			deleteReason = (String)commandDto.getVariables().get("deleteReason");
		 }	 
		
		for(String id : commandDto.getIds()){			
			try {
			  runtimeService.deleteProcessInstance(id,deleteReason);
			} catch(RuntimeException e) {
				bulkCommandExceptions.add(BulkCommandExceptionDto.fromExceptionDetails(id, e.getMessage()));
			}
		}				
		return bulkCommandExceptions;
	}
}

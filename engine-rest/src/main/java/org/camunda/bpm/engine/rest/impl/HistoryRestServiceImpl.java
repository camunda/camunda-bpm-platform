package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.HistoricActivityInstanceRestService;
import org.camunda.bpm.engine.rest.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.HistoryRestService;

public class HistoryRestServiceImpl extends AbstractRestProcessEngineAware implements HistoryRestService {

    public HistoryRestServiceImpl() {
	    super();
    }
		  
    public HistoryRestServiceImpl(String engineName) {
        super(engineName);
    }	
	
	@Override
	public HistoricProcessInstanceRestService getProcessInstanceService() {
		return new HistoricProcessInstanceRestServiceImpl();
	}

	@Override
	public HistoricActivityInstanceRestService getActivityInstanceService() {
		return new HistoricActivityInstanceRestServiceImpl();
	}

	@Override
	public HistoricVariableInstanceRestService getVariableInstanceService() {
		return new HistoricVariableInstanceRestServiceImpl();
	}

}

package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.rest.util.BusinessUriUtil.BusinessUriMapper;

public interface BusinessUriMapperProvider {
    BusinessUriMapper getBusinessUriMapper(String processDefinitionKey);
}


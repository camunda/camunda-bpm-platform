package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class BusinessUriUtil {

    public interface BusinessUriMapper {
        String createBusinessUri(String businessKey);
    }

    private BusinessUriUtil() {
        // Utility class, no instantiation
    }

    private static Map<String, BusinessUriMapper> businessUriMappers = new HashMap<>();

    public static String getBusinessUri(final String definitionId, final String businessKey) {
        BusinessUriMapper businessUriMapper;
        if (BusinessUriUtil.businessUriMappers.containsKey(definitionId)) {
            businessUriMapper = BusinessUriUtil.businessUriMappers.get(definitionId);
        } else {
            businessUriMapper = BusinessUriUtil.getBusinessUriMapper(definitionId);
            BusinessUriUtil.businessUriMappers.put(definitionId, businessUriMapper);
        }
        String result = null;
        if (businessUriMapper != null) {
            result = businessUriMapper.createBusinessUri(businessKey);
        }
        return result;
    }

    private static synchronized BusinessUriMapper getBusinessUriMapper(final String processDefinitionId) {
        BusinessUriMapper result = null;
        final Iterator<BusinessUriMapperProvider> iterator = ServiceLoader.load(BusinessUriMapperProvider.class).iterator();
        if (iterator.hasNext()) {
            final BusinessUriMapperProvider provider = iterator.next();
            final ProcessEngine engine = EngineUtil.lookupProcessEngineByDefinitionId(processDefinitionId);
            final ProcessDefinition definition = engine.getRepositoryService().getProcessDefinition(processDefinitionId);
            result = provider.getBusinessUriMapper(definition.getKey());
        }
        return result;
    }

}
package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.rest.dto.converter.DateConverter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Date;

public class MetricsUtil {

    public static Date extractEndDate(MultivaluedMap<String, String> queryParameters, DateConverter dateConverter) {
        if(queryParameters.getFirst("endDate") != null) {
            return dateConverter.convertQueryParameterToType(queryParameters.getFirst("endDate"));
        }
        return null;
    }

    public static Date extractStartDate(MultivaluedMap<String, String> queryParameters, DateConverter dateConverter) {
        if(queryParameters.getFirst("startDate") != null) {
            return dateConverter.convertQueryParameterToType(queryParameters.getFirst("startDate"));
        }
        return null;
    }

}

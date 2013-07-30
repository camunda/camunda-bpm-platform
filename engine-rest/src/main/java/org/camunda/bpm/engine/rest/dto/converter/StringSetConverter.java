package org.camunda.bpm.engine.rest.dto.converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringSetConverter implements StringToTypeConverter<Set<String>> {

	@Override
	public Set<String> convertQueryParameterToType(String value) {
		return new HashSet<String>(Arrays.asList(value.split(",")));
	}

}

package org.camunda.bpm.engine.spring.test.scripttask;

import org.springframework.stereotype.Component;

@Component
public class Testbean {
	private String name = "name property of the testbean";
	public String getName() {
		return name;
	}
}
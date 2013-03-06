package org.camunda.bpm.engine.spring.test.components.scope;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Josh Long
 * @since 5,3
 */

public class Delegate1 implements JavaDelegate,InitializingBean {

	private Logger log = Logger.getLogger( getClass().getName());

	@Autowired private ProcessInstance processInstance ;

	@Autowired private StatefulObject statefulObject;

	public void execute(DelegateExecution execution) throws Exception {

		 String pid = this.processInstance.getId();

		log.info("the processInstance#id is "+ pid) ;

		Assert.assertNotNull("the 'scopedCustomer' reference can't be null", statefulObject);
		String uuid =  UUID.randomUUID().toString();
		statefulObject.setName(uuid);
		log.info("the 'uuid' value given to the ScopedCustomer#name property is '" + uuid + "' in " + getClass().getName());

		this.statefulObject.increment();
	}

	public void afterPropertiesSet() throws Exception {
	 Assert.assertNotNull("the processInstance must not be null", this.processInstance) ;

	}
}

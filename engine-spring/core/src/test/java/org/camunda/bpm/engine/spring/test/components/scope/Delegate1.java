/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

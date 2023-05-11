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
package org.camunda.bpm.engine.spring.test.components;

import org.camunda.bpm.engine.spring.components.scope.ProcessScope;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;


@Scope(ProcessScope.PROCESS_SCOPE_NAME)
public class ScopedCustomer implements Serializable, InitializingBean{
	public ScopedCustomer() {
	}
	public ScopedCustomer(String name) {
		this.name = name;
	}

	private String name = Thread.currentThread().getId()+":" +System.currentTimeMillis();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void afterPropertiesSet() throws Exception {
	 System.out.println("starting ..." + this.name) ;
	}
}

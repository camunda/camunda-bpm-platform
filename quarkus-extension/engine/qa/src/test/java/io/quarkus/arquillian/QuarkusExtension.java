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
package io.quarkus.arquillian;

import org.camunda.bpm.engine.cdi.test.ProcessEngineDeployment;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class QuarkusExtension implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.service(DeployableContainer.class, QuarkusDeployableContainer.class);
    builder.service(Protocol.class, QuarkusProtocol.class);
    builder.service(TestEnricher.class, InjectionEnricher.class);
    builder.service(TestEnricher.class, ArquillianResourceURLEnricher.class);
    builder.observer(CreationalContextDestroyer.class);
    builder.observer(QuarkusBeforeAfterLifecycle.class);
    builder.observer(ProcessEngineDeployment.class);
    builder.observer(RequestContextLifecycle.class);
    builder.observer(ClassLoaderExceptionTransformer.class);
  }

}

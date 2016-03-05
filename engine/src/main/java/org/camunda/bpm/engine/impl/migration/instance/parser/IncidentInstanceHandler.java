/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.migration.instance.parser;

import org.camunda.bpm.engine.impl.migration.instance.MigratingIncident;
import org.camunda.bpm.engine.impl.migration.instance.MigratingJobInstance;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class IncidentInstanceHandler implements MigratingInstanceParseHandler<IncidentEntity> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, IncidentEntity incident) {
    if (IncidentEntity.FAILED_JOB_HANDLER_TYPE.equals(incident.getIncidentType())) {
      MigratingJobInstance owningInstance = parseContext.getMigratingJobInstanceById(incident.getConfiguration());
      parseContext.consume(incident);
      if (owningInstance.getTargetScope() != null) {
        MigratingIncident migratingIncident = new MigratingIncident(incident, (ScopeImpl) owningInstance.getTargetScope().getEventScope());
        owningInstance.addMigratingDependentInstance(migratingIncident);
      }
    }

  }

}

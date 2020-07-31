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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class VariableInTransactionTest extends PluggableProcessEngineTest {

  @Test
  public void testCreateAndDeleteVariableInTransaction() throws Exception {

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        //create a variable
        VariableInstanceEntity variable = VariableInstanceEntity.createAndInsert("aVariable", Variables.byteArrayValue(new byte[0]));
        String byteArrayId = variable.getByteArrayValueId();

        //delete the variable
        variable.delete();

        //check if the variable is deleted transient
        //-> no insert and delete stmt will be flushed
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        CachedDbEntity cachedEntity = dbEntityManager.getDbEntityCache().getCachedEntity(ByteArrayEntity.class, byteArrayId);

        DbEntityState entityState = cachedEntity.getEntityState();
        assertEquals(DbEntityState.DELETED_TRANSIENT, entityState);

        return null;
      }
    });

  }
}

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
package org.camunda.bpm.engine.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.junit.Test;

public class ExceptionUtilTest {

  @Test
  public void checkValueTooLongException() {
    assertThat(ExceptionUtil.checkValueTooLongException(mock(SQLException.class))).isFalse();

    SQLException tooLong = mock(SQLException.class);
    doReturn("too long").when(tooLong).getMessage();
    assertThat(ExceptionUtil.checkValueTooLongException(tooLong)).isTrue();
  }

  @Test
  public void checkConstraintViolationException() {
    assertThat(ExceptionUtil.checkConstraintViolationException(new ProcessEngineException(new PersistenceException(mock(SQLException.class))))).isFalse();

    SQLException constraintViolation = mock(SQLException.class);
    doReturn("ora-00001").when(constraintViolation).getMessage();
    assertThat(ExceptionUtil.checkConstraintViolationException(new ProcessEngineException(new PersistenceException(constraintViolation)))).isTrue();
  }

  @Test
  public void checkForeignKeyConstraintViolation() {
    assertThat(ExceptionUtil.checkForeignKeyConstraintViolation(mock(SQLException.class))).isFalse();

    SQLException constraintViolation = mock(SQLException.class);
    doReturn("integrity constraint").when(constraintViolation).getMessage();
    assertThat(ExceptionUtil.checkForeignKeyConstraintViolation(constraintViolation)).isTrue();
  }

  @Test
  public void checkVariableIntegrityViolation() {
    assertThat(ExceptionUtil.checkVariableIntegrityViolation(new PersistenceException(mock(SQLException.class)))).isFalse();

    SQLException integrityViolation = mock(SQLException.class);
    doReturn("act_uniq_variable").when(integrityViolation).getMessage();
    doReturn("23505").when(integrityViolation).getSQLState();
    assertThat(ExceptionUtil.checkVariableIntegrityViolation(new PersistenceException(integrityViolation))).isTrue();
  }

  @Test
  public void checkDeadlockException() {
    assertThat(ExceptionUtil.checkDeadlockException(mock(SQLException.class))).isFalse();

    SQLException deadlock = mock(SQLException.class);
    doReturn("40P01").when(deadlock).getSQLState();  // PostgreSQL
    assertThat(ExceptionUtil.checkDeadlockException(deadlock)).isTrue();
  }

}

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
package org.camunda.bpm.engine.impl.db.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.ibatis.type.DateOnlyTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedJdbcTypes(value = {JdbcType.DATE})
@MappedTypes(java.util.Date.class)
public class UTCDateOnlyTypeHandler extends DateOnlyTypeHandler {

  private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
    ps.setDate(i, new java.sql.Date(parameter.getTime()), Calendar.getInstance(TIME_ZONE));
  }

  @Override
  public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
    java.sql.Date sqlDate = rs.getDate(columnName, Calendar.getInstance(TIME_ZONE));
    return sqlDate != null ? new Date(sqlDate.getTime()) : null;
  }

  @Override
  public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    java.sql.Date sqlDate = rs.getDate(columnIndex, Calendar.getInstance(TIME_ZONE));
    return sqlDate != null ? new Date(sqlDate.getTime()) : null;
  }

  @Override
  public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    java.sql.Date sqlDate = cs.getDate(columnIndex, Calendar.getInstance(TIME_ZONE));
    return sqlDate != null ? new Date(sqlDate.getTime()) : null;
  }

}

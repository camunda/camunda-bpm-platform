package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.query.QueryProperty;

public interface HistoricIdentityLinkQueryProperty {

  public static final QueryProperty ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty TIME = new QueryPropertyImpl("TIMESTAMP_");
  public static final QueryProperty TYPE = new QueryPropertyImpl("TYPE_");
  public static final QueryProperty USER_ID = new QueryPropertyImpl("USER_ID_");
  public static final QueryProperty GROUP_ID = new QueryPropertyImpl("GROUP_ID_");
  public static final QueryProperty TASK_ID = new QueryPropertyImpl("TASK_ID_");
  public static final QueryProperty PROC_DEFINITION_ID = new QueryPropertyImpl("PROC_DEF_ID_");
  public static final QueryProperty OPERATION_TYPE = new QueryPropertyImpl("OPERATION_TYPE_");
  public static final QueryProperty ASSIGNER_ID = new QueryPropertyImpl("ASSIGNER_ID_");
}

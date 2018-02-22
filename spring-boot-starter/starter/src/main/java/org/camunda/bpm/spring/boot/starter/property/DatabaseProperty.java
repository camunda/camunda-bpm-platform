package org.camunda.bpm.spring.boot.starter.property;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;
import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE;
import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE;
import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class DatabaseProperty {
  public static final List<String> SCHEMA_UPDATE_VALUES = Arrays.asList(
    DB_SCHEMA_UPDATE_TRUE,
    DB_SCHEMA_UPDATE_FALSE,
    DB_SCHEMA_UPDATE_CREATE,
    DB_SCHEMA_UPDATE_CREATE_DROP,
    DB_SCHEMA_UPDATE_DROP_CREATE);

  /**
   * enables automatic schema update
   */
  private String schemaUpdate = DB_SCHEMA_UPDATE_TRUE;

  /**
   * the database type
   */
  private String type;

  /**
   * the database table prefix to use
   */
  private String tablePrefix = Defaults.INSTANCE.getDatabaseTablePrefix();

  /**
   * the database schema to use
   */
  private String schemaName = Defaults.INSTANCE.getDatabaseSchema();

  /**
   * enables batch processing mode for db operations
   */
  private boolean jdbcBatchProcessing = true;

  public String getSchemaUpdate() {
    return schemaUpdate;
  }

  /**
   * @param schemaUpdate the schemaUpdate to set
   */
  public void setSchemaUpdate(String schemaUpdate) {
    Assert.isTrue(SCHEMA_UPDATE_VALUES.contains(schemaUpdate), String.format("schemaUpdate: '%s' is not valid (%s)", schemaUpdate, SCHEMA_UPDATE_VALUES));
    this.schemaUpdate = schemaUpdate;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTablePrefix() {
    return tablePrefix;
  }

  public void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }

  public static List<String> getSchemaUpdateValues() {
    return SCHEMA_UPDATE_VALUES;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public boolean isJdbcBatchProcessing() {
    return jdbcBatchProcessing;
  }

  public void setJdbcBatchProcessing(boolean jdbcBatchProcessing) {
    this.jdbcBatchProcessing = jdbcBatchProcessing;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("type=" + type)
      .add("schemaUpdate=" + schemaUpdate)
      .add("schemaName=" + schemaName)
      .add("tablePrefix=" + tablePrefix)
      .add("jdbcBatchProcessing=" + jdbcBatchProcessing)
      .toString();
  }
}

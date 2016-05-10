package org.camunda.bpm.container.impl.jboss.extension;

import org.camunda.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration;
import org.camunda.bpm.container.impl.jboss.util.CustomMarshaller;
import org.camunda.bpm.container.impl.jboss.util.FixedObjectTypeAttributeDefinition;
import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class SubsystemAttributeDefinitons {

  public static final String DEFAULT_DATASOURCE = "java:jboss/datasources/ExampleDS";
  public static final String DEFAULT_HISTORY_LEVEL = "audit";
  public static final String DEFAULT_PROCESS_ENGINE_CONFIGURATION_CLASS = ManagedJtaProcessEngineConfiguration.class.getName();
  public static final String DEFAULT_ACQUISITION_STRATEGY = "SEQUENTIAL";
  public static final String DEFAULT_JOB_EXECUTOR_THREADPOOL_NAME = "job-executor-tp";
  public static final int DEFAULT_CORE_THREADS = 3;
  public static final int DEFAULT_MAX_THREADS = 5;
  public static final int DEFAULT_QUEUE_LENGTH = 10;
  public static final int DEFAULT_KEEPALIVE_TIME = 10;
  public static final boolean DEFAULT_ALLOW_CORE_TIMEOUT = true;

  // general
  public static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinition(ModelConstants.NAME, new ModelNode("default"), ModelType.STRING, false);
  public static final SimpleMapAttributeDefinition PROPERTIES = new SimpleMapAttributeDefinition.Builder(ModelConstants.PROPERTIES, true).build();

  // process engine
  public static final SimpleAttributeDefinition DEFAULT = new SimpleAttributeDefinition(ModelConstants.DEFAULT, new ModelNode(false), ModelType.BOOLEAN, true);
  public static final SimpleAttributeDefinition DATASOURCE = new SimpleAttributeDefinition(ModelConstants.DATASOURCE, new ModelNode(DEFAULT_DATASOURCE), ModelType.STRING, false);
  public static final SimpleAttributeDefinition HISTORY_LEVEL = new SimpleAttributeDefinition(ModelConstants.HISTORY_LEVEL, new ModelNode(DEFAULT_HISTORY_LEVEL), ModelType.STRING, true);
  public static final SimpleAttributeDefinition CONFIGURATION = new SimpleAttributeDefinition(ModelConstants.CONFIGURATION, new ModelNode(DEFAULT_PROCESS_ENGINE_CONFIGURATION_CLASS), ModelType.STRING, true);

  // job executor
  @Deprecated
  public static final SimpleAttributeDefinition THREAD_POOL_NAME = new SimpleAttributeDefinition(ModelConstants.THREAD_POOL_NAME, new ModelNode(DEFAULT_JOB_EXECUTOR_THREADPOOL_NAME), ModelType.STRING, true);
  public static final SimpleAttributeDefinition CORE_THREADS = new SimpleAttributeDefinition(ModelConstants.CORE_THREADS, new ModelNode(DEFAULT_CORE_THREADS), ModelType.INT, false);
  public static final SimpleAttributeDefinition MAX_THREADS = new SimpleAttributeDefinition(ModelConstants.MAX_THREADS, new ModelNode(DEFAULT_MAX_THREADS), ModelType.INT, false);
  public static final SimpleAttributeDefinition QUEUE_LENGTH = new SimpleAttributeDefinition(ModelConstants.QUEUE_LENGTH, new ModelNode(DEFAULT_QUEUE_LENGTH), ModelType.INT, false);
  public static final SimpleAttributeDefinition KEEPALIVE_TIME = new SimpleAttributeDefinition(ModelConstants.KEEPALIVE_TIME, new ModelNode(DEFAULT_KEEPALIVE_TIME), ModelType.INT, true);
  public static final SimpleAttributeDefinition ALLOW_CORE_TIMEOUT = new SimpleAttributeDefinition(ModelConstants.ALLOW_CORE_TIMEOUT, new ModelNode(DEFAULT_ALLOW_CORE_TIMEOUT), ModelType.BOOLEAN, true);
  @Deprecated
  public static final SimpleAttributeDefinition ACQUISITION_STRATEGY = new SimpleAttributeDefinition(ModelConstants.ACQUISITION_STRATEGY, new ModelNode(DEFAULT_ACQUISITION_STRATEGY), ModelType.STRING, true);

  public static final SimpleAttributeDefinition PLUGIN_CLASS = SimpleAttributeDefinitionBuilder.create(ModelConstants.PLUGIN_CLASS, ModelType.STRING, true)
      .setAttributeMarshaller(CustomMarshaller.ATTRIBUTE_AS_ELEMENT)
      .build();

  public static final AttributeDefinition[] PLUGIN_ATTRIBUTES = new AttributeDefinition[] {
      PLUGIN_CLASS,
      PROPERTIES
  };

  public static final FixedObjectTypeAttributeDefinition PLUGIN = FixedObjectTypeAttributeDefinition.Builder.of(ModelConstants.PLUGIN, PLUGIN_ATTRIBUTES)
      .setAttributeMarshaller(CustomMarshaller.OBJECT_AS_ELEMENT)
      .setAttributeParser(AttributeParser.LIST)
      .setRequires(ModelConstants.PLUGIN_CLASS)
      .setAllowNull(true)
      .build();

  public static final ObjectListAttributeDefinition PLUGINS = ObjectListAttributeDefinition.Builder.of(ModelConstants.PLUGINS, PLUGIN)
      .setAttributeMarshaller(CustomMarshaller.OBJECT_LIST)
      .setAllowNull(true)
      .setAllowExpression(true)
      .build();

  public static final AttributeDefinition[] JOB_EXECUTOR_ATTRIBUTES = new AttributeDefinition[] {
      THREAD_POOL_NAME,
      CORE_THREADS,
      MAX_THREADS,
      QUEUE_LENGTH,
      KEEPALIVE_TIME,
      ALLOW_CORE_TIMEOUT
  };

  public static final AttributeDefinition[] JOB_ACQUISITION_ATTRIBUTES = new AttributeDefinition[] {
      NAME,
      ACQUISITION_STRATEGY,
      PROPERTIES
  };

  public static final AttributeDefinition[] PROCESS_ENGINE_ATTRIBUTES = new AttributeDefinition[] {
      NAME,
      DEFAULT,
      DATASOURCE,
      HISTORY_LEVEL,
      CONFIGURATION,
      PROPERTIES,
      PLUGINS
  };

}

package org.camunda.bpm.container.impl.jboss.extension;

import org.camunda.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration;
import org.camunda.bpm.container.impl.jboss.util.CustomMarshaller;
import org.camunda.bpm.container.impl.jboss.util.FixedObjectTypeAttributeDefinition;
import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class Constants {

  public static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinition(ModelConstants.NAME, new ModelNode("default"), ModelType.STRING, false);

  public static final SimpleAttributeDefinition DEFAULT = new SimpleAttributeDefinition(ModelConstants.DEFAULT, new ModelNode(false), ModelType.BOOLEAN, true);

  public static final SimpleAttributeDefinition DATASOURCE = new SimpleAttributeDefinition(ModelConstants.DATASOURCE, new ModelNode("java:jboss/datasources/ExampleDS"), ModelType.STRING, false);

  public static final SimpleAttributeDefinition HISTORY_LEVEL = new SimpleAttributeDefinition(ModelConstants.HISTORY_LEVEL, new ModelNode("audit"), ModelType.STRING, true);

  public static final SimpleAttributeDefinition CONFIGURATION = new SimpleAttributeDefinition(ModelConstants.CONFIGURATION, new ModelNode(ManagedJtaProcessEngineConfiguration.class.getName()), ModelType.STRING, true);

  public static final SimpleAttributeDefinition THREAD_POOL_NAME = new SimpleAttributeDefinition(ModelConstants.THREAD_POOL_NAME, new ModelNode("default"), ModelType.STRING, true);

  @Deprecated
  public static final SimpleAttributeDefinition ACQUISITION_STRATEGY = new SimpleAttributeDefinition(ModelConstants.ACQUISITION_STRATEGY, new ModelNode("SEQUENTIAL"), ModelType.STRING, true);

  public static final SimpleMapAttributeDefinition PROPERTIES = new SimpleMapAttributeDefinition.Builder(ModelConstants.PROPERTIES, true).build();

  public static final SimpleAttributeDefinition PLUGIN_CLASS = SimpleAttributeDefinitionBuilder.create(ModelConstants.PLUGIN_CLASS, ModelType.STRING, true)
      .setAttributeMarshaller(CustomMarshaller.ATTRIBUTE_AS_ELEMENT)
      .build();

  public static final AttributeDefinition[] PLUGIN_ATTRIBUTES = new AttributeDefinition[] {
      PLUGIN_CLASS,
      PROPERTIES
  };

  public static final FixedObjectTypeAttributeDefinition PLUGIN = FixedObjectTypeAttributeDefinition.Builder.of(ModelConstants.PLUGIN, PLUGIN_ATTRIBUTES)
      .setAttributeMarshaller(CustomMarshaller.OBJECT_AS_ELEMENT)
      .setRequires(ModelConstants.PLUGIN_CLASS)
      .setAllowNull(true)
      .build();

  public static final ObjectListAttributeDefinition PLUGINS = ObjectListAttributeDefinition.Builder.of(ModelConstants.PLUGINS, PLUGIN)
      .setAttributeMarshaller(CustomMarshaller.OBJECT_LIST)
      .setAllowNull(true)
      .setAllowExpression(true)
      .build();

  public static final AttributeDefinition[] JOB_EXECUTOR_ATTRIBUTES = new AttributeDefinition[] {
      THREAD_POOL_NAME
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

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
package org.camunda.bpm.container.impl.jboss.extension;

import org.camunda.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration;
import org.camunda.bpm.container.impl.jboss.util.CustomMarshaller;
import org.jboss.as.controller.*;
import org.jboss.as.controller.registry.AttributeAccess;
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
    public static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder(ModelConstants.NAME, ModelType.STRING, false)
                .setDefaultValue(new ModelNode("default"))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .build();
    public static final SimpleMapAttributeDefinition PROPERTIES =
            new SimpleMapAttributeDefinition.Builder(ModelConstants.PROPERTIES, true)
                .setAttributeMarshaller(CustomMarshaller.PROPERTIES_MARSHALLER)
                .setRestartAllServices()
                .setAllowExpression(true)
                .build();

    // process engine
    public static final SimpleAttributeDefinition DEFAULT =
            new SimpleAttributeDefinitionBuilder(ModelConstants.DEFAULT, ModelType.BOOLEAN, true)
                .setDefaultValue(new ModelNode(false))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition DATASOURCE =
            new SimpleAttributeDefinitionBuilder(ModelConstants.DATASOURCE, ModelType.STRING, false)
                .setDefaultValue(new ModelNode(DEFAULT_DATASOURCE))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition HISTORY_LEVEL =
            new SimpleAttributeDefinitionBuilder(ModelConstants.HISTORY_LEVEL, ModelType.STRING, true)
                .setDefaultValue(new ModelNode(DEFAULT_HISTORY_LEVEL))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition CONFIGURATION =
            new SimpleAttributeDefinitionBuilder(ModelConstants.CONFIGURATION, ModelType.STRING, true)
                .setDefaultValue(new ModelNode(DEFAULT_PROCESS_ENGINE_CONFIGURATION_CLASS))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();

    // job executor
    public static final AttributeDefinition THREAD_POOL_NAME =
            new SimpleAttributeDefinitionBuilder(ModelConstants.THREAD_POOL_NAME, ModelType.STRING, true)
                .setDefaultValue(new ModelNode(DEFAULT_JOB_EXECUTOR_THREADPOOL_NAME))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition CORE_THREADS =
            new SimpleAttributeDefinitionBuilder(ModelConstants.CORE_THREADS, ModelType.INT, false)
                .setDefaultValue(new ModelNode(DEFAULT_CORE_THREADS))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition MAX_THREADS =
            new SimpleAttributeDefinitionBuilder(ModelConstants.MAX_THREADS, ModelType.INT, false)
                .setDefaultValue(new ModelNode(DEFAULT_MAX_THREADS))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition QUEUE_LENGTH =
            new SimpleAttributeDefinitionBuilder(ModelConstants.QUEUE_LENGTH, ModelType.INT, false)
                .setDefaultValue(new ModelNode(DEFAULT_QUEUE_LENGTH))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition KEEPALIVE_TIME =
            new SimpleAttributeDefinitionBuilder(ModelConstants.KEEPALIVE_TIME, ModelType.INT, true)
                .setDefaultValue(new ModelNode(DEFAULT_KEEPALIVE_TIME))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();
    public static final AttributeDefinition ALLOW_CORE_TIMEOUT =
            new SimpleAttributeDefinitionBuilder(ModelConstants.ALLOW_CORE_TIMEOUT, ModelType.BOOLEAN, true)
                .setDefaultValue(new ModelNode(DEFAULT_ALLOW_CORE_TIMEOUT))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();

    @Deprecated
    public static final AttributeDefinition ACQUISITION_STRATEGY =
            new SimpleAttributeDefinitionBuilder(ModelConstants.ACQUISITION_STRATEGY, ModelType.STRING, true)
                .setDefaultValue(new ModelNode(DEFAULT_ACQUISITION_STRATEGY))
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .setAllowExpression(true)
                .build();

    public static final SimpleAttributeDefinition PLUGIN_CLASS =
            new SimpleAttributeDefinitionBuilder(ModelConstants.PLUGIN_CLASS, ModelType.STRING, true)
                .setAttributeMarshaller(CustomMarshaller.ATTRIBUTE_AS_ELEMENT)
                .setAllowExpression(true)
                .build();

    public static final AttributeDefinition[] PLUGIN_ATTRIBUTES = new AttributeDefinition[] {
        PLUGIN_CLASS,
        PROPERTIES
    };

    public static final ObjectTypeAttributeDefinition PLUGIN = ObjectTypeAttributeDefinition.Builder
            .of(ModelConstants.PLUGIN, PLUGIN_ATTRIBUTES)
            .setAttributeMarshaller(CustomMarshaller.OBJECT_AS_ELEMENT)
            .setAttributeParser(AttributeParser.OBJECT_LIST_PARSER)
            .setRequires(ModelConstants.PLUGIN_CLASS)
            .setRequired(false)
            .setRestartAllServices()
            .build();

    public static final ObjectListAttributeDefinition PLUGINS = ObjectListAttributeDefinition.Builder
            .of(ModelConstants.PLUGINS, PLUGIN)
            .setAttributeMarshaller(CustomMarshaller.OBJECT_LIST)
            .setRequired(false)
            .setAllowExpression(true)
            .setRestartAllServices()
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

package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializerIntern;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFieldsImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class PerfTestBase64 {

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .threads(2)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        JavaObjectSerializerIntern serializerIntern;
        JavaObjectSerializer serializer;
        ValueFields fields;
        ObjectValue object;

CommandContext mockedCmdContext;
        ProcessEngineConfigurationImpl mockedConfiguration;
        AuthorizationManager authorizationManager;
        DbEntityManager mockedEntityManager;

        @Setup(Level.Trial)
        public void initialize() {
            serializerIntern = new JavaObjectSerializerIntern();
            serializer = new JavaObjectSerializer();
            fields = new ValueFieldsImpl();
            fields.setTextValue("test");
            object = Variables.objectValue(new String("mystring"))
                    .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                    .create();

            mockedCmdContext = mock(CommandContext.class);
            mockedConfiguration = new ProcessEngineConfigurationImpl();
            authorizationManager = spy(new AuthorizationManager());
            mockedEntityManager = mock(DbEntityManager.class);

            Context.setCommandContext(mockedCmdContext);
            Context.setProcessEngineConfiguration(mockedConfiguration);
        }
    }

    @Benchmark
    public void base64Intern(BenchmarkState state, Blackhole bh) {
        state.serializerIntern.writeValue(state.object, state.fields);
    }

    @Benchmark
    public void base64(BenchmarkState state, Blackhole bh) {
        state.serializer.writeValue(state.object, state.fields);
    }
}

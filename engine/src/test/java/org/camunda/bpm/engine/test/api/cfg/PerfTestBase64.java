package org.camunda.bpm.engine.test.api.cfg;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
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
import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(5)
                .measurementIterations(10)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        CommandContext mockedCmdContext;
        ProcessEngineConfigurationImpl mockedConfiguration;
        ProcessEngineImpl mockedEngine;

        @Setup(Level.Trial)
        public void initialize() {

            mockedCmdContext = mock(CommandContext.class);
            mockedConfiguration = mock(ProcessEngineConfigurationImpl.class);
            mockedEngine = mock(ProcessEngineImpl.class);

            when(mockedConfiguration.getProcessEngine()).thenReturn(mockedEngine);
            when(mockedEngine.getProcessEngineConfiguration()).thenReturn(mockedConfiguration);
            when(mockedConfiguration.getDefaultCharset()).thenReturn(Charset.forName("UTF-8"));

            Context.setCommandContext(mockedCmdContext);
            Context.setProcessEngineConfiguration(mockedConfiguration);
        }
    }

    @Benchmark
    public void base64Intern(BenchmarkState state, Blackhole bh) {
        ValueFieldsImpl fields = new ValueFieldsImpl();
        DummyDomainobject domainobject = new DummyDomainobject();
        domainobject.name = "MeinName";
        domainobject.datum = new Date();
        Child child = new Child();
        child.number = 43;
        child.liste = Arrays.asList("eintrag1", "eintrag2", "eintragN");
        domainobject.child = child;
        ObjectValue object = Variables.objectValue(domainobject)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();
        JavaObjectSerializerIntern serializerIntern = new JavaObjectSerializerIntern();
        serializerIntern.writeValue(object, fields);
    }

    @Benchmark
    public void base64(BenchmarkState state, Blackhole bh) {
        ValueFieldsImpl fields = new ValueFieldsImpl();
        DummyDomainobject domainobject = new DummyDomainobject();
        domainobject.name = "MeinName";
        domainobject.datum = new Date();
        Child child = new Child();
        child.number = 43;
        child.liste = Arrays.asList("eintrag1", "eintrag2", "eintragN");
        domainobject.child = child;
        ObjectValue object = Variables.objectValue(domainobject)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();
        JavaObjectSerializer serializer = new JavaObjectSerializer();
        serializer.writeValue(object, fields);
    }
}

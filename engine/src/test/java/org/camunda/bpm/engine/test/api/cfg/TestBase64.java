package org.camunda.bpm.engine.test.api.cfg;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializerIntern;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFieldsImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class TestBase64 {

    CommandContext mockedCmdContext;
    ProcessEngineConfigurationImpl mockedConfiguration;
    ProcessEngineImpl mockedEngine;

    @Before
    public void setup() {
        mockedCmdContext = mock(CommandContext.class);
        mockedConfiguration = mock(ProcessEngineConfigurationImpl.class);
        mockedEngine = mock(ProcessEngineImpl.class);

        when(mockedConfiguration.getProcessEngine()).thenReturn(mockedEngine);
        when(mockedEngine.getProcessEngineConfiguration()).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getDefaultCharset()).thenReturn(Charset.forName("UTF-8"));

        Context.setCommandContext(mockedCmdContext);
        Context.setProcessEngineConfiguration(mockedConfiguration);
    }

    @After
    public void cleanup() {
        Context.removeCommandContext();
        Context.removeProcessEngineConfiguration();
    }

    @Test
    public void testSerializeString() {
        JavaObjectSerializerIntern serializerIntern = new JavaObjectSerializerIntern();
        JavaObjectSerializer serializerOld = new JavaObjectSerializer();

        ObjectValue object = Variables.objectValue(new String("mystring"))
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();

        ValueFields fieldsIntern = new ValueFieldsImpl();
        serializerIntern.writeValue(object, fieldsIntern);

        ValueFields fieldsOld = new ValueFieldsImpl();
        serializerOld.writeValue(object, fieldsOld);

        assertEquals("base64-Serializer apacheCommons and java.xml produce different output", Arrays.toString(fieldsOld.getByteArrayValue()), Arrays.toString(fieldsIntern.getByteArrayValue()));
    }

    @Test
    public void testDeserializeString() {
        final String dataType = String.class.getName();

        JavaObjectSerializerIntern serializerIntern = new JavaObjectSerializerIntern();
        JavaObjectSerializer serializerOld = new JavaObjectSerializer();

        ObjectValue object = Variables.objectValue(new String("HelloWorld"))
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();
        ValueFields fieldsInput = new ValueFieldsImpl();
        serializerOld.writeValue(object, fieldsInput);

        ValueFields fieldsIntern = fieldsInput;
        fieldsIntern.setTextValue2(dataType);
        ObjectValue objectIntern = serializerIntern.readValue(fieldsIntern, true);

        ValueFields fieldsOld = fieldsInput;
        fieldsOld.setTextValue2(dataType);
        ObjectValue objectOld = serializerOld.readValue(fieldsOld, true);
        assertEquals("base64-Deserializer apacheCommons and java.xml produce different output", objectOld.getObjectTypeName(), objectIntern.getObjectTypeName());
        assertEquals("base64-Deserializer apacheCommons and java.xml produce different output", objectOld.getValue(), objectIntern.getValue());
    }

    @Test
    public void testSerializeDomainobject() {
        JavaObjectSerializerIntern serializerIntern = new JavaObjectSerializerIntern();
        JavaObjectSerializer serializerOld = new JavaObjectSerializer();

        DummyDomainobject domainobject = new DummyDomainobject();
        domainobject.name = "MeinName";
        domainobject.datum = new Date();
        Child child = new Child();
        child.number = 43;
        child.liste = Arrays.asList("eintrag1, eintrag2, eintragN");
        domainobject.child = child;
        ObjectValue object = Variables.objectValue(domainobject)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();

        ValueFields fieldsIntern = new ValueFieldsImpl();
        serializerIntern.writeValue(object, fieldsIntern);

        ValueFields fieldsOld = new ValueFieldsImpl();
        serializerOld.writeValue(object, fieldsOld);

        assertEquals("base64-Serializer apacheCommons and java.xml produce different output", Arrays.toString(fieldsOld.getByteArrayValue()), Arrays.toString(fieldsIntern.getByteArrayValue()));
    }

    @Test
    public void testDeserializeDomainobject() {
        final String dataType = DummyDomainobject.class.getName();

        JavaObjectSerializerIntern serializerIntern = new JavaObjectSerializerIntern();
        JavaObjectSerializer serializerOld = new JavaObjectSerializer();

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
        ValueFields fieldsInput = new ValueFieldsImpl();
        serializerOld.writeValue(object, fieldsInput);

        ValueFields fieldsIntern = fieldsInput;
        fieldsIntern.setTextValue2(dataType);
        ObjectValue objectIntern = serializerIntern.readValue(fieldsIntern, true);

        ValueFields fieldsOld = fieldsInput;
        fieldsOld.setTextValue2(dataType);
        ObjectValue objectOld = serializerOld.readValue(fieldsOld, true);
        assertEquals("base64-Deserializer apacheCommons and java.xml produce different output", objectOld.getObjectTypeName(), objectIntern.getObjectTypeName());
        assertEquals("base64-Deserializer apacheCommons and java.xml produce different output", objectOld.getValue(), objectIntern.getValue());
    }
}

class DummyDomainobject implements Serializable {
    String name;
    Date datum;
    Child child;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyDomainobject that = (DummyDomainobject) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (datum != null ? !datum.equals(that.datum) : that.datum != null) return false;
        return child != null ? child.equals(that.child) : that.child == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (datum != null ? datum.hashCode() : 0);
        result = 31 * result + (child != null ? child.hashCode() : 0);
        return result;
    }
}

class Child implements Serializable {
    int number;
    List<String> liste;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Child child = (Child) o;

        if (number != child.number) return false;
        return liste != null ? liste.equals(child.liste) : child.liste == null;
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + (liste != null ? liste.hashCode() : 0);
        return result;
    }
}

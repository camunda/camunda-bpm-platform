package org.camunda.bpm.integrationtest.functional.spin.dataformat;

import java.io.IOException;
import org.camunda.bpm.integrationtest.functional.spin.XmlSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Svetlana Dorokhova.
 */
public class XmlSerializableJsonSerializer extends StdSerializer<XmlSerializable> {

  public XmlSerializableJsonSerializer() {
    super(XmlSerializable.class);
  }

  @Override
  public void serialize(XmlSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
    gen.writeString(value.getProperty());
  }
}

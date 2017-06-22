package org.camunda.bpm.integrationtest.functional.spin.dataformat;

import java.io.IOException;
import org.camunda.bpm.integrationtest.functional.spin.XmlSerializable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author Svetlana Dorokhova.
 */
public class XmlSerializableJsonDeserializer extends JsonDeserializer<XmlSerializable> {

  @Override
  public XmlSerializable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    XmlSerializable xmlSerializable = new XmlSerializable();
    xmlSerializable.setProperty(p.getValueAsString());
    return xmlSerializable;
  }

}

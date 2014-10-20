import org.camunda.spin.DataFormats.xmlDom as xmlDom
result = XML(input, xml().mapper().config("schema", schema).done()).mapTo("org.camunda.spin.xml.mapping.Order")
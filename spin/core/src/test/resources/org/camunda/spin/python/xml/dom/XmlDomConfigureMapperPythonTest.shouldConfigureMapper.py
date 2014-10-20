import org.camunda.spin.DataFormats.xmlDom as xmlDom;
result = XML(input, xml().mapper().config("jaxb.encoding", "UTF-8").done())
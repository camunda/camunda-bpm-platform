import org.camunda.spin.DataFormats.xmlDom as xmlDom;
result = XML(input, xmlDom().mapper().config("jaxb.encoding", "UTF-8").done())
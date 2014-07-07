import org.camunda.spin.DataFormats.jsonTree as jsonTree;

json1 = S(input, jsonTree().allowNumericLeadingZeros(True))

json2 = JSON(input, jsonTree().allowNumericLeadingZeros(True))

json3 = JSON(input, {"allowNumericLeadingZeros": True})

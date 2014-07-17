import org.camunda.spin.DataFormats.jsonTree as jsonTree;

json1 = S(input, jsonTree().reader().allowNumericLeadingZeros(True).done())

json2 = JSON(input, jsonTree().reader().allowNumericLeadingZeros(True).done())

json3 = JSON(input, {"ALLOW_NUMERIC_LEADING_ZEROS": True}, None)

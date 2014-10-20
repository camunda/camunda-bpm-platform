import org.camunda.spin.DataFormats.jsonTree as jsonTree;

json1 = S(input, json().reader().allowNumericLeadingZeros(True).done())

json2 = JSON(input, json().reader().allowNumericLeadingZeros(True).done())

json3 = JSON(input, {"ALLOW_NUMERIC_LEADING_ZEROS": True}, None, None)

json1 = S(input, org.camunda.spin.DataFormats.jsonTree().reader().allowNumericLeadingZeros(true).done());

json2 = JSON(input, org.camunda.spin.DataFormats.jsonTree().reader().allowNumericLeadingZeros(true).done());

json3 = JSON(input, {ALLOW_NUMERIC_LEADING_ZEROS: true}, null, null);

$json1 = S($input, org.camunda.spin.DataFormats.jsonTree().allowNumericLeadingZeros(true))

$json2 = JSON($input, org.camunda.spin.DataFormats.jsonTree().allowNumericLeadingZeros(true))

$json3 = JSON($input, {"ALLOW_NUMERIC_LEADING_ZEROS" => true})

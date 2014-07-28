node = JSON($input)

currencies = node.prop("orderDetails").prop("currencies")

$value = currencies.indexOf("dollar")
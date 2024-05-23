node = S(input, "application/json")

currencies = node.prop("orderDetails").prop("currencies")

value = currencies.indexOf("dollar")
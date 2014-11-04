node = JSON(input)

currencies = node.prop("orderDetails").prop("currencies")

value = currencies.lastIndexOf("dollar")
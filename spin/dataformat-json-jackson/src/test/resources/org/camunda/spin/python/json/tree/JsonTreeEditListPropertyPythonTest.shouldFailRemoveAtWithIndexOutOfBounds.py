node = JSON(input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.removeAt(6)

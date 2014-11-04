node = JSON(input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAfter(None, "test")
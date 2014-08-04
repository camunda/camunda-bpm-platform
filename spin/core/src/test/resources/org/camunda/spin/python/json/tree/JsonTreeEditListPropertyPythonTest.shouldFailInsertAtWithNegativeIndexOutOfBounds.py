node = JSON(input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAt(-6, "test1")

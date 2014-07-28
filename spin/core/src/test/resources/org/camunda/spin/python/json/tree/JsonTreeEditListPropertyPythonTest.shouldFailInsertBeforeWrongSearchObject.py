class testObject:
    pass

node = JSON(input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertBefore(testObject(), "test")

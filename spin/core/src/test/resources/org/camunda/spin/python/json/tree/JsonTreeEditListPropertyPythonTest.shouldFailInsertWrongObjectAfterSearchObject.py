class TestObject:
    pass

node = JSON(input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAfter("euro", TestObject())

class TestObject:
    pass

node = S(input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertBefore(TestObject(), "test")

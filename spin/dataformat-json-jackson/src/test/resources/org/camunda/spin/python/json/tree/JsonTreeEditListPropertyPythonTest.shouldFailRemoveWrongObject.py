class TestObject:
    pass

node = S(input, "application/json")
customers = node.prop("customers")

customers.remove(TestObject())

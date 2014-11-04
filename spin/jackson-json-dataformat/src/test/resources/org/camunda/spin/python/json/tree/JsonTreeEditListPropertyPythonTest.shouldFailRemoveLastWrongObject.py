class TestObject:
    pass

node = JSON(input)
customers = node.prop("customers")

customers.removeLast(TestObject())

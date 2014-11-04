class TestObject:
    pass

node = JSON(input)
customers = node.prop("customers")

customers.remove(TestObject())

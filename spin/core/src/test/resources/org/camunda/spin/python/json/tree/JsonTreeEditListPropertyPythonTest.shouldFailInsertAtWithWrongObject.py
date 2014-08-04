class TestObject:
    pass

node = JSON(input)
customers = node.prop("customers")
customers.insertAt(1, TestObject())

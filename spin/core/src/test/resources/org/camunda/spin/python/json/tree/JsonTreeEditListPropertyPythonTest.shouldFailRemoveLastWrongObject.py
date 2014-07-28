class testObject:
    pass

node = JSON(input)
customers = node.prop("customers")

customers.removeLast(testObject())

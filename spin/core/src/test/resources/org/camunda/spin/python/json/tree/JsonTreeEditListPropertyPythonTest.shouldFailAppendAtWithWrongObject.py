class testObject:
    pass

node = JSON(input)
customers = node.prop("customers")
customers.appendAt(1, testObject())
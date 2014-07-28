class testObject:
    pass

node = JSON(input)
customers = node.prop("customers")
customers.append(testObject())
node = S(input, "application/json")
customers = node.prop("customers")
customers.insertAt(1, None)
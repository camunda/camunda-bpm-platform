class TestObject:
    pass

node = S(input, "application/json")
list = [TestObject()]

node.prop("comment", list)

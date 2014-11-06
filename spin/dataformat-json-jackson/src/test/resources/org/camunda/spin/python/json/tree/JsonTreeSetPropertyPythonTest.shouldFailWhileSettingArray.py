class TestObject:
    pass

node = JSON(input)
list = [TestObject()]

node.prop("comment", list)

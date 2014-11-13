class TestObject:
    pass


node = S(input, "application/json")
object = {
    "date": TestObject()
}

node.prop("comment", object)

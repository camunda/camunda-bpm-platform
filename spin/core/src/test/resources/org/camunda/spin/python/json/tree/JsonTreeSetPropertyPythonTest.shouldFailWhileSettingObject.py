class TestObject:
    pass


node = JSON(input)
object = {
    "date": TestObject()
}

node.prop("comment", object)

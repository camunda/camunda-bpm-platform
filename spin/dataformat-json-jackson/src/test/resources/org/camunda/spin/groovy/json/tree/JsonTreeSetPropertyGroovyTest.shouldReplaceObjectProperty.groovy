node = S(input, "application/json");

oldValue = node.prop("order");

def object = [
    name: "test",
    comment: "test2"
]

node.prop("order", object);
newValue = node.prop("order");
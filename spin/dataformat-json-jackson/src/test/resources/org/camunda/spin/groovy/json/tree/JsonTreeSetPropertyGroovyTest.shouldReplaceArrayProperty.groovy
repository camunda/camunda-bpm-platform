node = S(input, "application/json");

oldValue = node.prop("order");

def list = new ArrayList();
list.push("test");
list.push("test2");

node.prop("order", list);
newValue = node.prop("order");
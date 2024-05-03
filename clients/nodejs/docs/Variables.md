# Variables
```js
const { Variables } = require("camunda-external-task-client-js");

// ... somewhere in the handler function
const variables = new Variables().setAll({ foo: "some foo value" });
console.log("foo", variables.get("foo"));
```

> **Note:** All setters return the variables instance. This can be helpful
for chaining calls.


## `new Variables(options)`

The following option can be passed but is **not required**:

| Option   | Description                                           | Type    |
|----------|-------------------------------------------------------|---------|
| readOnly | If set to true, only getters functions are available. | boolean |

## `variables.get(variableName)`

Returns the value of the variable with key _variableName_.

```js
// Given:
// score: { value: 5, type: "integer", valueInfo: {} }
const score = variables.get("score");
console.log(score);
```

Output:

```js
5
```

## `variables.getTyped(variableName)`

Returns the typed value of the variable with key _variableName_.

```js
// Given
// score: { value: 5, type: "integer", valueInfo: {} }
const score = variables.getTyped("score");
console.log(score);
```

Output:

```js
{ value: 5, type: "integer", valueInfo: {} }
```

### About _typed values_
A typed value is an object with the following structure:

```
{
  value: "some value",
  // type of the variable, e.g. integer, long, string, boolean, json ...
  type: "string",
  // An object containing additional, value-type-dependent properties
  valueInfo: {}
}
```

## `variables.getAll()`

Returns the values of all variables.

```js
// Given:
// {
//    score: { value: 5, type: "integer", valueInfo: {} }
//    isWinning: { value: false, type: "boolean", valueInfo: {} }
// }
const values = variables.getAll();
console.log(values);
```

Output:

```js
{  score: 5, isWinning: false }
```

## `variables.getAllTyped()`

Returns the typed values of all variables.

```js
// Given:
// {
//    score: { value: 5, type: "integer", valueInfo: {} }
//    isWinning: { value: false, type: "boolean", valueInfo: {} }
// }
const typedValues = variables.getAllTyped();
console.log(typedValues);
```

Output:

```js
{
    score: { value: 5, type: "integer", valueInfo: {} },
    isWinning: { value: false, type: "boolean", valueInfo: {} }
}
```

## `variables.set(variableName, value)`

Sets a value for the variable with key _variableName_.

> **Note:** The variable type is determined automatically.

```js
variables.set("fullName", { first: "John", last: "Doe" });
console.log(variables.getTyped("fullName"));
```

Output:

```js
{
      value: { first: "John", last: "Doe" },
      type:  "json",
      valueInfo: {}
}
```

## `variables.setTransient(variableName, value)`

Sets a value for the variable with key _variableName_, also sets transient flag true to variable.

> **Note:** The variable type is determined automatically.

```js
variables.setTransient("fullName", { first: "John", last: "Doe" });
console.log(variables.getTyped("fullName"));
```

Output:

```js
{
      value: { first: "John", last: "Doe" },
      type:  "json",
      valueInfo: {transient: true}
}
```

## `variables.setTyped(variableName, typedValue)`

Sets a typed value for the variable with key _variableName_

>**Note:** The variable type is **not** case sensitive.

```js
variables.setTyped("test", {
  value: "<test id=2 />",
  type: "XML",
  valueInfo: {}
});

console.log(variables.getTyped("test"));
```

Output

```js
 {
    value: "<test id=2 />",
    type: "XML",
    valueInfo: {}
  }
```

## `variables.setAll(values)`

Sets the values of multiple variables at once.

```js
// Given:
// {
//    score: { value: 6, type: "integer", valueInfo: {} }
//    isWinning: { value: true, type: "boolean", valueInfo: {} }
// }
variables.setAll({
  score: 8,
  message: "Score is on 🔥"
});

console.log(variables.getAll());
```

Output:

```js
 { score: 8, isWinning: true, message: "Score is on 🔥" }
```

## `variables.setAllTyped(typedValues)`

Sets the typed values of multiple variables at once.

```js
// Given:
// {
//    score: { value: 6, type: "integer", valueInfo: {} }
//    isWinning: { value: true, type: "boolean", valueInfo: {} }
// }
variables.setAllTyped({
  score: { value: 8, type: "short", valueInfo: {} },
  message: { value: "Score is on 🔥", type: "string", valueInfo: {} }
});

console.log(variables.getAllTyped());
```

Output:

```js
 {
    score: { value: 8 , type: "short", valueInfo: {} },
    isWinning: { value: true, type: "boolean", valueInfo: {} },
    message: { value: "Score is on 🔥" , type: "string", valueInfo: {} },
 }
```

## About JSON & Date Variables
Date and JSON values are automatically serialized when being set and deserialized when being read.

### Date

```js
// 'variables.set()' can be used to set a date by providing a date object
variables.set("someDate", new Date());

// 'variables.setTyped()' can be used to set a date by either:
// 1- providing a date object as a value:
variables.setTyped("anotherDate", { type: "date", value: new Date(), valueInfo: {} });
// 2- providing a date string as a value:
variables.setTyped("anotherDate", { type: "date", value: "2016-01-25T13:33:42.165+0100", valueInfo: {} });

// `variables.get("anotherDate")` is a date object
console.log(typeof variables.get("anotherDate")); // output: object

// `variables.getTyped("anotherDate").value` is date object
console.log(typeof variables.getTyped("anotherDate").value); // output: object
```

### JSON
```js
// 'variables.set()' can be used to set a JSON object by providing an object
variables.set("meal", { id: 0, name: "pasta" });

// The same is also possible with `variables.setTyped()`
variables.setTyped({
  type: "json",
  value: { id: 0, name: "pasta" },
  valueInfo: {}
});

// `variables.get("meal")` is an object
console.log(variables.get("someJSON")); // output: { id: 0, name: "pasta" }

// `variables.getTyped("meal").value` is an object
console.log(variables.getTyped("someJSON").value); // output: { id: 0, name: "pasta" }
```

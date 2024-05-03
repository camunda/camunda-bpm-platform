# Example
> NodeJS >= v18 is required

<img alt="A Process for Handling Orders" src="assets/order.svg" />

## Running the example

1. First, make sure to have [Camunda](https://camunda.com/download/) running.

2. Download the following [model](assets/order.bpmn) and deploy it using the Camunda Modeler.

3. Install Dependencies:

```sh
npm install
```

Or:

```sh
yarn
```

4. Run the example:
```sh
node index.js
```

### Output
The terminal output should be:
```
✓ subscribed to topic invoiceCreator
✓ completed task 21d19522-3e4c-11e8-b8df-186590db1cd7
```

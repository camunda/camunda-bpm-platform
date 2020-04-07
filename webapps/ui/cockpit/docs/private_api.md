# Private API for Plugins

## The render Function
### Returns
Opposed to the public documentation, a render function can return JSX and React Components to be rendered. This will only work when the **Plugin Container** is already implemented in React, otherwise it will be ignored. This can be usefull wen we need the components to be in the same virtual DOM tree.

```JavaScript
render() {
  return <Link to="foobar">DOMRouter Link</Link>;
}
```

### Arguments
The render function receives a third argument, which is not documented. It can be used by us to provide additional data to the Plugin, such as an already fetched Process Definition instead of the Id. Currently, plugins in an Angular host will receive the `scope`. The contents in this parameter are not documented and subject to change.

## Plugin Points
In this section, Additional Information to certain Plugin Points is presented. Some of the Plugin Points we offer are not public yet because we want to change the API.

## Route
`cockpit.route`

Placeholders for URLs can be used like this: `path: "my/:id/plugin"`.
The named url params are passed into the plugin as additional data.

```
path attribute: "my/:id/plugin"
Requested URL: my/123456/plugin
data passed as second argument: {id: "123456"}
```

## Case Definition Diagram Overlay
`cockpit.caseDefinition.diagram.overlay`
Needs refactoring --> Currently instantiated for every Diagram element

## Case Instance Diagram Overlay
`cockpit.caseInstance.diagram.overlay`
Needs refactoring --> Currently instantiated for every Diagram element

## Process Definition Diagram Overlay
`cockpit.processDefinition.diagram.overlay`
Behaves same as the Case Definition Diagram Overlay, but we already have a plugin point with correct behavior. This one will probably disappear once angular is gone

## Process Definition View
`cockpit.processDefinition.view`

This is the place where runtime/history links are rendered. To have this work, you also have to create a new [route](#route).
We need to refactor this: only the label is rendered. Either have the route automatically resolve or mount the `render` function.

## Process Instance View
`cockpit.processInstance.view`

This is the place where runtime/history links are rendered. To have this work, you also have to create a new [route](#route).

## DRD Definition Overlay
`cockpit.drd.definition.overlay`

Currently not rendered at all

## DRD Instance Overlay
`cockpit.drd.instance.overlay`

Currently not rendered at all
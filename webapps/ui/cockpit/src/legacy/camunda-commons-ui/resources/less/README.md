# Camunda commons styling

## Scaffolding

In this folder you can find 3 sub-folders:

 - `sites`: in which styles for sites like documentation, blog, presence, … are defined
 - `shared`: contains other files which _might_ relevant for web applications or web sites but are not imported by default


## File name convention

Files starting with a `_` are aimed to provide variables or mixins only (importing them will not output anything).


## Best practices

In your project, you should use a `_vars.less` file which imports the [`shared/_variables.less`](./shared/_variables.less) of this project.

```less
// note: you may need to adapt this path depending on where your `_vars.less` is
@import "node_modules/camunda-commons-ui/resources/less/shared/_variables.less";

// override the default `@main-color` color defined in `shared/_variables.less`
@main-color: #7fa;

// add custom variables for your project
@custom-variable: 10px;
```

Then, you will have a `styles.less` (which will probably be compiled as `styles.css`).

```less
@import "./_vars.less";

// adapt the path if / as needed
@import "node_modules/camunda-commons-ui/resources/less/shared/base.less";
```

# File
The File class provides an easy way to create file variables and asynchronously load their content.

```js
const { Client, logger, File } = require("camunda-external-task-handler-js");

const client = new Client({ baseUrl: "http://localhost:8080/engine-rest" });

client.subscribe("foo", async function({ task, taskService}) {
   const file = await new File({ localPath: "./data.txt" }).load();
   variables.set("dataFile", file);
});
```

> **Note:** File variables contents are internally converted to _base64_ strings when completing a task.
> They are also internally parsed to buffers when polling variables.


## `new File(options)`
Here's a list of the available options:

| Option    | Description                | Type   | Required | Default                                                                                                                  |
|-----------|----------------------------|--------|----------|--------------------------------------------------------------------------------------------------------------------------|
| localPath | Path used to load the file | string | ✓        |                                                                                                                          |
| filename  | Name of the file           | string |          | Basename of the localPath. e.g. If the localPath is: `path/to/something.txt`, the filename will then be: `something.txt` |
| encoding  | Encoding of the file       | string |          |                                                                                                                          |
| mimetype  | Mimetype of the file       | string |          |                                                                                                                          |                                                                                                                                                                                                                   |


## `file.load()`
Loads **asynchronously** the file content from the `localPath`.

> **Note:** `file.load()` returns the File instance. This can be helpful
for chaining calls.

## File Properties
| Property         | Description                             | Type   |
|------------------|-----------------------------------------|--------|
| `file.content`   | Binary content of the file              | buffer |
| `file.localPath` | Path that used to load the file         | string |
| `file.filename`  | Name of the file                        | string |
| `file.encoding`  | Encoding of the file                    | string |
| `file.mimetype`  | Mimetype of the file                    | string |

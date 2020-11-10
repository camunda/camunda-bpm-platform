const fs = require("fs-extra"); 

const args = process.argv.slice(2);

const sourceDirectory = args[0];
const destinationDirectory = args[1];

fs.rmdirSync(destinationDirectory, { recursive: true });
fs.copySync(sourceDirectory, destinationDirectory);
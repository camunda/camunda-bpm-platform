# Builds

Currently, we only support Enterprise distributions. Once we are ready for a public release, we will also provide a CE distribution build

## Production

This part explains how to build the prepackaged distributions (e.g. on a tomcat server).

1. Check out this repository and camunda-bpm-platform-ee in parallel

   ```sh
   git clone git@github.com:camunda/camunda-bpm-platform-ee.git
   git clone git@github.com:camunda/camunda-bpm-webapps-react.git
   ```

2. in the camunda-bpm-platform-ee repo, checkout the react compatibility branch. This will be the master branch once react is the default

   ```sh
   cd camunda-bpm-platform-ee
   git checkout CAM-11441
   ```

3. Build the distribution using maven. If this command fails, make sure you configured maven with credentials for the camunda nexus.
   ```sh
   mvn clean install -Pdistro
   ```

The artifacts are located in `/distro/{{server name}}/distro/target`, e.g. `/distro/tomcat/distro/target`.

## Development

1. Have a camunda distro running on port 8080
2. run `npm start`
3. Site is available on port 3000, happy coding

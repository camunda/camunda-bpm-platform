/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const scope = 'xlts.dev';

const {execSync} = require('child_process');

const exec = (cmd, successMsg) => {
  return execSync(cmd, (error, stdout, stderr) => {
    if (error) {
      console.err(`error: ${error.message}`);
      return;
    }
    if (stderr) {
      console.err(`stderr: ${stderr}`);
      return;
    }
    console.log(successMsg ? successMsg : `stdout: ${stdout}`);
  }).toString();
};

const registryConfigured = exec(`npm get @${scope}:registry`) !== 'undefined\n';

const {XLTS_REGISTRY, XLTS_AUTH_TOKEN} = process.env;

if (!registryConfigured && (!XLTS_REGISTRY || !XLTS_AUTH_TOKEN)) {
  console.log('XLTS installation skipped.');
  return;
}

if (!registryConfigured) {
  exec(
    `npm set @${scope}:registry https://${XLTS_REGISTRY}/`,
    'XLTS.dev registry configured.'
  );

  exec(
    `npm set //${XLTS_REGISTRY}/:_authToken ${XLTS_AUTH_TOKEN}`,
    'XLTS.dev auth token configured.'
  );
}

if (process.argv[2] === 'install') {
  const {xltsVersion, dependencies} = require('../package.json').xlts;

  const getNpmPackages = dependencies =>
    dependencies
      .map(npmPackage => `${npmPackage}@npm:@${scope}/${npmPackage}@${xltsVersion}`)
      .join(' ');

  exec(`npm i --no-save ${getNpmPackages(dependencies)}`);
}

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

const scope = 'neverendingsupport';

const {execSync} = require('child_process');

const exec = (cmd, successMsg) => {
  return execSync(cmd, (error, stdout, stderr) => {
    if (error) {
      console.err(`error: ${error.message}`); // eslint-disable-line
      return;
    }
    if (stderr) {
      console.err(`stderr: ${stderr}`); // eslint-disable-line
      return;
    }
    console.log(successMsg ? successMsg : `stdout: ${stdout}`); // eslint-disable-line
  }).toString();
};

const getDependencyVersion = (nameSpace, npmPackage, xltsVersion) => {
  if (nameSpace !== 'angular') {
    return `${npmPackage}@npm:@${scope}/${npmPackage}@${xltsVersion}`;
  }

  let versionPostfix = npmPackage.split('-')[1] || '';
  versionPostfix = versionPostfix ? '-' + versionPostfix : '';
  return `${npmPackage}@npm:@${scope}/angularjs@${xltsVersion}${versionPostfix}`;
};

const registryConfigured = exec(`npm get @${scope}:registry`) !== 'undefined\n';

const {XLTS_REGISTRY, XLTS_AUTH_TOKEN} = process.env;

if (!registryConfigured && XLTS_REGISTRY && XLTS_AUTH_TOKEN) {
  exec(
    `npm set @${scope}:registry https://${XLTS_REGISTRY}`,
    'XLTS.dev registry configured.'
  );

  exec(
    `npm set //${XLTS_REGISTRY}:_authToken ${XLTS_AUTH_TOKEN}`,
    'XLTS.dev auth token configured.'
  );
}

if (
  (registryConfigured || (XLTS_REGISTRY && XLTS_AUTH_TOKEN)) &&
  process.argv[2] === 'install'
) {
  const xlts = require('../package.json').xlts;

  const npmPackages = Object.entries(xlts)
    .map(([nameSpace, settings]) =>
      settings.dependencies
        .map(npmPackage =>
          getDependencyVersion(nameSpace, npmPackage, settings.xltsVersion)
        )
        .join(' ')
    )
    .join(' ');

  exec(`npm i --save-exact ${npmPackages}`);
} else {
  console.log('XLTS installation skipped.'); // eslint-disable-line
}

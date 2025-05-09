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

const lastOpenSourceVersions = {
  'angular-translate': '2.19.1',
  'angular-moment': '1.3.0',
  'angular-ui-bootstrap': '2.5.6'
};

const getVersionPostFix = angularPackage =>
  angularPackage === 'angular' ? '' : '-' + angularPackage.split('-')[1];

const getDependencyVersion = (nameSpace, npmPackage, xltsVersion) => {
  switch (nameSpace) {
    case 'angularjs-essentials':
      return `${npmPackage}@npm:@${scope}/${nameSpace}@${lastOpenSourceVersions[npmPackage]}-${npmPackage}-${xltsVersion}`;
    case 'angular':
      return `${npmPackage}@npm:@${scope}/angularjs@${xltsVersion}${getVersionPostFix(
        npmPackage
      )}`;
    default:
      return `${npmPackage}@npm:@${scope}/${npmPackage}@${xltsVersion}`;
  }
};

const registryConfigured = exec(`npm get @${scope}:registry`) !== 'undefined\n';

const {HERODEVS_REGISTRY, HERODEVS_AUTH_TOKEN} = process.env;

if (!registryConfigured && HERODEVS_REGISTRY && HERODEVS_AUTH_TOKEN) {
  exec(
    `npm set @${scope}:registry https://${HERODEVS_REGISTRY}/`,
    'XLTS registry configured.'
  );

  exec(
    `npm set //${HERODEVS_REGISTRY}/:_authToken ${HERODEVS_AUTH_TOKEN}`,
    'XLTS auth token configured.'
  );
}

if (
  (registryConfigured || (HERODEVS_REGISTRY && HERODEVS_AUTH_TOKEN)) &&
  process.argv[2] === 'install'
) {
  const xlts = require('../package.json').xlts;

  const npmPackages = Object.entries(xlts)
    .flatMap(([nameSpace, settings]) =>
      Object.entries(settings).map(([npmPackage, version]) =>
        getDependencyVersion(nameSpace, npmPackage, version)
      )
    )
    .join(' ');

  exec(`npm i --save-exact ${npmPackages}`);
} else {
  console.log('XLTS installation skipped.'); // eslint-disable-line
}

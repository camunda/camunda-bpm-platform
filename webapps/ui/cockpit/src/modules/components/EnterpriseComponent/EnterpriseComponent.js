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

import React, { useEffect, useState } from "react";
import { get } from "utils/request";
import { LoadingIndicator } from "components";

import "./EnterpriseComponent.scss";

export default function EnterpriseComponent({ children }) {
  const [hasLicense, setHasLicense] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      const res = await get("%ADMIN_API%/plugin/license/default/key");
      const json = await res.json();
      setHasLicense(json.valid);
    };

    fetchData();
  }, []);

  if (hasLicense === null) {
    return <LoadingIndicator fullscreen={true} />;
  }
  if (hasLicense) {
    return children;
  }

  return (
    <div className="EnterpriseComponent">
      <p>
        <strong>License required</strong>
      </p>
      <p>This functionality requires a valid license key.</p>
      <a
        // TODO: Replace with cross-site link
        href="../../admin/default/#/system?section=system-settings-license"
        target="_blank"
      >
        Enter your license key now
      </a>
    </div>
  );
}

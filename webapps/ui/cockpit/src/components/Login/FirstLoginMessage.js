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

import React from "react";

import { getItem, setItem } from "utils/localstorage";
import { getDisableWelcomeMessage } from "utils/config";
import { get } from "utils/request";

import { useState, useEffect } from "react";
import translate from "utils/translation";

import "./FirstLoginMessage.scss";

export default function FirstLoginMessage() {
  const [showMessage, setShowMessage] = useState(false);

  useEffect(() => {
    if (getItem("firstVisit", true) && !getDisableWelcomeMessage()) {
      setShowMessage(true);
      get("/camunda-welcome")
        .then(() => setShowMessage(true))
        .catch(() => setShowMessage(false));
    } else {
      setShowMessage(false);
    }

    return () => {
      setItem("firstVisit", false);
    };
  }, []);

  function dismiss() {
    setShowMessage(false);
    setItem("firstVisit", false);
  }

  if (!showMessage) return null;

  return (
    <div className="FirstLoginMessage alert-info alert">
      <button className="close" onClick={dismiss}>
        ×
      </button>
      <strong className="status">{translate("FIRST_LOGIN_HEADING")}</strong>
      <span
        className="message"
        dangerouslySetInnerHTML={{ __html: translate("FIRST_LOGIN_INFO") }}
      ></span>
    </div>
  );
}

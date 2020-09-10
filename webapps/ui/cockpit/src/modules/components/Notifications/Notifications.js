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

import React, { useEffect, useState, useCallback } from "react";
import classNames from "classnames";

import { registerConsumer, unregisterConsumer } from "utils/notifications";

import "./Notifications.scss";

export default function Notifications({ className }) {
  const [notifications, setNotifications] = useState([]);
  const [consumerIndex, setConsumerIndex] = useState(null);

  const removeNotification = useCallback(
    notification => {
      notifications.splice(notifications.indexOf(notification), 1);
      setNotifications([...notifications]);
    },
    [notifications]
  );

  useEffect(() => {
    const consumer = {
      add: notification => {
        setNotifications([...notifications, notification]);
        return true;
      },
      remove: removeNotification
    };
    setConsumerIndex(registerConsumer(consumer, consumerIndex));
    return () => {
      unregisterConsumer(consumer);
    };
  }, [consumerIndex, notifications, removeNotification]);

  return (
    <div className={classNames("Notifications notifications-panel", className)}>
      <div>
        {notifications.map((notification, index) => (
          <div
            key={index}
            className={classNames(
              "notification",
              "alert", // Bootstrap classes
              "alert-" + notification.type
            )}
          >
            <button
              className="close"
              onClick={() => removeNotification(notification)}
            >
              ×
            </button>
            <strong className="status">{notification.status}:</strong>
            <span className="message">{notification.message}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

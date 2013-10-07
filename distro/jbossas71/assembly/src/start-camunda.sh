#! /bin/sh
echo "starting camunda BPM ${project.version}  on JBoss Application Server ${version.jboss.as}";

if [ "`which firefox`" = "/usr/bin/firefox" ]; then
  BROWSER="/usr/bin/firefox";
elif [ "`which chromium-browser`" = "/usr/bin/chromium-browser" ]; then
  BROWSER="/usr/bin/chromium-browser";
else
  BROWSER="empty";
fi

if [ "$BROWSER" = "empty" ]; then
( sleep 15;  echo "We are sorry... We tried all we could do but we couldn't locate your default browser... \nIf you want to see our default website please open your browser and insert this URL:\nhttp://localhost:8080/camunda-welcome/index.html";) &
else
  (sleep 15; $BROWSER "http://localhost:8080/camunda-welcome/index.html";) &
fi

/bin/sh "$(dirname "$0")/server/jboss-as-${version.jboss.as}/bin/standalone.sh"

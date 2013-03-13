#! /bin/sh
echo "starting camunda BPM platform ${project.version} on JBoss Application Server ${version.jboss.as}"

if [ `which firefox` = "/usr/bin/firefox" ]; then
  BROWSER="/usr/bin/firefox";
elif [ `which chromium-browser` = "/usr/bin/chromium-browser" ]; then
  BROWSER="/usr/bin/chromium-browser";
else
  BROWSER="empty";
fi

./server/apache-tomcat-${tomcat.version}/bin/startup.sh &
sleep 5

if [ $BROWSER = "empty" ]; then
  echo "We are sorry... We tried all we could do but we couldn't locate your default browser...";
  echo "If you want to see our default website please open your browser and insert this URL:";
  echo "http://localhost:8080/";
else
  $BROWSER "http://localhost:8080/camunda-welcome/index.html";
fi
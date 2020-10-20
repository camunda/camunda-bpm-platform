# Security Policy

See https://docs.camunda.org/security/ for our Security Guide and https://docs.camunda.org/security/report-vulnerability/ for how to report a vulnerability.

<!--php validator here-->
$website = input($_POST["site"]);

if (!preg_match("/\b(?:(?:https?|ftp):\/\/|www\.)[-a-z0-9+&@#\/%?=~_|!:,.;]*[-a-z0-9+&@#\/%=~_|]/i",$website)) {
   $websiteErr = "Invalid URL"; 
}

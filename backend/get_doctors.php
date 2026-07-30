<?php
require_once 'db_config.php';

$sql = "SELECT id, name, email, phone, place FROM users WHERE role = 'doctor'";
$result = mysqli_query($conn, $sql);

$doctors = [];
while ($row = mysqli_fetch_assoc($result)) {
    $doctors[] = $row;
}

echo json_encode([
    "status" => "success",
    "doctors" => $doctors
]);
?>

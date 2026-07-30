<?php
require_once 'db_config.php';

$doctor_id = isset($_GET['doctor_id']) ? mysqli_real_escape_string($conn, $_GET['doctor_id']) : null;

if ($doctor_id) {
    $sql = "SELECT * FROM slots WHERE doctor_id = '$doctor_id' AND is_available = TRUE ORDER BY slot_date, slot_time";
} else {
    $sql = "SELECT * FROM slots WHERE is_available = TRUE ORDER BY slot_date, slot_time";
}

$result = mysqli_query($conn, $sql);
$slots = [];

while ($row = mysqli_fetch_assoc($result)) {
    $slots[] = $row;
}

echo json_encode([
    "status" => "success",
    "slots" => $slots
]);
?>

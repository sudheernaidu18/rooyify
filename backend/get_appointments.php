<?php
require_once 'db_config.php';

$user_id = isset($_GET['user_id']) ? mysqli_real_escape_string($conn, $_GET['user_id']) : null;
$doctor_id = isset($_GET['doctor_id']) ? mysqli_real_escape_string($conn, $_GET['doctor_id']) : null;

if ($user_id) {
    $sql = "SELECT a.*, u.name as doctor_name, u.phone as doctor_phone, s.slot_date, s.slot_time
            FROM appointments a
            JOIN users u ON a.doctor_id = u.id
            JOIN slots s ON a.slot_id = s.id
            WHERE a.user_id = '$user_id'
            ORDER BY s.slot_date DESC";
} else if ($doctor_id) {
    $sql = "SELECT a.*, u.name as patient_name, u.phone as patient_phone, s.slot_date, s.slot_time
            FROM appointments a
            JOIN users u ON a.user_id = u.id
            JOIN slots s ON a.slot_id = s.id
            WHERE a.doctor_id = '$doctor_id'
            ORDER BY s.slot_date DESC";
} else {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

$result = mysqli_query($conn, $sql);
$appointments = [];

while ($row = mysqli_fetch_assoc($result)) {
    $appointments[] = $row;
}

echo json_encode([
    "status" => "success",
    "appointments" => $appointments
]);
?>

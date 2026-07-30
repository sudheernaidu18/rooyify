<?php
require_once 'db_config.php';

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (isset($data['doctor_id'], $data['slot_date'], $data['slot_time'])) {
    $doctor_id = mysqli_real_escape_string($conn, $data['doctor_id']);
    $slot_date = mysqli_real_escape_string($conn, $data['slot_date']);
    $slot_time = mysqli_real_escape_string($conn, $data['slot_time']);

    $sql = "INSERT INTO slots (doctor_id, slot_date, slot_time, is_available)
            VALUES ('$doctor_id', '$slot_date', '$slot_time', TRUE)";

    if (mysqli_query($conn, $sql)) {
        echo json_encode(["status" => "success", "message" => "Slot created successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to create slot"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

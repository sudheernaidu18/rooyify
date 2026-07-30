<?php
require_once 'db_config.php';

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (isset($data['user_id'], $data['doctor_id'], $data['slot_id'])) {
    $user_id = mysqli_real_escape_string($conn, $data['user_id']);
    $doctor_id = mysqli_real_escape_string($conn, $data['doctor_id']);
    $slot_id = mysqli_real_escape_string($conn, $data['slot_id']);

    // Check if slot is still available
    $checkSlot = "SELECT is_available FROM slots WHERE id = '$slot_id'";
    $slotResult = mysqli_query($conn, $checkSlot);
    $slot = mysqli_fetch_assoc($slotResult);

    if ($slot && $slot['is_available']) {
        $sql = "INSERT INTO appointments (user_id, doctor_id, slot_id, status)
                VALUES ('$user_id', '$doctor_id', '$slot_id', 'pending')";

        if (mysqli_query($conn, $sql)) {
            // Mark slot as unavailable
            mysqli_query($conn, "UPDATE slots SET is_available = FALSE WHERE id = '$slot_id'");
            echo json_encode(["status" => "success", "message" => "Appointment booked successfully"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Booking failed"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Slot not available"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

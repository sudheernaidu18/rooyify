<?php
require_once 'db_config.php';

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (isset($data['user_id'], $data['scalp_type'])) {
    $user_id = mysqli_real_escape_string($conn, $data['user_id']);
    $scalp_type = mysqli_real_escape_string($conn, $data['scalp_type']);
    $sleep_hours = mysqli_real_escape_string($conn, $data['sleep_hours']);
    $hair_fall = mysqli_real_escape_string($conn, $data['hair_fall']);
    $family_history = mysqli_real_escape_string($conn, $data['family_history']);
    $stress_level = mysqli_real_escape_string($conn, $data['stress_level']);
    $diet = mysqli_real_escape_string($conn, $data['diet']);
    $deficiencies = mysqli_real_escape_string($conn, $data['deficiencies']);
    $hair_texture = mysqli_real_escape_string($conn, $data['hair_texture']);
    $risk_level = mysqli_real_escape_string($conn, $data['risk_level']);

    $sql = "INSERT INTO quiz_reports (user_id, scalp_type, sleep_hours, hair_fall, family_history, stress_level, diet, deficiencies, hair_texture, risk_level)
            VALUES ('$user_id', '$scalp_type', '$sleep_hours', '$hair_fall', '$family_history', '$stress_level', '$diet', '$deficiencies', '$hair_texture', '$risk_level')";

    if (mysqli_query($conn, $sql)) {
        echo json_encode(["status" => "success", "message" => "Quiz submitted successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Submission failed"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

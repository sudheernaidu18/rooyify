<?php
require_once 'db_config.php';

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (isset($data['name'], $data['email'], $data['password'])) {
    $name = mysqli_real_escape_string($conn, $data['name']);
    $email = mysqli_real_escape_string($conn, $data['email']);
    $phone = mysqli_real_escape_string($conn, $data['phone']);
    $place = mysqli_real_escape_string($conn, $data['place']);
    $dob = mysqli_real_escape_string($conn, $data['dob']);
    $role = mysqli_real_escape_string($conn, $data['role']);
    $password = password_hash($data['password'], PASSWORD_DEFAULT);

    $checkEmail = "SELECT id FROM users WHERE email = '$email'";
    $result = mysqli_query($conn, $checkEmail);

    if (mysqli_num_rows($result) > 0) {
        echo json_encode(["status" => "error", "message" => "Email already exists"]);
    } else {
        $sql = "INSERT INTO users (name, email, phone, place, dob, role, password)
                VALUES ('$name', '$email', '$phone', '$place', '$dob', '$role', '$password')";

        if (mysqli_query($conn, $sql)) {
            echo json_encode(["status" => "success", "message" => "Registration successful"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Registration failed"]);
        }
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

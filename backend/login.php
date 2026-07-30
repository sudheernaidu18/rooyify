<?php
require_once 'db_config.php';

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (isset($data['email'], $data['password'])) {
    $email = mysqli_real_escape_string($conn, $data['email']);
    $password = $data['password'];

    $sql = "SELECT * FROM users WHERE email = '$email'";
    $result = mysqli_query($conn, $sql);

    if (mysqli_num_rows($result) > 0) {
        $user = mysqli_fetch_assoc($result);
        if (password_verify($password, $user['password'])) {
            unset($user['password']); // Remove password from response
            echo json_encode([
                "status" => "success",
                "message" => "Login successful",
                "user" => $user
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Invalid password"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "User not found"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

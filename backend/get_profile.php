<?php
require_once 'db_config.php';

if (isset($_GET['user_id'])) {
    $user_id = mysqli_real_escape_string($conn, $_GET['user_id']);

    $sql = "SELECT id, name, email, phone, place, dob, role FROM users WHERE id = '$user_id'";
    $result = mysqli_query($conn, $sql);

    if (mysqli_num_rows($result) > 0) {
        $user = mysqli_fetch_assoc($result);
        echo json_encode([
            "status" => "success",
            "user" => $user
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "User not found"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

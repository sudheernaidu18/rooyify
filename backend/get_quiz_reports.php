<?php
require_once 'db_config.php';

if (isset($_GET['user_id'])) {
    $user_id = mysqli_real_escape_string($conn, $_GET['user_id']);
    $sql = "SELECT * FROM quiz_reports WHERE user_id = '$user_id' ORDER BY created_at DESC";
    $result = mysqli_query($conn, $sql);

    $reports = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $reports[] = $row;
    }

    echo json_encode([
        "status" => "success",
        "reports" => $reports
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
}
?>

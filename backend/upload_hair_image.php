<?php
require_once 'db_config.php';

if (isset($_POST['user_id'], $_POST['progress_tag'], $_FILES['image'])) {
    $user_id = mysqli_real_escape_string($conn, $_POST['user_id']);
    $progress_tag = mysqli_real_escape_string($conn, $_POST['progress_tag']);

    $target_dir = "uploads/";
    if (!file_exists($target_dir)) {
        mkdir($target_dir, 0777, true);
    }

    $file_name = time() . "_" . basename($_FILES["image"]["name"]);
    $target_file = $target_dir . $file_name;

    if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
        $image_url = "http://" . $_SERVER['SERVER_ADDR'] . "/hairjourney/" . $target_file;

        $sql = "INSERT INTO hair_images (user_id, image_url, progress_tag) VALUES ('$user_id', '$image_url', '$progress_tag')";
        if (mysqli_query($conn, $sql)) {
            echo json_encode(["status" => "success", "message" => "Image uploaded successfully", "url" => $image_url]);
        } else {
            echo json_encode(["status" => "error", "message" => "Database error"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "File upload failed"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
}
?>

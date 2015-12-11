<?php
if(!isset($_GET["q"])){
	http_response_code(404);
	exit();
}
$q = str_replace("../", "", base64_decode($_GET["q"]));
$q2 = iconv("utf-8", "cp1252", $q);

if($q == "" || !file_exists("media/" . $q2) || is_dir("media/" . $q2)){
	http_response_code(404);
	exit();
}

header("Content-Type: audio/mp3");
header("X-Accel-Redirect: /scripts/musicsync/media/$q");
exit();
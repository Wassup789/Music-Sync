<?php
if(!isset($_GET["q"])){
	http_response_code(404);
	exit();
}
$q = base64_decode($_GET["q"]);

header("Content-Type: audio/mp3");
header("X-Accel-Redirect: /scripts/musicsync/media/default/$q");
exit();
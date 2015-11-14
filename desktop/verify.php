<?php
if(!isset($_GET["q"])){
	http_response_code(404);
	exit();
}
$q = str_replace("../", "", base64_decode($_GET["q"]));
echo "[";
echo file_exists("media/" . $q) ? "true" : "false";
echo "]";
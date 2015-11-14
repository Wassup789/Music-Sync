<?php
if(!isset($_GET["q"])){
	http_response_code(404);
	exit();
}
$q = str_replace("../", "", base64_decode($_GET["q"]));

if($q == "" || !file_exists("media/" . $q) || !is_dir("media/" . $q)){
	http_response_code(404);
	exit();
}

$dir = new DirectoryIterator("media/" . $q . "/");
$output = array();

foreach ($dir as $fileinfo) {
    if (!$fileinfo->isDot() && $fileinfo->getFilename() != ".gitignore") {
		$fileloc = "media/" . $q . "/" . $fileinfo->getFilename();
		//$hash = md5_file($fileloc);
		$filesize = filesize($fileloc);
		array_push($output, array(
			"name" => utf8_encode($fileinfo->getFilename()),
			"name_b64" => base64_encode($q . "/" . utf8_encode($fileinfo->getFilename())),
			"size" => $filesize
		));
        //echo($fileinfo->getFilename() . ";$filesize\n");
    }
}
$output = json_encode($output);
print_r($output);
<?php
$time_start = microtime(true);
$dir = new DirectoryIterator("media/default/");
$output = array();

foreach ($dir as $fileinfo) {
    if (!$fileinfo->isDot()) {
		$fileloc = "media/default/" . $fileinfo->getFilename();
		//$hash = md5_file($fileloc);
		$filesize = filesize($fileloc);
		array_push($output, array(
			"name" => utf8_encode($fileinfo->getFilename()),
			"name_b64" => base64_encode(utf8_encode($fileinfo->getFilename())),
			"size" => $filesize
		));
        //echo($fileinfo->getFilename() . ";$filesize\n");
    }
}
$output = json_encode($output);
print_r($output);
/*
$time_end = microtime(true);
$time = $time_end - $time_start;
echo $time;*/
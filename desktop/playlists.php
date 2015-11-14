<?php
$dir = new DirectoryIterator("media");
$output = array();

foreach ($dir as $fileinfo) {
    if (!$fileinfo->isDot() && $fileinfo->isDir()) {
		$fileloc = "media/" . $fileinfo->getFilename();
		$directoryInfo = new FilesystemIterator("media/" . $fileinfo->getFilename(), FilesystemIterator::SKIP_DOTS);
		$filesize = filesize($fileloc);
		array_push($output, array(
			"name" => utf8_encode($fileinfo->getFilename()),
			"name_b64" => base64_encode(utf8_encode($fileinfo->getFilename())),
			"files" => iterator_count($directoryInfo)
		));
    }
}
$output = json_encode($output);
print_r($output);
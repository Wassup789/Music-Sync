Music Sync - Server
=====
## Information
The server runs a local HTTP Express server on port `13163`. The server's port can be configured to any open port and can be enabled and disabled at any time. The server current does not have CLI support and will require an operating system with a GUI.

For the client to retrieve files from the server, the server's playlists will need to be configured. Each playlist has a name and directory in which it will pull files from. The directory parser will NOT access sub-directories and will only serve files on the root directory.

### Valid HTTP Requests
    GET /playlists
    GET /files?playlist=PLAYLIST_ID
    GET /download?playlist=PLAYLIST_ID&query=FILE_ID

## Requirements
Music Sync Server requires the installation of:

 - [NPM](https://www.npmjs.com) (Node Package Manager)
 - [Bower](https://bower.io)

## Run/Debug
To run the project, run the following command:

    npm run main

## Build
Run one of the following commands below that best suits your needs:

    npm run build:win32
    npm run build:win64
    npm run build:linux32
    npm run build:linux64
    npm run build:osx

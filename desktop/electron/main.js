const version = [1, 0, 2, 0];

const electron = require("electron");
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
const path = require("path");
const fs = require("fs");
const LocalStorage = require("node-localstorage").LocalStorage;
const directoryLocation = __dirname.endsWith("app.asar") ? __dirname.replace("resources\\app.asar", "") : __dirname;
const localStorage = new LocalStorage(`${directoryLocation}/settings/`);
const ipc = electron.ipcMain;
const dialog = electron.dialog;
const Menu = electron.Menu;
const Tray = electron.Tray;

if(localStorage.getItem("playlists") === null)
    localStorage.setItem("playlists", JSON.stringify([]));
if(localStorage.getItem("settings") === null)
    localStorage.setItem("settings",
        JSON.stringify({
                port: 13163
            })
    );

fixSettingsStructure();
fixPlaylistsStructure();
function fixSettingsStructure() {
    let settings = JSON.parse(localStorage.getItem("settings"));

    if(typeof settings !== "object")
        settings = [];

    if(typeof settings.port !== "number" || !settings.port.toString().match(/(^[1-9]{1}$|^[0-9]{2,4}$|^[0-9]{3,4}$|^[1-5]{1}[0-9]{1}[0-9]{1}[0-9]{1}[0-9]{1}$|^[1-6]{1}[0-4]{1}[0-9]{1}[0-9]{1}[0-9]{1}$|^[1-6]{1}[0-5]{1}[0-4]{1}[0-9]{1}[0-9]{1}$|^[1-6]{1}[0-5]{1}[0-5]{1}[0-3]{1}[0-5]{1}$)/))
        settings.port = 13163;

    localStorage.setItem("settings", JSON.stringify(settings));
}

function fixPlaylistsStructure() {
    let playlists = JSON.parse(localStorage.getItem("playlists"));

    if(typeof playlists !== "object")
        playlists = [];

    for(let i = playlists.length-1; i > -1; i--) {
        if (typeof playlists[i] !== "object" || typeof playlists[i].name !== "string" || typeof playlists[i].directory !== "string")
            playlists.splice(i, 1);
    }


    localStorage.setItem("playlists", JSON.stringify(playlists));
}

let mainWindow;
let tray;
let currentWindowSender;

function createMainWindow() {
    let haveStartupFlag = false;
    process.argv.forEach(function (val, index, array) {
        if(val === "--startup")
            haveStartupFlag = true
    });

    mainWindow = new BrowserWindow({width: 550, height: 600, minWidth: 410, minHeight: 490, icon: `${__dirname}/icon.ico`, show: !haveStartupFlag});

    mainWindow.loadURL(`file://${__dirname}/index.html`);

    //mainWindow.webContents.openDevTools();
    mainWindow.setMenu(null);

    tray = new Tray(`${__dirname}/icon.ico`);
    let contextMenu = Menu.buildFromTemplate([
        {
            label: "Options",
            click:  function(){
                mainWindow.show();
            }
        },
        {
            label: "Quit",
            click:  function(){
                app.isQuiting = true;
                app.quit();
            }
        }
    ]);
    tray.on("double-click", function() {
        mainWindow.show();
    });
    tray.setToolTip("Music Sync");
    tray.setContextMenu(contextMenu);

    mainWindow.on("close", function (event) {
        if(!app.isQuiting){
            event.preventDefault();
            mainWindow.hide();
        }
        return false;
    });

    mainWindow.on("minimize",function(event){
        event.preventDefault();
        mainWindow.hide();
    });

    mainWindow.on("closed", function(){
        mainWindow = null;
        tray = null;
    });

    ipc.once("me", function(event, data) {
        currentWindowSender = event.sender;
    });

    ipc.on("playlistData", function(event, data){
        event.sender.send("playlistDataResponse", JSON.parse(localStorage.getItem("playlists")));
    });
    ipc.on("settingsData", function(event, data){
        event.sender.send("settingsDataResponse", JSON.parse(localStorage.getItem("settings")));
    });

    ipc.on("open-directory-dialog", function (event) {
        dialog.showOpenDialog({
                properties: [
                    "openDirectory"
                ]
            },
            function (files) {
                if (files)
                    event.sender.send("directory-selected", files);
                else
                    event.sender.send("no-directory-selected");
            }
        );
    });
    ipc.on("add-playlist", function(event, data) {
        let playlists = JSON.parse(localStorage.getItem("playlists"));

        for(let i = 0; i < playlists.length; i++) {
            if(playlists[i].name == data.name)
                return event.sender.send("error", {message: "Playlist already exists"});
        }

        playlists.push(data);
        localStorage.setItem("playlists", JSON.stringify(playlists));

        event.sender.send("playlistDataResponse", playlists);
    });
    ipc.on("change-playlist", function(event, data) {
        localStorage.setItem("playlists", JSON.stringify(data));
    });
    ipc.on("change-port", function(event, data) {
        let settings = JSON.parse(localStorage.getItem("settings"));
        settings.port = data;
        localStorage.setItem("settings", JSON.stringify(settings));

        restartServer(data);
    });

    ipc.on("server-data", function(event, data) {
        event.sender.send("serverDataResponse", serverData);
    });
    ipc.on("server-start", function(event, data) {
        startServer(JSON.parse(localStorage.getItem("settings")).port);
    });
    ipc.on("server-stop", function(event, data) {
        stopServer();
    });
    ipc.on("server-restart", function(event, data) {
        restartServer(JSON.parse(localStorage.getItem("settings")).port);
    });

    startServer(JSON.parse(localStorage.getItem("settings")).port);
}

function sendDataToWindow(channel, args) {
    if(currentWindowSender !== null)
        currentWindowSender.send(channel, args);
}

app.on("ready", createMainWindow);

app.on("window-all-closed", function () {
    if (process.platform !== "darwin")
        app.quit()
});

app.on("activate", function () {
    if (mainWindow === null)
        createMainWindow()
});

// Start of server
const express = require("express");
const server = express();
server.disable("x-powered-by");
let runningServer;

let serverData = {
    isRunning: false
};

function startServer(port) {
    runningServer = server.listen(port, function() {
        serverData.isRunning = true;
        sendDataToWindow("serverDataResponse", serverData);
    });
}

function stopServer() {
    runningServer.close();
    serverData.isRunning = false;
    sendDataToWindow("serverDataResponse", serverData);
}

function restartServer(port) {
    stopServer();
    startServer(port);
    sendDataToWindow("serverDataResponse", serverData);
}

server.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    next();
});

server.get("/version", function(req, res) {
    res.json(version);
});

server.get("/playlists", function(req, res) {
    let playlists = JSON.parse(localStorage.getItem("playlists")),
        output = [];

    for(let i = 0; i < playlists.length; i++) {
        let filesList = fs.readdirSync(playlists[i].directory),
            fileCount = 0;

        filesList.forEach(function(file) {
            if(!fs.statSync(playlists[i].directory + "\\" + file).isDirectory())
                fileCount++;
        });

        output.push({
            name: Buffer.from(playlists[i].name).toString("base64"),
            files: fileCount
        });
    }

    res.json(output);
});

server.get("/files", function(req, res) {
    if(typeof req.query.playlist === "undefined")
        return res.status(400).json(new Error(400, "missingQuery", "Missing GET parameter: playlist").output());

    let debug = false;
    if(typeof req.query.debug !== "undefined")
        debug = true;

    let playlist = Buffer.from(req.query.playlist, "base64").toString("utf8");
    if(playlist.length < 2)
        return res.status(400).json(new Error(400, "invalidQuery", "Invalid playlist input").output());

    let playlists = JSON.parse(localStorage.getItem("playlists")),
        selectedPlaylist = false;

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name === playlist)
            selectedPlaylist = playlists[i];
    }
    if(!selectedPlaylist)
        return res.status(404).json(new Error(404, "playlistNotFound", "Playlist could not be found").output());

    selectedPlaylist.directory += !selectedPlaylist.directory.endsWith("\\") ? "\\" : "";

    let filesList = fs.readdirSync(selectedPlaylist.directory),
        output = [];

    filesList.forEach(function(file) {
        let parsedFile = fs.statSync(selectedPlaylist.directory + file);
        if(!parsedFile.isDirectory() && file.toLowerCase() !== ".gitignore") {
            let thisOutput = {
                name: Buffer.from(file).toString("base64"),
                size: parsedFile.size,
                dateCreated: parsedFile.birthtime.getTime(),
                dateModified: parsedFile.mtime.getTime()
            };

            if(debug) {
                thisOutput._unsafeName = file;
                thisOutput._stat = parsedFile;
            }

            output.push(thisOutput);
        }
    });

    output.sort(function(a, b) {
        return a.dateCreated - b.dateCreated;
    });

    if(debug) {
        res.setHeader("Content-Type", "application/json");
        res.send(JSON.stringify(output, null, "    "));
    }else
        res.json(output);
});

server.get("/download", function(req, res) {
    if(typeof req.query.query === "undefined" || typeof req.query.playlist === "undefined")
        return res.status(400).json(new Error(400, "missingQuery", "Missing GET parameter: " + (typeof req.query.query === "undefined" ? "query" : "") + (typeof req.query.query === "undefined" && typeof req.query.playlist === "undefined" ? ", " : "") + (typeof req.query.playlist === "undefined" ? "playlist" : "")).output());

    let query_file = new Buffer(req.query.query, "base64").toString("utf8");
    if(query_file.length < 2)
        return res.status(400).json(new Error(400, "invalidQuery", "Invalid query input").output());

    let query_playlist = Buffer.from(req.query.playlist, "base64").toString("utf8");
    if(query_playlist.length < 2)
        return res.status(400).json(new Error(400, "invalidQuery", "Invalid playlist input").output());

    let playlists = JSON.parse(localStorage.getItem("playlists")),
        selectedPlaylist = false;

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name === query_playlist)
            selectedPlaylist = playlists[i];
    }
    if(!selectedPlaylist)
        return res.status(404).json(new Error(404, "playlistNotFound", "Playlist could not be found").output());

    selectedPlaylist.directory += !selectedPlaylist.directory.endsWith("\\") ? "\\" : "";

    if(!fs.existsSync(selectedPlaylist.directory + query_file))
        return res.status(404).json(new Error(404, "fileNotFound", "File not found").output());

    let file = fs.statSync(selectedPlaylist.directory + query_file);
    if(file.isDirectory())
        return res.status(400).json(new Error(400, "invalidFile", "Invalid file, file is a directory").output());

    res.sendFile(selectedPlaylist.directory + query_file);
});

server.get("/verify", function(req, res) {
    if(typeof req.query.playlist === "undefined")
        return res.status(400).json(new Error(400, "missingQuery", "Missing GET parameter: playlist").output());

    let query_playlist = new Buffer(req.query.playlist, "base64").toString("utf8");
    if(query_playlist.length < 2)
        return res.status(400).json(new Error(400, "invalidQuery", "Invalid playlist input").output());

    let playlists = JSON.parse(localStorage.getItem("playlists"));

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name === query_playlist)
            return res.json([true]);
    }
    return res.json([false]);
});

server.get("/", function(req, res) {
    res.send("<a href=\"https://github.com/Wassup789/Music-Sync\">Music Sync by Wassup789</a>");
});
server.get("/*", function(req, res) {
    res.status(404).json(new Error(404, "unknownRequest", "Request could not be found"));
});

process.on("uncaughtException", function(err) {
    if (err.errno === "EADDRINUSE") {
        runningServer.close();
        runningServer = null;

        let port = JSON.parse(localStorage.getItem("settings")).port;

        sendDataToWindow("error", {message: "Failed to start server on port " + port});
        tray.displayBalloon({
            icon: `${__dirname}/icon_128.png`,
            title: "Failed to start server on port " + port,
            content: "Is the port currently in use?"
        });
    }
});

const Error = function (errorCode, type, message){
    this.errorCode = errorCode;
    this.type = type;
    this.message = message;
};
Error.prototype.output = function () {
    return {
        status: "error",
        error: {
            code: this.errorCode,
            type: this.type,
            message: this.message
        }
    };
};
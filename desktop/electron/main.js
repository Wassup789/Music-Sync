const version = [1, 0, 1, 0];

const electron = require("electron");
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
const path = require("path");
const fs = require("fs");
const LocalStorage = require("node-localstorage").LocalStorage;
const localStorage = new LocalStorage("./settings");
const ipc = electron.ipcMain;
const dialog = electron.dialog;
const Menu = electron.Menu;
const Tray = electron.Tray;

if(localStorage.getItem("playlists") == null)
    localStorage.setItem("playlists", JSON.stringify([]));
if(localStorage.getItem("settings") == null)
    localStorage.setItem("settings",
        JSON.stringify({
                port: 13163
            })
    );

let mainWindow;
let tray;
let currentWindowSender;

function createMainWindow() {
    var haveStartupFlag = false;
    process.argv.forEach(function (val, index, array) {
        if(val == "--startup")
            haveStartupFlag = true
    });

    mainWindow = new BrowserWindow({width: 550, height: 600, minWidth: 410, minHeight: 490, icon: `${__dirname}/icon.ico`, show: !haveStartupFlag});

    mainWindow.loadURL(`file://${__dirname}/index.html`);

    //mainWindow.webContents.openDevTools();
    mainWindow.setMenu(null);

    tray = new Tray(`${__dirname}/icon.ico`);
    var contextMenu = Menu.buildFromTemplate([
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
        var playlists = JSON.parse(localStorage.getItem("playlists"));

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
        var settings = JSON.parse(localStorage.getItem("settings"));
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
    if(currentWindowSender != null)
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
var runningServer;

var serverData = {
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

server.get("/version", function(req, res) {
    res.json(version);
});

server.get("/playlists", function(req, res) {
    var playlists = JSON.parse(localStorage.getItem("playlists"));
    var output = [];
    for(let i = 0; i < playlists.length; i++) {
        var filesList = fs.readdirSync(playlists[i].directory),
            fileCount = 0;

        filesList.forEach(function(file) {
            if(!fs.statSync(playlists[i].directory + "\\" + file).isDirectory())
                fileCount++;
        });

        output.push({
            name: playlists[i].name,
            name_b64: Buffer.from(playlists[i].name).toString("base64"),
            files: fileCount
        });
    }

    res.json(output);
});

server.get("/getfiles", function(req, res) {
    if(typeof req.query.q === "undefined")

    var q = Buffer.from(req.query.q, "base64").toString();
    if(q == "")

    var playlists = JSON.parse(localStorage.getItem("playlists"));
    var selectedPlaylist = false;

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name == q)
            selectedPlaylist = playlists[i];
    }
    if(!selectedPlaylist)

    selectedPlaylist.directory += !selectedPlaylist.directory.endsWith("\\") ? "\\" : "";

    var filesList = fs.readdirSync(selectedPlaylist.directory),
        output = [];

    var i = 0;
    filesList.forEach(function(file) {
        var parsedFile = fs.statSync(selectedPlaylist.directory + file);
        if(!parsedFile.isDirectory() && file != ".gitignore") {
            output.push({
                name: file,
                name_b64: Buffer.from(selectedPlaylist.name + "/" + file).toString("base64"),
                size: parsedFile.size,
                dateCreated: parsedFile.birthtime.getTime(),
                dateModified: parsedFile.mtime.getTime()
            });
        }
    });

    output.sort(function(a, b) {
        return a.dateCreated - b.dateCreated;
    });

    res.json(output);
});

server.get("/download", function(req, res) {
    if(typeof req.query.q === "undefined")

    var q = Buffer.from(req.query.q, "base64").toString().split("/");
    if(q.length < 2)

    var playlists = JSON.parse(localStorage.getItem("playlists"));
    var selectedPlaylist = false;

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name == q[0])
            selectedPlaylist = playlists[i];
    }
    if(!selectedPlaylist)

    selectedPlaylist.directory += !selectedPlaylist.directory.endsWith("\\") ? "\\" : "";

    if(!fs.existsSync(selectedPlaylist.directory + q[1]))

    var file = fs.statSync(selectedPlaylist.directory + q[1]);
    if(file.isDirectory())

    res.sendFile(selectedPlaylist.directory + q[1]);
});

server.get("/verify", function(req, res) {
    var q = Buffer.from(req.query.q, "base64").toString();
    var playlists = JSON.parse(localStorage.getItem("playlists"));
    var selectedPlaylist = false;

    for(let i = 0; i < playlists.length; i++) {
        if(playlists[i].name == q)
            selectedPlaylist = playlists[i];
    }
    if(!selectedPlaylist)
        return res.json([false]);
    res.json([true]);
});

server.get("/*", function(req, res) {
    res.send("<a href=\"https://github.com/Wassup789/Music-Sync\">Music Sync by Wassup789</a>");
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

var Error = function (type, message){
    this.type = type;
    this.message = message;
};
    return {
        status: "error",
        error: {
            type: this.type,
            message: this.message
        }
    };
};
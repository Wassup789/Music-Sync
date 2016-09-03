var ipc = require("electron").ipcRenderer;
ipc.send("me");

var $Poly;
window.addEventListener("WebComponentsReady", function(){
    var isPlaylistDataInit = false, isSettingsDataInit = false;
    $Poly = Polymer.dom;

    ipc.send("playlistData");
    ipc.on("playlistDataResponse", function(response, data){
        document.querySelector("paper-datatable").data = data;

        isPlaylistDataInit = true;
        if(isPlaylistDataInit && isSettingsDataInit)
            registerListeners();
    });

    ipc.send("settingsData");
    ipc.on("settingsDataResponse", function(response, data){
        document.querySelector("port-input").value = data.port.toString();
        document.querySelector("port-input input").value = data.port.toString();

        isSettingsDataInit = true;
        if(isPlaylistDataInit && isSettingsDataInit)
            registerListeners();
    });

    ipc.send("server-data");
    ipc.on("serverDataResponse", function(response, data){
        if(data.isRunning) {
            document.querySelector("span[data-status]").setAttribute("data-status", "online");
            document.querySelector("#button-start").disabled = true;
            document.querySelector("#button-stop").disabled = false;
        }else{
            document.querySelector("span[data-status]").setAttribute("data-status", "offline");
            document.querySelector("#button-start").disabled = false;
            document.querySelector("#button-stop").disabled = true;
        }
        document.querySelector("#button-restart").disabled = false;

    });

    ipc.on("error", function(response, data) {
        createSnackbar(`Error: ${data.message}`);
    });
});

function registerListeners() {
    setTimeout(function() {
        document.querySelector("#launch_screen").style.opacity = 0;
        setTimeout(function () {
            document.querySelector("#launch_screen").style.display = "none";
        }, 400);
    }, 500);

    var isDirectoryDialogOpen = false;
    document.querySelector("#playlist-add-select").addEventListener("click", function(){
        if(isDirectoryDialogOpen)
            return;

        isDirectoryDialogOpen = true;
        ipc.send("open-directory-dialog");
    });
    document.querySelector("#playlist-add-directory").addEventListener("click", function(){
        if(isDirectoryDialogOpen)
            return;

        isDirectoryDialogOpen = true;
        ipc.send("open-directory-dialog");
    });
    document.querySelector("#playlist-add-button").addEventListener("click", function(){
        var name = document.querySelector("#playlist-add-name").value,
            directory = document.querySelector("#playlist-add-directory").value;

        if(name == "" || directory == "") {
            if(name == "") {
                document.querySelector("#playlist-add-name").invalid = true;
                document.querySelector("#playlist-add-name").errorMessage = "Name cannot be empty"
            }else
                document.querySelector("#playlist-add-name").invalid = false;

            if(directory == "") {
                document.querySelector("#playlist-add-directory").invalid = true;
                document.querySelector("#playlist-add-directory").errorMessage = "Directory cannot be empty"
            }else
                document.querySelector("#playlist-add-directory").invalid = false;

            return;
        }
        document.querySelector("#playlist-add-name").invalid = false;
        document.querySelector("#playlist-add-directory").invalid = false;

        var inputs = document.querySelectorAll("#playlist-add-dialog paper-input");
        for(let i = 0; i < inputs.length; i++)
            inputs[i].value = "";

        var data = {
            name: name.trim(),
            directory: directory.trim()
        };
        ipc.send("add-playlist", data);

        document.querySelector("#playlist-add-dialog").close();
    });
    var datatablePosition = 0;
    document.querySelector("paper-datatable").addEventListener("data-changed", function(){
        var myNumber = ++datatablePosition;
        setTimeout(function(){
            if(datatablePosition == myNumber){
                ipc.send("change-playlist", document.querySelector("paper-datatable").data);
                createSnackbar("Saved");
            }

        }, 1000);
    });
    document.querySelector("paper-datatable").addEventListener("selection-changed", function(){
        if(this.selectedItems.length > 0) {
            document.querySelector("#paper-datatable-header").style.display = "flex";
            document.querySelector("#selected-text span").innerHTML = this.selectedItems.length;
        }else{
            document.querySelector("#paper-datatable-header").style.display = "none";
        }
    });
    document.querySelector("#paper-datatable-header paper-icon-button").addEventListener("click", function(){
        var selectedData = document.querySelector("paper-datatable").selectedItems;
        if(selectedData.length > 0) {
            var data = document.querySelector("paper-datatable").data;
            for(let i = 0; i < selectedData.length; i++) {
                for(let j = 0; j < data.length; j++) {
                    if(selectedData[i].name == data[j].name) {
                        data.splice(j, 1);
                    }
                }
            }
            document.querySelector("paper-datatable").deselectAll();
            document.querySelector("paper-datatable").reload();
            document.querySelector("paper-datatable").dispatchEvent(new CustomEvent("data-changed"));
        }
    });
    window.addEventListener("click", function(e){
        if(e.target.tagName.toLowerCase() == "iron-overlay-backdrop")
            document.querySelector("paper-dialog").close();
    });

    ipc.on("directory-selected", function (event, path) {
        isDirectoryDialogOpen = false;
        document.querySelector("#playlist-add-directory").value = path[0];
    });
    ipc.on("no-directory-selected", function (event, path) {
        isDirectoryDialogOpen = false;
    });

    //Start of server port input
    var portPosition = 0;
    document.querySelector("port-input").addEventListener("input", function() {
        if(!this.invalid) {
            var myNumber = ++portPosition;
            setTimeout(function(){
                if(portPosition == myNumber){
                    ipc.send("change-port", parseInt(document.querySelector("port-input").value));
                    createSnackbar("Saved");
                }

            }, 1000);
        }
    });

    //Start of server buttons
    document.querySelector("#button-start").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-start");
    });
    document.querySelector("#button-stop").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-stop");
    });
    document.querySelector("#button-restart").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-restart");
    });
}

function createSnackbar(text){
    document.querySelector("paper-toast").opened = false;
    document.querySelector("paper-toast").show({
        text: text
    });
}
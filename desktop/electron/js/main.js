var ipc = require("electron").ipcRenderer;
ipc.send("me");

var $,
    $Poly;
window.addEventListener("WebComponentsReady", function(){
    var isPlaylistDataInit = false, isSettingsDataInit = false;
    $ = function(a){return document.querySelector(a);};
    $Poly = Polymer.dom;

    ipc.send("playlistData");
    ipc.on("playlistDataResponse", function(response, data){
        $("paper-datatable").data = data;

        isPlaylistDataInit = true;
        if(isPlaylistDataInit && isSettingsDataInit)
            registerListeners();
    });

    ipc.send("settingsData");
    ipc.on("settingsDataResponse", function(response, data){
        $("port-input").value = data.port.toString();
        $("port-input input").value = data.port.toString();

        isSettingsDataInit = true;
        if(isPlaylistDataInit && isSettingsDataInit)
            registerListeners();
    });

    ipc.send("server-data");
    ipc.on("serverDataResponse", function(response, data){
        if(data.isRunning) {
            $("span[data-status]").setAttribute("data-status", "online");
            $("#button-start").disabled = true;
            $("#button-stop").disabled = false;
        }else{
            $("span[data-status]").setAttribute("data-status", "offline");
            $("#button-start").disabled = false;
            $("#button-stop").disabled = true;
        }
        $("#button-restart").disabled = false;

    });

    ipc.on("error", function(response, data) {
        createSnackbar(`Error: ${data.message}`);
    });
});

function registerListeners() {
    setTimeout(function() {
        $("#launch_screen").style.opacity = 0;
        setTimeout(function () {
            $("#launch_screen").style.display = "none";
        }, 400);
    }, 500);

    var isDirectoryDialogOpen = false;
    $("#playlist-add-select").addEventListener("click", function(){
        if(isDirectoryDialogOpen)
            return;

        isDirectoryDialogOpen = true;
        ipc.send("open-directory-dialog");
    });
    $("#playlist-add-directory").addEventListener("click", function(){
        if(isDirectoryDialogOpen)
            return;

        isDirectoryDialogOpen = true;
        ipc.send("open-directory-dialog");
    });
    $("#playlist-add-button").addEventListener("click", function(){
        var name = $("#playlist-add-name").value,
            directory = $("#playlist-add-directory").value;

        if(name == "" || directory == "") {
            if(name == "") {
                $("#playlist-add-name").invalid = true;
                $("#playlist-add-name").errorMessage = "Name cannot be empty"
            }else
                $("#playlist-add-name").invalid = false;

            if(directory == "") {
                $("#playlist-add-directory").invalid = true;
                $("#playlist-add-directory").errorMessage = "Directory cannot be empty"
            }else
                $("#playlist-add-directory").invalid = false;

            return;
        }
        $("#playlist-add-name").invalid = false;
        $("#playlist-add-directory").invalid = false;

        var inputs = document.querySelectorAll("#playlist-add-dialog paper-input");
        for(let i = 0; i < inputs.length; i++)
            inputs[i].value = "";

        var data = {
            name: name.trim(),
            directory: directory.trim()
        };
        ipc.send("add-playlist", data);

        $("#playlist-add-dialog").close();
    });
    var datatablePosition = 0;
    $("paper-datatable").addEventListener("data-changed", function(){
        var myNumber = ++datatablePosition;
        setTimeout(function(){
            if(datatablePosition == myNumber){
                let data = $("paper-datatable").data,
                    canSave = true;
                for(let i = 0; i < data.length; i++) {
                    for(let j = 0; j < data.length; j++) {
                        if(i !== j && data[i].name === data[j].name) {
                            canSave = false;
                        }
                    }
                }

                if(canSave) {
                    ipc.send("change-playlist", $("paper-datatable").data);
                    createSnackbar("Saved");
                }else{
                    createSnackbar("Error: Duplicate playlist name");
                }
            }

        }, 1000);
    });
    $("paper-datatable").addEventListener("selection-changed", function(){
        if(this.selectedItems.length > 0) {
            $("#paper-datatable-header").style.display = "flex";
            $("#selected-text span").innerHTML = this.selectedItems.length;
        }else{
            $("#paper-datatable-header").style.display = "none";
        }
    });
    $("#paper-datatable-header paper-icon-button").addEventListener("click", function(){
        var selectedData = $("paper-datatable").selectedItems;
        if(selectedData.length > 0) {
            var data = $("paper-datatable").data;
            for(let i = 0; i < selectedData.length; i++) {
                for(let j = 0; j < data.length; j++) {
                    if(selectedData[i].name == data[j].name) {
                        data.splice(j, 1);
                    }
                }
            }
            $("paper-datatable").deselectAll();
            $("paper-datatable").reload();
            $("paper-datatable").dispatchEvent(new CustomEvent("data-changed"));
        }
    });
    window.addEventListener("click", function(e){
        if(e.target.tagName.toLowerCase() == "iron-overlay-backdrop")
            $("paper-dialog").close();
    });

    ipc.on("directory-selected", function (event, path) {
        isDirectoryDialogOpen = false;
        $("#playlist-add-directory").value = path[0];
    });
    ipc.on("no-directory-selected", function (event, path) {
        isDirectoryDialogOpen = false;
    });

    //Start of server port input
    var portPosition = 0;
    $("port-input").addEventListener("input", function() {
        if(!this.invalid) {
            var myNumber = ++portPosition;
            setTimeout(function(){
                if(portPosition == myNumber){
                    ipc.send("change-port", parseInt($("port-input").value));
                    createSnackbar("Saved");
                }

            }, 1000);
        }
    });

    //Start of server buttons
    $("#button-start").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-start");
    });
    $("#button-stop").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-stop");
    });
    $("#button-restart").addEventListener("click", function() {
        if(this.disabled)
            return;

        ipc.send("server-restart");
    });
}

function createSnackbar(text){
    $("paper-toast").opened = false;
    $("paper-toast").show({
        text: text
    });
}
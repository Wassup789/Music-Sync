; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{4F95EEBB-93EF-44CD-9869-16B6D7130E1A}
AppName=Music Sync
AppVersion=1.0.2.0
AppVerName=Music Sync
AppPublisher=Wassup789
AppPublisherURL=https://github.com/Wassup789/Music-Sync/
AppSupportURL=https://github.com/Wassup789/Music-Sync/
AppUpdatesURL=https://github.com/Wassup789/Music-Sync/
DefaultDirName={userappdata}\Wassup789\Music Sync
DisableDirPage=yes
DisableProgramGroupPage=yes
LicenseFile=..\..\..\LICENSE
OutputDir=.
OutputBaseFilename=setup
SetupIconFile=..\..\..\logo\icon.ico
Compression=lzma
SolidCompression=yes
WizardImageFile=Images\WizardImageFile.bmp
PrivilegesRequired=admin
DisableWelcomePage=no
UninstallDisplayIcon={app}\music-sync-server.exe
CloseApplications=force

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]                                                                                           
Name: "startup"; Description: "Run on Windows Startup"; GroupDescription: "Startup:";
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\dist\windows\music-sync-server-win32-ia32\*"; Excludes: "..\dist\windows\music-sync-server-win32-ia32\settings"; DestDir: "{app}"; Flags: recursesubdirs ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{commonprograms}\Music Sync"; Filename: "{app}\music-sync-server.exe";
Name: "{commondesktop}\Music Sync"; Filename: "{app}\music-sync-server.exe"; Tasks: desktopicon

[Registry]
Root: HKLM; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "Music Sync Server"; ValueData: """{app}\music-sync-server.exe"" --startup"; Flags: uninsdeletevalue; Tasks: startup

[Run]
Filename: "{app}\music-sync-server.exe"; Description: "{cm:LaunchProgram,Music Sync}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}\settings"
Type: dirifempty; Name: "{userappdata}\Wassup789\Music Sync"
Type: dirifempty; Name: "{userappdata}\Wassup789"

[UninstallRun]
Filename: {cmd}; Parameters: /c taskkill /f /im music-sync-server.exe; Flags: runhidden
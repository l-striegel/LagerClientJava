@echo off
echo LagerClient - Setup
echo ===================
echo.

set INSTALL_DIR=%~dp0LagerClient
echo Installation in: %INSTALL_DIR%

:: Verzeichnis erstellen
if not exist "%INSTALL_DIR%" (
  echo Erstelle Installationsverzeichnis...
  mkdir "%INSTALL_DIR%"
)

:: JAR-Datei kopieren, falls im gleichen Verzeichnis
if exist "lagerclient.jar" (
  echo Kopiere lagerclient.jar...
  copy "lagerclient.jar" "%INSTALL_DIR%\"
) else (
  echo [FEHLER] lagerclient.jar nicht gefunden!
  echo Diese Datei muss im selben Verzeichnis wie setup.bat liegen.
  echo Installation wird abgebrochen.
  pause
  exit /b 1
)

:: Config-Datei erstellen
echo Erstelle Konfigurationsdatei...
(
echo # API-Konfiguration
echo api.url=https://localhost:5001/api/article
echo.
echo # Debug-Einstellungen
echo app.debug=false
echo.
echo # UI-Einstellungen
echo ui.table.rowheight=25
echo ui.table.zebracolor=#F0F0F0
) > "%INSTALL_DIR%\config.properties"

:: Verzeichnisse für Daten erstellen
echo Erstelle Verzeichnisse...
mkdir "%INSTALL_DIR%\localData"
mkdir "%INSTALL_DIR%\logs"

echo Erstelle Beispieldaten...
cd "%INSTALL_DIR%\localData"
echo {> articles.json
echo   "data" : [ {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 1,>> articles.json
echo     "name" : "LED-Streifen",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 10,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 5.99,>> articles.json
echo     "location" : "Regal 2",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "https://example.com",>> articles.json
echo     "timestamp" : "2025-03-06T18:51:34.527611",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#FF0000\"},\"Typ\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Link\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Lagerplatz\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#FF0000\"},\"Status\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Type\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Stock\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Unit\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Price\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Location\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#FF0000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#FF0000">> articles.json
echo       },>> articles.json
echo       "Typ" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Link" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Lagerplatz" : {>> articles.json
echo         "validColor" : "#FF0000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#FF0000">> articles.json
echo       },>> articles.json
echo       "Status" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Type" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Stock" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Unit" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Price" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Location" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   }, {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 4,>> articles.json
echo     "name" : "LED-Halter",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 4,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 0.3,>> articles.json
echo     "location" : "R1B2",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "https://www.aliexpress.de",>> articles.json
echo     "timestamp" : "2025-03-08T09:55:51",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Status\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Type\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Stock\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Unit\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Price\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Location\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Link\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Typ\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Bestand\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Status" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Type" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Stock" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Unit" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Price" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Location" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Link" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Typ" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Bestand" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   }, {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 5,>> articles.json
echo     "name" : "Neuer Artikel",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 1,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 18.93,>> articles.json
echo     "location" : "Regal 2",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "https://www.test.de",>> articles.json
echo     "timestamp" : "2025-03-08T12:04:37",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   }, {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 2,>> articles.json
echo     "name" : "Testartikel",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 10,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 9.99,>> articles.json
echo     "location" : "Regal 5",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "https://test.com",>> articles.json
echo     "timestamp" : "2025-03-08T12:05:49",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Typ\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Status\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Link\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Bestand\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Einheit\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Preis (\\u20AC)\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Lagerplatz\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Type\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Stock\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Unit\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Price\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"},\"Location\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Typ" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Status" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Link" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Bestand" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Einheit" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Preis (€)" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Lagerplatz" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Type" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Stock" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Unit" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Price" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       },>> articles.json
echo       "Location" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   }, {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 7,>> articles.json
echo     "name" : "Diode",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 14,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 5.0,>> articles.json
echo     "location" : "Regal 1 Box 2",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "https://www.amazon.de",>> articles.json
echo     "timestamp" : "2025-03-09T09:25:18",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":true,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : true,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   }, {>> articles.json
echo     "valid" : true,>> articles.json
echo     "id" : 6,>> articles.json
echo     "name" : "Relais",>> articles.json
echo     "type" : "Elektronik",>> articles.json
echo     "stock" : 1,>> articles.json
echo     "unit" : "Stück",>> articles.json
echo     "price" : 17.95,>> articles.json
echo     "location" : "Regal 1",>> articles.json
echo     "status" : "Auf Lager",>> articles.json
echo     "link" : "http://test.de",>> articles.json
echo     "timestamp" : "2025-03-09T09:38:11",>> articles.json
echo     "stylesJson" : "{\"Name\":{\"Bold\":false,\"Italic\":false,\"Underline\":false,\"Color\":\"#000000\"}}",>> articles.json
echo     "styles" : {>> articles.json
echo       "Name" : {>> articles.json
echo         "validColor" : "#000000",>> articles.json
echo         "bold" : false,>> articles.json
echo         "italic" : false,>> articles.json
echo         "underline" : false,>> articles.json
echo         "color" : "#000000">> articles.json
echo       }>> articles.json
echo     }>> articles.json
echo   } ],>> articles.json
echo   "hash" : "t0uV5RR2xlG2ST5bU5vUGVayBvHjSJnEIbVWuBXLxYE=">> articles.json
echo }>> articles.json
cd "%~dp0"
:: Start-Datei erstellen
echo Erstelle Startdatei...
(
echo @echo off
echo java -jar lagerclient.jar
echo pause
) > "%INSTALL_DIR%\start.bat"

echo.
echo Setup abgeschlossen!
echo.
echo Die Anwendung kann gestartet werden mit:
echo %INSTALL_DIR%\lagerclient.jar
echo oder
echo %INSTALL_DIR%\start.bat
echo.

:: Verknüpfung zum Desktop erstellen
set /p CREATE_SHORTCUT="Soll eine Verknüpfung auf dem Desktop erstellt werden? (J/N): "
if /i "%CREATE_SHORTCUT%"=="J" (
  echo Erstelle Desktop-Verknüpfung...
  
  :: PowerShell verwenden für Shortcut-Erstellung
  PowerShell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut([Environment]::GetFolderPath('Desktop') + '\LagerClient.lnk'); $Shortcut.TargetPath = '%INSTALL_DIR%\lagerclient.jar'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Save()"
  
  echo Desktop-Verknüpfung erstellt!
)

echo.
echo Installation abgeschlossen!
pause
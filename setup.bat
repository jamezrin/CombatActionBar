@echo off
echo Downloading dependencies...
if not exist "libs" mkdir libs
powershell -Command "wget https://www.spigotmc.org/resources/1315/download?version=95119 -UseBasicParsing -OutFile %~dp0\libs\ActionBarAPI.jar"
powershell -Command "wget https://www.spigotmc.org/resources/1884/download?version=94543 -UseBasicParsing -OutFile %~dp0\libs\GriefPrevention.jar"
powershell -Command "wget https://www.spigotmc.org/resources/4775/download?version=86004 -UseBasicParsing -OutFile %~dp0\libs\CombatTagPlus.jar"
powershell -Command "wget https://www.spigotmc.org/resources/4278/download?version=31685 -UseBasicParsing -OutFile %~dp0\libs\AntiCombatLog.jar"
powershell -Command "wget https://www.spigotmc.org/resources/845/download?version=85799 -UseBasicParsing -OutFile %~dp0\libs\PvPManager.jar"
powershell -Command "wget https://repo.techcable.net/service/local/repositories/snapshots/content/com/trc202/combattag/7.0.0-beta-1-SNAPSHOT/combattag-7.0.0-beta-1-20150731.214122-6.jar -UseBasicParsing -OutFile %~dp0\libs\CombatTag.jar"
powershell -Command "wget http://addons-origin.cursecdn.com/files/723/360/CombatLog.jar -UseBasicParsing -OutFile %~dp0\libs\CombatLog.jar"

if errorlevel 0 goto install
goto error

:install
echo Download completed, installing dependencies...
cd libs
start cmd.exe /c mvn install:install-file -Dfile=ActionBarAPI.jar -DgroupId=com.connorlinfoot -DartifactId=ActionBarAPI -Dversion=1.5.2 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=GriefPrevention.jar -DgroupId=me.ryanhamshire -DartifactId=GriefPrevention -Dversion=14.7 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=CombatTagPlus.jar -DgroupId=net.minelink -DartifactId=ctplus -Dversion=1.2.3 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=AntiCombatLog.jar -DgroupId=com.mlgprocookie -DartifactId=acl -Dversion=1.2 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=PvPManager.jar -DgroupId=me.NoChance -DartifactId=PvPManager -Dversion=3.0 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=CombatTag.jar -DgroupId=net.techcable -DartifactId=combattag -Dversion=7.0.0 -Dpackaging=jar
start cmd.exe /c mvn install:install-file -Dfile=CombatLog.jar -DgroupId=com.jackproehl -DartifactId=plugins -Dversion=1.8.11 -Dpackaging=jar
goto completed

:error
echo An error occurred while downloading the dependencies, exiting...
exit

:completed
echo This script has finished downloading and installing the dependencies, exiting...
exit
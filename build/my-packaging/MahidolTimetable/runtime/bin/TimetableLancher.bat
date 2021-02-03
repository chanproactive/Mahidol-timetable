@echo off
set DIR="%~dp0"
set JAVA_EXEC="%DIR:"=%\java"
pushd %DIR% & %JAVA_EXEC% -Dlog4j.configurationFile=./log4j2.xml -p "%~dp0/../app" -m org.mahidol/org.mahidol.Main  %* & popd

set basedir=e:\lanes
set cp=%basedir%\lib\*;%basedir%\*
set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_05"
set PATH=%JAVA_HOME%\bin;%PATH%

%JAVA_HOME%\bin\java -classpath %cp% org.lanes.Example

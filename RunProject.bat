ECHO on

set CLASSPATH=.;c:\Program Files\Java\jdk-15\lib;C:\Users\User\Desktop\SCC-311\Coursework\Coursework 3 - Clustering\jgroups-5.0.0.Final.jar

ECHO Starting RMI Registry
start rmiregistry


ECHO Compiling Files ...
javac  project_LogicLayer/*.java


ECHO Running Server ...
java  project_LogicLayer/MyServer


ECHO on

set CLASSPATH=.;c:\Program Files\Java\jdk-15\lib;C:\Users\User\Desktop\SCC-311\Coursework\Coursework 3 - Clustering\jgroups-5.0.0.Final.jar


ECHO Running Server ...
ECHO start java  MyServer

ECHO %1

if [%1]==[] GOTO singleExecution
FOR /L %%A IN (1,1, %1) DO ECHO (%%A) Starting new Replica: && start java  project_LogicLayer/MyServer

:singleExecution
start java  project_LogicLayer/MyServer
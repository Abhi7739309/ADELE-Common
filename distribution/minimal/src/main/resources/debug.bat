echo Starting the gateway with remote debugger connection (on port 8000)
echo The execution is suspended until the debugger is connected
java -Dchameleon.home=%CD% -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -jar bin/chameleon-core.jar --interactive

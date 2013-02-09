Pre-release SICS TAC Classic Java Server 1.0 beta 11 - 2004-08-12

Note that this is a Java port of the TAC Classic server. Please report
any problems or bugs to tac-support@sics.se


This is a quickly compiled README file.  Please bear with it.

Directories (will automatically be created when needed)

public_html/history	where game logs and results are stored
public_html/code	where the game viewer is stored
logs			where server log files are stored


Upgrading from a previous versions of the TAC servers
-----------------------------------------------------
Note that the format of the stored state has changed in TAC Classic
Server and the stored state from older SICS TAC servers can not be
used with TAC Classic Java Server 1.0 beta.

Please contact tac-dev@sics.se for more information if this is a
problem.


Configuring the TAC Classic Java Server
---------------------------------------
The server reads the configuration file 'tacserver.conf' in the
directory 'config' at startup. This file allows, among other things,
the configuration of log levels and auto join behaviour. See the file
'config/tacserver.conf' for more information.

(Note that if you get collisions with the ports of other applications
 when you start the TAC server, you can change the ports in this
 configuration file.)

For information about scheduling competitions please see
http://www.sics.se/tac/docs/classic/server/1.0b11/admin.html

For information about generation of statistics please see
http://www.sics.se/tac/docs/classic/server/1.0b11/statistics.html


Requirements
------------
The TAC Classic Java Server requires Java 2 SDK 1.4.


Running the TAC Classic Java Server
-----------------------------------
Start the server with the command

$ java -jar tacserver.jar

The server will wait for the InfoServer to start (see below).


Starting up the InfoServer
--------------------------
The InfoServer is responsible for the web interface and game result
generation.

From another shell on the same computer do

$ java -jar infoserver.jar

The InfoServer should now establish contact with the TAC Classic
Server.


Accessing the server
--------------------
Surf to http://<yourcomputer>:8080/ and create users, games, etc.

You should register a user with the name 'admin', and then restart
the server. Only this user will have access to the administration
pages for scheduling competitions, etc.


Stopping the server
-------------------
The server can currently only be stopped with control-C. The Java
InfoServer can be stopped and restarted without restarting the TAC
server but please avoid restarting it during result generation
(shortly after a game ended).

If you have any problems with the server, please send an email to
tac-support@sics.se

Best,
The TAC Team, SICS

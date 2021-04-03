# BlueWarfare-RWF

Attempt to update libraryaddict's Search and Destroy to 1.16.5
(plus whatever changes i made in bluewarfare)

Doesn't include the Hub or Build servers

Some changes will have comments to explain my terrible code or backup some old value used

Requires Maven to build, PaperMC 1.16.5 to run as well as ProtocolLib 4.6.0 and LibsDisguises 10.0.20
The jars in \dependencies aren't needed, but they're there i guess.

Credit to libraryaddict for creating the original server and plugins: https://github.com/libraryaddict/RedWarfare.
Also credit to jacky8399 and thanks Martoph for that sql script from ages ago.

If you use this make sure to credit libraryaddict, me, and jacky8399

You'll need to move MysqlManager and RedisManager from \managers to redwarfare-global16\src\me\libraryaddict\redis and \mysql respectively.
Then put your own MySQL/Redis server credentials into them.

The plugin will create the MySQL tables by itself, just create the database "RedWarfare" beforehand.

You'll also have to set up BungeeCord.

When putting in maps make sure to update them to 1.16.5 or the game will crash.

To have a server icon, put the server-icon.png into the bungee's \plugins folder.

----------------------------------------
libs notes:

The large majority of Red Warfare's code

To get this running you will need a MySQL server and a redis server. You will need to change the settings in
MysqlManager and RedisManager, its not user friendly as it was hardcoded. It was hardcoded namely because I didn't
believe it would ever be released to the public, and if the plugin was leaked then I'll need to change the login details
regardless.

For the build server, it should require some work to get running.

You may notice that some references to outside projects are missing, namely a server spinner and manager. Those managed
the servers themselves.

In the process of updating to 1.12.2 some features may have been broken.

The maven structure is a bit weird and doesn't follow convention, when I created the project the folders were not
created properly and I didn't want to waste time debugging a minor issue.

Another point of interest is the exception handling. Namely: UtilError.handle(e);

I wasn't sure how to log all exceptions and was trying this, by the time I realized it was a bad method it was not worth
changing back.

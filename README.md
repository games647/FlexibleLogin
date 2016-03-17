# FlexibleLogin

###Description

A Sponge minecraft server plugin for second authentication. It has a built-in
TOTP support.

Do you want to let your players protect their account (from hackers/login stealers) and keep playing while the session server is down. You can use this little plugin. You can protect your account with a password you choose or with a time based password created from a secret key, generated just for you.

Tested against:

	Sponge: sponge-3.0

	Minecraft (Client): 1.8.8

###Commands

    /register <password> <password> - Registers using a specific password
    /register - Generates your secret code for TOTP
    /changepassword <password> <password> - Changes your current password
    /login <password/code> - Login using your password or time based code
    /logout - Logs you out
    /setemail - Sets your email address
    /forgotpassword - Sends a recovery email to the email address
    /unregister <uuid/name> - delete an account

###Permissions

    flexiblelogin.admin - Permission to delete accocunts
    flexiblelogin.command.login - Use the /login command
    flexiblelogin.command.logout - Use the /logout command
    flexiblelogin.command.changepw - Use the /changepassword command
    flexiblelogin.command.register - Use the /register command
    flexiblelogin.command.email - Use the /setemail command
    flexiblelogin.command.forgot - Use the /forgot command
    flexiblelogin.bypass - Users who have this permission can skip authentication

###Config

    # Do you allow your users to skip authentication with the bypass permission
    bypassPermission=false
    # Should only the specified commands be protected from unauthorized access
    commandOnlyProtection=false
    # Email configuration for password recovery
    emailConfiguration {
        # Username for the account you want to the email from
        account=""
        # Is password recovery using an email allowed
        enabled=false
        # Mail server
        host="smtp.gmail.com"
        # Password for the account you want to the email from
        password=""
        # SMTP Port for outgoing messages
        port=465
        # Displays as sender in the email client
        senderName="Your minecraft server name"
        # Email subject/title
        subject="Your new Password"
        # Email contents. You can use HTML here
        text="New password for %player% on Minecraft server %server%: %password%"
    }
    # Algorithms for hashing user passwords. You can also choose totp
    hashAlgo=bcrypt
    # Should the plugin login users automatically if it's the same account from the same IP
    ipAutoLogin=false
    # Should this plugin check for player permissions
    playerPermissions=false
    # If command only protection is enabled, these commands are protected. If the list is empty all commands are protected
    protectedCommands=[
        op,
        pex
    ]
    # Database configuration
    sqlConfiguration {
        # Database name
        database=flexiblelogin
        # Password in order to login
        password=""
        # Path where the database is located. This can be a file path (h2/SQLite) or an IP/Domain(MySQL)
        path="%DIR%"
        # Port for example MySQL connections
        port=3306
        # SQL server type. You can choose between h2, SQLite and MySQL
        type=H2
        # Username to login the database system
        username=""
    }
    teleportConfig {
        coordX=0
        coordY=0
        coordZ=0
        # Should the plugin use the default spawn from the world you specify below
        defaultSpawn=false
        enabled=false
        # Spawn world or let it empty to use the default world specified in the server properties
        worldName=""
    }
    # Text configuration for custom messages in chat
    textConfiguration {
        [...]
    }
    # Number of seconds a player has time to login or will be kicked.-1 deactivates this features
    timeoutLogin=60
    # Should the plugin save the login status to the database
    updateLoginStatus=false


### Links

[Github - Source Code][1]
[Download][2]
Please leave star on Github or a like/comment on this thread. Feel free to clone, fork or contribute to this repository.
This is a complete new project, so many features can be added.

###Screenshots:

####TOTP Key generation (/register)
![Minecraft image picture](http://fs2.directupload.net/images/150805/eu3fycsp.png)

####Android App Google Authenticator (IOS App exists too)
![Minecraft image picture](http://fs2.directupload.net/images/150804/3qrcb9j3.png)
You can see there a time generated code which can be used for the login process. (/login <code>)
Additionally it display your user account name and the server ip.

  [1]: https://github.com/games647/FlexibleLogin
  [2]: https://github.com/games647/FlexibleLogin/releases
# FlexibleLogin

###Description

A Sponge minecraft server plugin for second authentication. It has a built-in
TOTP support.

Do you want to let your players protect their account (from hackers/login stealers) and keep playing while the session server is down. You can use this little plugin. You can protect your account with a password you choose or with a time based password created from a secret key, generated just for you.

Tested against:

	Sponge: sponge-1.8-1499-2.1DEV-602

	Minecraft (Client): 1.8.8

###Commands

    /register <password> <password> - Registers using a specific password
    /register - Generates your secret code for TOTP
    /login <password/code> - Login using your password or time based code
    /logout - Logs you out
    /flexiblelogin - Displays plugin name and version

###Permissions

    None

###Config

    # Algorithms for hashing user passwords. You can also choose totp
    hashAlgo=totp
    # Should the plugin login users automatically if it's the same account from the same IP
    ipAutoLogin=true
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
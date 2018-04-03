# FlexibleLogin

## **Security Notice:** If you use a version 0.16.X version, update to 0.16.5+ or disable the change password command. 0.16.5 fixed this security bug occurred in this [commit](https://github.com/games647/FlexibleLogin/commit/43f74a466e73b0f2cfa522b5bfd68480010a7934). Older versions before 0.16 are not affected.

## Description

A Sponge Minecraft server plugin for second authentication. It has a built-in
TOTP support.

Do you want to let your players protect their account (from hackers/login stealer) and keep playing 
while the session server is down. You can use this little plugin. You can protect your account with 
a password you choose or with a time based password created from a secret key, generated just for you.

## Requirements

* Sponge 7.0+
* Java 8+

## Language

This plugin has configurable language files. By default it only ships the english version of it, but there are community
driven templates on the wiki page: https://github.com/games647/FlexibleLogin/wiki

## Commands

    /reg /register <password> <password> - Registers using a specific password
    /register - Generates your secret code for TOTP
    /changepw /cp /changepassword <password> <password> - Changes your current password
    /log /l /login <password|code> - Login using your password or time based code
    /logout - Logs you out
    /email /setemail - Sets your email address
    /forgot /forgotpassword - Sends a recovery email to the email address
    /unregister <uuid|name> - delete an account

    Admin commands
    /flexiblelogin <reload|rl> - reloads the config
    /flexiblelogin forcelogin <name> - Force login the user
    /flexiblelogin <accounts|acc> <name|ip> - Get list of user accounts
    /flexiblelogin <unregister|unreg> <name|uuid|-a> - Deletes the account of a user or all using the -a flag
    /flexiblelogin <register|reg> <name|uuid> <pass> - Register the user with a specific password
    /flexiblelogin <resetpw|resetpassword> <name> - Sets a new temp password for a new user
    
## Permissions

    flexiblelogin.admin - Permission to delete accocunts
    flexiblelogin.command.login - Use the /login command
    flexiblelogin.command.logout - Use the /logout command
    flexiblelogin.command.changepw - Use the /changepassword command
    flexiblelogin.command.register - Use the /register command
    flexiblelogin.command.email - Use the /setemail command
    flexiblelogin.command.forgot - Use the /forgot command
    flexiblelogin.no_auto_login - Players with this won't be auto logged in by the ip auto login feature
    flexiblelogin.bypass - Users who have this permission can skip authentication

## Config

    # Should unregistered player be able to join the server?
    allowUnregistered=true
    # Do you allow your users to skip authentication with the bypass permission
    bypassPermission=false
    # Should the player name always be case sensitive equal to the time the player registered?
    caseSensitiveNameCheck=true
    # Should only the specified commands be protected from unauthorized access
    commandOnlyProtection=false
    # Email configuration for password recovery
    emailConfiguration {
        # Username for the account you want to the email from
        account=""
        # Email contents. You can use HTML here
        contentTemplate {
            arguments {}
            closeArg="}"
            content {
                text="New password for Builder{name=player, optional=true} on Minecraft server Builder{name=server, optional=true}: Builder{name=, optional=true}"
            }
            openArg="{"
            options {
                closeArg="}"
                openArg="{"
            }
        }
        # Is password recovery using an email allowed
        enabled=false
        # Mail server
        host="smtp.gmail.com"
        # Password for the account you want to the email from
        password=""
        # SMTP Port for outgoing messages
        port=465
        # Displays as sender in the email client
        senderName="Your Minecraft server name"
        # Email subject/title
        subjectTemplate {
            arguments {}
            closeArg="}"
            content {
                text="Your new Password"
            }
            openArg="{"
            options {
                closeArg="}"
                openArg="{"
            }
        }
    }
    # Algorithms for hashing user passwords. You can also choose totp
    hashAlgo=bcrypt
    # Should the plugin login users automatically if it's the same account from the same IP
    ipAutoLogin=false
    # Custom command that should run after the user tried to make too many attempts
    lockCommand=""
    # How many login attempts are allowed until everything is blocked
    maxAttempts=3
    # How many accounts are allowed per ip-address. Use 0 to disable it
    maxIpReg=0
    # Interval where the please login will be printed to the user
    messageInterval=2
    # The user should use a strong password
    minPasswordLength=4
    # Should this plugin check for player permissions
    playerPermissions=false
    # Experimental feature to protect permissions for players who aren't logged in yet
    protectPermissions=false
    # If command only protection is enabled, these commands are protected. If the list is empty all commands are protected
    protectedCommands=[
        op,
        pex
    ]
    # Teleport the player to a safe location based on the last login coordinates
    safeLocation=false
    # Database configuration
    sqlConfiguration {
        # Database name
        database=flexiblelogin
        # Password in order to login
        password=""
        # Path where the database is located. This can be a file path (h2/SQLite) or an IP/Domain (MySQL/MariaDB)
        path="%DIR%"
        # Port for example MySQL connections
        port=3306
        # SQL server type. You can choose between h2, SQLite and MySQL/MariaDB
        type=H2
        # It's strongly recommended to enable SSL and setup a SSL certificate if the MySQL/MariaDB server isn't running on the same machine
        useSSL=false
        # Username to login the database system
        username=""
    }
    # Should the plugin don't register alias /l (used by some chat plugins) for /login command 
    supportSomeChatPlugins=false
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
    # Number of seconds a player has time to login or will be kicked.-1 deactivates this features
    timeoutLogin=60
    # Should the plugin save the login status to the database
    updateLoginStatus=false
    # Regular expression for verifying validate player names. Default is a-zA-Z with 2-16 length
    validNames="^\\w{2,16}$"
    # How seconds the user should wait after the user tried to make too many attempts
    waitTime=300

## Downloads

https://github.com/games647/FlexibleLogin/releases

###  Development builds

Development builds of this project can be acquired at the provided CI (continuous integration) server. It contains the
latest changes from the Source-Code in preparation for the following release. This means they could contain new
features, bug fixes and other changes since the last release.

Nevertheless builds are only tested using a small set of automated and minor manual tests. Therefore they **could**
contain new bugs and are likely to be less stable than released versions.

https://ci.codemc.org/job/Games647/job/FlexibleLogin/changes

## Screenshots:

### TOTP Key generation (/register)
![Minecraft image picture](https://i.imgur.com/K2GDqfW.png)

### Android App Google Authenticator (IOS App exists too)
![Google authenticator](https://i.imgur.com/Zz6RkdY.png)
You can see there a time generated code which can be used for the login process. `/login <code>`
Additionally it display your user account name and the server ip.

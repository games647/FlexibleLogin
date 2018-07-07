# Changelog

###### 0.17.4

* Fix loading storage type method after recent configurate update

###### 0.17.3

* Fix breaking configuration change for BCrypt hashing method

###### 0.17.2

* Remove GriefPrevention version (Fixes #123)

###### 0.17.1

* Fix attempts checking condition (Fixes #112)

###### 0.17

**Warning:** There could be a breaking change that requires you to change the configuration of hashing from BCRYPT to BCrypt.

* Add experimental permission protection to fix compatibility with NEB (Fixes #111)
* Use a single task to print not logged messages
* Kick unregistered players if configured to do so (Fixes #93)
* Add support for AuthMeBridge (Fixes #108) to allow integration on BungeeCord
* Catch uncheckedexception for database setup too
* Add workaround for time parsing bug in SQLite (Fixes #106)
* Delay server shutdown to shutdown correctly on failed database setup (Related #109)
* Reduce file size by minimizing google auth dependency 
* Use JSON Text objects for more flexibility of messages
* Save account on password save (Fixes #101)
* Add configuration option for case sensitive name checking (Fixes #99)
* Don't require the latest version of GriefPrevention (Fixes #98)

###### 0.16.8

* Ignore teleport for prevent listener otherwise we prevent our own teleport
* Override smtp provider (Related #92)
* Add support for plain text messages
* Make valid usernames configurable (Fixes #94)

###### 0.16.7

* Relocate BCrypt and GoogleAuth correctly
* Explicit add smtp provider (Related #92)

###### 0.16.9

safeLoc.setPosition(newPos)

###### 0.16.6

* Relocate libraries for version conflicts

###### 0.16.5

* Fix security check in the change password command

###### 0.16.4

* Fix account saving because of timestamp parsing
* Update lastlogin on password login not on Minecraft login
* Downgrade to SHA256 for compatibility Base64
* Perform invalid case check earlier (Fixes #88)

###### 0.16.3

* Whitelist all aliases by FlexibleLogin

###### 0.16.2

* Remove CommandManager injection to support older Sponge versions
* Fix dependency injection

###### 0.16.1

* Fix SQL syntax for new tables

###### 0.16

* Safe teleport players outside of Nether portals (Fixes #81)
* Cache flexiblelogin plugin commands that should be ignored for events
* Fix errors if GriefPrevention isn't loaded
* Enhance mail SSL security
* Revert BCrypt workaround
* Use Instant for lastLogin
* Remove code duplicates
* Stronger TOTP code generation
* Compile constant patterns for better performance

###### 0.15.3

* Fix database cleanup on shutdown (Fixes #78)

###### 0.15.2

* Fix missing ItemDropEvent cause for Sponge 7.0 (Fixes #76)

###### 0.15.1

* Cancel CreateClaimEvents for GriefPrevention

##### 0.15

* Remove version check, because the sponge is much more stable nowadays
* Add more events that will be cancelled if not logged in (some of them might be redundant):
    * ItemPickup
    * ItemInteract
    * InventoryInteract
    * ClickInventory
    * EntityInteract
* Add beforeModification annotation to prevent forge mods from seeing the cancelled events

##### 0.14.7

* Shutdown the server if the major version doesn't equal the target version
* Prevent different case name stealing (Fix #71)
* Fix max ip registrations (Fixes #72)

##### 0.14.3

* Generate $2y instead of $2a bcrypt hashes to support PHP impl

##### 0.14.2

* Fix account password saving (Fixes #66)

##### 0.14.1

* Compare the playername case sensitive (Fixes #66)
* Fix creating account (Fixes #67)

##### 0.14 

* Add version number to the file name (Fixes #61)
* Fix time calc for auto login (Fixes #59)
* Fix forceRegister not working at all
* Configurable message interval (Fixes #65)

##### 0.13

* Do not timeout kick for bypassed players (Fixes #55)
* Update to Sponge 7.0 (Minecraft 1.12) Fixes #64

##### 0.12.3

* Add missing = for key=value sql config

##### 0.12.2

* Fix timeout check (Fix #51)

##### 0.12.1

* Add /l and /reg as command aliases
* Only auto login if the last login is between 12 hours
* Add permission to prevent ip auto login (Fixes #43)
* Remove cache
* Fix mail prefix (now from https://emailregex.com/) (Fixes #483)
* Fix creating default config
* Change login alias from /l -> /log

##### 0.11

* Make safeLocation configurable (Fixes #36)
* Send already logged in message
* Load the account into the cache after a save (Fixes #40)
* Prevent player logins if that player is already online (Fixes #41)

##### 0.10

* Add reload command (Fixes #27)
* Add force register command
* Fix attempts NPE on disconnect (Fixes #29)
* Fix messages file extension name
* Restructure admin commands (Fixes #30)
* Fix cancel drop item (Fix #34)

##### 0.9.2

* Extract messages to an extra file (Fixes #19)
* Backport to Sponge 4.X (Fixes #23)

##### 0.9.1

* Catch NPE on unprotect location

##### 0.9

* Update to 1.9
* Add a repeating message task and add a not registered message
* Fail safely on already created accounts (Fixes #16)
* Add resetpassword other command (Related #13)
* Add max login attempts
* Add max ip registrations (Fixes #13)
* Add custom lock command (Fixes #18)

##### 0.8

* Delete last ip on manually logout (Related #13)
* Added minimum password length (Related to #13)

##### 0.7

* Added changepassword command

##### 0.6.1

* Added plugin meta data

##### 0.6

* Added teleport to spawn for unlogged players
* Make permissions optional

##### 0.5.1

* Fixed timeout kicks not scheduled correctly

##### 0.5

* Moved Bcrypt to a maven maven dependency
* Added timeout for login
* Added permissions
* Added loggedIn boolean column
* Renamed table to flexiblelogin_users - the plugin will automatically convert it into the new table

##### 0.4.4

* Prevent players from all damage if they are not logged in
* Try to players safely on login
* Simplify and optimize prevent listener
* Add Updatifier support

##### 0.4.3

* Protect players from attack damage
* Fixed not logged in message for non existing accounts

##### 0.4.2

* Fixes not logged in message (Fixes #10)

##### 0.4.1

* Fixes SQLite database setup (Fixes #9)

##### 0.4

* Implement ip auto login
* Added command only protection
* Added permission .registerRequired

##### 0.3.3

* Make TextConfiguration complete (Fixes #5)

##### 0.3.2

* Update for the newest Sponge version (SpongePowered/SpongeAPI@3699c1e)
* Update for the newest Text changes (SpongePowered/SpongeAPI@3699c1e)

##### 0.3.1

* Fix compatibility with Bcrypt generated passwords by other implementations
* Update for SpongePowered/SpongeAPI@588e4eb
* Add options for customizing text (Thanks to @frogocomics)

##### 0.2.8

* Updated for the newest Sponge API https://github.com/SpongePowered/SpongeAPI@c7a9590f9ff81a4fab37adb33751a7ab6eda1768

##### 0.2.7

* Removed VersionCommand
* Fixed NoSuchMethodError player.sendMessage SpongePowered/SpongeAPI@405632f
* Fixed various command results if the invoker is a console.

##### 0.2.6

* Prevent inventory events
* Update against newest Sponge version for the inventory refactor update

##### 0.2.5

* Update for the newest event changes where ItemPickUpEvent is removed

##### 0.2.4

* Update for Sponge 613+

##### 0.2.3

* Update for Sponge 591+

##### 0.2.2

* Fixed deleted account remains in cache
* Update to newest sponge version (555+)

##### 0.2.1

* Fixed mail sending
* Fixed MySQL support (Missing connection account properties)
* Fixed UUID support for MySQL

##### 0.2

* Implement Password recovery
* Added /forgotPassword command
* Added /setEmail command
* Added mail column to the database
* Added /unregister command for admins to delete user accounts
* Added player messages if a command fails to execute


##### 0.1.1

* Fix thread-safety
* Fix logout on player quit (Security)

##### 0.1

* First release

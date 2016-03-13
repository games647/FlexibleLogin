#Changelog

##### 0.1

+ First release

##### 0.1.1

* Fix thread-safety
* Fix logout on player quit (Security)

##### 0.2

+ Implement Password recovery
+ Added /forgotPassword command
+ Added /setEmail command
+ Added email column to the database
+ Added /unregister command for admins to delete user accounts
+ Added player messages if a command fails to execute

##### 0.2.1

+ Fixed email sending
+ Fixed MySQL support (Missing connection account properties)
+ Fixed UUID support for MySQL

##### 0.2.2

+ Fixed deleted account remains in cache
+ Update to newest sponge version (555+)

##### 0.2.3

+ Update for Sponge 591+

##### 0.2.4

+ Update for Sponge 613+

##### 0.2.5

+ Update for the newest event changes where ItemPickUpEvent is removed

##### 0.2.6

+ Prevent inventory events
+ Update against newest Sponge version for the inventory refactor update

##### 0.2.7

+ Removed VersionCommand
+ Fixed NoSuchMethodError player.sendMessage SpongePowered/SpongeAPI@405632f
+ Fixed various command results if the invoker is a console.

##### 0.2.8

+ Updated for the newest Sponge API https://github.com/SpongePowered/SpongeAPI@c7a9590f9ff81a4fab37adb33751a7ab6eda1768

##### 0.3.1

+ Fix compatibility with Bcrypt generated passwords by other implementations
+ Update for SpongePowered/SpongeAPI@588e4eb
+ Add options for customizing text (Thanks to @frogocomics)

##### 0.3.2

+ Update for the newest Sponge version (SpongePowered/SpongeAPI@3699c1e)
+ Update for the newest Text changes (SpongePowered/SpongeAPI@3699c1e)

##### 0.3.3

+ Make TextConfiguration complete (Fixes #5)

##### 0.4

+ Implement ip auto login
+ Added command only protection
+ Added permission .registerRequired

##### 0.4.1

+ Fixes SQLite database setup (Fixes #9)

##### 0.4.2

+ Fixes not logged in message (Fixes #10)

##### 0.4.3

+ Protect players from attack damage
+ Fixed not logged in message for non existing accounts

##### 0.4.4

+ Prevent players from all damage if they are not logged in
+ Try to players safely on login
+ Simplify and optimize prevent listener
+ Add Updatifier support

##### 0.5

+ Moved Bcrypt to a maven maven dependency
+ Added timeout for logins
+ Added permissions
+ Added loggedIn boolean column
+ Renamed table to flexiblelogin_users - the plugin will automatically convert it into the new table

##### 0.5.1

* Fixed timeout kicks not scheduled correctly

##### 0.6

* Added teleport to spawn for unlogged players
* Make permissions optional
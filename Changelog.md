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
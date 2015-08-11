#Changelog

####0.1

+ First release

#####0.1.1

* Fix thread-safety
* Fix logout on player quit (Security)

####0.2

+ Implement Password recovery
+ Added /forgotPassword command
+ Added /setEmail command
+ Added email column to the database
+ Added /unregister command for admins to delete user accounts
+ Added player messages if a command fails to execute

#####0.2.1

+ Fixed email sending
+ Fixed MySQL support (Missing connection account properties)
+ Fixed UUID support for MySQL
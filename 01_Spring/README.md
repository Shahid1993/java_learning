### [SpringBoot with LogBack creating LOG_PATH_IS_UNDEFINED folder](https://stackoverflow.com/questions/25251983/springboot-with-logback-creating-log-path-is-undefined-folder)

In your case `LOG_PATH` is not defined on startup. You should use `${LOG_PATH:-.}` instead , See .  

But if you define `logging.path` in your `application.properties` you will see two log files in `.` and in `${logging.path}` directory.  

Spring container set `LOG_PATH` after Logback initialization... Logback is not supported lazy file creation as far as I know. In this case you should use `logback-spring.xml` instead `logback.xml`.

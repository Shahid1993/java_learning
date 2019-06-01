#### 1. [SpringBoot with LogBack creating LOG_PATH_IS_UNDEFINED folder](https://stackoverflow.com/questions/25251983/springboot-with-logback-creating-log-path-is-undefined-folder)
  In your case `LOG_PATH` is not defined on startup. You should use `${LOG_PATH:-.}` instead , See . 
    
  But if you define `logging.path` in your `application.properties` you will see two log files in `.` and in `${logging.path}` directory.  

  Spring container set `LOG_PATH` after Logback initialization... Logback is not supported lazy file creation as far as I know. In this case you should use `logback-spring.xml` instead `logback.xml`.

#### 2. [Spring and Threads: TaskExecutor](https://egkatzioura.com/2017/10/25/spring-and-threads-taskexecutor/)

#### 3. [Two reasons why your Spring @Autowired component is null](https://www.moreofless.co.uk/spring-mvc-java-autowired-component-null-repository-service/)
- YOU INSTANTIATED THE CLASS MANUALLY
- YOU FORGOT TO ANNOTATE A CLASS AS A COMPONENT OR ONE OF ITS DESCENDANTS

#### 4. [Java: How to fix Spring @Autowired annotation not working issues](https://technology.amis.nl/2018/02/22/java-how-to-fix-spring-autowired-annotation-not-working-issues/)

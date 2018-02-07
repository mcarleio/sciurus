![Sciurus](./logo.png) 

# Sciurus

Sciurus is a collection of useful aspects to
* monitor execution runtimes of methods
* lock method executions
* cache method results

[![Maven Central][maven-image]][maven-url] 
[![License][license-image]](LICENSE)
[![Build status][travis-image]][travis-url]
[![Build status][codecov-image]][codecov-url]

## General Usage

You need to include Sciurus as dependeny and declare it as an aspect library in the `aspectj-maven-plugin`. For an example, see the [example](./example/) project.

1. Include Sciurus as dependency
    ```xml
    <dependency>
        <groupId>io.mcarle</groupId>
        <artifactId>sciurus</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```
    
2. Include aspectj-maven-plugin and define Sciurus as `aspectLibrary`
    ```xml
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <version>1.11</version>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>test-compile</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <complianceLevel>${maven.compiler.source}</complianceLevel>
            <source>${maven.compiler.source}</source>
            <target>${maven.compiler.target}</target>
            <aspectLibraries>
                <aspectLibrary>
                    <groupId>io.mcarle</groupId>
                    <artifactId>sciurus</artifactId>
                </aspectLibrary>
            </aspectLibraries>
        </configuration>
    </plugin>
    ```

## Annotations / Aspects

### @Monitor
On all methods annotated with `@Monitor`, Sciurus will measure the execution time and by default log them. 
Additionally, you can register custom monitors to e.g. store execution times in databases or further utilize the data.

##### General Usage
1. Annotate your methods, which should be monitored with Sciurus' [`@Monitor`](src/main/java/io/mcarle/sciurus/annotation/Monitor.java) annotation.
    ```java
    import io.mcarle.sciurus.annotation.Monitor;
    import io.mcarle.sciurus.annotation.LoggingMonitorParams;
 
    public class ToBeMonitored {
       @Monitor
       public int toBeMonitoredMethod(String xyz) {
            // do something
       }
    
       @Monitor
       @LoggingMonitorParams(warnLimit = 2000)
       public int shouldExecuteInUnderTwoSeconds() {
            // do something 
       }
    }
    ```
    
2. As Sciurus' `@Monitor` is deactivated per default, you have to activate it yourself, e.g. at startup or with the help of a setting. 
    ```java
    import io.mcarle.sciurus.Sciurus;
    import org.glassfish.jersey.servlet.ServletContainer;

    public class ExampleServlet extends ServletContainer {
    
        @Override
        public void init() throws ServletException {
            Sciurus.startMonitor();
            super.init();
        }
    }
    ```
    
##### Logging Monitor
By default, Sciurus will log the duration together with the class and method, which will look like this
```
DEBUG : ( 150 ms) - io.mcarle.example.sciurus.resources.ExampleResource#longRunningProcess
```

###### @LoggingMonitorParams
With the help of [`@LoggingMonitorParams`](src/main/java/io/mcarle/sciurus/annotation/LoggingMonitorParams.java), you can add some additional information to Sciurus' logging monitor:

* `warnLimit` and `warnLimitUnit` can be used to define an amount of time. If the method execution takes longer, the log level will be warning, otherwise debug.
* `logParameter`: When true, the log message also contains the actual parameters of the method
* `logResult`: When true, the log message also contains the result of the method

When all parameters are set, the result may look like this 
```
WARN : ( 200 ms) - io.mcarle.example.sciurus.resources.ExampleResource#longRunningProcess([150]) : 42
```

 
##### Custom Monitor
You can register [custom monitor](src/main/java/io/mcarle/sciurus/monitor/CustomMonitor.java) which will get notified after each `@Monitor` annotated method finished its execution.
```java
Sciurus.registerMonitor(new CustomMonitor() {
      @Override
      public void monitored(Duration duration, String declaringTypeName, Method method, Object[] methodArgs, Throwable throwable, Class returnType, Object returnValue) {
          // do something
      }
});
```

**Note**: You can also deregister the logging monitor, if you do not need it: `Sciurus.deregisterMonitor(LoggingMonitor.INSTANCE)`

### @Cache
On all methods annotated with `@Cache`, before execution, Sciurus will check in the defined cache (global, map, or some custom cache) if there is a result stored for this method.
If that is the case, that value is returned and the actual method execution will be skipped. When there is no such value, Sciurus will store it in the defined cache after 
executing the method.

**Note**: Sciurus does not distinguish the method call from various instances. So it is not good, to use it on POJOs. A good example would be a proxy or a database repository.

##### General Usage
```java
import io.mcarle.sciurus.annotation.Cache;
import java.time.temporal.ChronoUnit;

public class ToBeCached {
    
    @Cache(
        time = 2,
        unit = ChronoUnit.SECONDS,
        cacheName = <Sciurus.CACHE_GLOBAL or Sciurus.CACHE_MAP or some custom cache name>
    )
    public String shouldBeCached(int param) {
        // do domething
    }
}
```

##### Custom Caches
Sciurus comes with an in memory [expiring map](https://github.com/jhalterman/expiringmap) cache as the default one.
You may register [custom caches](./src/main/java/io/mcarle/sciurus/cache/CustomCache.java) and define them in the `@Cache` annotation as the target cache.

**Note**: Every custom cache must return [`CustomCache.EMPTY`](src/main/java/io/mcarle/sciurus/cache/CustomCache.java) on get, if no cache entry is present! 
```java
Sciurus.registerCache("<name of your cache>", new CustomCache() {
      @Override
      public Object get(ExecutionIdentifier executionIdentifier) {
          // Loading from cache or return CustomCache.EMPTY
      }
    
      @Override
      public void put(ExecutionIdentifier executionIdentifier, Object result, Duration duration) {
          // Saving to cache
      }
});
```  

### @Lock
Before executing a method annotated with `@Lock`, Sciurus will check if there is any running instance of that method and wait till that execution is finished before starting.
You may ask, how this is different from javas synchronize? 
* It works on any instance of the class
  If the same method of a class is being called in parallel on two different instances, then the methods are executed successively.
* It can include method parameters as condition
  If the same method of a class is being called in parallel three times, all with different parameters, then the methods are all executed in parallel.
  If one call has the same parameters as another, the later one waits until the first execution is finished.
  
  exampleMethod("A") | exampleMethod("B") | exampleMethod("A")
  ------------------ | ------------------ | ------------------
  check -> ok        |                    |
  store exec info    |                    | 
  execute            | check -> ok        | check -> not ok
     ...             | store exec info    | wait
     ...             | execute            | ... 
  finished           |    ...             | ... 
  remove exec info   | finished           | ... 
  notify             | remove exec info   | ...
  |                  | notify             | check -> ok
  |                  |     -              | store exec info
  |                  |     -              | execute
     
  **Note**: This table is a simplified scenario. Sciurus will also work on more complex scenarios!   
  
## Use Case Examples
There are a number of possible use cases for each aspect.

### @Monitor
* Notify a monitoring application
* Find bottlenecks
* Test the speed of a new software version before deploying it in production
* Store execution times (e.g. in a database) and
  * see how they evolve over time
  * see runaway values
  * monitor them through a MBean
  
### @Cache
* Caching (surprise!)
* Facade for various caches which can be exchanged with merely little code changes

### @Lock
* Prevent multiple storing (e.g. in databases)
* Prevent multiple execution of long running or high cpu consuming methods (e.g. in combination with `@Cache`)  

## License

Unless explicitly stated otherwise all files in this repository are licensed under the Apache Software License 2.0

Copyright 2017 Marcel Carlé

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


[maven-image]: https://img.shields.io/maven-central/v/io.mcarle/sciurus.svg
[maven-url]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.mcarle%22%20a%3A%22sciurus%22
[license-image]: https://img.shields.io/github/license/mcarleio/sciurus.svg
[license-url]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.mcarle%22%20a%3A%22sciurus%22
[travis-image]: https://img.shields.io/travis/mcarleio/sciurus.svg
[travis-url]: https://travis-ci.org/mcarleio/sciurus
[codecov-image]: https://img.shields.io/codecov/c/github/mcarleio/sciurus.svg
[codecov-url]: https://codecov.io/gh/mcarleio/sciurus
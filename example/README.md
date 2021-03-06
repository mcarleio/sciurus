# Sciurus Example

This is an example project presenting Sciurus with some example use cases.

1. Execute `mvn jetty:run`
2. Check any of the following descriptions

## Use Case Descriptions

### `@Monitor`
In this example, a custom monitor was written, which stores the execution times.
Additionally, it provides some aggregate functions to determine the average, maximum and minimum execution time.

To see it in action, visit `http://localhost:8080/monitor` and reload a few times.

### `@Cache`
In this example, Sciurus' default cache (i.e. expiring map cache) is used to cache the result of a web request.

To see it in action, visit `http://localhost:8080/cache`. The first execution takes some time.
Afterwards the result is stored for 10 seconds in Sciurus' cache.
If you reload the page within the 10 seconds, the answer will be returned almost immediately.  
 
### `@Lock`
This example is a simplified use case, where a call to `http://localhost:8080/lock` emulates
the parallel call of a method, which stores a value into a map.
If the value is already stored, it throws an exception i.e. emulating a unique constraint.

You can see the result by visiting `http://localhost:8080/lock`. After one second, you should see the result.

### `@Retry`
This very simple example retries a method execution five times at most. A call to `http://localhost:8080/retry?errors=3` means, that the method will throw an exception for the first three executions.
The result of the call is an json array:
* if you specified zero errors, then the result should only contain one 'success' element.
* if you specified one to five errors, then the result should start with the specified amount of 'error' elements, followed by a final 'success' element.
* if you specified more then five errors, it will contain six 'error' elements and a final 'exception' element.

## Note
For `@Monitor` and `@Cache` a MBean is implemented to deactivate and reactive the monitor/cache. To check it up:
* Start e.g. `jconsole`
* connect to the `mvn jetty:run` process
* go to tab MBeans
* in the list, open `io.mcarle.example.sciurus`
* for `@Cache` open `CacheSwitcher`, for `@Monitor` open `MonitorSwitcher`  
* click on `Operations`
* e.g. execute the related stop method
* check the changed behaviour of the related use case
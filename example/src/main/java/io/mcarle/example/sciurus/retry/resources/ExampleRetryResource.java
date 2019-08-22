package io.mcarle.example.sciurus.retry.resources;

import io.mcarle.sciurus.annotation.Retry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/retry")
public class ExampleRetryResource {


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> retry(@QueryParam("errors") @DefaultValue("3") int errors) {
        List<String> strings = new ArrayList<>();
        try {
            doSomething(new AtomicInteger(errors), strings);
        } catch (Exception e) {
            strings.add(e.getMessage());
        }
        return strings;
    }

    /*
     * INFO: This is an example. In real use cases, a method annotated with @Retry should (almost) never
     * * change the state/values of the object it belongs to
     * * change the state/values of any method parameters
     * * or call other methods doing so.
     */
    @Retry(times = 5)
    public void doSomething(AtomicInteger x, List<String> result) throws InterruptedException {
        Thread.sleep(250); // just to emulate load time
        if (x.getAndDecrement() > 0) {
            result.add("error");
            throw new RuntimeException("exception");
        }
        result.add("success");
    }


}

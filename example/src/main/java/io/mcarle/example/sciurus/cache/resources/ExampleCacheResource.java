package io.mcarle.example.sciurus.cache.resources;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.mcarle.sciurus.annotation.Cache;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.temporal.ChronoUnit;

@Path("/cache")
public class ExampleCacheResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String cache() throws UnirestException, InterruptedException {
        return queryPhotos();
    }

    @Cache(time = 10, unit = ChronoUnit.SECONDS)
    public String queryPhotos() throws UnirestException, InterruptedException {
        Thread.sleep(2000); // Just to simulate a longer response time
        return Unirest.get("https://jsonplaceholder.typicode.com/albums")
                .asString().getBody();
    }
}

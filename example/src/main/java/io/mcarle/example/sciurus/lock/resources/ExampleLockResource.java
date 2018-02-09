package io.mcarle.example.sciurus.lock.resources;

import io.mcarle.example.sciurus.lock.manager.ExampleManager;
import org.apache.logging.log4j.util.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/lock")
public class ExampleLockResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String lock(@DefaultValue("1") @QueryParam("album") int albumNr) throws InterruptedException {
        if (albumNr < 1 || albumNr > 3) {
            return "Illegal argument! album must be a value between 1 and 3 (both inclusive)";
        }
        ExampleManager exampleManager = new ExampleManager();

        new Thread(() -> exampleManager.queryAlbum(1)).start();
        new Thread(() -> exampleManager.queryAlbum(1)).start();
        new Thread(() -> exampleManager.queryAlbum(2)).start();
        new Thread(() -> exampleManager.queryAlbum(2)).start();
        new Thread(() -> exampleManager.queryAlbum(3)).start();
        new Thread(() -> exampleManager.queryAlbum(3)).start();
        Thread.sleep(100);

        return Strings.join(exampleManager.queryAlbum(albumNr), '\n');
    }

}

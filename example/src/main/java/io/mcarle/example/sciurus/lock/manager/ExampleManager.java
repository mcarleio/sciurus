package io.mcarle.example.sciurus.lock.manager;

import com.mashape.unirest.http.Unirest;
import io.mcarle.sciurus.annotation.Lock;

import java.util.*;

public class ExampleManager {

    private Map<Integer, String> albums = new HashMap<>();

    /**
     * Think about this method storing the value into a database. After some time for the external resource, every call has its own transaction and tries to store an entity.
     * If that entity has some kind of unique constraint, then there would be an exception, which ist emulated here with an IllegalStateException.
     */
    @Lock(on = 0)
    public Collection<String> queryAlbum(int albumNr) {
        if (!albums.containsKey(albumNr)) {
            try {
                Thread.sleep(albumNr * 1000); // Just to simulate a longer response time
                String oldValue = albums.put(
                        albumNr,
                        Unirest.get("https://jsonplaceholder.typicode.com/albums/" + albumNr).asString().getBody()
                );
                if (oldValue != null) {
                    throw new IllegalStateException();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return albums.values();
    }
}

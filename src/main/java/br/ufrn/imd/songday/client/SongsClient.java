package br.ufrn.imd.songday.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "songs-service", url = "${songs.api.address}")
public interface SongsClient {
    @GetMapping(value = "/songs/{id}")
    ResponseEntity<Object> findById(@PathVariable String id);
}

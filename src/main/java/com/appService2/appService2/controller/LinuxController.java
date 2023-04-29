package com.appService2.appService2.controller;

import com.appService2.appService2.entity.LinusPojo;
import com.appService2.appService2.service.LinusPojoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/service")
@RequiredArgsConstructor
public class LinuxController {

    private final LinusPojoService service;

    
    @GetMapping(value = "/list-all-services", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LinusPojo> listAllRunningServices(){
        return service.getAllRunningService();
    }


    @GetMapping(value = "/list-running-service-by-prefix/{prefix}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LinusPojo> listRunningServiceByPrefix(@PathVariable("prefix") String prefix) {
        return service.getRunningServiceByPrefix(prefix);
    }

    @GetMapping(value = "/down-service-notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public void notifyDownService() throws IOException {
        service.notifyDownService();
    }


}

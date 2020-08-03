package com.server.task.Controller;


import com.server.task.Model.Server;
import com.server.task.Service.ResourceManagementService;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "cloudservice/api")
public class ServerController {

    @Autowired
    private ResourceManagementService resourceManagementService;

    @RequestMapping(value = "servers", method = RequestMethod.GET)
    public ResponseEntity<List<Server>> getAllServers() {
        try {
            List<Server> serverList = resourceManagementService.getAllServers();
            return new ResponseEntity<>(serverList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<String> allocateServer(@NotEmpty @RequestParam int size) {
        String responseBody = resourceManagementService.allocateServer(size);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

}

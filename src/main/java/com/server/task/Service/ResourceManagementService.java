package com.server.task.Service;

import com.server.task.Dao.ServerDao;
import com.server.task.Model.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceManagementService {

    @Autowired
    @Qualifier("AerospikeData")
    private ServerDao serverDao;

    public List<Server> getAllServers() {
        return serverDao.getAllServers();
    }

    public String allocateServer(int size) {
        return serverDao.allocateServer(size);
    }
}

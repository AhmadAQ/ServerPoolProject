package com.server.task.Dao;

import com.server.task.Model.Server;

import java.util.List;

public interface ServerDao {
    List<Server> getAllServers();

    String allocateServer(int size);

}

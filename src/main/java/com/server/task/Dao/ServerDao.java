package com.server.task.Dao;

import com.server.task.Model.Server;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface ServerDao {
    List<Server> getAllServers();

    List<Server> getServersInSortedManner(List<Server> servers);

    String allocateServer(int size);

    String cloudService(int size);

    String updateServer(ExecutorService executor, Server server, int size);

    String spinServer(ExecutorService executor, int size);

    Server getAvailableServer(int size);

    void saveAllocatedServer(Server server, int size);

    void updateAllocatedSize(Server server, int size);

    void updateFreeSize(Server server);

    Server createServer();
}

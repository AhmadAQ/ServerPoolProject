package com.server.task.ThreadUtility;

import com.server.task.Model.Server;
import com.server.task.Repository.ServerRepository;

public class UpdateServerThread implements Runnable {

    private Server server;
    private ServerRepository serverRepository;

    public UpdateServerThread(Server server, ServerRepository serverRepository) {
        this.server = server;
        this.serverRepository = serverRepository;
    }

    public UpdateServerThread() {
    }

    @Override
    public void run() {
        try {
            Server fetchedServer = server;
            serverRepository.delete(server);
            serverRepository.save(fetchedServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

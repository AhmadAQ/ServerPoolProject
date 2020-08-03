package com.server.task.ThreadUtility;

import com.server.task.Model.Server;
import com.server.task.Repository.ServerRepository;

import java.util.logging.Logger;

public class UpdateServerThread implements Runnable {

    private Server server;
    private ServerRepository serverRepository;
    private Logger logger = Logger.getLogger(UpdateServerThread.class.getName());

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
            logger.info("Successfully Allocated in an Existing server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

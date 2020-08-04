package com.server.task.ThreadUtility;

import com.server.task.Dao.AerospikeDao;
import com.server.task.Model.Server;
import com.server.task.Repository.ServerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AllocateServerThread implements Runnable {

    private AerospikeDao aerospikeDao;
    private ServerRepository serverRepository;
    private int size;
    private int serverId;
    private static Map<Integer, Integer> serversWaiting;
    private Logger logger = Logger.getLogger(AllocateServerThread.class.getName());

    public AllocateServerThread(AerospikeDao aerospikeDao, ServerRepository serverRepository, int size, Map<Integer, Integer> serversWaiting, int serverId) {
        this.aerospikeDao = aerospikeDao;
        this.size = size;
        this.serversWaiting = serversWaiting;
        this.serverRepository = serverRepository;
        this.serverId = serverId;
    }

    public AllocateServerThread() {
    }

    @Override
    public void run() {
        try {
            logger.info("Server is in Creating Status (20s)");
            Server createdServer = aerospikeDao.createServer(serverId);
            serverRepository.save(createdServer);
            Thread.sleep(5000);
            aerospikeDao.saveAllocatedServer(createdServer,serversWaiting.get(serverId));
            createdServer.setStatus(true);
            serverRepository.save(createdServer);
            serversWaiting.remove(serverId);
            logger.info("Server Successfully Created of size: " + size + " GB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

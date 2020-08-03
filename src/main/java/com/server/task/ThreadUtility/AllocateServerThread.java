package com.server.task.ThreadUtility;

import com.server.task.Dao.AerospikeDao;
import com.server.task.Model.Server;

import java.util.logging.Logger;

public class AllocateServerThread implements Runnable {

    private AerospikeDao aerospikeDao;
    private int size;
    private Logger logger = Logger.getLogger(AllocateServerThread.class.getName());


    public AllocateServerThread(AerospikeDao aerospikeDao, int size) {
        this.aerospikeDao = aerospikeDao;
        this.size = size;
    }

    public AllocateServerThread() {
    }

    @Override
    public void run() {
        try {
            logger.info("Server is in Creating status (20s)");
            Thread.sleep(20000);
            Server createdServer = aerospikeDao.createServer();
            createdServer.setStatus(true);
            aerospikeDao.saveAllocatedServer(createdServer, size);
            logger.info("Server Successfully Created and initialized with value of: " + size +" GB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.server.task.ThreadUtility;

import com.server.task.Dao.ServerDao;
import com.server.task.Model.Server;

public class AllocateServerThread implements Runnable {


    ServerDao serverDao;
    private int size;

    public AllocateServerThread(ServerDao serverDao, int size) {
        this.serverDao = serverDao;
        this.size = size;
    }

    public AllocateServerThread() {
    }

    @Override
    public void run() {
        try {
            Thread.sleep(20000);
            Server createdServer = serverDao.createServer();
            createdServer.setStatus(true);
            serverDao.saveAllocatedServer(createdServer, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

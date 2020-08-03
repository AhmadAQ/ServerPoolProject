package com.server.task.Model;

import org.springframework.data.annotation.Id;

public class Server implements Comparable<Server> {

    @Id
    private int serverId;
    private boolean isActive;
    private int allocatedSize;
    private int freeSize;

    public Server() {
    }

    public Server(int serverId) {
        this.serverId = serverId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setStatus(boolean status) {
        this.isActive = status;
    }

    public int getAllocatedSize() {
        return allocatedSize;
    }

    public void setAllocatedSize(int allocatedSize) {
        this.allocatedSize = allocatedSize;
    }

    public int getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(int freeSize) {
        this.freeSize = freeSize;
    }

    @Override
    public int compareTo(Server o) {
        return Integer.compare(serverId, o.getServerId());
    }

}


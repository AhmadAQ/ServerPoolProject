package com.server.task.Dao;

import com.server.task.Model.Server;
import com.server.task.Repository.ServerRepository;
import com.server.task.ThreadUtility.AllocateServerThread;
import com.server.task.ThreadUtility.UpdateServerThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.server.task.Constants.AerospikeConstants.MAXIMUM_SERVER_SIZE;
import static com.server.task.Constants.AerospikeConstants.MINIMUM_SERVER_SIZE;

@Component
@Qualifier("AerospikeData")
public class AerospikeDao implements ServerDao {

    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private ServerDao serverDao;

    /**
     * Fetches All servers allocated in the DB
     *
     * @return All servers in the DB as a list
     */
    @Override
    public List<Server> getAllServers() {
        List<Server> servers = new ArrayList<>();
        servers = getServersInSortedManner(servers);
        return servers;
    }

    /**
     * Gets Servers from DB in a sorted manner
     *
     * @param servers A server list to store the servers from the DB
     * @return sorted servers as a list
     */
    @Override
    public List<Server> getServersInSortedManner(List<Server> servers) {
        serverRepository.findAll().forEach(servers::add);
        Collections.sort(servers);
        return servers;
    }

    /**
     * @param size
     * @return
     */
    @Override
    public String allocateServer(int size) {
        if (size > MAXIMUM_SERVER_SIZE) return size + ": exceeds the allowed server size";
        if (size <= MINIMUM_SERVER_SIZE) return "Provided server size must be greater than zero";
        return cloudService(size);

    }

    @Override
    public String cloudService(int size) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Server availableServer = getAvailableServer(size);
        if (availableServer != null) {
            return updateServer(executor, availableServer, size);
        } else return spinServer(executor, size);
    }


    @Override
    public String updateServer(ExecutorService executor, Server server, int size) {
        executor.execute(new UpdateServerThread(server, serverRepository));
        return "Server of size: " + size + " has been allocated";
    }

    @Override
    public String spinServer(ExecutorService executor, int size) {
        executor.execute(new AllocateServerThread(serverDao, size));
        return "Server is being spun";
    }

    @Override
    public Server getAvailableServer(int size) {
        List<Server> servers = getAllServers();
        for (Server server : servers) {
            synchronized (server) {
                if (server.getFreeSize() >= size && server.isActive()) {
                    updateAllocatedSize(server, size);
                    updateFreeSize(server);
                    return server;
                }
            }
        }
        return null; // Server Does not exist
    }


    //
    @Override
    public void saveAllocatedServer(Server server, int size) {
        updateAllocatedSize(server, size);
        updateFreeSize(server);
        serverRepository.save(server);
    }

    /**
     * Updates Allocated Server
     *
     * @param server
     * @param size
     */
    @Override
    public void updateAllocatedSize(Server server, int size) {
        int newAllocatedSize = server.getAllocatedSize() + size;
        server.setAllocatedSize(newAllocatedSize);
    }

    /**
     * Updates Free size of a Server
     *
     * @param server
     */
    @Override
    public void updateFreeSize(Server server) {
        int newFreeSize = MAXIMUM_SERVER_SIZE - server.getAllocatedSize();
        server.setFreeSize(newFreeSize);
    }

    // Creates a Server with id and return it fields are empty
    @Override
    public Server createServer() {
        int newServerId = (int) serverRepository.count() + 1;
        Server server = new Server(newServerId);
        return server;
    }
}

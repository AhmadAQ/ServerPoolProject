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
    private AerospikeDao aerospikeDao;

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
    public List<Server> getServersInSortedManner(List<Server> servers) {
        serverRepository.findAll().forEach(servers::add);
        Collections.sort(servers);
        return servers;
    }

    /**
     * Allocates a server
     *
     * @param size The amount of server size to allocate
     * @return a string that expresses the output of the operation
     */
    @Override
    public String allocateServer(int size) {
        if (size > MAXIMUM_SERVER_SIZE) return size + " GB : exceeds the allowed server size";
        if (size <= MINIMUM_SERVER_SIZE) return "Provided server size must be greater than zero GB";
        return cloudService(size);
    }

    /**
     * Allocates memory for a specified size by searching through active
     * servers if there isn't sufficient memory space a new server will be spun.
     *
     * @param size The amount of server size to be allocated
     * @return A string that represents the operation done
     */
    public String cloudService(int size) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Server availableServer = getAvailableServer(size);
        if (availableServer != null) {
            return updateServer(executor, availableServer, size);
        } else return spinServer(executor, size);
    }

    /**
     * Allocates memory space in an existing server
     *
     * @param executor The thread allocated for an operation
     * @param server   The server to update its parameters
     * @param size     The amount of server size be allocated
     * @return A string that represents the operation
     */
    public String updateServer(ExecutorService executor, Server server, int size) {
        executor.execute(new UpdateServerThread(server, serverRepository));
        return "Server of size: " + size + " GB has been allocated in an existing Server Pool";
    }

    /**
     * Creates a new server
     *
     * @param executor The thread allocated for an operation
     * @param size     The amount of server size be allocated
     * @return A string that represents that the server is being created
     */
    public String spinServer(ExecutorService executor, int size) {
        executor.execute(new AllocateServerThread(aerospikeDao, size));
        return "A new server is being spun, the storage will be allocated once the server is active";
    }

    /**
     * Checks if there is an available server with the required free size be allocated
     *
     * @param size The amount of server size be allocated
     * @return the server if it exists, null other wise
     */
    public synchronized Server getAvailableServer(int size) {
        List<Server> servers = getAllServers();
        for (Server server : servers) {
            if (server.getFreeSize() >= size && server.isActive()) {
                updateServerData(server, size);
                return server;
            }
        }
        return null; // Server Does not exist
    }

    /**
     * Updates a servers data
     *
     * @param server The server to update
     * @param size   The amount of server size to allocate
     */
    public void updateServerData(Server server, int size) {
        updateAllocatedSize(server, size);
        updateFreeSize(server);
    }

    /**
     * Saves a server in a database
     *
     * @param server The server to store
     * @param size   The amount of server size be allocated
     */
    public void saveAllocatedServer(Server server, int size) {
        updateAllocatedSize(server, size);
        updateFreeSize(server);
        serverRepository.save(server);
    }

    /**
     * Updates Allocated Server Size data field
     *
     * @param server The server to update its AllocatedSize data field
     * @param size   The amount of server size be allocated
     */
    public void updateAllocatedSize(Server server, int size) {
        int newAllocatedSize = server.getAllocatedSize() + size;
        server.setAllocatedSize(newAllocatedSize);
    }

    /**
     * Updates Free size data field of a Server
     *
     * @param server The server to update its FreeSize data field
     */
    public void updateFreeSize(Server server) {
        int newFreeSize = MAXIMUM_SERVER_SIZE - server.getAllocatedSize();
        server.setFreeSize(newFreeSize);
    }

    /**
     * Creates a new Server
     *
     * @return returns a new server with an id and empty data fields
     */
    public Server createServer() {
        int newServerId = (int) serverRepository.count() + 1;
        return new Server(newServerId);
    }
}

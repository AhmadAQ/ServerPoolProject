package com.server.task.Dao;

import com.server.task.Model.Server;
import com.server.task.Repository.ServerRepository;
import com.server.task.ThreadUtility.AllocateServerThread;
import com.server.task.ThreadUtility.UpdateServerThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.server.task.Constants.ServerPoolConstants.MAXIMUM_SERVER_SIZE;
import static com.server.task.Constants.ServerPoolConstants.MINIMUM_SERVER_SIZE;

@Component
@Qualifier("AerospikeData")
public class AerospikeDao implements ServerDao {

    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private AerospikeDao aerospikeDao;

    private Map<Integer, Integer> serversInCreatingStatus = new HashMap<>();
    private Logger logger = Logger.getLogger(AerospikeDao.class.getName());

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
        ExecutorService executor = Executors.newFixedThreadPool(1); //allocates thread pool for each instance
        Server availableServer = getAvailableServer(size); // checks in database if there is available memory
        if (availableServer != null) {
            logger.info("Allocated in an Existing Server");
            return updateServer(executor, availableServer, size);
        } else if (searchWaitingServers(size) != 0) {
            logger.info("Allocated in a Waiting Server");
            return "Server has been allocated in a waitingServer";
        } else {
            int newServerId = createServerId();
            serversInCreatingStatus.put(newServerId, size);
            return spinServer(executor, serversInCreatingStatus.get(newServerId), newServerId);
        }
    }

    /**
     * Checks if there is an available server with the required free size to be allocated
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
     * checks if their exists available storage that can be allocated on a waiting server
     *
     * @param size The amount of server size be allocated
     * @return returns ture if there is storage to be allocated on
     * a waiting server
     */
    public synchronized int searchWaitingServers(int size) {
        Iterator iterator = serversInCreatingStatus.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) iterator.next();
            int sizeLeft = MAXIMUM_SERVER_SIZE - ((int) mapElement.getValue());
            if (sizeLeft >= size) {
                int elementID = (int) mapElement.getKey();
                serversInCreatingStatus.computeIfPresent(elementID, (k, v) -> v + size);
                return 1;
            }
        }
        return 0;
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
    public String spinServer(ExecutorService executor, int size, int serverId) {
        executor.execute(new AllocateServerThread(aerospikeDao, serverRepository, size, serversInCreatingStatus, serverId));
        return "A new server is being spun, the storage will be allocated once the server is active";
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
    public Server createServer(int serverId) {
        return new Server(serverId);
    }

    /**
     * Creates an id for a server
     *
     * @return returns server id
     */
    public int createServerId() {
        return getAllServers().size();
    }

}

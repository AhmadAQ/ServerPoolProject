package com.server.task.Repository;

import com.server.task.Model.Server;
import org.springframework.data.aerospike.repository.AerospikeRepository;


public interface ServerRepository extends AerospikeRepository<Server, Integer>{
}


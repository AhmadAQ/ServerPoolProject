package com.server.task.Configuration;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.policy.ClientPolicy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.aerospike.core.AerospikeTemplate;
import org.springframework.data.aerospike.repository.config.EnableAerospikeRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static com.server.task.Constants.AerospikeConstants.*;

@Configuration
@EnableAerospikeRepositories(basePackages = {"com.server.task.Repository"})
@EnableAutoConfiguration
@EnableTransactionManagement
public class RepositoryConfiguration {

	public @Bean(destroyMethod = "close") AerospikeClient aerospikeClient() {
		ClientPolicy policy = new ClientPolicy();
		policy.failIfNotConnected = true;

		return new AerospikeClient(policy, AEROSPIKE_SERVER_HOST_NAME, AEROSPIKE_SERVER_PORT);
	}

	public @Bean AerospikeTemplate aerospikeTemplate() {
		return new AerospikeTemplate(aerospikeClient(), AEROSPIKE_SERVER_NAMESPACE);
	}
}

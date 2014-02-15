package com.pivotal.cf.config.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;

/**
 * 
 * @author Johannes Hiemer, cloudscale.
 *
 */
@Configuration
@Import(CustomPropertiesResolver.class)
@EnableMongoRepositories(value = "com.pivotal.cf.broker.mongodb.repositories")
public class CustomMongoDBRepositoryConfig extends AbstractMongoConfiguration {

	@Value("${database.nosql.host}")
	private String databaseHost;
	
	@Value("${database.nosql.user}")
	private String databaseUsername;
	
	@Value("${database.nosql.password}")
	private String databasePassword;
	
	@Value("${database.nosql.port}")
	private int databasePort;
	
	@Value("${database.nosql.database}")
	private String databaseName;
	
	@Override
	protected String getDatabaseName() {
		return databaseName;
	}
	
	@Override
	protected UserCredentials getUserCredentials() {
		return new UserCredentials(databaseUsername, databasePassword);
	}

	@Override
    protected String getMappingBasePackage() {
        return "com.pivotal.cf.broker.model";
    }

	@Override
	@SuppressWarnings("deprecation")
	public Mongo mongo() throws Exception {
		return new Mongo(databaseHost, databasePort);
	}
	
}
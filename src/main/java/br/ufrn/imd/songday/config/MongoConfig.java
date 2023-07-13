package br.ufrn.imd.songday.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {

//    @Value("${spring.data.mongodb.uri}")
    private final String mongoUri = "mongodb://localhost:27017";

//    @Value("${spring.data.mongodb.database}")
    private final String mongoDb = "songday";

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDb;
    }
}

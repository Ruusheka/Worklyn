package com.worklyn.common.config;

import org.springframework.context.annotation.Configuration;

/** MongoDB configuration is driven by spring.data.mongodb.uri. Transactions require a replica set. */
@Configuration
public class MongoConfig {
    @org.springframework.context.annotation.Bean
    public org.springframework.data.mongodb.MongoTransactionManager transactionManager(
            org.springframework.data.mongodb.MongoDatabaseFactory dbFactory) {
        // Multi-document transactions require MongoDB to run as a replica set.
        return new org.springframework.data.mongodb.MongoTransactionManager(dbFactory);
    }
}

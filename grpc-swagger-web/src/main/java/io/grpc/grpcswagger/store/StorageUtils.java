package io.grpc.grpcswagger.store;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import io.grpc.grpcswagger.config.AppConfig;
import io.grpc.grpcswagger.config.AppConfigValues;
import io.grpc.grpcswagger.config.StorageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static io.grpc.grpcswagger.config.StorageEnum.DYNAMO_DB;

/**
 * @author Jikai Zhang
 * @date 2019-08-24
 */
public class StorageUtils {

    private static final int MAX_CACHE_SIZE = 100000;

    public static <K, V> BaseStorage<K, V> newLocalStorage() {
        if (AppConfig.serviceExpiredSeconds() > 0) {
            return LocalCacheStorage.<K, V>newBuilder()
                    .setExpireSeconds(AppConfig.serviceExpiredSeconds())
                    .setMaxSize(MAX_CACHE_SIZE)
                    .build();
        } else {
            return new MapStorage<>();
        }
    }

    public static <K, V> BaseStorage<K, V> newDynamoDBStorage(AmazonDynamoDB amazonDynamoDB) {
        //TODO: this storage is completely only useable for ServiceConfig storage since the table name,keys are
        // hardcoded inside it
        return new DynamoDBStorage<>(amazonDynamoDB);
    }
}

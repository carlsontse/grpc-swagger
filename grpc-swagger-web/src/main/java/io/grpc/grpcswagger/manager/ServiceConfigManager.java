package io.grpc.grpcswagger.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.google.common.net.HostAndPort;

import io.grpc.grpcswagger.config.AppConfig;
import io.grpc.grpcswagger.model.ServiceConfig;
import io.grpc.grpcswagger.store.BaseStorage;
import io.grpc.grpcswagger.store.StorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhangjikai
 */
@Component
public class ServiceConfigManager {

    private BaseStorage<String, ServiceConfig> storage;

    @Autowired
    public ServiceConfigManager(AmazonDynamoDB amazonDynamoDB) {
        switch (AppConfig.getServiceStorage()) {
            case DYNAMO_DB:
                storage = StorageUtils.newDynamoDBStorage(amazonDynamoDB);
                break;
            case LOCAL:
            default:
                storage = StorageUtils.newLocalStorage();
                break;
        }
    }

    @Nullable
    public HostAndPort getEndPoint(String fullServiceName) {
        ServiceConfig config = storage.get(fullServiceName);
        if (config == null) {
            return null;
        }
        return HostAndPort.fromString(config.getEndPoint());
    }

    public List<ServiceConfig> getServiceConfigs() {
        return storage.getAll().values().asList();
    }

    public ServiceConfig addServiceConfig(ServiceConfig serviceConfig) {
        storage.put(serviceConfig.getService(), serviceConfig);
        return serviceConfig;
    }
}

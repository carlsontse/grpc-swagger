package io.grpc.grpcswagger.store;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.google.common.collect.ImmutableMap;
import io.grpc.grpcswagger.model.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBStorage<K, V> implements BaseStorage<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBStorage.class);

    private final AmazonDynamoDB amazonDynamoDB;
    private final static String TABLE_NAME = "grpc_service_registry";
    private final static String SERVICE_KEY_NAME = "Service";
    private final static String ENDPOINT_KEY_NAME = "Endpoint";

    public DynamoDBStorage(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void put(K key, V value) {
        ServiceConfig serviceConfigValue = null;

        if (value instanceof ServiceConfig) {
            serviceConfigValue = (ServiceConfig) value;
        } else {
            throw new RuntimeException();
        }

        HashMap<String, AttributeValue> itemValues = new HashMap<>();

        itemValues.put(SERVICE_KEY_NAME, new AttributeValue(serviceConfigValue.getService()));
        itemValues.put(ENDPOINT_KEY_NAME, new AttributeValue(serviceConfigValue.getEndPoint()));

        try {
            amazonDynamoDB.putItem(TABLE_NAME, itemValues);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    @Override
    public V get(K key) {
        String keyStr;

        if (!(key instanceof String)) {
            keyStr = key.toString();
        } else {
            keyStr = (String) key;
        }

        HashMap<String,AttributeValue> keyToGet = new HashMap<>();

        keyToGet.put(SERVICE_KEY_NAME, new AttributeValue(keyStr));

        Map<String, AttributeValue> result;

        try {
            result = amazonDynamoDB.getItem(TABLE_NAME, keyToGet).getItem();

            if (result != null) {
                return (V)new ServiceConfig(result.get(SERVICE_KEY_NAME).getS(),
                        result.get(ENDPOINT_KEY_NAME).getS());
            } else {
                System.out.format("No item found with the key %s!\n", keyToGet);
                return null;
            }
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            throw e;
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public void remove(K key) {
        String keyStr;

        if (!(key instanceof String)) {
            keyStr = key.toString();
        } else {
            keyStr = (String) key;
        }

        HashMap<String,AttributeValue> keyToDelete = new HashMap<>();

        keyToDelete.put(SERVICE_KEY_NAME, new AttributeValue(keyStr));

        Map<String, AttributeValue> result;

        try {
            amazonDynamoDB.deleteItem(TABLE_NAME, keyToDelete);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            throw e;
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean exists(K key) {
        return get(key) != null;
    }

    @Override
    public ImmutableMap<K, V> getAll() {

        List<Map<String, AttributeValue>> results;
        Map<K, V> returnVal = new HashMap<>();

        try {
            results = amazonDynamoDB.scan(TABLE_NAME, Arrays.asList(SERVICE_KEY_NAME, ENDPOINT_KEY_NAME)).getItems();

            for (Map<String, AttributeValue> result : results) {
                returnVal.put((K) result.get(SERVICE_KEY_NAME).getS(),
                        (V)new ServiceConfig(result.get(SERVICE_KEY_NAME).getS(),
                        result.get(ENDPOINT_KEY_NAME).getS()));
            }
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            throw e;
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw e;
        }

        return ImmutableMap.copyOf(returnVal);
    }

}

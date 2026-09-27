package org.images;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPostConfirmationEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PostConfirmationHandler implements RequestHandler<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public PostConfirmationHandler() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(System.getenv("REGION")))
                .build();
        this.tableName = System.getenv("USERS_TABLE_NAME");
    }

    @Override
    public CognitoUserPoolPostConfirmationEvent handleRequest(
            CognitoUserPoolPostConfirmationEvent event,
            Context context) {
        context.getLogger().log("PostConfirmation trigger received for user: " + event.getUserName());

        try {
            Map<String, String> userAttributes = event.getRequest().getUserAttributes();
            String userId = event.getUserName();
            String email = userAttributes.get("email");
            String name = userAttributes.get("name");

            var item = getAttributeValueMap(userId, email, name);

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

            context.getLogger().log("Successfully saved confirmed user to DynamoDB: " + userId);

        } catch (Exception e) {
            context.getLogger().log("Error in PostConfirmation: " + e.getMessage());
            throw new RuntimeException("Failed to save user to DynamoDB", e);
        }

        return event;
    }

    private static Map<String, AttributeValue> getAttributeValueMap(String userId, String email, String name) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("userId", AttributeValue.builder().s(userId).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("name", AttributeValue.builder().s(name).build());
        item.put("createdAt", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("emailVerified", AttributeValue.builder().bool(true).build());
        item.put("status", AttributeValue.builder().s("CONFIRMED").build());
        return item;
    }
}
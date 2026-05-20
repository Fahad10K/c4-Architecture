package com.pizza.delivery.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

import java.net.URI;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.endpoint:}")
    private String endpoint;

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }

    private <B> void applyEndpoint(software.amazon.awssdk.awscore.client.builder.AwsClientBuilder<B, ?> builder) {
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS S3 client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS SQS client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public SnsClient snsClient() {
        var builder = SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS SNS client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public SesClient sesClient() {
        var builder = SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS SES client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        var builder = CognitoIdentityProviderClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Cognito client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public EventBridgeClient eventBridgeClient() {
        var builder = EventBridgeClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS EventBridge client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        var builder = BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Bedrock Runtime client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public KendraClient kendraClient() {
        var builder = KendraClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Kendra client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public PollyClient pollyClient() {
        var builder = PollyClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Polly client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public TranscribeClient transcribeClient() {
        var builder = TranscribeClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Transcribe client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        var builder = SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS Secrets Manager client initialized [region={}]", awsRegion);
        return builder.build();
    }

    @Bean
    public CloudWatchClient cloudWatchClient() {
        var builder = CloudWatchClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider());
        applyEndpoint(builder);
        log.info("AWS CloudWatch client initialized [region={}]", awsRegion);
        return builder.build();
    }
}

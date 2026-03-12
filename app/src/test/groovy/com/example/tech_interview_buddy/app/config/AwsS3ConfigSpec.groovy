package com.example.tech_interview_buddy.app.config

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification
import spock.lang.Subject

class AwsS3ConfigSpec extends Specification {

    @Subject
    AwsS3Config config = new AwsS3Config()

    def "s3Client - Static 자격증명으로 S3Client를 생성한다"() {
        given:
        def properties = new AwsS3Properties()
        properties.setBucketName("test-bucket")
        properties.setRegion("ap-northeast-2")
        properties.setAccessKeyId("test-access-key")
        properties.setSecretAccessKey("test-secret-key")

        when:
        def client = config.s3Client(properties)

        then:
        client != null
        client instanceof S3Client

        cleanup:
        client?.close()
    }

    def "s3Client - region이 null이면 기본 리전으로 S3Client를 생성한다"() {
        given:
        def properties = new AwsS3Properties()
        properties.setBucketName("test-bucket")
        properties.setAccessKeyId("test-access-key")
        properties.setSecretAccessKey("test-secret-key")
        properties.setRegion(null)

        when:
        def client = config.s3Client(properties)

        then:
        client != null

        cleanup:
        client?.close()
    }

    def "s3Client - region이 빈 문자열이면 기본 리전으로 S3Client를 생성한다"() {
        given:
        def properties = new AwsS3Properties()
        properties.setBucketName("test-bucket")
        properties.setAccessKeyId("test-access-key")
        properties.setSecretAccessKey("test-secret-key")
        properties.setRegion("")

        when:
        def client = config.s3Client(properties)

        then:
        client != null

        cleanup:
        client?.close()
    }

    def "s3Client - 버킷이 설정되지 않으면 예외를 던진다"() {
        given:
        def properties = new AwsS3Properties()
        properties.setAccessKeyId("test-access-key")
        properties.setSecretAccessKey("test-secret-key")

        when:
        config.s3Client(properties)

        then:
        def e = thrown(IllegalStateException)
        e.message == "aws.s3.bucket-name must be configured"
    }
}

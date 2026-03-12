package com.example.tech_interview_buddy.app.config

import spock.lang.Specification
import spock.lang.Subject

class AwsS3PropertiesSpec extends Specification {

    @Subject
    AwsS3Properties properties = new AwsS3Properties()

    // === hasStaticCredentials ===

    def "hasStaticCredentials - accessKeyId와 secretAccessKey가 모두 있으면 true를 반환한다"() {
        given:
        properties.setAccessKeyId("test-key")
        properties.setSecretAccessKey("test-secret")

        expect:
        properties.hasStaticCredentials()
    }

    def "hasStaticCredentials - accessKeyId가 null이면 false를 반환한다"() {
        given:
        properties.setAccessKeyId(null)
        properties.setSecretAccessKey("test-secret")

        expect:
        !properties.hasStaticCredentials()
    }

    def "hasStaticCredentials - secretAccessKey가 null이면 false를 반환한다"() {
        given:
        properties.setAccessKeyId("test-key")
        properties.setSecretAccessKey(null)

        expect:
        !properties.hasStaticCredentials()
    }

    def "hasStaticCredentials - accessKeyId가 빈 문자열이면 false를 반환한다"() {
        given:
        properties.setAccessKeyId("")
        properties.setSecretAccessKey("test-secret")

        expect:
        !properties.hasStaticCredentials()
    }

    def "hasStaticCredentials - accessKeyId가 공백만 있으면 false를 반환한다"() {
        given:
        properties.setAccessKeyId("   ")
        properties.setSecretAccessKey("test-secret")

        expect:
        !properties.hasStaticCredentials()
    }

    // === isBucketConfigured ===

    def "isBucketConfigured - bucketName이 있으면 true를 반환한다"() {
        given:
        properties.setBucketName("my-bucket")

        expect:
        properties.isBucketConfigured()
    }

    def "isBucketConfigured - bucketName이 null이면 false를 반환한다"() {
        expect:
        !properties.isBucketConfigured()
    }

    def "isBucketConfigured - bucketName이 빈 문자열이면 false를 반환한다"() {
        given:
        properties.setBucketName("")

        expect:
        !properties.isBucketConfigured()
    }

    def "isBucketConfigured - bucketName이 공백만 있으면 false를 반환한다"() {
        given:
        properties.setBucketName("   ")

        expect:
        !properties.isBucketConfigured()
    }
}

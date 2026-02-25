package com.example.tech_interview_buddy.app.service.resume

import com.example.tech_interview_buddy.app.config.AwsS3Properties
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import spock.lang.Specification
import spock.lang.Subject

class ResumeStorageServiceSpec extends Specification {

    S3Client s3Client = Mock()
    AwsS3Properties properties = Mock()

    @Subject
    ResumeStorageService service = new ResumeStorageService(properties, s3Client)

    // === buildStorageKey ===

    def "buildStorageKey - 정상적인 파일명으로 스토리지 키를 생성한다"() {
        when:
        def key = service.buildStorageKey(1L, 10L, "my_resume.pdf")

        then:
        key == "resumes/1/10/my_resume.pdf"
    }

    def "buildStorageKey - 파일명의 공백을 언더스코어로 변환한다"() {
        when:
        def key = service.buildStorageKey(1L, 10L, "my resume file.pdf")

        then:
        key == "resumes/1/10/my_resume_file.pdf"
    }

    def "buildStorageKey - 파일명이 null이면 기본값 resume.pdf를 사용한다"() {
        when:
        def key = service.buildStorageKey(1L, 10L, null)

        then:
        key == "resumes/1/10/resume.pdf"
    }

    // === uploadFile ===

    def "uploadFile - S3에 파일을 정상적으로 업로드한다"() {
        given:
        def file = Mock(MultipartFile) {
            getContentType() >> "application/pdf"
            getSize() >> 1024L
            getInputStream() >> new ByteArrayInputStream(new byte[1024])
        }
        properties.isBucketConfigured() >> true
        properties.getBucketName() >> "test-bucket"

        when:
        service.uploadFile(file, "resumes/1/10/resume.pdf")

        then:
        1 * s3Client.putObject(_ as PutObjectRequest, _ as RequestBody) >> PutObjectResponse.builder().build()
    }

    def "uploadFile - 버킷이 설정되지 않으면 예외를 던진다"() {
        given:
        def file = Mock(MultipartFile)
        properties.isBucketConfigured() >> false

        when:
        service.uploadFile(file, "resumes/1/10/resume.pdf")

        then:
        def e = thrown(IllegalStateException)
        e.message == "AWS S3 bucket is not configured"
    }

    def "uploadFile - S3 업로드 실패 시 IllegalStateException을 던진다"() {
        given:
        def file = Mock(MultipartFile) {
            getContentType() >> "application/pdf"
            getSize() >> 1024L
            getInputStream() >> new ByteArrayInputStream(new byte[1024])
        }
        properties.isBucketConfigured() >> true
        properties.getBucketName() >> "test-bucket"
        s3Client.putObject(_ as PutObjectRequest, _ as RequestBody) >> {
            throw S3Exception.builder().message("S3 error").build()
        }

        when:
        service.uploadFile(file, "resumes/1/10/resume.pdf")

        then:
        def e = thrown(IllegalStateException)
        e.message == "Failed to upload resume to storage"
    }

    // === deleteFile ===

    def "deleteFile - S3에서 파일을 정상적으로 삭제한다"() {
        given:
        properties.isBucketConfigured() >> true
        properties.getBucketName() >> "test-bucket"

        when:
        service.deleteFile("resumes/1/10/resume.pdf")

        then:
        1 * s3Client.deleteObject(_ as DeleteObjectRequest) >> DeleteObjectResponse.builder().build()
    }

    def "deleteFile - 버킷이 설정되지 않으면 예외를 던진다"() {
        given:
        properties.isBucketConfigured() >> false

        when:
        service.deleteFile("resumes/1/10/resume.pdf")

        then:
        def e = thrown(IllegalStateException)
        e.message == "AWS S3 bucket is not configured"
    }

    def "deleteFile - S3 삭제 실패 시 IllegalStateException을 던진다"() {
        given:
        properties.isBucketConfigured() >> true
        properties.getBucketName() >> "test-bucket"
        s3Client.deleteObject(_ as DeleteObjectRequest) >> {
            throw S3Exception.builder().message("S3 error").build()
        }

        when:
        service.deleteFile("resumes/1/10/resume.pdf")

        then:
        def e = thrown(IllegalStateException)
        e.message == "Failed to delete resume from storage"
    }
}

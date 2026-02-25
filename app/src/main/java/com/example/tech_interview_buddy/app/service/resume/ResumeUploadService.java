package com.example.tech_interview_buddy.app.service.resume;

import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.repository.resume.ResumeRepository;
import com.example.tech_interview_buddy.domain.resume.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeUploadService {

    private final ResumeFileValidator fileValidator;
    private final ResumeStorageService storageService;
    private final ResumeRepository resumeRepository;

    @Transactional
    public Resume upload(User user, MultipartFile file) {
        fileValidator.validate(file);

        Resume resume = resumeRepository.save(
            Resume.createUploaded(
                user,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                "temp"
            )
        );

        String storageKey = storageService.buildStorageKey(user.getId(), resume.getId(), file.getOriginalFilename());
        storageService.uploadFile(file, storageKey);
        resume.updateStorageKey(storageKey);

        log.info("Resume uploaded - id: {}, userId: {}, filename: {}", resume.getId(), user.getId(), file.getOriginalFilename());
        return resume;
    }
}

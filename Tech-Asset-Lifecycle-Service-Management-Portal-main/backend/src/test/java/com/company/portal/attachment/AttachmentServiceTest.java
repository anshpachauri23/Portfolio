package com.company.portal.attachment;

import com.company.portal.attachment.domain.Attachment;
import com.company.portal.attachment.dto.AttachmentResponse;
import com.company.portal.attachment.repository.AttachmentRepository;
import com.company.portal.attachment.service.AttachmentService;
import com.company.portal.attachment.service.StorageService;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock private AttachmentRepository attachmentRepository;
    @Mock private StorageService storageService;
    @Mock private UserRepository userRepository;

    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService(attachmentRepository, storageService, userRepository);
    }

    private User uploader() {
        User u = new User();
        u.setId(1L);
        u.setName("Alice Admin");
        u.setEmail("alice@company.com");
        return u;
    }

    @Test
    void upload_savesAttachmentRecordAndCallsStorage() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "pdf content".getBytes());

        given(userRepository.findByEmail("alice@company.com")).willReturn(Optional.of(uploader()));
        given(storageService.store(any(MultipartFile.class), eq("SERVICE_REQUEST"), eq("100")))
                .willReturn("SERVICE_REQUEST/100/uuid_report.pdf");
        given(attachmentRepository.save(any())).willAnswer(inv -> {
            Attachment a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AttachmentResponse result = attachmentService.upload("SERVICE_REQUEST", "100", file, "alice@company.com");

        assertThat(result.fileName()).isEqualTo("report.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.fileSize()).isEqualTo(11L);
        assertThat(result.uploadedByName()).isEqualTo("Alice Admin");

        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo("SERVICE_REQUEST");
        assertThat(captor.getValue().getEntityId()).isEqualTo("100");
        assertThat(captor.getValue().getStorageKey()).isEqualTo("SERVICE_REQUEST/100/uuid_report.pdf");
    }

    @Test
    void upload_unknownUploader_throwsNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);
        given(userRepository.findByEmail("unknown@company.com")).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                attachmentService.upload("SERVICE_REQUEST", "1", file, "unknown@company.com")
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upload_storageFailure_throwsRuntimeException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[]{1});
        given(userRepository.findByEmail("alice@company.com")).willReturn(Optional.of(uploader()));
        given(storageService.store(any(), any(), any())).willThrow(new IOException("Disk full"));

        assertThatThrownBy(() ->
                attachmentService.upload("SERVICE_REQUEST", "1", file, "alice@company.com")
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Disk full");
    }

    @Test
    void listAttachments_returnsOrderedList() {
        Attachment a1 = attachmentWithId(1L, "doc1.pdf");
        Attachment a2 = attachmentWithId(2L, "doc2.pdf");
        given(attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("SERVICE_REQUEST", "5"))
                .willReturn(List.of(a1, a2));

        List<AttachmentResponse> result = attachmentService.listAttachments("SERVICE_REQUEST", "5");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).fileName()).isEqualTo("doc1.pdf");
        assertThat(result.get(1).fileName()).isEqualTo("doc2.pdf");
    }

    @Test
    void download_loadsResourceFromStorage() throws IOException {
        Attachment attachment = attachmentWithId(10L, "screenshot.png");
        attachment.setContentType("image/png");
        attachment.setStorageKey("SERVICE_REQUEST/1/uuid_screenshot.png");

        Resource mockResource = new ByteArrayResource("image bytes".getBytes());

        given(attachmentRepository.findById(10L)).willReturn(Optional.of(attachment));
        given(storageService.load("SERVICE_REQUEST/1/uuid_screenshot.png")).willReturn(mockResource);

        AttachmentService.DownloadResult result = attachmentService.download(10L);

        assertThat(result.fileName()).isEqualTo("screenshot.png");
        assertThat(result.contentType()).isEqualTo("image/png");
        assertThat(result.resource()).isEqualTo(mockResource);
    }

    @Test
    void download_attachmentNotFound_throwsNotFound() {
        given(attachmentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.download(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void download_storageFailure_throwsRuntimeException() throws IOException {
        Attachment attachment = attachmentWithId(5L, "broken.pdf");
        attachment.setStorageKey("missing/key");
        given(attachmentRepository.findById(5L)).willReturn(Optional.of(attachment));
        given(storageService.load("missing/key")).willThrow(new IOException("File not found"));

        assertThatThrownBy(() -> attachmentService.download(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File not found");
    }

    private Attachment attachmentWithId(Long id, String fileName) {
        Attachment a = new Attachment();
        a.setId(id);
        a.setFileName(fileName);
        a.setFileSize(100L);
        a.setEntityType("SERVICE_REQUEST");
        a.setEntityId("5");
        a.setStorageKey("SERVICE_REQUEST/5/" + fileName);
        a.setUploadedBy(uploader());
        return a;
    }
}

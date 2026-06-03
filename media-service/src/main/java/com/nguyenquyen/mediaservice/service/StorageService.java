package com.nguyenquyen.mediaservice.service;

import com.nguyenquyen.mediaservice.dto.response.FileResponse;
import com.nguyenquyen.mediaservice.dto.response.PreSignedResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    FileResponse uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String fileKey);
    PreSignedResponse generatePresignedUrl(String fileName);
}

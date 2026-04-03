package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.receipt.ReceiptDto;
import com.costrip.costrip_backend.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final OcrService ocrService;

    @PostMapping("/analyze")
    public ResponseEntity<ReceiptDto> analyze(@RequestBody Map<String, String> body) {
        String base64 = body.get("imageBase64");
        if (base64 == null || base64.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ReceiptDto result = ocrService.analyze(base64);
        return ResponseEntity.ok(result);
    }
}
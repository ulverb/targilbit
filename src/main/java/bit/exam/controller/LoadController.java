package bit.exam.controller;

import bit.exam.service.TransactionEnrichmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class LoadController {

    private TransactionEnrichmentService enrichmentService;

    @Autowired
    public LoadController(TransactionEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    @PostMapping("/fraud/transaction/{fileName}")
    public ResponseEntity uploadTransactionsFile(@RequestParam("fileName") MultipartFile file){

        try {
            enrichmentService.proceedFile(file);
        } catch (IOException e) {
            return new ResponseEntity("Server Error ", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity("POST Response: \"File " + file.getOriginalFilename() + " uploaded successfully", HttpStatus.OK);

    }

    @GetMapping("/fraud/transaction/{fileName}")
    public ResponseEntity getEnrichedFile(@PathVariable String fileName){

        Resource resource = null;
        try {
            resource = enrichmentService.getEnrichedFile(fileName);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }


        if (resource == null) {
            return new ResponseEntity("File not found ", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}

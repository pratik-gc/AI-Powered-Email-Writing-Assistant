package com.email.writer.app;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailWriterController {


    private final EmailWriterService emailWriterService;

//    public EmailWriterController(EmailWriterService emailWriterService) {
//        this.emailWriterService = emailWriterService;
//    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateEmailReply(@RequestBody EmailRequest emailRequest){
        String response = emailWriterService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}

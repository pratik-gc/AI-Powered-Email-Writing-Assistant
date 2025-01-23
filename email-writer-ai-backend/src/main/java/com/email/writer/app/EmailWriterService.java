package com.email.writer.app;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailWriterService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public EmailWriterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){
        //Building the prompt as per Gemini Format
        String prompt = buildPrompt(emailRequest);

        //Crafting a request for Gemini API Consumption which is as follows:
//        {
//            "contents": [{
//            "parts":[{"text": "What are coordinates of Pune?"}]
//            }]
//        }
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })

                }
        );

        //Making the actual request to the Gemini Server and getting back the response
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //Extract the relevant content of the response of Gemini API and Return it
        //Response Format of Google Gemini API is as follows:
//        {
//            "candidates": [
//            {
//                "content": {
//                "parts": [
//                {
//                    "text": "Pune doesn't have one single coordinate, as it's a city with an area.  However, the coordinates for the **center** of Pune are approximately:\n\n**18.5204° N, 73.8567° E**\n"
//                }
//                ],
//                "role": "model"
//            },
//                "finishReason": "STOP",
//                    "avgLogprobs": -0.066958731618420836
//            }
//    ],
//            "usageMetadata": {
//            "promptTokenCount": 8,
//                    "candidatesTokenCount": 58,
//                    "totalTokenCount": 66
//        },
//            "modelVersion": "gemini-1.5-flash"
//        }
        return extractedResponseContent(response);

    }


    //Building the prompt as per Gemini Format
    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email. Don't generate a subject line. ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }

    //Extract the relevant content of the response of Gemini API
    private String extractedResponseContent(String response) {
        try {
            //ObjectMapper is tool from Jackson library which helps in working with JSON data.
            //It can read, write & convert JSON object into Java object and vice-versa.
            ObjectMapper mapper = new ObjectMapper();
            //Iterate through the JSON Response
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error in processing the request: " + e.getMessage();
        }
    }

}

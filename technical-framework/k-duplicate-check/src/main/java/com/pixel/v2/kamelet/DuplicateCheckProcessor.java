package com.pixel.v2.kamelet;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor to compute SHA-1 checksum for duplicate check
 */
@Component("duplicateCheckProcessor")
public class DuplicateCheckProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Get the message body
        String body = exchange.getIn().getBody(String.class);
        
        // Compute SHA-1 checksum
        String checksum = computeSHA1(body != null ? body : "");
        
        // Set checksum as exchange property
        exchange.setProperty("checksum", checksum);
        
        // Also compute and set file size
        int fileSize = body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0;
        exchange.setProperty("fileSize", fileSize);
    }
    
    private String computeSHA1(String input) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // Convert byte array to hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
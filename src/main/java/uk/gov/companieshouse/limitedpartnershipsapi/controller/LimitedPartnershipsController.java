package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitedPartnershipsController {
    @PostMapping("/")
    public ResponseEntity<String> createNewSubmission() {
        return ResponseEntity.ok().body("This is the Limited Partnerships API");
    }
}

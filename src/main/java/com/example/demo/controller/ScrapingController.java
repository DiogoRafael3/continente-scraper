package com.example.demo.controller;

import com.example.demo.service.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrapingController {

    @Autowired
    private final ScrapingService scraperService;


    @GetMapping("/scrape")
    public String scrapeProducts() {
        scraperService.scrapeProductData();
        return "Scraping completed!";
    }

}

package com.myapp.news.controllers;

import com.myapp.news.dtos.NewsContent;
import com.myapp.news.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
public class NewsController {

    public NewsService newsService;
    @Autowired
    public NewsController(NewsService newsService){
        this.newsService = newsService;
    }

    @PostMapping("/create")
    public String createNewsContent(@RequestBody NewsContent newsContent) throws ExecutionException, InterruptedException {
        return newsService.createContent(newsContent);
    }
    @GetMapping("/get")
    public NewsContent getContent(@RequestBody String newsId) throws ExecutionException, InterruptedException {
        return newsService.getNewsById(newsId);
    }

    @PutMapping("/update")
    public String updateNewsContent(@RequestBody NewsContent newsContent){
        return newsService.updateContent(newsContent);
    }

    @DeleteMapping("/delete")
    public String deleteContent(@RequestBody String newsId){
        return newsService.deleteNewsById(newsId);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is working");
    }

}

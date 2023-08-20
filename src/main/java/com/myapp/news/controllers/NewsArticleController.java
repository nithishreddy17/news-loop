package com.myapp.news.controllers;

import com.myapp.news.dtos.NewsArticle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/news-articles")
public class NewsArticleController {

    private final List<NewsArticle> newsArticleList = new ArrayList<>();
    private long newsArticleIdCounter = 1;

    @PostMapping
    @Operation(summary = "Create a new news article", description = "Create a new news article and assign a unique newsArticleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "News article created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<NewsArticle> createNewsArticle(@RequestBody NewsArticle newNewsArticle) {
        newNewsArticle.setNewsArticleId(newsArticleIdCounter++);
        newNewsArticle.setPostedOn(new Date());
        newNewsArticle.setLastModified(new Date());
        newsArticleList.add(newNewsArticle);
        return ResponseEntity.status(HttpStatus.CREATED).body(newNewsArticle);
    }

    @GetMapping
    @Operation(summary = "Get all news articles", description = "Retrieve a list of all news articles.")
    @ApiResponse(responseCode = "200", description = "List of news articles", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = List.class, type = "NewsArticle"))
    })
    public ResponseEntity<List<NewsArticle>> getAllNewsArticles() {
        return ResponseEntity.ok(newsArticleList);
    }

    @GetMapping("/{newsArticleId}")
    @Operation(summary = "Get news article by ID", description = "Retrieve a news article by its newsArticleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News article found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "404", description = "News article not found"),
    })
    public ResponseEntity<NewsArticle> getNewsArticleById(@PathVariable long newsArticleId) {
        Optional<NewsArticle> newsArticle = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == newsArticleId)
                .findFirst();

        return newsArticle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{newsArticleId}")
    @Operation(summary = "Update news article by ID", description = "Update an existing news article by its newsArticleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News article updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "404", description = "News article not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
            @ApiResponse(responseCode = "400", description = "Invalid News article")
    })
    public ResponseEntity<NewsArticle> updateNewsArticle(@PathVariable long newsArticleId, @RequestBody NewsArticle updatedNewsArticle) {
        Optional<NewsArticle> newsArticleToUpdate = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == newsArticleId)
                .findFirst();

        if (newsArticleToUpdate.isPresent()) {
            NewsArticle existingNewsArticle = newsArticleToUpdate.get();
            existingNewsArticle.setTitle(updatedNewsArticle.getTitle());
            existingNewsArticle.setContent(updatedNewsArticle.getContent());
            existingNewsArticle.setLastModified(new Date());
            return ResponseEntity.ok(existingNewsArticle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{newsArticleId}")
    @Operation(summary = "Delete news article by ID", description = "Delete a news article by its newsArticleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "News article deleted"),
            @ApiResponse(responseCode = "404", description = "News article not found"),
            @ApiResponse(responseCode = "400", description = "Invalid News article "),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
    })
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable long newsArticleId) {
        Optional<NewsArticle> newsArticleToDelete = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == newsArticleId)
                .findFirst();

        if (newsArticleToDelete.isPresent()) {
            newsArticleList.remove(newsArticleToDelete.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

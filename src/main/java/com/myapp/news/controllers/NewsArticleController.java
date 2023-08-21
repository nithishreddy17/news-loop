package com.myapp.news.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.myapp.news.dtos.Comment;
import com.myapp.news.dtos.NewsArticle;
import com.myapp.news.utils.CommentJsonFileHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/articles")
public class NewsArticleController {

    private final List<NewsArticle> newsArticleList = new ArrayList<>();
    private long articleIdCounter = 1;

    @Value("${comments.file.path}")
    private String commentsFilePath;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @PostMapping
    @Operation(summary = "Create a new news article", description = "Create a new news article and assign a unique articleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "News article created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<NewsArticle> createNewsArticle(@RequestBody NewsArticle newNewsArticle) {
        newNewsArticle.setNewsArticleId(articleIdCounter++);
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

    @GetMapping("/{articleId}")
    @Operation(summary = "Get news article by ID", description = "Retrieve a news article by its articleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News article found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "404", description = "News article not found"),
    })
    public ResponseEntity<NewsArticle> getNewsArticleById(@PathVariable long articleId) {
        Optional<NewsArticle> newsArticle = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == articleId)
                .findFirst();

        return newsArticle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{articleId}")
    @Operation(summary = "Update news article by ID", description = "Update an existing news article by its articleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "News article updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewsArticle.class))
            }),
            @ApiResponse(responseCode = "404", description = "News article not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
            @ApiResponse(responseCode = "400", description = "Invalid News article")
    })
    public ResponseEntity<NewsArticle> updateNewsArticle(@PathVariable long articleId, @RequestBody NewsArticle updatedNewsArticle) {
        Optional<NewsArticle> newsArticleToUpdate = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == articleId)
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

    @DeleteMapping("/{articleId}")
    @Operation(summary = "Delete news article by ID", description = "Delete a news article by its articleId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "News article deleted"),
            @ApiResponse(responseCode = "404", description = "News article not found"),
            @ApiResponse(responseCode = "400", description = "Invalid News article "),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges"),
    })
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable long articleId) {
        Optional<NewsArticle> newsArticleToDelete = newsArticleList.stream()
                .filter(article -> article.getNewsArticleId() == articleId)
                .findFirst();

        if (newsArticleToDelete.isPresent()) {
            newsArticleList.remove(newsArticleToDelete.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{articleId}/comments")
    @Operation(summary = "Get comments by News Article ID", description = "Retrieve comments by the ID of the associated news article.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            }),
            @ApiResponse(responseCode = "404", description = "Comments not found")
    })
    public ResponseEntity<Page<Comment>> getCommentsByArticleId(
            @PathVariable long articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "commentedOn") String sortBy,
            @RequestHeader(defaultValue = "V1",name = "Accept-Version", required = false) String apiVersion) throws IOException {

        // Read the comments from the local JSON file
        List<Comment> comments = readCommentsFromJsonFile();
        List<Comment> filteredComments = filterCommentsByArticleId(comments, articleId);
        // Create a PageRequest for pagination and sorting
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        filteredComments.sort((c1, c2) -> c2.getCommentedOn().compareTo(c1.getCommentedOn()));

        // Create a Page object with the specified page, size, and sorted comments
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredComments.size());
        Page<Comment> commentPage = new PageImpl<>(filteredComments.subList(start, end), pageRequest, filteredComments.size());

        return ResponseEntity.ok(commentPage);
    }

    private List<Comment> filterCommentsByArticleId(List<Comment> comments, long newsArticleId) {
        return comments.stream()
                .filter(comment -> comment.getNewsArticleId() == newsArticleId)
                .collect(Collectors.toList());
    }

    public List<Comment> readCommentsFromJsonFile() throws IOException {
        Resource resource = new ClassPathResource(commentsFilePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Comment>>() {});
    }


}

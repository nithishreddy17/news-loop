package com.myapp.news.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.news.dtos.Comment;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentJsonFileHandler jsonFileHandler;
    private final ObjectMapper objectMapper;
    private final String commentsFilePath;

    private final List<Comment> commentList = new ArrayList<>();

    private long commentIdCounter = 50;

    public CommentController(ObjectMapper objectMapper, @Value("${comments.file.path}") String commentsFilePath) {
        this.objectMapper = objectMapper;
        this.commentsFilePath = commentsFilePath;
    }

    @GetMapping("/getCommentsByNewsId/{newsArticleId}")
    @Operation(summary = "Get comments by News Article ID", description = "Retrieve comments by the ID of the associated news article.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            }),
            @ApiResponse(responseCode = "404", description = "Comments not found")
    })
    public ResponseEntity<Page<Comment>> getCommentsByArticleId(
            @PathVariable long newsArticleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "commentedOn") String sortBy,
            @RequestHeader(name = "Accept-Version", required = false) String apiVersion) throws IOException {

        // Read the comments from the local JSON file
        List<Comment> comments = readCommentsFromJsonFile();
        List<Comment> filteredComments = filterCommentsByArticleId(comments, newsArticleId);
        // Create a PageRequest for pagination and sorting
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        filteredComments.sort((c1, c2) -> c2.getCommentedOn().compareTo(c1.getCommentedOn()));

        // Create a Page object with the specified page, size, and sorted comments
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredComments.size());
        Page<Comment> commentPage = new PageImpl<>(filteredComments.subList(start, end), pageRequest, filteredComments.size());

        return ResponseEntity.ok(commentPage);
    }

    @PostMapping
    @Operation(summary = "Create a new comment", description = "Create a new comment associated with a news article.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))
            }),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<Comment> createComment(@RequestBody Comment newComment) {
        try {
            List<Comment> comments = jsonFileHandler.readComments();
            newComment.setCommentId(commentIdCounter++);
            comments.add(newComment);
            jsonFileHandler.writeComments(comments);
            return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateComment/{commentId}")
    @Operation(summary = "Update an existing comment", description = "Update an existing comment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))
            }),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<Comment> updateComment(
            @PathVariable long commentId,
            @RequestBody Comment updatedComment) {
        try {
            List<Comment> comments = jsonFileHandler.readComments();

            // Find the existing comment by its commentId
            Optional<Comment> existingComment = comments.stream()
                    .filter(comment -> comment.getCommentId() == commentId)
                    .findFirst();

            if (existingComment.isPresent()) {
                // Update the content of the existing comment
                Comment foundComment = existingComment.get();
                foundComment.setText(updatedComment.getText());
                jsonFileHandler.writeComments(comments);
                return ResponseEntity.ok(foundComment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteComment/{commentId}")
    @Operation(summary = "Delete a comment by ID", description = "Delete a comment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<Void> deleteComment(@PathVariable long commentId) {
        try {
            List<Comment> comments = jsonFileHandler.readComments();

            Optional<Comment> commentToRemove = comments.stream()
                    .filter(comment -> comment.getCommentId() == commentId)
                    .findFirst();

            if (commentToRemove.isPresent()) {
                comments.remove(commentToRemove.get());

                jsonFileHandler.writeComments(comments);

                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<Comment> readCommentsFromJsonFile() throws IOException {
        Resource resource = new ClassPathResource(commentsFilePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Comment>>() {});
    }

    private List<Comment> filterCommentsByArticleId(List<Comment> comments, long newsArticleId) {
        return comments.stream()
                .filter(comment -> comment.getNewsArticleId() == newsArticleId)
                .collect(Collectors.toList());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reading comments from JSON file: " + e.getMessage());
    }


}

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

import static com.myapp.news.utils.CommentJsonFileHandler.filterCommentsByArticleId;

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

    @GetMapping("/{commentId}")
    @Operation(summary = "Get a comment by Comment ID", description = "Retrieve a comment by its unique Comment ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))
            }),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Comment> getCommentByCommentId(
            @PathVariable long commentId,
            @RequestHeader(name = "Accept-Version", required = false) String apiVersion) throws IOException {

        // Read the comments from the local JSON file or your data source
        List<Comment> comments = jsonFileHandler.readCommentsFromJsonFile();

        // Find the comment with the specified commentId
        Optional<Comment> foundComment = comments.stream()
                .filter(comment -> comment.getCommentId() == commentId)
                .findFirst();

        // If the comment is found, return it; otherwise, return a 404 response
        if (foundComment.isPresent()) {
            return ResponseEntity.ok(foundComment.get());
        } else {
            return ResponseEntity.notFound().build();
        }
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

    @PutMapping("/{commentId}")
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

    @DeleteMapping("/{commentId}")
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


    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reading comments from JSON file: " + e.getMessage());
    }


}

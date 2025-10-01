package com.example.demo.controller;

import com.example.demo.entity.BroadcastPost;
import com.example.demo.repository.BroadcastPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/broadcast")
public class BroadcastPostController {
    private final BroadcastPostRepository repo;

    public BroadcastPostController(BroadcastPostRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> getPosts() {
        try {
            List<BroadcastPost> posts = repo.findTop8ByOrderByCreatedAtDesc();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error fetching posts. Please try again later.");
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> addPost(@RequestBody BroadcastPost post) {
        try {
            post.setId(null);
            BroadcastPost saved = repo.save(post);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error saving post. Please try again later.");
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            repo.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error deleting post. Please try again later.");
            return ResponseEntity.status(500).body(error);
        }
    }
}
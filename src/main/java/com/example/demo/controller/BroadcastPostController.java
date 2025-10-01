package com.example.demo.controller;

import com.example.demo.entity.BroadcastPost;
import com.example.demo.repository.BroadcastPostRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/broadcast")
public class BroadcastPostController {
    private final BroadcastPostRepository repo;

    public BroadcastPostController(BroadcastPostRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<BroadcastPost> getPosts() {
        return repo.findTop8ByOrderByCreatedAtDesc();
    }

    @PostMapping
    public BroadcastPost addPost(@RequestBody BroadcastPost post) {
        post.setId(null);
        return repo.save(post);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
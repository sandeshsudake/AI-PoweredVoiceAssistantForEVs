package com.example.demo.repository;

import com.example.demo.entity.BroadcastPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BroadcastPostRepository extends JpaRepository<BroadcastPost, Long> {
    List<BroadcastPost> findTop8ByOrderByCreatedAtDesc();
}
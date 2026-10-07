package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.model.HealthBlog;
import com.example.onlinepharmacy.repository.HealthBlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogApiController {

    @Autowired
    private HealthBlogRepository blogRepository;

    @GetMapping
    public List<HealthBlog> getBlogs(@RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return blogRepository.findByCategory(category);
        }
        return blogRepository.findAll();
    }

    @GetMapping("/{id}")
    public HealthBlog getBlogById(@PathVariable Long id) {
        return blogRepository.findById(id).orElse(null);
    }
}

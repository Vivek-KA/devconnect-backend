package devconnect_backend.controller;

import devconnect_backend.entity.Post;
import devconnect_backend.entity.User;
import devconnect_backend.repository.PostRepository;
import devconnect_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all posts — public, no login needed
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Get a single post by ID — public
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new post — requires login
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        post.setId(null);
        post.setAuthor(userOptional.get());
        post.setCreatedAt(java.time.LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Post updatedPost, Authentication authentication) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Post existingPost = postOptional.get();
        String email = authentication.getName();

        if (!existingPost.getAuthor().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own posts"));
        }

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setImageUrl(updatedPost.getImageUrl());

        Post savedPost = postRepository.save(existingPost);
        return ResponseEntity.ok(savedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Post post = postOptional.get();
        String email = authentication.getName();

        if (!post.getAuthor().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own posts"));
        }

        postRepository.delete(post);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }
}

package devconnect_backend.controller;

import devconnect_backend.entity.Like;
import devconnect_backend.entity.Post;
import devconnect_backend.entity.User;
import devconnect_backend.repository.LikeRepository;
import devconnect_backend.repository.PostRepository;
import devconnect_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Get like count for a post — public
    @GetMapping
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        long count = likeRepository.countByPostId(postId);
        return ResponseEntity.ok(Map.of("likeCount", count));
    }

    // Toggle like/unlike — requires login
    @PostMapping
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Authentication authentication) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Post not found"));
        }

        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        Post post = postOptional.get();

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, user.getId());

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return ResponseEntity.ok(Map.of("liked", false, "message", "Post unliked"));
        } else {
            Like newLike = new Like();
            newLike.setPost(post);
            newLike.setUser(user);
            likeRepository.save(newLike);
            return ResponseEntity.ok(Map.of("liked", true, "message", "Post liked"));
        }
    }
}

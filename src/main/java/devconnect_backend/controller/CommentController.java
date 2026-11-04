package devconnect_backend.controller;

import devconnect_backend.entity.Comment;
import devconnect_backend.entity.Post;
import devconnect_backend.entity.User;
import devconnect_backend.repository.CommentRepository;
import devconnect_backend.repository.PostRepository;
import devconnect_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all comments for a post — public
    @GetMapping
    public List<Comment> getCommentsForPost(@PathVariable Long postId) {
        return commentRepository.findByPostId(postId);
    }

    // Add a comment to a post — requires login
    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Comment comment, Authentication authentication) {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Post not found"));
        }

        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        comment.setId(null);
        comment.setPost(postOptional.get());
        comment.setAuthor(userOptional.get());
        comment.setCreatedAt(java.time.LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return ResponseEntity.ok(savedComment);
    }

    // Delete your own comment — requires login + ownership
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication authentication) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Comment not found"));
        }

        Comment comment = commentOptional.get();
        String email = authentication.getName();

        if (!comment.getAuthor().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own comments"));
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }
}

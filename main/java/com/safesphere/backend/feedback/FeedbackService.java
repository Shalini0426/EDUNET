package com.safesphere.backend.feedback;

import com.safesphere.backend.account.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedback;
    private final UserAccountService accounts;

    public FeedbackService(FeedbackRepository feedback, UserAccountService accounts) {
        this.feedback = feedback;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<Feedback> findAllForUser(String email) {
        return feedback.findAllByAuthorEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        return feedback.findAll();
    }

    @Transactional(readOnly = true)
    public double averageRating() {
        return feedback.calculateAverageRating().orElse(0.0);
    }

    @Transactional
    public Feedback create(String email, int rating, String comments) {
        Feedback entry = new Feedback();
        entry.setAuthor(accounts.requireByEmail(email));
        apply(entry, rating, comments);
        return feedback.save(entry);
    }

    @Transactional
    public Feedback update(String email, Long id, int rating, String comments, boolean administrator) {
        Feedback entry = administrator ? feedback.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found."))
                : requireOwned(email, id);
        apply(entry, rating, comments);
        return feedback.save(entry);
    }

    @Transactional
    public void delete(String email, Long id, boolean administrator) {
        Feedback entry = administrator ? feedback.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found."))
                : requireOwned(email, id);
        feedback.delete(entry);
    }

    private Feedback requireOwned(String email, Long id) {
        return feedback.findByIdAndAuthorEmailIgnoreCase(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found."));
    }

    private void apply(Feedback entry, int rating, String comments) {
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
        }
        if (comments == null || comments.isBlank() || comments.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback must contain between 1 and 2000 characters.");
        }
        entry.setRating(rating);
        entry.setComments(comments.trim());
    }
}

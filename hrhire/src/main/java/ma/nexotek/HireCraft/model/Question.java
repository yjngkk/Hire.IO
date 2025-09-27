package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer correctAnswer;

    // FIXED: Added orphanRemoval = true
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @JsonIgnore
    private Exercise exercise;

    // Helper method to properly manage the bidirectional relationship
    public void setAnswers(List<Answer> answers) {
        // Clear existing answers
        if (this.answers == null) {
            this.answers = new ArrayList<>();
        }
        this.answers.clear();

        // Add new answers and set the parent relationship
        if (answers != null) {
            for (Answer answer : answers) {
                answer.setQuestion(this);
                this.answers.add(answer);
            }
        }
    }

    // Helper method to add a single answer
    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new ArrayList<>();
        }
        answer.setQuestion(this);
        this.answers.add(answer);
    }

    // Helper method to remove a single answer
    public void removeAnswer(Answer answer) {
        if (this.answers != null) {
            this.answers.remove(answer);
            answer.setQuestion(null);
        }
    }
}
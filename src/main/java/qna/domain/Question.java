package qna.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import qna.CannotDeleteException;
import qna.domain.message.ExceptionMessage;

@Entity
public class Question extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Title title;

    @Embedded
    private Contents contents;

    @ManyToOne
    @JoinColumn(name = "writer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @Column(nullable = false)
    private boolean deleted = false;

    @Embedded
    private Answers answers = Answers.init();

    public Question(String title, String contents) {
        this(null, title, contents);
    }

    public Question(Long id, String title, String contents) {
        this.id = id;
        this.title = Title.from(title);
        this.contents = Contents.from(contents);
    }

    protected Question() {
    }

    public Question writeBy(User writer) {
        this.writer = writer;
        return this;
    }

    public boolean isOwner(User writer) {
        return this.writer.equals(writer);
    }

    public void addAnswer(Answer answer) {
        if (answers.contains(answer)) {
            return;
        }
        answers.add(answer);
        answer.toQuestion(this);
    }

    public void removeAnswer(Answer answer) {
        if (!answers.contains(answer)) {
            return;
        }
        answers.remove(answer);
        answer.toQuestion(null);
    }

    public Long getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void deleted() {
        this.deleted = true;
    }

    public DeleteHistories delete(User writer) throws CannotDeleteException {
        if (!isOwner(writer)) {
            throw new CannotDeleteException(ExceptionMessage.NO_PERMISSION_DELETE_QUESTION);
        }

        this.deleted();

        return getDeleteHistories(writer);
    }

    private DeleteHistories getDeleteHistories(User writer) throws CannotDeleteException {
        List<DeleteHistory> deleteHistories = new ArrayList<>();

        deleteHistories.add(DeleteHistory.ofQuestion(id, writer));
        deleteHistories.addAll(answers.delete(writer));

        return DeleteHistories.from(deleteHistories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Question question = (Question) o;
        return deleted == question.deleted &&
                Objects.equals(id, question.id) &&
                title.equals(question.title) &&
                Objects.equals(contents, question.contents) &&
                writer.equals(question.writer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, contents, writer, deleted);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", writerId=" + writer.getId() +
                ", deleted=" + deleted +
                '}';
    }
}

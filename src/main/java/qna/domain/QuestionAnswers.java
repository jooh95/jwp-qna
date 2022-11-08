package qna.domain;

import qna.CannotDeleteException;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class QuestionAnswers {

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>();

    public QuestionAnswers() {
    }

    public void add(Answer answer) {
        answers.add(answer);
    }

    public Collection<DeleteHistory> getAnswerDeleteHistories(User loginUser) throws CannotDeleteException {
        Collection<DeleteHistory> newDeleteHistories = new ArrayList<>();
        for (Answer answer : getNotDeletedAnswers()) {
            newDeleteHistories.add(answer.delete(loginUser));
        }
        return newDeleteHistories;
    }

    private List<Answer> getNotDeletedAnswers() {
        return this.answers.stream()
                .filter(answer -> !answer.isDeleted()).collect(Collectors.toList());
    }

    public boolean isDeletable(User loginUser) throws CannotDeleteException {
        if (noAnswerExist()) {
            return true;
        }
        if (allAnswerWrittenBySameUser(loginUser)) {
            return true;
        }
        throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
    }

    private boolean allAnswerWrittenBySameUser(User loginUser) {
        return answers.stream().allMatch(answer -> answer.isOwner(loginUser));
    }

    private boolean noAnswerExist() {
        return answers.isEmpty();
    }
}

package qna.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DeleteHistoryRepositoryTest extends NewEntityTestBase {

    @Autowired
    private DeleteHistoryRepository repository;

    @Test
    @DisplayName("저장된 entity는 id가 양수이고 각각 지정한 content를 문자열로 가진다")
    void test1() {
        List<Question> newUser1Questions = questionRepository.findByWriterAndDeletedFalse(NEWUSER1);
        List<Answer> newUser1Answers = answerRepository.findByWriterAndDeletedFalse(NEWUSER1);

        List<DeleteHistory> deleteHistories = Arrays.asList(
                new DeleteHistory(ContentType.QUESTION, newUser1Questions.get(0).getId(), NEWUSER1, LocalDateTime.now()),
                new DeleteHistory(ContentType.ANSWER, newUser1Answers.get(0).getId(), NEWUSER1, LocalDateTime.now())
        );

        List<DeleteHistory> deleted = repository.saveAll(deleteHistories);

        assertAll(
                () -> assertThat(deleted.size()).isEqualTo(2),
                () -> assertTrue(deleted.stream().allMatch(DeleteHistory::saved)),
                () -> assertTrue(deleted.get(0).hasContent(ContentType.QUESTION)),
                () -> assertTrue(deleted.get(1).hasContent(ContentType.ANSWER))
        );
    }
}
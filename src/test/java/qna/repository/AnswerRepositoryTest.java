package qna.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import qna.CannotDeleteException;
import qna.constant.ErrorCode;
import qna.domain.Answer;
import qna.domain.Question;
import qna.domain.TestAnswerFactory;
import qna.domain.TestQuestionFactory;
import qna.domain.TestUserFactory;
import qna.domain.User;

@DataJpaTest
public class AnswerRepositoryTest {

    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Test
    void 답변을_저장하면_저장한_답변을_반환한다() {
        //given
        User writer = TestUserFactory.create("javajigi");
        Question question = TestQuestionFactory.create(writer);
        Answer answer = TestAnswerFactory.create(writer, question);

        //when
        Answer saveAnswer = answerRepository.save(answer);

        //then
        assertAll(
                () -> assertThat(saveAnswer.getId()).isNotNull(),
                () -> assertThat(saveAnswer.getContents()).isEqualTo(answer.getContents()),
                () -> assertThat(saveAnswer.isDeleted()).isFalse()
        );
    }

    @Test
    void 답변의_삭제여부를_참으로_바꾸면_조회할_수_없다() {
        //given
        User writer = TestUserFactory.create("sanjigi");
        Question question = TestQuestionFactory.create(writer);
        Answer answer = TestAnswerFactory.create(writer, question);
        Answer saveAnswer = answerRepository.save(answer);
        Long saveAnswerId = saveAnswer.getId();

        //when
        saveAnswer.delete(writer);
        Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(saveAnswerId);

        //then
        assertThat(findAnswer).isNotPresent();
    }

    @TestFactory
    Collection<DynamicTest> 답변_조회_시나리오() {
        //given
        User writer = TestUserFactory.create("javajigi");
        Question question = TestQuestionFactory.create(writer);
        Answer answer = TestAnswerFactory.create(writer, question);
        Answer saveAnswer = answerRepository.save(answer);
        Long saveAnswerId = saveAnswer.getId();
        return Arrays.asList(
                DynamicTest.dynamicTest("id로 답변을 조회한다.", () -> {
                    //when
                    Optional<Answer> findAnswer = answerRepository.findById(saveAnswerId);

                    //then
                    assertThat(findAnswer).isPresent();
                }),
                DynamicTest.dynamicTest("답변을 삭제하면 조회할 수 없다.", () -> {
                    //when
                    answerRepository.delete(saveAnswer);
                    Optional<Answer> deleteAnswer = answerRepository.findById(saveAnswerId);

                    //then
                    assertThat(deleteAnswer).isNotPresent();
                })
        );
    }

    @Test
    void cascade_REMOVE_테스트() {
        //given
        User writer = TestUserFactory.create("javajigi");
        Question question = TestQuestionFactory.create(writer);
        Answer answer1 = TestAnswerFactory.create(writer, question);
        Answer answer2 = TestAnswerFactory.create(writer, question);
        Answer saveAnswer1 = answerRepository.save(answer1);
        Answer saveAnswer2 = answerRepository.save(answer2);
        Question saveQuestion = questionRepository.save(question);

        //when
        answerRepository.delete(saveAnswer1);
        Optional<Question> findQuestion = questionRepository.findById(saveQuestion.getId());
        Optional<Answer> findAnswer = answerRepository.findById(saveAnswer2.getId());

        //then
        assertThat(findQuestion).isPresent();
        assertThat(findAnswer).isPresent();
    }

    @Test
    void 질문자와_답변자_달라_예외_발생하면_질문_삭제여부는_거짓이다() {
        //given
        User writer = TestUserFactory.create("sanjigi");
        User fakeWriter = TestUserFactory.create("javajigi");
        Question question = TestQuestionFactory.create(writer);
        Answer answer = TestAnswerFactory.create(fakeWriter, question);
        answerRepository.save(answer);
        Long questionId = question.getId();

        //when
        assertThatThrownBy(() -> question.delete(writer))
                .isInstanceOf(CannotDeleteException.class)
                .hasMessage(ErrorCode.답변_중_다른_사람이_쓴_답변_있어_삭제_못함.getErrorMessage());
        questionRepository.saveAndFlush(question);
        Optional<Question> findQuestion = questionRepository.findByIdAndDeletedFalse(questionId);

        //then
        assertThat(findQuestion).isPresent();
    }
}

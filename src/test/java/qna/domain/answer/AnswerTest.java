package qna.domain.answer;

import qna.domain.question.QuestionTest;
import qna.domain.user.User;
import qna.domain.user.UserTest;

public class AnswerTest {
    public static final Answer A1 = new Answer(UserTest.JAVAJIGI, QuestionTest.Q1, "Answers Contents1");
    public static final Answer A2 = new Answer(UserTest.SANJIGI, QuestionTest.Q1, "Answers Contents2");

    public static Answer createAnswer(User writeUser) {
        return new Answer(writeUser, QuestionTest.createQuestion(writeUser), "Answers Contents1");
    }

}

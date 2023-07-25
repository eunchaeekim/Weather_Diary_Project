package zerobase.weather.error;

public class InvalidDate extends RuntimeException {
    private static final String MESSAGE = "너무 과거 혹은 미래의 날짜입니다.";

    public InvalidDate(){ //message 자동 호출
        super(MESSAGE);
    }
}

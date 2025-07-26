package cn.shalee.workupload.exception;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;  // 改为String类型，更灵活
    private final String errorMessage;

    public BusinessException(String errorMessage) {
        this("400", errorMessage);
    }

    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorMessage, Throwable cause) {
        this("400", errorMessage, cause);
    }

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] %s", errorCode, errorMessage);
    }
}
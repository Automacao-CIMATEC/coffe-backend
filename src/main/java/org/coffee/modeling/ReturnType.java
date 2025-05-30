package org.coffee.modeling;

public enum ReturnType
{
    /*
     * @brief 100 - 199: In Operation
     */
    IN_PROGRESS         (102, false),

    /*
     * @brief 200 - 299: Operation Succeeded
     */
    SUCCESS             (200, true),

    /*
     * @brief 400 - 499: Operation Halted
     */
    INVALID_DATA        (400, false),
    UNAUTHORIZED        (401, false),
    NOT_FOUND           (404, false),
    CONFLICT            (409, false),

    /*
     * @brief 500 - 599: Operation did not Succeed
     */
    ERROR               (500, false),
    SERVICE_UNAVAILABLE (503, false),
    TIMEOUT             (504, false);

    private final int code;
    private final boolean successful;

    ReturnType(int code, boolean successful) {
        this.code = code;
        this.successful = successful;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isError() {
        return !successful && this != IN_PROGRESS;
    }
}

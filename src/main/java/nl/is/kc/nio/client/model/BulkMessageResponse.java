package nl.is.kc.nio.client.model;

/**
 * Created by ruben on 2-4-15.
 */
public class BulkMessageResponse {
    private int messageCounter;
    private int messageSuccessCounter;
    private int messageFailCounter;
    private int timeoutCounter;
    private int exceptionCounter;

    public BulkMessageResponse(int messageCounter, int messageSuccessCounter, int messageFailCounter, int timeoutCounter, int exceptionCounter) {
        this.messageCounter = messageCounter;
        this.messageSuccessCounter = messageSuccessCounter;
        this.messageFailCounter = messageFailCounter;
        this.timeoutCounter = timeoutCounter;
        this.exceptionCounter = exceptionCounter;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public int getMessageSuccessCounter() {
        return messageSuccessCounter;
    }

    public int getMessageFailCounter() {
        return messageFailCounter;
    }

    public int getTimeoutCounter() {
        return timeoutCounter;
    }

    public int getExceptionCounter() {
        return exceptionCounter;
    }
}

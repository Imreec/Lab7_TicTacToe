package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private Object data;

    public Message(String message) {
        this.message = message;
    }

    public Message(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message: " + message + ", Data: " + data;
    }
}
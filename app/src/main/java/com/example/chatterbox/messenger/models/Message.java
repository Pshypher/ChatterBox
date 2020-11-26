package com.example.chatterbox.messenger.models;

import android.os.Build;
import android.os.CountDownTimer;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Message {

    private final String source;
    private String messageBody;
    private long timestamp;
    private final CountDownTimer timer;

    public static final long TIME_SPAN = 5000;
    private static final long INTERVAL = 1000;
    private CountDownListener listener;

    public interface CountDownListener {
        void onFinished();
    }


    public Message(String source, String msg, long milliseconds) {
        this.source = source;
        messageBody = msg;
        timestamp = milliseconds;
        timer = new CountDownTimer(TIME_SPAN, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (listener != null) listener.onFinished();
            }
        };
    }

    public void setListener(CountDownListener listener) {
        this.listener = listener;
    }

    public String getSource() {
        return source;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return source.equals(message.source) &&
                this.messageBody.equals(message.messageBody) &&
                timestamp == message.timestamp;
    }

    public void startCountDown() {
        timer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(source, messageBody, timestamp);
    }
}

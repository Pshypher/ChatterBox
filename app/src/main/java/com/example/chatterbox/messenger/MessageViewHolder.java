package com.example.chatterbox.messenger;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.R;
import com.example.chatterbox.messenger.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.chatterbox.messenger.models.Message.TIME_SPAN;

public class MessageViewHolder extends RecyclerView.ViewHolder implements Message.CountDownListener {

    private final ConstraintLayout bubbleLayout;
    private final TextView userTextView;
    private final TextView messageBodyTextView;
    private final TextView timeStampTextView;
    private final TextView editButton;
    private final TextView deleteButton;

    private static final String SENDER = "Sender";
    private static final String RESPONDER = "Responder";



    public interface OnChangeMessageListener {
        void onEditButtonClicked(int position);
        void onDeleteButtonClicked(int position);
    }

    private MessageViewHolder(View itemView, OnChangeMessageListener listener) {
        super(itemView);

        bubbleLayout = itemView.findViewById(R.id.bubble_layout);
        userTextView = itemView.findViewById(R.id.user_text_view);
        messageBodyTextView = itemView.findViewById(R.id.msg_body_text_view);
        timeStampTextView = itemView.findViewById(R.id.timestamp_text_view);
        editButton = itemView.findViewById(R.id.edit_button);
        deleteButton = itemView.findViewById(R.id.delete_button);

        editButton.setOnClickListener(view -> {
            listener.onEditButtonClicked(getAdapterPosition());
            conceal();
        });

        deleteButton.setOnClickListener(view -> {
            listener.onDeleteButtonClicked(getAdapterPosition());
            conceal();
        });
    }

    private void reveal() {
        editButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        bubbleLayout.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                R.drawable.sent_msg_bubble_alt));
    }

    private void conceal() {
        editButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        bubbleLayout.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                R.drawable.sent_msg_bubble));
    }

    public static MessageViewHolder from(ViewGroup parent, OnChangeMessageListener listener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.msg_bubble, parent, false);
        return new MessageViewHolder(itemView, listener);
    }

    public void bind(Message message, String macAddress) {
        Context context = itemView.getContext();
        setupMessageBubble(message, macAddress, context, bubbleLayout);

        String user = (message.getSource().equals(macAddress)) ? SENDER : RESPONDER;
        userTextView.setText(user);
        messageBodyTextView.setText(message.getMessageBody());
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm", Locale.getDefault());
        timeStampTextView.setText(formatter.format(new Date(message.getTimestamp())));

        itemView.setOnLongClickListener(view -> {
            if (!user.equals(SENDER)) return false;
            if (System.currentTimeMillis() - message.getTimestamp() <= TIME_SPAN) reveal();
            else conceal();
            return true;
        });

        message.setListener(this);
        message.startCountDown();
    }

    private void setupMessageBubble(Message message, String macAddress, Context context, View bubbleLayout) {
        if (message.getSource().equals(macAddress)) {
            bubbleLayout.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.sent_msg_bubble));
            LinearLayout container = itemView.findViewById(R.id.message_container);
            container.setGravity(Gravity.END);
        } else {
            bubbleLayout.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.received_msg_bubble));
            userTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            messageBodyTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            timeStampTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    @Override
    public void onFinished() {
        conceal();
    }
}

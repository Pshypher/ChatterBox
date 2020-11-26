package com.example.chatterbox.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.R;
import com.example.chatterbox.chat.models.Message;

import java.util.Locale;

public class MessageAdapter extends ListAdapter<Message, MessageAdapter.MessageViewHolder> {

    private MessageAdapter(@NonNull MessageDiffUtil diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = getItem(position);
        holder.bind(message);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView userTextView;
        private TextView messageBodyTextView;
        private TextView timeStampTextView;

        private MessageViewHolder(View itemView) {
            super(itemView);

            userTextView = itemView.findViewById(R.id.user_text_view);
            messageBodyTextView = itemView.findViewById(R.id.msg_body_text_view);
            timeStampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }

        public static MessageViewHolder from(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.msg_bubble, parent, false);
            return new MessageViewHolder(itemView);
        }

        public void bind(Message message) {
            Context context = itemView.getContext();
            if (message.getUser().equals(Message.USER.SENDER))
                itemView.setBackground(
                        ContextCompat.getDrawable(context, R.drawable.sent_msg_bubble));
            else
                itemView.setBackground(
                        ContextCompat.getDrawable(context, R.drawable.received_msg_bubble));

            String userType = message.getUser().toString();
            String capitalizedUserString = userType.charAt(0)
                    + userType.substring(1).toLowerCase();
            userTextView.setText(capitalizedUserString);
            messageBodyTextView.setText(message.getMessageBody());
            timeStampTextView.setText(String.format(Locale.getDefault(),
                    "%d", message.getTimestamp()));
        }
    }

    static class MessageDiffUtil extends DiffUtil.ItemCallback<Message> {

        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static MessageAdapter getInstance() {
        return new MessageAdapter(new MessageDiffUtil());
    }
}

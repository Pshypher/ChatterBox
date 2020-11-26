package com.example.chatterbox.messenger;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.messenger.models.Message;

import java.util.ArrayList;
import java.util.List;

import static com.example.chatterbox.messenger.models.Message.TIME_SPAN;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder>
        implements MessageViewHolder.OnChangeMessageListener {

    public interface OnChangeMessageListener {
        void onEditClicked(Message msg, int index);
    }

    private final String hostMACAddress;
    private final List<Message> messages;

    public final OnChangeMessageListener listener;

    private MessageAdapter(String macAddress, List<Message> msgs,
                           OnChangeMessageListener msgListener) {
        messages = msgs;
        hostMACAddress = macAddress;
        listener = msgListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageViewHolder.from(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message, hostMACAddress);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        ArrayList<Message> oldMessages = new ArrayList<>(messages);
        messages.add(message);
        // compute diffs
        final MessageDiffCallback diffCallback = new MessageDiffCallback(oldMessages, messages);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        diffResult.dispatchUpdatesTo(this);
    }

    public void deleteMessage(Message message) {
        if (System.currentTimeMillis() - message.getTimestamp() >= TIME_SPAN) return;
        ArrayList<Message> oldMessages = new ArrayList<>(messages);
        messages.remove(message);
        // compute diffs
        final MessageDiffCallback diffCallback = new MessageDiffCallback(oldMessages, messages);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        diffResult.dispatchUpdatesTo(this);
    }

    public void editMessage(String body, int index) {
        Message message = messages.get(index);
        if (System.currentTimeMillis() - message.getTimestamp() >= TIME_SPAN) return;
        message.setMessageBody(body);
        notifyItemChanged(index);
    }

    @Override
    public void onEditButtonClicked(int position) {
        listener.onEditClicked(messages.get(position), position);
    }

    @Override
    public void onDeleteButtonClicked(int position) {
        deleteMessage(messages.get(position));
    }

    static class MessageDiffCallback extends DiffUtil.Callback {

        private final List<Message> oldList;
        private final List<Message> newList;

        public MessageDiffCallback(List<Message> oldList, List<Message> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Message oldMessage = oldList.get(oldItemPosition);
            Message newMessage = newList.get(newItemPosition);

            return oldMessage.getSource().equals(newMessage.getSource()) &&
                    oldMessage.getTimestamp() == newMessage.getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    public static MessageAdapter getInstance(String macAddress, List<Message> messages,
                                             OnChangeMessageListener listener) {
        return new MessageAdapter(macAddress, messages, listener);
    }
}

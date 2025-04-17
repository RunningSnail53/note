package edu.hebut.retrofittest.Adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;


import java.util.List;

import edu.hebut.retrofittest.chat.model.Message;

public class MyMessagesListAdapter extends MessagesListAdapter<Message> {

    private Message messageOnGoingToUpdate;

    public MyMessagesListAdapter(String senderId, ImageLoader imageLoader) {
        super(senderId, imageLoader);
    }

    public MyMessagesListAdapter(String senderId, MessageHolders holdersConfig, ImageLoader imageLoader){
        super(senderId, holdersConfig, imageLoader);
    }
    public void updateMessageContent(Message newMessage) {
        for (int i = 0; i < items.size(); i++) {
            Wrapper wrapper = items.get(i);
            if (wrapper.item instanceof IMessage) {
                messageOnGoingToUpdate = (Message) wrapper.item;
                if (messageOnGoingToUpdate.getId().contentEquals(newMessage.getId())) {
                    messageOnGoingToUpdate.setText(newMessage.getText());
                    new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());notifyItemChanged(i, "update_text");
                }
            }
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List payloads) {
        if (!payloads.isEmpty()) {
            holder.onBind(messageOnGoingToUpdate);
        }
        super.onBindViewHolder(holder, position);
    }

    public static class OutcomingTextMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

        private TextView text;

        public OutcomingTextMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
        }

        @Override
        public void onBind(Message message) {
            super.onBind(message);
            text.setText(message.getText());
        }
    }

}
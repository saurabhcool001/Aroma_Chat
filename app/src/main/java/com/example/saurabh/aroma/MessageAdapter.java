package com.example.saurabh.aroma;

import android.content.Context;
import android.graphics.Color;
import android.provider.SyncStateContract;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.saurabh.aroma.R.id.messengerImageView;
import static com.example.saurabh.aroma.R.id.messengerImageView1;

/**
 * Created by saurabh on 06-08-2017.
 */



public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int ITEM_TYPE_NORMAL = 0;
    public static final int ITEM_TYPE_HEADER = 1;
    private List<FriendlyMessage> mMessageList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Context context;


    public MessageAdapter(Context context, List<FriendlyMessage> mMessageList) {
        this.mMessageList = mMessageList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_message, parent, false);
//
//        return new MessageViewHolder(view);
        switch (viewType) {
            case ITEM_TYPE_NORMAL:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(view);
            case ITEM_TYPE_HEADER:
                View view_user = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_user, parent, false);
                return new MessageViewHolder(view_user);
        }
        return null;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage, profileImageUser;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.messageTextView);
            profileImage = (CircleImageView) itemView.findViewById(messengerImageView);
            profileImageUser = (CircleImageView) itemView.findViewById(messengerImageView1);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        String current_userId = mAuth.getCurrentUser().getUid();

        FriendlyMessage c = mMessageList.get(position);

        final int itemType = getItemViewType(position);

        String from_user = c.getFrom();


        if (itemType == ITEM_TYPE_NORMAL) {

            //holder.messageText.setBackgroundResource(R.drawable.speech_bubbles_user);
            //holder.messageText.setBackgroundColor(Color.WHITE);
            //holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.BLACK);
            Picasso.with(context).load(c.getPhotoUrl()).placeholder(R.mipmap.user_image_transparent).into(holder.profileImage);
            //holder.messageText.setGravity(Gravity.RIGHT | Gravity.END);


        } else if (itemType == ITEM_TYPE_HEADER){

            //holder.messageText.setBackgroundResource(R.drawable.speech_bubbles_user);
            holder.messageText.setTextColor(Color.WHITE);
            Picasso.with(context).load(c.getPhotoUrl()).placeholder(R.mipmap.user_image_transparent).into(holder.profileImageUser);
        }
        holder.messageText.setText(c.getMessages());
        //Picasso.with(context).load(c.getPhotoUrl()).placeholder(R.mipmap.user_image_transparent).into(holder.profileImage);
    }

    @Override
    public int getItemViewType(int position) {
        String current_userId = mAuth.getCurrentUser().getUid();
        FriendlyMessage c = mMessageList.get(position);
        String from_user = c.getFrom();
        if (from_user.equals(current_userId)) {
            return ITEM_TYPE_HEADER;
        }else {
            return ITEM_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}


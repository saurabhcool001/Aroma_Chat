package com.example.saurabh.aroma;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFregment extends Fragment {

    Context context;

    private RecyclerView mFriendsList;

    private FirebaseAuth mAuth;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mFriendsDatabase, mUserDatabase;
    private String current_userId;

    private View mMainView;

    public FriendsFregment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity();
        FirebaseApp.initializeApp(context);

        mMainView = inflater.inflate(R.layout.fragment_friends_fregment, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = Database.getReference().child("Friends").child(current_userId);
        mUserDatabase = Database.getReference().child("Users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendsDatabase.keepSynced(true);
        mUserDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.user_single_layout,
                        FriendsViewHolder.class,
                        mFriendsDatabase
                ) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, final int position) {

                        viewHolder.setDate(model.getDate());

                        final String list_user_id = getRef(position).getKey();

                        mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                                if (dataSnapshot.hasChild("online")) {
                                    //Boolean onlineUser = (Boolean) dataSnapshot.child("online").getValue();
                                    String onlineUser = dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setUserOnline(onlineUser);
                                }

                                viewHolder.setImage(getContext(),thumbImage);
                                viewHolder.setName(userName);


                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            //Toast.makeText(context, "val : " + getRef(position).getKey(), Toast.LENGTH_SHORT).show();
                                                                            CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                            builder.setTitle("Select Option");
                                                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    //Click Event
                                                                                    switch (which) {
                                                                                        case 0:
                                                                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                                                            profileIntent.putExtra("user_id", list_user_id);
                                                                                            startActivity(profileIntent);
                                                                                            break;
                                                                                        case 1:
                                                                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                                                            Bundle bundle = new Bundle();
                                                                                            bundle.putString("user_id", list_user_id);
                                                                                            bundle.putString("userName", userName);
                                                                                            chatIntent.putExtras(bundle);
                                                                                            startActivity(chatIntent);
                                                                                            break;

                                                                                    }
                                                                                }
                                                                            });
                                                                            builder.show();
                                                                        }
                                                                    }
                                );
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(date);
        }

        public void setName(String name) {
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }

        public void setImage(Context context, String thumb_image) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.user_image_transparent).into(userImageView);
        }

        public void setUserOnline(String online_icon) {
            ImageView userOnline = (ImageView) mView.findViewById(R.id.user_single_online_icon);
            if (online_icon.equals("true")) {
                userOnline.setVisibility(View.VISIBLE);
            }else {
                userOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}


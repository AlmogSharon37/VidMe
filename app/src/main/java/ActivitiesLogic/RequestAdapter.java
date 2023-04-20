package ActivitiesLogic;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omeglewhatsapphybrid.R;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestHolder> {
    private ArrayList<User> users;
    private Context context;
    private RequestAdapter.OnClickListener onClickListener;
    private RequestAdapter.OnClickListener onClickAcceptListener;
    private RequestAdapter.OnClickListener onClickDenyListener;

    interface OnClickListener{
        void onClick(int position);
    }



    public RequestAdapter(ArrayList<User> users, Context context, RequestAdapter.OnClickListener onClickListener,
                          RequestAdapter.OnClickListener onClickAcceptListener, RequestAdapter.OnClickListener onClickDenyListener) {
        this.users = users;
        this.context = context;
        this.onClickListener = onClickListener;
        this.onClickAcceptListener = onClickAcceptListener;
        this.onClickDenyListener = onClickDenyListener;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.request_holder, parent, false);
        return new RequestHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestHolder holder, int position) {
        holder.usernameText.setText(users.get(position).getUsername());
        if(users.get(position).getProfilePic().equals("")){
            UtilityFunctions.setImageOnImageView(context, null,holder.profileImage);
        }
        UtilityFunctions.setImageOnImageView(context, Uri.parse(users.get(position).getProfilePic()),holder.profileImage);

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class RequestHolder extends RecyclerView.ViewHolder{
        public TextView usernameText;
        ShapeableImageView profileImage;
        ImageButton acceptBtn;
        ImageButton denyButton;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
            usernameText = itemView.findViewById(R.id.usernameText);
            profileImage = itemView.findViewById(R.id.profilePic);
            acceptBtn = itemView.findViewById(R.id.acceptFriendBtn);
            denyButton = itemView.findViewById(R.id.denyFriendBtn);

            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickAcceptListener.onClick(getAdapterPosition());
                }
            });

            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickDenyListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}

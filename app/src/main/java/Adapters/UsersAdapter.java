package Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omeglewhatsapphybrid.R;
import UtilityClasses.User;
import UtilityClasses.UtilityFunctions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserHolder> {


    private ArrayList<User> users;
    private Context context;
    private OnClickListener onClickListener;

    public interface OnClickListener{
        void onClick(int position);
    }

    public UsersAdapter(ArrayList<User> users, Context context, OnClickListener onClickListener) {
        this.users = users;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.user_holder, parent, false);
        return new UserHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
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

    class UserHolder extends RecyclerView.ViewHolder{
        public TextView usernameText;
        ShapeableImageView profileImage;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(getAdapterPosition());
                }
            });
            usernameText = itemView.findViewById(R.id.usernameText);
            profileImage = itemView.findViewById(R.id.profilePic);
        }
    }
}

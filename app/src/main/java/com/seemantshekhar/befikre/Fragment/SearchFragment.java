package com.seemantshekhar.befikre.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.seemantshekhar.befikre.Adapter.UserAdapter;
import com.seemantshekhar.befikre.MainActivity;
import com.seemantshekhar.befikre.Model.User;
import com.seemantshekhar.befikre.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private static final String PROFILE_ID = "profileID";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    EditText searchText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchText = view.findViewById(R.id.search_text_search);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(userAdapter);

        readUsers();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void searchUsers(String s){
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void readUsers(){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(searchText.getText().toString().equals("")){
                    userList.clear();

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        userList.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    
}

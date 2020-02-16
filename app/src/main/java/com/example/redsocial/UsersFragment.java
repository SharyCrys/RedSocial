package com.example.redsocial;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.redsocial.adapters.AdapterUsers;
import com.example.redsocial.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    // Firebase Auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Inicialización
        firebaseAuth = FirebaseAuth.getInstance();

        // Inicializamos el Reclycler View
        recyclerView = view.findViewById((R.id.users_recyclerView));

        // Fijamos
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Inicializamos la lista
        userList = new ArrayList<>();

        // Cargamos los usuarios
        getAllUsers();

        return view;

    }

    private void getAllUsers() {

        // Cogemos al usuario en concreto
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // Cogemos el path de "usuarios"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Cogemos toda la información del path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    // Cogemos todos los usuarios menos el logeado
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }
                    // Adaptador
                    adapterUsers =  new AdapterUsers(getActivity(), userList);
                    // Adaptador instanciado en el Recycled View
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {

        // Cogemos al usuario en concreto
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // Cogemos el path de "usuarios"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Cogemos toda la información del path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    // Cogemos los usuarios buscados menos el logeado
                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())) {

                            userList.add(modelUser);

                        }


                    }

                    // Adaptador
                    adapterUsers =  new AdapterUsers(getActivity(), userList);
                    // Refrescamos el adaptador
                    adapterUsers.notifyDataSetChanged();
                    // Adaptador instanciado en el Recycled View
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void checkUserStatus(){
        // Leemos el usuario introducido
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            // Usuario logeado
            // Mostar el email del usuario logeado
            //mProfileTv.setText(user.getEmail());
        } else {
            // Usuario no loegado, vuelve al MainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Para mostrar opciones en el fragmento
        super.onCreate(savedInstanceState);
    }

    // Inflamos las opciones del menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflamos el menu
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Search View
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // Buscamos el listener del search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!s.equals(""))
                {                    // search using this string ..
                    searchUsers(s);
                }
                else
                {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!s.equals(""))
                {                    // search using this string ..
                    searchUsers(s);
                }
                else
                {                    getAllUsers();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Cogemos el ID
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}

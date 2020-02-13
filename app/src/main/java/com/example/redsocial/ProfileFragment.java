package com.example.redsocial;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // Storage
    StorageReference storageReference;

    // Path donde la foto de perfil y la portada serán guardadas
    String storagePath ="Users_Profile_Cover_Imgs/";

    // Vistas del XML
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;

    // Progress Bar
    ProgressDialog pd;

    // Constantes de permiso
    private static final int CAMERA_REQUEST_CODEE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE  = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    // Arrays de permiso
    String cameraPermissions[];
    String storagePermissions[];

    // Cogemos la imagen selccionada (URI)
    Uri image_uri;

    // Para revisar la foto de perfil o la portada
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Iniciar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase =  FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //FireBaseStorage Reference

        // Iniciar los arrays de permiso
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Iniciar vistas
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        // Iniciamos el progress dialog
        pd = new ProgressDialog(getActivity());


        // Tenemos que coger información del usuario logeado , podemos coger su correo o su ID
        // Para ello usamos una Query
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Check hasta recibir la información
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    // Cogemos la información
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    // Cambiar e insertar la informacion
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        // Si se recive una imagen se inserta
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_defualt_img_white).into(avatarIv);
                    }

                    try {
                        // Si se recive una imagen se inserta
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Boton de editar perfil
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission(){
        // Mirar si los permisos del Storage están activas o no
        // Retornar verdadero si están activas
        // Retornar false si no están activas
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestStoragePermission(){
        // Tiempo de ejecución de los permisos
       requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        // Mirar si los permisos del Storage están activas o no
        // Retornar verdadero si están activas
        // Retornar false si no están activas
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }
    private void requestCameraPermission(){
        // Tiempo de ejecución de los permisos
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODEE);
    }

    private void showEditProfileDialog() {
        // Mostramos opciones al pulsar el botón
        // 1. Editar foto de perfil
        // 2. Editar Portada
        // 3. Editar nombre
        // 4. Editar teléfono

        // Opciones a mostrar
        String options[] =  {"Editar foto de perfil","Editar portada", "Editar nombre", "Editar teléfono"};
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ponemos un titulo
        builder.setTitle("Escoge una opción");
        // Colocamos las opciones en el cuadro de dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Controlamos los botones
                if (which == 0){
                    // Boton de editar perfil
                    pd.setMessage("Actualizando foto de perfil");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if (which == 1){
                    // Boton de editar portada
                    pd.setMessage("Actualizando portada");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if (which == 2){
                    // Boton de editar nombre
                    pd.setMessage("Actualizando nombre");
                    // Llamamos a este método para actualizar el nombre y el telefoono en la base de datos del usuario
                    showNamePhoneUpdateDialog("name");
                }
                else if (which == 3){
                    // Boton de editar teléfono
                    pd.setMessage("Actualizando teléfono");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        // Mostramos el cuadro de dialogo
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(final String key) {

        // Cuadro de dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Actualizando " + key);

        // Le damos un layout al cuadro de dialogo
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        // Añadimos un Edit Text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Introduce "+ key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        // Añadimos botones al cuadro de dialogo
        // Añadimos un botón de actualizar
        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                // Cogemos la información puesta en el EditText
                String value = editText.getText().toString().trim();
                // Vemos si el usuario ha introducido algún valor o no
                if (!TextUtils.isEmpty(value)){

                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid)  {
                                    // Actualizado, quitamos la barra de progreso
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Actualizando...", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                 // Actualizacion fallida, quitamos la barra de progreso y mostramos el error
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(),"Por favor introduce " + key, Toast.LENGTH_SHORT).show();

                }
            }
        });

        // Añadimos un botón de cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();

    }

    private void showImagePicDialog() {

        // Muestra un dialogo que contiene las opciones de camara y galería para coger una imagen
        // Opciones a mostrar
        String options[] =  {"Camara","Galeria"};
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Ponemos un titulo
        builder.setTitle("Escoge una imagen de");
        // Colocamos las opciones en el cuadro de dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Controlamos los botones
                if (which == 0){
                    // Boton de camara
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    // Boton de galeria
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        // Mostramos el cuadro de dialogo
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      // Este método es llamado cuando el usuario presiona Permitir o Denegar los permisos

        switch (requestCode){
            case CAMERA_REQUEST_CODEE: {
                // Ha cogido la opcion de camara, primero revisa si los permisos de la camara y almacenamiento están activos o no
                if (grantResults.length > 0 ){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        // Los permisos están aceptados
                        pickFromCamera();
                    }
                    else {
                        // Permisos denegados
                        Toast.makeText(getActivity(), "Por favor acepte los permisos", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{

                // Ha cogido la opcion de galeria, primero revisa si los permisos de almacenamiento están activos o no
                if (grantResults.length > 0 ){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        // Los permisos están aceptados
                        pickFromGallery();
                    }
                    else {
                        // Permisos denegados
                        Toast.makeText(getActivity(), "Por favor acepte los permisos", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        // Este método será llamado después de haber escodigo una foto de cámara o de galería
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
            // La imagen ha sido escogida de galería, cogemos la URI de la imagen
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                // La imagen ha sido tomada con la cámara
                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        // Mostar barrra de progreso
        pd.show();

        // En vez de crear funciones distintas para cámara y galería, usamos una en común

        // Path y nombre de la imagen que va a ser guardada en Firebase Storage
        String filePathAndName = storagePath+ ""+profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // La imagen ha sido subida, ahora toca coger su ID y guardarla en la base de datos del usuario
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        // Revisamos si la imagen ha sido subida y si hemos rcibido su ID
                        if(uriTask.isSuccessful()){
                            // La imagen ha sido subida
                            // Actualizar la URL en la base de datos del usuario
                            HashMap<String, Object> results = new HashMap<>();

                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // URL en la base de datos del usuario ha sido actualizada con exito
                                            // Quitamos la bara de progreso
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Imagen actualizada...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Error al añadir la URL a la base de datos del usuario
                                            // Quitamos la barra de progreso
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error actualizando la imagen...", Toast.LENGTH_SHORT).show();


                                        }
                                    });
                        }
                        else {
                            //Error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Ha ocurrido un error..." , Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            // Mostar error y cerrar barra de progreso
                pd.dismiss();
                Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromCamera()
    {
        // Intent al seleccionar camara
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        // COgemos la imagen y la colocamos
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent para iniciar la camara
        Intent camaraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(camaraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {

        // Coger una foto de galeria
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
}

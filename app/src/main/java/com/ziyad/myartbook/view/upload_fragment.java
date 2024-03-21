package com.ziyad.myartbook.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.ziyad.myartbook.R;
import com.ziyad.myartbook.databinding.FragmentUploadBinding;
import com.ziyad.myartbook.model.Art;
import com.ziyad.myartbook.roomdb.ArtDao;
import com.ziyad.myartbook.roomdb.ArtDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class upload_fragment extends Fragment {

    private FragmentUploadBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    ArtDatabase artdb;
    ArtDao artDao;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String info = "";
    Art artFromMain;


    public upload_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLauncher();
        artdb = Room.databaseBuilder(requireContext(), ArtDatabase.class, "Art").build();
        artDao = artdb.artDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUploadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return  view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(v);
            }
        });

        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(v);
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(v);
            }
        });

        if(getArguments() != null){
            info = upload_fragmentArgs.fromBundle(getArguments()).getInfo();
        }else{
            info = "new";
        }

        if(info.equals("new")){
            binding.nameText.setText("");
            binding.imageView.setImageResource(R.drawable.save);
            binding.artistText.setText("");
            binding.yearText.setText("");

            binding.deleteBtn.setVisibility(View.GONE);
            binding.uploadBtn.setVisibility(View.VISIBLE);
        }else{
            int artId = upload_fragmentArgs.fromBundle(getArguments()).getArtid();

            compositeDisposable.add(artDao.getArtById(artId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(upload_fragment.this::handlerResponseByOldId));

            binding.deleteBtn.setVisibility(View.VISIBLE);
            binding.uploadBtn.setVisibility(View.GONE);
        }
    }

    private  void handlerResponseByOldId(Art art){
        artFromMain = art;
        binding.nameText.setText(art.artName);
        binding.yearText.setText(art.year);
        binding.artistText.setText(art.artistName);

        Bitmap bitmap = BitmapFactory.decodeByteArray(art.image, 0, art.image.length);
        binding.imageView.setImageBitmap(bitmap);
    }

    public void upload(View view){
        String artName = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();
        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        Art art = new Art(artName, artistName, year, byteArray);

        compositeDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(upload_fragment.this::handleResponse)
        );
    }

    public void delete(View view){
        compositeDisposable.add(artDao.delete(artFromMain)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(upload_fragment.this::handleResponseDelete));
    }

    private void handleResponseDelete(){
        NavDirections action = upload_fragmentDirections.actionUploadFragmentToRecyclerFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void handleResponse(){
        NavDirections action = upload_fragmentDirections.actionUploadFragmentToRecyclerFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    public void pickImage(View view){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }
                }).show();
            }else{
               permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }else{
            Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intenttoGallery);
        }
    }

    public void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();
                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if(o){
                    Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttoGallery);
                }else{
                    Toast.makeText(requireContext(), "Permission needed!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

}
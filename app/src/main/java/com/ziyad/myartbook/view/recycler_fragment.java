package com.ziyad.myartbook.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ziyad.myartbook.R;
import com.ziyad.myartbook.adapter.ArtAdapter;
import com.ziyad.myartbook.databinding.RecyclerFragmentBinding;
import com.ziyad.myartbook.model.Art;
import com.ziyad.myartbook.roomdb.ArtDao;
import com.ziyad.myartbook.roomdb.ArtDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class recycler_fragment extends Fragment implements MenuProvider{

    private RecyclerFragmentBinding binding;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ArtDatabase artdb;
    ArtDao artDao;
    ArtAdapter artAdapter;
    int menuNum = 0;
    Lifecycle lifecycle;

    public recycler_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artdb = Room.databaseBuilder(requireContext(), ArtDatabase.class, "Art").build();
        artDao = artdb.artDao();


        compositeDisposable.add(artDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recycler_fragment.this::handleResponse));
    }

    private void handleResponse(List<Art> artList){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        artAdapter = new ArtAdapter(artList);
        binding.recyclerView.setAdapter(artAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = RecyclerFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            MenuHost menuHost = requireActivity();

            menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.add_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.add_art){
            NavDirections action = recycler_fragmentDirections.actionRecyclerFragmentToUploadFragment("new");
            Navigation.findNavController(requireView()).navigate(action);
        }
        return false;
    }
}
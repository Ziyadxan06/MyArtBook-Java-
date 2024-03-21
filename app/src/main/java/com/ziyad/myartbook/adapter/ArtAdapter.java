package com.ziyad.myartbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ziyad.myartbook.databinding.RecyclerFragmentBinding;
import com.ziyad.myartbook.databinding.RecyclerRowBinding;
import com.ziyad.myartbook.model.Art;
import com.ziyad.myartbook.view.recycler_fragmentDirections;

import java.util.List;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    List<Art> artList;

    public ArtAdapter(List<Art> artList) {
        this.artList = artList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.recyclerRowBinding.recyclerViewTextView.setText(artList.get(position).artName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycler_fragmentDirections.ActionRecyclerFragmentToUploadFragment action = recycler_fragmentDirections.actionRecyclerFragmentToUploadFragment("old");
                action.setArtid(artList.get(position).id);
                action.setInfo("old");
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{

        RecyclerRowBinding recyclerRowBinding;
        public ArtHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}

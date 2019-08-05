package com.google.alc.travelmantics.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.alc.travelmantics.app.R;
import com.google.alc.travelmantics.app.model.TravelDeal;
import com.google.alc.travelmantics.app.ui.TravelDealActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TravelDealAdapter  extends RecyclerView.Adapter<TravelDealAdapter.MyViewHolder>{

    private Context mCTx;
    private List<TravelDeal> travelDealList;
    private LayoutInflater inflater;

    // default constructor
    public TravelDealAdapter() {}

    public TravelDealAdapter(Context mCTx, List<TravelDeal> travelDealList) {
        this.mCTx = mCTx;
        this.travelDealList = travelDealList;
        inflater = LayoutInflater.from(mCTx);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = inflater.inflate(R.layout.deal_item,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TravelDeal travelDeal = travelDealList.get(position);
        holder.onBind(travelDeal);
    }

    @Override
    public int getItemCount() {
        return travelDealList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView imageDeal;

        TextView tvTitle,tvDescription,tvPrice;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // binding views
            imageDeal = itemView.findViewById(R.id.imageDeal);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            // attaching on click listener to itemView
            itemView.setOnClickListener(this);

        }

        // bind data to views
        private void onBind(TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvPrice.setText(deal.getPrice());
            tvDescription.setText(deal.getDescription());
            showDealImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            TravelDeal deal = travelDealList.get(position);
            Intent intent = new Intent(v.getContext(), TravelDealActivity.class);
            intent.putExtra(v.getContext().getString(R.string.intent_extra_deal),deal);
            v.getContext().startActivity(intent);
        }

        private void showDealImage(String url){
            if(url != null && !url.isEmpty()){
                Picasso.get()
                        .load(url)
                        .resize(200,200)
                        .centerCrop()
                        .into(imageDeal);
            }
        }
    }

}

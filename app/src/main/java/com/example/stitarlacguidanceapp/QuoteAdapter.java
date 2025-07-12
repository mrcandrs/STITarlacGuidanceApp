package com.example.stitarlacguidanceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.Models.Quote;

import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private List<Quote> quotes;

    public QuoteAdapter(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuote, tvAuthor;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuote = itemView.findViewById(R.id.tvQuote);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        Quote quote = quotes.get(position);
        holder.tvQuote.setText("\"" + quote.getText() + "\"");
        holder.tvAuthor.setText("- " + quote.getAuthor());
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }
}


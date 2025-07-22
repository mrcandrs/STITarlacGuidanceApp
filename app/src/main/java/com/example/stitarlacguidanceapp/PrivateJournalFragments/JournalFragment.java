package com.example.stitarlacguidanceapp.PrivateJournalFragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stitarlacguidanceapp.Adapters.JournalAdapter;
import com.example.stitarlacguidanceapp.Models.JournalEntry;
import com.example.stitarlacguidanceapp.R;

import java.util.ArrayList;
import java.util.List;

public class JournalFragment extends Fragment {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalEntries;

    public JournalFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        recyclerView = view.findViewById(R.id.recyclerJournal);

        // Dummy entries
        journalEntries = new ArrayList<>();
        journalEntries.add(new JournalEntry("Weekend Reflection",
                "Had a wonderful time studying for my upcoming exams...",
                "July 20, 2025", "😊 Happy • 🌟 Grateful"));

        journalEntries.add(new JournalEntry("Project Presentation",
                "Successfully presented my research project today...",
                "July 18, 2025", "🎉 Accomplished • 😅 Relieved"));

        journalEntries.add(new JournalEntry("New Semester Goals",
                "Setting my intentions for this semester...",
                "July 15, 2025", "💪 Motivated • 🎯 Focused"));

        adapter = new JournalAdapter(journalEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}



package com.example.stitarlacguidanceapp.Activities;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.stitarlacguidanceapp.Models.Quote;
import com.example.stitarlacguidanceapp.QuoteAdapter;
import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.ActivityRegistrationBinding;
import com.example.stitarlacguidanceapp.databinding.ActivityStudentDashboardBinding;

import java.util.Arrays;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding root;
    private ViewPager2 viewPager;
    private Handler sliderHandler = new Handler();
    private int currentPage = 0;
    private final long SLIDE_DELAY = 4000; // 4 seconds
    private List<Quote> quoteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        viewPager = findViewById(R.id.viewPager);

        quoteList = Arrays.asList(
                new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
                new Quote("Push yourself, because no one else is going to do it for you.", "Unknown"),
                new Quote("Don’t watch the clock; do what it does. Keep going.", "Sam Levenson"),
                new Quote("Wake up with determination. Go to bed with satisfaction.", "George Lorimer"),
                new Quote("Success is not for the lazy.", "Unknown")
        );

        QuoteAdapter adapter = new QuoteAdapter(quoteList);
        viewPager.setAdapter(adapter);

        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager.getAdapter() != null) {
                int itemCount = viewPager.getAdapter().getItemCount();
                currentPage = (currentPage + 1) % itemCount;
                viewPager.setCurrentItem(currentPage, true);
                sliderHandler.postDelayed(this, SLIDE_DELAY);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }

}

package com.example.stitarlacguidanceapp.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.stitarlacguidanceapp.Activities.Step1Fragment;
import com.example.stitarlacguidanceapp.Activities.Step2Fragment;
import com.example.stitarlacguidanceapp.Activities.Step3Fragment;
import com.example.stitarlacguidanceapp.Activities.WelcomeFragment;

public class ExitFormAdapter extends FragmentStateAdapter {

    public ExitFormAdapter(FragmentActivity activity) {
        super(activity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new WelcomeFragment();
            case 1: return new Step1Fragment();
            case 2: return new Step2Fragment();
            case 3: return new Step3Fragment();
            default: return new WelcomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}

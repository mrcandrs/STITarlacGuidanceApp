package com.example.stitarlacguidanceapp.MoodTrackerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.stitarlacguidanceapp.R;
import com.example.stitarlacguidanceapp.databinding.FragmentDisclaimerBinding;

public class DisclaimerFragment extends Fragment {

    private FragmentDisclaimerBinding root;

    public DisclaimerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentDisclaimerBinding.inflate(inflater, container, false);

        root.btnProceed.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

            //Setting custom animations
            ft.setCustomAnimations(
                    R.anim.slide_in_right,  //Enter
                    R.anim.slide_out_left,  //Exit
                    R.anim.slide_in_left,   //PopEnter (when back)
                    R.anim.slide_out_right  //PopExit (when back)
            );

            ft.replace(R.id.fragment_container, new Question1Fragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        return root.getRoot();
    }
}


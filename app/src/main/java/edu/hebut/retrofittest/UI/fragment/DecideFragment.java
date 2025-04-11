package edu.hebut.retrofittest.UI.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils;
import edu.hebut.retrofittest.R;
import io.github.jan.supabase.SupabaseClient;

public class DecideFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_decide, null);

        SupabaseClient client = SupabaseDataUtils.Companion.getClient();
        return v;

    }
}

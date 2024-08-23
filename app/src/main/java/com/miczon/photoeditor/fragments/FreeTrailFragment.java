package com.miczon.photoeditor.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hardik.clickshrinkeffect.ClickShrinkEffect;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.eventListeners.FragmentClickListener;
import com.miczon.photoeditor.utils.Utility;

public class FreeTrailFragment extends Fragment {

    String TAG = "FreeTrailFragment", from = "";

    RelativeLayout closeLayout;
    FragmentClickListener fragmentClickListener;
    LinearLayout lifetimeLayout, monthlyLayout, yearlyLayout;
    ImageView animImage1, animImage2, animImage3, monthlyIcon, yearlyIcon, lifetimeIcon;
    TextView tvManageSub, tvSubPrice, tvPrivacyPolicy;
    ClickShrinkEffect clickShrinkEffect;

    Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        if (context instanceof SplashActivity) {
        fragmentClickListener = (FragmentClickListener) context;
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_trail, container, false);

        closeLayout = view.findViewById(R.id.rl_close);
        lifetimeLayout = view.findViewById(R.id.ll_lifetime);
        yearlyLayout = view.findViewById(R.id.ll_yearly);
        monthlyLayout = view.findViewById(R.id.ll_monthly);
        tvManageSub = view.findViewById(R.id.tv_manageSub);
        tvPrivacyPolicy = view.findViewById(R.id.tv_privacyPolicy);
        animImage1 = view.findViewById(R.id.iv_animSplash1);
        animImage2 = view.findViewById(R.id.iv_animSplash2);
        animImage3 = view.findViewById(R.id.iv_animSplash3);
        tvSubPrice = view.findViewById(R.id.tv_subPrice);
        monthlyIcon = view.findViewById(R.id.iv_monthly);
        yearlyIcon = view.findViewById(R.id.iv_yearly);
        lifetimeIcon = view.findViewById(R.id.iv_lifetime);

        clickShrinkEffect = new ClickShrinkEffect(monthlyLayout);
        clickShrinkEffect = new ClickShrinkEffect(yearlyLayout);
        clickShrinkEffect = new ClickShrinkEffect(lifetimeLayout);

        yearlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.new_purple)));
        yearlyIcon.setImageResource(R.drawable.ic_white_circle_tick);
        tvSubPrice.setText(R.string.yearly_payment_body);

        bundle = getArguments();
        if (bundle != null) {
            from = bundle.getString("from");

            Log.e(TAG, "onCreateView: from: " + from);

        } else {
            Log.e(TAG, "onCreateView: bundle is null");
        }

        closeLayout.setOnClickListener(v -> {
            if (getActivity() != null) {
                Utility.getInstance().addSplashAnimation(animImage1, animImage2, animImage3, false);
//                HomeActivity.isFragmentVisible = false;
//                ApplyEffectActivity.isFragmentVisible = false;
                if (from != null) {
                    if (from.equalsIgnoreCase("splash") || from.equalsIgnoreCase("home")) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(FreeTrailFragment.this).commit();
                        fragmentClickListener.itemClicked();
                    }
                }
            } else {
                Log.e(TAG, "onCreateView: from is null");
            }
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            Uri webpage = Uri.parse("https://airportflightsstatus.com/");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(browserIntent);
        });

        monthlyLayout.setOnClickListener(v -> {
            // set monthly as selected and change icon
            monthlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.new_purple)));
            monthlyIcon.setImageResource(R.drawable.ic_white_circle_tick);
            tvSubPrice.setText(R.string.monthly_payment_body);

            //reset all others
            yearlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            yearlyIcon.setImageResource(R.drawable.ic_white_circle);
            yearlyLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);

            lifetimeLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            lifetimeIcon.setImageResource(R.drawable.ic_white_circle);
            lifetimeLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);
        });

        yearlyLayout.setOnClickListener(v -> {
            // set yearly as selected and change icon
            yearlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.new_purple)));
            yearlyIcon.setImageResource(R.drawable.ic_white_circle_tick);
            tvSubPrice.setText(R.string.yearly_payment_body);

            //reset all others
            monthlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            monthlyIcon.setImageResource(R.drawable.ic_white_circle);
            monthlyLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);

            lifetimeLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            lifetimeIcon.setImageResource(R.drawable.ic_white_circle);
            lifetimeLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);
        });


        lifetimeLayout.setOnClickListener(v -> {
            // set yearly as selected and change icon
            lifetimeLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.new_purple)));
            lifetimeIcon.setImageResource(R.drawable.ic_white_circle_tick);
            tvSubPrice.setText(R.string.lifetime_payment_body);

            //reset all others
            monthlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            monthlyIcon.setImageResource(R.drawable.ic_white_circle);
            lifetimeLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);

            yearlyLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.greyish_black)));
            yearlyIcon.setImageResource(R.drawable.ic_white_circle);
            yearlyLayout.setBackgroundResource(R.drawable.bg_dialog_rounded_sides);
        });

        tvManageSub.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions"));
            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "getDeviceInfo: exception: " + e.getLocalizedMessage());
                Toast.makeText(getActivity(), R.string.con_error_msg, Toast.LENGTH_SHORT).show();
            }
            startActivity(browserIntent);
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utility.getInstance().addSplashAnimation(animImage1, animImage2, animImage3, true);
    }
}
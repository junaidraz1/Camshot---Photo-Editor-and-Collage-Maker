package com.miczon.photoeditor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.miczon.photoeditor.BaseActivity;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.viewPagerAdapter.OnBoardAdapter;

@SuppressWarnings("deprecation")
public class OnBoardActivity extends BaseActivity {

    String TAG = "OnBoardActivity";

    ViewPager onBoardViewPager;
    LinearLayout mDotLayout;
    TextView tvSkip;
    RelativeLayout nextLayout;

    TextView[] dots;
    OnBoardAdapter onBoardAdapter;

    int count = 0;
    boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);

        tvSkip = findViewById(R.id.tv_skip);
        mDotLayout = findViewById(R.id.ll_indicator);
        nextLayout = findViewById(R.id.rl_continue);
        onBoardViewPager = findViewById(R.id.slider);

        onBoardAdapter = new OnBoardAdapter(this);
        onBoardViewPager.setAdapter(onBoardAdapter);
        setUpIndicator(0);
        onBoardViewPager.addOnPageChangeListener(viewListener);

        nextLayout.setOnClickListener(v -> {
            Log.e(TAG, "onCreate: count val: " + count);
            if (count <= 2) {
                onBoardViewPager.setCurrentItem(count);
                count++;
            } else {
                startActivity(new Intent(OnBoardActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        tvSkip.setOnClickListener(v -> startActivity(new Intent(OnBoardActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

    }

    public void setUpIndicator(int position) {
        dots = new TextView[3];
        mDotLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(getResources().getColor(R.color.white));
            mDotLayout.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.light_pink));
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.e(TAG, "onPageScrolled: working");
            Log.e(TAG, "onPageScrolled: position: " + position);
            if (isFirstTime && position == 0) {
                Log.e(TAG, "onPageScrolled: if working");
                count++;
                isFirstTime = false;

            } else if (!isFirstTime && position == 0) {
                Log.e(TAG, "onPageScrolled: else if working");
                count = 1;

            } else if (!isFirstTime && position == 1) {
                Log.e(TAG, "onPageScrolled: else if working");
                count = 2;

            } else {
                Log.e(TAG, "onPageScrolled: else working");
                count++;
            }
        }

        @Override
        public void onPageSelected(int position) {
            setUpIndicator(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
}
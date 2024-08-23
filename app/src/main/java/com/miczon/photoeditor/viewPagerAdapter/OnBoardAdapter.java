package com.miczon.photoeditor.viewPagerAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.miczon.photoeditor.R;

public class OnBoardAdapter extends PagerAdapter {

    Context context;
    ImageView onBoardImage;
    TextView onBoardTitle1, onBoardDescription;

    int[] images = {R.drawable.ic_onboard_1, R.drawable.ic_onboard_2, R.drawable.ic_onboard_3};
    int[] title = {R.string.on_board_title_1, R.string.on_board_title_2, R.string.on_board_title_3};
    int[] description = {R.string.on_board_description_1, R.string.on_board_description_2, R.string.on_board_description_3};

    public OnBoardAdapter(Context context) {
        this.context = context;

    }

    @Override
    public int getCount() {
        return description.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_screen_onboard, container, false);

        onBoardImage = view.findViewById(R.id.iv_image);
        onBoardTitle1 = view.findViewById(R.id.tv_title);
        onBoardDescription = view.findViewById(R.id.tv_description);

        onBoardImage.setImageResource(images[position]);
        onBoardTitle1.setText(title[position]);
        onBoardDescription.setText(description[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }

}

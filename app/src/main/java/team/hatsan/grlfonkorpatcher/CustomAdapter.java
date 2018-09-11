package team.hatsan.grlfonkorpatcher;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by KNH on 2018-02-07.
 */

public class CustomAdapter extends PagerAdapter {

    LayoutInflater inflater;
    int currentFragment;
    int[][] imageData = {{R.drawable.helpimg_text1, R.drawable.helpimg_text2, 0},
            {R.drawable.helpimg_gui1, R.drawable.helpimg_gui2, R.drawable.helpimg_gui3},
            {R.drawable.helpimg_addon1, R.drawable.helpimg_addon2, R.drawable.helpimg_addon3}}; //0 : TextKR 1 : GUIKR 2 : Addon
    int[] imageVal = {2, 3, 3};


    public CustomAdapter(LayoutInflater inflater, int page){
        this.inflater=inflater;
        currentFragment = page;
    }

    @Override
    public int getCount(){
        return imageVal[currentFragment];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_helpimgview, null);

        ImageView imgview = (ImageView) view.findViewById(R.id.img_viewpagerimg);
        imgview.setImageResource(imageData[currentFragment][position]);
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }
}

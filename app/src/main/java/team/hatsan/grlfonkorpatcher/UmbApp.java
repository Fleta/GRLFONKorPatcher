package team.hatsan.grlfonkorpatcher;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by KNH on 2017-09-20.
 */

public class UmbApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "fonts/NanumBarunGothic.ttf"))
                .addBold(Typekit.createFromAsset(this, "fonts/NanumBarunGothicBold.ttf"));
    }
}

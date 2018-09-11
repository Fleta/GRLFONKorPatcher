package team.hatsan.grlfonkorpatcher;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KNH on 2017-09-10.
 */

public class TextPatchFragment extends BaseFragment {

    public TextPatchFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getMainActivity().bAsked) {
            switch (getMainActivity().agreePush) {
                case -1:
                    getMainActivity().askAgree(true);
                    break;
                case 0:
                    FirebaseMessaging.getInstance().subscribeToTopic("notice");
                    getMainActivity().askUpdate();
                    break;
                case 1:
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("notice");
                    getMainActivity().askUpdate();
                    break;
            }
            getMainActivity().bAsked = true;
        }
    }

    public void refresh(View view)
    {
        TextView tv_textUpdate = (TextView) view.findViewById(R.id.text_textUpdate);
        TextView tv_textNotice = (TextView) view.findViewById(R.id.text_textNotice);

        tv_textUpdate.setText(getMainActivity().textesChangeDate);
        tv_textNotice.setText(Html.fromHtml(parsingNotice("posts_text.json")));

        Button.OnClickListener bl_patchDigitalsky = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().preparePatch(MainActivity.platformType.DIGITALSKY);
            }
        };
        Button.OnClickListener bl_patchBilibili = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getMainActivity().preparePatch(MainActivity.platformType.BILIBILI);
            }
        };

        Button btn_patchDigitalsky = (Button) view.findViewById(R.id.btn_dsky);
        Button btn_patchBilibili = (Button) view.findViewById(R.id.btn_bili);

        btn_patchDigitalsky.setOnClickListener(bl_patchDigitalsky);
        btn_patchBilibili.setOnClickListener(bl_patchBilibili);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_textpatch, container, false);
        if(getMainActivity().getSuccessConnect()) {
            refresh(view);
        }
        else {
            TextView tv_textNotice = (TextView) view.findViewById(R.id.text_textNotice);
            tv_textNotice.setText(getString(R.string.xml_noticeNone));
        }

       /* AdView mAdView;

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("8BE7B0D4026145C035E2AA2142D20EFB").build();
        mAdView.loadAd(adRequest);*/

        return view;
    }
}

package team.hatsan.grlfonkorpatcher;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by KNH on 2017-09-11.
 */

public class GuiDownloadFragment extends BaseFragment {

    public GuiDownloadFragment()
    {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void refresh(View view){

        TextView tv_CurrentDigitalskyVersion = (TextView) view.findViewById(R.id.text_currentDskyVersion);
        TextView tv_LatestDigitalskyVersion = (TextView) view.findViewById(R.id.text_latestDskyVersion);
        TextView tv_CurrentBiliVersion = (TextView) view.findViewById(R.id.text_currentBiliVersion);
        TextView tv_LatestBiliVersion = (TextView) view.findViewById(R.id.text_latestBiliVersion);

        TextView tv_GuiNotice = (TextView) view.findViewById(R.id.text_guiNotice);

        tv_LatestDigitalskyVersion.setText(getMainActivity().versions.latestDigitalSky);
        tv_LatestBiliVersion.setText(getMainActivity().versions.latestBiliBili);

        String temp = getMainActivity().getPlatformPackageVersion(MainActivity.platformType.DIGITALSKY);
        if(temp.equals(getMainActivity().versions.latestDigitalSky))
        {
            tv_CurrentDigitalskyVersion.setTextColor(getResources().getColor(R.color.correctBlue));
        }else{
            tv_CurrentDigitalskyVersion.setTextColor(getResources().getColor(R.color.diffRed));
        }
        tv_CurrentDigitalskyVersion.setText(temp);

        temp = getMainActivity().getPlatformPackageVersion(MainActivity.platformType.BILIBILI);
        if(temp.equals(getMainActivity().versions.latestBiliBili))
        {
            tv_CurrentBiliVersion.setTextColor(getResources().getColor(R.color.correctBlue));
        }else{
            tv_CurrentBiliVersion.setTextColor(getResources().getColor(R.color.diffRed));
        }
        tv_CurrentBiliVersion.setText(temp);

        tv_GuiNotice.setText(Html.fromHtml(parsingNotice("posts_gui.json")));

        Button.OnClickListener bl_apkDownload = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder askPlatform = new AlertDialog.Builder(getMainActivity());
                askPlatform.setTitle(getString(R.string.yesnobox_title_askPlatform));
                askPlatform.setMessage(Html.fromHtml(getString(R.string.yesnobox_desc_askPlatform)));
                askPlatform.setNegativeButton(getString(R.string.yesnobox_dsky_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getMainActivity().apkDownload(MainActivity.platformType.DIGITALSKY);
                    }
                });
                askPlatform.setPositiveButton(getString(R.string.yesnobox_bili_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getMainActivity().apkDownload(MainActivity.platformType.BILIBILI);
                    }
                });
                askPlatform.show();
            }
        };
        Button btn_apkDownload = (Button) view.findViewById(R.id.btn_reqestApkDownload);
        btn_apkDownload.setOnClickListener(bl_apkDownload);

        Button.OnClickListener bl_showDownList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                //getMainActivity().forceStop();
            }
        };

        Button btn_showDownList = (Button) view.findViewById(R.id.btn_showDownList);
        btn_showDownList.setOnClickListener(bl_showDownList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_guidownload, container, false);
        if(getMainActivity().getSuccessConnect()) {
            refresh(view);
        }
        else {
            TextView tv_GuiNotice = (TextView) view.findViewById(R.id.text_guiNotice);
            tv_GuiNotice.setText(getString(R.string.xml_noticeNone));
        }

        /*AdView mAdView;

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("8BE7B0D4026145C035E2AA2142D20EFB").build();
        mAdView.loadAd(adRequest);*/


        return view;
    }
}

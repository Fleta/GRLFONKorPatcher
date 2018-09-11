package team.hatsan.grlfonkorpatcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by KNH on 2017-09-13.
 */

public class AddonsFragment extends BaseFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void refresh(final View view) {

        TextView textView = (TextView) view.findViewById(R.id.text_descAddon);
        textView.setText(Html.fromHtml(getString(R.string.xml_descAddons)));

        Button updateButton = (Button) view.findViewById(R.id.btn_umbUpdate);
        TextView curVersion = (TextView) view.findViewById(R.id.text_curUmbVersion);
        TextView lateVersion = (TextView) view.findViewById(R.id.text_latestUmbVersion);
        try{
            curVersion.setText(getMainActivity().getPackageManager().getPackageInfo(getMainActivity().getPackageName(), 0).versionName);
        }catch(PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        lateVersion.setText(getMainActivity().versions.latestUmb);

        if(curVersion.getText().equals(lateVersion.getText()))
        {
            curVersion.setTextColor(getResources().getColor(R.color.correctBlue));
            updateButton.setVisibility(View.INVISIBLE);
        }else{
            curVersion.setTextColor(getResources().getColor(R.color.diffRed));
        }

        Button.OnClickListener bl_umbUpdate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getMainActivity().umbURL));
                startActivity(intent);
            }
        };
        updateButton.setOnClickListener(bl_umbUpdate);

        Button.OnClickListener bl_deletePatch = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                preDeletePatch();
            }
        };
        Button.OnClickListener bl_unInstallDsky = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getMainActivity().unInstallPackage(MainActivity.platformType.DIGITALSKY);
            }
        };
        Button.OnClickListener bl_unInstallBili = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().unInstallPackage(MainActivity.platformType.BILIBILI);
            }
        };
        Button.OnClickListener bl_unCensored = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preUnCensored();
            }
        };

        Button btn_deletePatch = (Button) view.findViewById(R.id.btn_deletePatch);
        Button btn_unInstallDsky = (Button) view.findViewById(R.id.btn_deleteDsky);
        Button btn_unInstallBili = (Button) view.findViewById(R.id.btn_deleteBili);
        Button btn_unCensored = (Button) view.findViewById(R.id.btn_deleteCensored);

        btn_deletePatch.setOnClickListener(bl_deletePatch);
        btn_unInstallDsky.setOnClickListener(bl_unInstallDsky);
        btn_unInstallBili.setOnClickListener(bl_unInstallBili);
        btn_unCensored.setOnClickListener(bl_unCensored);

        /*Spinner spinner = (Spinner) view.findViewById(R.id.spinner_icon);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getMainActivity(), R.array.spinner_icon, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        spinner.setSelection(getMainActivity().iconType);*/

        Button.OnClickListener bl_askAgree = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().askAgree(false);
            }
        };
        Button btn_askAgree = (Button) view.findViewById(R.id.btn_askAgree);
        btn_askAgree.setOnClickListener(bl_askAgree);
    }

    public void elseRefresh(final View view){
        Button updateButton = (Button) view.findViewById(R.id.btn_umbUpdate);
        updateButton.setVisibility(View.INVISIBLE);

        final SharedPreferences.Editor editor = getMainActivity().settings.edit();

        final Button btn_customServerChange = (Button) view.findViewById(R.id.btn_serverChange);
        btn_customServerChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder serverChangeDialog = new AlertDialog.Builder(getMainActivity());
                final EditText et = new EditText(getMainActivity());
                et.setText(getMainActivity().customJSONURL);
                serverChangeDialog.setView(et);
                serverChangeDialog.setNeutralButton(getString(R.string.msgbox_ok_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postServerChange(et.getText().toString());
                    }
                });
                serverChangeDialog.setTitle(getText(R.string.msgbox_title_customURL));
                serverChangeDialog.setMessage(getText(R.string.msgbox_customURL));
                serverChangeDialog.show();
            }
        });

        Switch swi_customServer = (Switch) view.findViewById(R.id.swi_customServer);
        swi_customServer.setChecked(getMainActivity().getCustomServer());
        swi_customServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    btn_customServerChange.setVisibility(View.VISIBLE);
                    editor.putBoolean("useCustomServer", true);
                }
                else {
                    btn_customServerChange.setVisibility(View.INVISIBLE);
                    editor.putBoolean("useCustomServer", false);
                }
                editor.commit();
                postServerSwitch();
            }
        });
        if(getMainActivity().getCustomServer())
        {
            btn_customServerChange.setVisibility(View.VISIBLE);
        }
        else{
            btn_customServerChange.setVisibility(View.INVISIBLE);
        }
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            getMainActivity().changeIcon(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void preDeletePatch() {
        AlertDialog.Builder askPlatform = new AlertDialog.Builder(getMainActivity());
        askPlatform.setTitle(getString(R.string.yesnobox_title_askDeletePatchPlatform));
        askPlatform.setMessage(getString(R.string.yesnobox_desc_askDeletePatchPlatform));
        askPlatform.setNegativeButton(getString(R.string.yesnobox_dsky_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getMainActivity().deletePatch(MainActivity.platformType.DIGITALSKY);
            }
        });
        askPlatform.setPositiveButton(getString(R.string.yesnobox_bili_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getMainActivity().deletePatch(MainActivity.platformType.BILIBILI);
            }
        });
        askPlatform.show();
    }

    public void preUnCensored(){
        try{
            Process process = Runtime.getRuntime().exec("su");
            AlertDialog.Builder askPlatform = new AlertDialog.Builder(getMainActivity());
            askPlatform.setTitle(getString(R.string.yesnobox_title_askUnCensored));
            askPlatform.setMessage(Html.fromHtml(getString(R.string.yesnobox_desc_askUnCensored)));
            askPlatform.setNegativeButton(getString(R.string.yesnobox_dsky_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getMainActivity().unCensored(MainActivity.platformType.DIGITALSKY);
                }
            });
            askPlatform.setPositiveButton(getString(R.string.yesnobox_bili_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getMainActivity().unCensored(MainActivity.platformType.BILIBILI);
                }
            });
            askPlatform.show();
        }catch(Exception e)
        {
            getMainActivity().okMsgBox(getString(R.string.msgbox_title_nonRooting), getString(R.string.msgbox_desc_nonRooting));
        }
    }

    public void postServerSwitch(){
        getMainActivity().refresh();
    }

    public void postServerChange(String CustomURL) {
        SharedPreferences.Editor editor = getMainActivity().settings.edit();
        editor.putString("customURL", CustomURL);
        editor.commit();
        getMainActivity().refresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_addons, container, false);
        elseRefresh(view);
        if(getMainActivity().getSuccessConnect()) {
            refresh(view);
        }

        AdView mAdView;

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("8BE7B0D4026145C035E2AA2142D20EFB").build();
        mAdView.loadAd(adRequest);

        return view;
    }
}

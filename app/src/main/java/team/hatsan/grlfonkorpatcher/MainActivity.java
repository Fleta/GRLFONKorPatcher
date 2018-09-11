package team.hatsan.grlfonkorpatcher;

import android.*;
import android.Manifest;
import android.app.ActivityGroup;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tsengvn.typekit.Typekit;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    enum platformType {
        DIGITALSKY, BILIBILI;
    }

    public class URLS {
        public String checkJSON;
        public String textNoticeJSON;
        public String guiNoticeJSON;

        public String sideA;
        public String sideB;

        public String[] apks;
        public String patchSite;
    }

    public class Versions {
        public String latestDigitalSky;
        public String latestBiliBili;
        public String latestUmb;
    }

    public class Directories {
        public String forDownload;

        public String[] forPatch = {Environment.getExternalStorageDirectory() + "/Android/data/com.digitalsky.girlsfrontline.cn/files/Android/",
                Environment.getExternalStorageDirectory() + "/Android/data/com.digitalsky.girlsfrontline.cn.bili/files/Android/"};
    }

    SharedPreferences settings;

    URLS urls = new URLS();
    Versions versions = new Versions();
    Directories directories = new Directories();

    boolean firstTime;

    String[] TextURL = new String[2];

    ProgressDialog refreshBar;
    ProgressDialog downloadBar;

    NavigationView navigationView;

    //String languageFileSize;
    //String textesFileSize;

    public class Sizes {
        public String langSize;
        public String textesSize;
    }

    Sizes sideA = new Sizes();
    Sizes sideB = new Sizes();

    String textesChangeDate;

    String btn_sideA = "";
    String btn_sideB = "";

    boolean oneSideMode = false;
    String patchMessage = "";

    String[] components = {"team.hatsan.grlfonkorpatcher.sohatsan", "team.hatsan.grlfonkorpatcher.suomi", "team.hatsan.grlfonkorpatcher.dasboots"};

    int iconType = 0;
    int agreePush;

    platformType lastPlatformType;
    long latestID = -1;

    String[] platformPackageName = {"com.digitalsky.girlsfrontline.cn", "com.digitalsky.girlsfrontline.cn.bili"};
    String[] patchFileType = {"asset_language.ab", "asset_textes.ab"};

    String umbURL;

    boolean bShowUmbUpdateBox;
    boolean bAsked = false;

    boolean bCanGui = false;
    boolean bCanText = false;

    boolean bSuccessConnect = true;
    boolean bCustomServer = false;

    String customJSONURL = "";
    String appInfo = "";

    int currentFragmentPosition = 0; //0 : textKR, 1 : GUIKR, 2 : Addon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = getSharedPreferences("settings", 0);
        directories.forDownload = getExternalCacheDir().toString();
        firstTime = false;

        checkPermission();
        initProgressDialog();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MobileAds.initialize(this, "ca-app-pub-8020827438513908~1052721285");

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_help:
                callHelp(currentFragmentPosition);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) { //좌측에 네비게이션 드로어 메뉴 선택
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        if (id == R.id.nav_textpatch) {
            fragment = new TextPatchFragment();
            currentFragmentPosition = 0;
        } else if (id == R.id.nav_guipatch) {
            fragment = new GuiDownloadFragment();
            currentFragmentPosition = 1;
        } else if (id == R.id.nav_additional) {
            fragment = new AddonsFragment();
            currentFragmentPosition = 2;
        } else if (id == R.id.nav_info) {
            okMsgBox(getString(R.string.msgbox_appInfo), appInfo);
        } else if (id == R.id.nav_gofrozen) {
            Intent goFrozenIntent = new Intent(Intent.ACTION_VIEW);
            goFrozenIntent.setData(Uri.parse(urls.patchSite));
            startActivity(goFrozenIntent);
        }

        if (fragment != null) {
            fragmentTransaction.replace(R.id.content_fragment_layout, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    finish();
                    System.exit(0);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(completeReceiver, completeFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(completeReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    public void changeIcon(int index) {
        if (index != iconType) {
            int i;
            for (i = 0; i < components.length; i++)
                getPackageManager().setComponentEnabledSetting(new ComponentName("team.hatsan.grlfonkorpatcher", components[i]), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            switch (index) {
                case 0:
                    getPackageManager().setComponentEnabledSetting(new ComponentName("team.hatsan.grlfonkorpatcher", components[index]), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    break;
                case 1:
                    getPackageManager().setComponentEnabledSetting(new ComponentName("team.hatsan.grlfonkorpatcher", components[index]), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    break;
                case 2:
                    getPackageManager().setComponentEnabledSetting(new ComponentName("team.hatsan.grlfonkorpatcher", components[index]), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    break;
            }
            iconType = index;
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("icon", iconType);
            editor.commit();
        }
    }

    // 설정하는 구간
    // ------------------------------------------------------------

    public void callHelp(int pos) {
        Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
        intent.putExtra("page", pos);
        startActivity(intent);
    }

    public void askAgree(final boolean first) {
        final SharedPreferences.Editor editor = settings.edit();

        AlertDialog.Builder askPush = new AlertDialog.Builder(this);
        askPush.setTitle(getString(R.string.yesnobox_title_askAgreePush));
        askPush.setMessage(getString(R.string.yesnobox_desc_askAgreePush));
        if (first) {
            askPush.setMessage(getString(R.string.yesnobox_desc_firstAskAgreePush));
        }
        askPush.setPositiveButton(getString(R.string.yesnobox_yes_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                okMsgBox("", getString(R.string.msgbox_agreePush));
                FirebaseMessaging.getInstance().subscribeToTopic("notice");
                editor.putInt("push", 0);
                editor.commit();
                if (first) {
                    askUpdate();
                }
            }
        });
        askPush.setNegativeButton(getString(R.string.yesnobox_no_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                okMsgBox("", getString(R.string.msgbox_denyPush));
                FirebaseMessaging.getInstance().unsubscribeFromTopic("notice");
                editor.putInt("push", 1);
                editor.commit();
                if (first) {
                    askUpdate();
                }
            }
        });
        askPush.show();
    }

    public void askUpdate() {
        if (bShowUmbUpdateBox && bSuccessConnect) {
            final SharedPreferences.Editor editor = settings.edit();
            try {
                if (!(getPackageManager().getPackageInfo(getPackageName(), 0).versionName.equals(versions.latestUmb))) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View checkLayout = inflater.inflate(R.layout.checkbox_layout, null);
                    final CheckBox cbox = (CheckBox) checkLayout.findViewById(R.id.check_oneTime);

                    AlertDialog.Builder askUpdate = new AlertDialog.Builder(this);
                    askUpdate.setNegativeButton(getString(R.string.yesnobox_no_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putBoolean("showUpdate", !(cbox.isChecked()));
                            editor.commit();
                        }
                    });
                    askUpdate.setPositiveButton(getString(R.string.yesnobox_yes_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putBoolean("showUpdate", !(cbox.isChecked()));
                            editor.commit();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(umbURL)));
                        }
                    });
                    askUpdate.setTitle(getString(R.string.yesnobox_title_askUpdate));
                    askUpdate.setMessage(getString(R.string.yesnobox_desc_askUpdate));
                    askUpdate.setView(checkLayout);
                    askUpdate.show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void refresh() {
        refreshBar.show();
        iconType = settings.getInt("icon", 0);
        agreePush = settings.getInt("push", -1);
        bShowUmbUpdateBox = settings.getBoolean("showUpdate", true);
        bCustomServer = settings.getBoolean("useCustomServer", false);
        customJSONURL = settings.getString("customURL", "");

        bSuccessConnect = true;

        if (bCustomServer) {
            urls.checkJSON = customJSONURL;
        } else {
            urls.checkJSON = getString(R.string.request_URL);
        }
        File checkJSONFile = new File(directories.forDownload, "check.json");
        checkJSONFile.delete();
        DownloadTask downloadTask = new DownloadTask(this, "check.json", getExternalCacheDir().toString(), false, "REFRESH_FILE", null);
        downloadTask.execute(urls.checkJSON);
    }

    public void requestNotice() {
        DownloadTask downloadTask = new DownloadTask(this, "posts_text.json", directories.forDownload, false, "TEXT_NOTICE_FILE", null);
        downloadTask.execute(urls.textNoticeJSON);
    }

    public void checkPermission() { // 저장소 권한 확인 및 요청 함수
        int currentPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (currentPermission == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder askContinue = new AlertDialog.Builder(this);
                    askContinue.setTitle(getString(R.string.yesnobox_title_requestPermission));
                    askContinue.setMessage(getString(R.string.yesnobox_desc_requestPermission));
                    askContinue.setPositiveButton(getString(R.string.yesnobox_yes_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                            }
                            dialog.dismiss();
                        }
                    });
                    askContinue.setNegativeButton(getString(R.string.yesnobox_no_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
                    askContinue.show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                }
                return;
            }
        }
    }

    public boolean checkMatchFileSize(platformType platform, int side) { //0 : A, 1 : B
        File languageFile = new File(directories.forPatch[getPlatformIndex(platform)], patchFileType[0]);
        File textesFile = new File(directories.forPatch[getPlatformIndex(platform)], patchFileType[1]);
        String languageFileSize = "";
        String textesFileSize = "";

        switch(side){
            case 0:
                languageFileSize = sideA.langSize;
                textesFileSize = sideA.textesSize;
                break;
            case 1:
                languageFileSize = sideB.langSize;
                textesFileSize = sideB.textesSize;
                break;
        }

        if (languageFile.exists() && textesFile.exists()) {
            Long langSize = languageFile.length();
            Long textesSize = textesFile.length();
            if (languageFileSize.equals(langSize.toString()) && textesFileSize.equals(textesSize.toString())) {
                return true;
            }
        }
        return false;
    }

    public void initProgressDialog() {
        refreshBar = new ProgressDialog(this);
        refreshBar.setCancelable(false);
        refreshBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        refreshBar.setMessage(getString(R.string.progressdialog_refresh));

        downloadBar = new ProgressDialog(this);
        downloadBar.setIndeterminate(true);
        downloadBar.setCancelable(false);
    }

    public void preparePatch(platformType p) {
        final platformType platform = p;
        AlertDialog.Builder askSide = new AlertDialog.Builder(this);
        askSide.setTitle(getString(R.string.yesnobox_title_askSide));
        askSide.setMessage(Html.fromHtml(patchMessage));
        if (!oneSideMode) {
            askSide.setPositiveButton(btn_sideA, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    patch(platform, 0);
                    dialog.dismiss();
                }
            });
            askSide.setNegativeButton(btn_sideB, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    patch(platform, 1);
                    dialog.dismiss();
                }
            });
        } else {
            askSide.setPositiveButton(getString(R.string.yesnobox_yes_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    patch(platform, 0);
                    dialog.dismiss();
                }
            });
            askSide.setNegativeButton(getString(R.string.yesnobox_no_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        askSide.show();
    }

    public void patch(platformType p, int side) { // 0 : A, 1 : B
        if(checkMatchFileSize(p, side)) {
            DownloadTask downloadTask = new DownloadTask(this, "patch.zip", directories.forDownload, true, "PATCH_FILE", p);
            setDownloadBar("", getString(R.string.progressdialog_patchDownload), ProgressDialog.STYLE_HORIZONTAL);
            if (side == 0) {
                downloadTask.execute(urls.sideA);
            } else {
                downloadTask.execute(urls.sideB);
            }
        }
        else
        {
            okMsgBox(getString(R.string.msgbox_title_notmatchSize), getString(R.string.msgbox_desc_notmatchSize));
        }
    }

    public void apkDownload(platformType p){
        DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request;
        Uri apkUrl;

        String apkName = (new File(urls.apks[getPlatformIndex(p)])).getName();
        String[] realApkName = apkName.split("[.]");
        File apkF = new File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS, realApkName[0] + ".apk");
        if(apkF.exists()) {
            apkF.delete();
        }

        if(latestID != -1)
        {
            downloadManager.remove(latestID);
        }

        apkUrl = Uri.parse(urls.apks[getPlatformIndex(p)]);
        List<String> path = apkUrl.getPathSegments();
        request = new DownloadManager.Request(apkUrl);
        request.setTitle(getString(R.string.downloadManager_title));
        request.setDescription(p.toString());
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path.get(path.size()-1));
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
        latestID = downloadManager.enqueue(request);
        lastPlatformType = p;
        Toast.makeText(this, getString(R.string.toast_apkWarning), Toast.LENGTH_LONG).show();
        //startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String apkName = (new File(urls.apks[getPlatformIndex(lastPlatformType)])).getName();
            String[] realApkName = apkName.split("[.]");
            File apkF = new File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS, realApkName[0] + ".apk");
            installApk(apkF);
        }
    };

    public void installApk(File apkFile){
        Uri apkUri = Uri.fromFile(apkFile);
        try{
            Intent installer = new Intent(Intent.ACTION_VIEW);
            installer.setDataAndType(apkUri, "application/vnd.android.package-archive");
            startActivity(installer);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void forceStop() {
        AlertDialog.Builder askForceStop = new AlertDialog.Builder(this);
        askForceStop.setTitle(getString(R.string.yesnobox_title_askForceStop));
        askForceStop.setMessage(getString(R.string.yesnobox_desc_askForceStop));
        askForceStop.setPositiveButton(getString(R.string.yesnobox_yes_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                if(latestID != -1)
                {
                    downloadManager.remove(latestID);
                }
                dialog.dismiss();
            }
        });
        askForceStop.setNegativeButton(getString(R.string.yesnobox_no_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        askForceStop.show();
    }

    public void unInstallPackage(platformType p){
        try{
            if(isInstalled(getPlatformPackageName(p))){
                Uri packageUri = Uri.parse("package:" + getPlatformPackageName(p));
                Intent unInstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                startActivity(unInstallIntent);
                return;
            }
            okMsgBox(getString(R.string.msgbox_title_noPackage), getString(R.string.msgbox_desc_noPackage));
        }catch(Exception e){
        }
    }

    public void deletePatch(platformType p){
        File langFile = new File(directories.forPatch[getPlatformIndex(p)], patchFileType[0]);
        File textesFile = new File(directories.forPatch[getPlatformIndex(p)], patchFileType[1]);

        if(langFile.exists() | textesFile.exists()){
            if(langFile.exists())
                langFile.delete();
            if(textesFile.exists())
                textesFile.delete();
            okMsgBox(getString(R.string.msgbox_title_doneDeletePatch), getString(R.string.msgbox_desc_doneDeletePatch));
            return;
        }
        okMsgBox(getString(R.string.msgbox_title_failedDeletePatch), getString(R.string.msgbox_desc_failedDeletePatch));
    }

    public boolean isInstalled(String packName) {
        PackageManager pm = this.getPackageManager();
        try{
            pm.getPackageInfo(packName, PackageManager.GET_ACTIVITIES);
            return true;
        }catch(PackageManager.NameNotFoundException e){
            return false;
        }
    }

    public void unCensored(platformType p){
        uncensorSource usource = new uncensorSource();
        switch(p)
        {
            case DIGITALSKY:
                usource.noMosaicPatch("mica");
                break;
            case BILIBILI:
                usource.noMosaicPatch("bili");
                break;
        }
    }

    public class uncensorSource{ //by 류하/Ather
        private String TEMP_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        void noMosaicPatch(String flag){
            BufferedReader reader, reader1;
            if(flag.equals("mica")){ //mica
                try{
                    execute("chmod 777 data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.xml");
                    //reader = execute("cd data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs\nls -al");
                    String micaPath = "data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.xml";
                    reader = execute("cat " + micaPath);
                    String output, sTemp = "";
                    StringBuffer buffer = new StringBuffer();
                    while((output = reader.readLine())!=null){
                        if(output.contains("int name=\"Normal\"")){
                            StringBuilder temp = new StringBuilder(output);
                            temp.setCharAt(30, '1');
                            //Log.d("check", temp.toString() + " && " + output);
                            output = temp.toString();
                        }
                        buffer.append(output);
                        buffer.append("\n");
                    }
                    reader.close();
                    //Log.d("output", buffer.toString());

                    //file change
                    writeFile(micaPath, buffer.toString());
                    execute("chmod 440 data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.xml");
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{ //bili
                try{
                    execute("chmod 777 data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.bili.xml");
                    //reader = execute("cd data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs\nls -al");
                    String biliPath = "data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.bili.xml";
                    reader = execute("cat " + biliPath);
                    String output, sTemp = "";
                    StringBuffer buffer = new StringBuffer();
                    while((output = reader.readLine())!=null){
                        if(output.contains("int name=\"Normal\"")){
                            StringBuilder temp = new StringBuilder(output);
                            temp.setCharAt(30, '1');
                            //Log.d("check", temp.toString() + " && " + output);
                            output = temp.toString();
                        }
                        buffer.append(output);
                        buffer.append("\n");
                    }
                    reader.close();
                    //Log.d("output", buffer.toString());

                    //file change
                    writeFile(biliPath, buffer.toString());
                    execute("chmod 440 data/data/com.digitalsky.girlsfrontline.cn.bili/shared_prefs/com.digitalsky.girlsfrontline.cn.bili.xml");
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        public BufferedReader execute(String cmd){
            BufferedReader reader = null; //errReader = null;
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(cmd + "\n");
                os.writeBytes("exit\n");
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String err = (new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine();
                os.flush();

                if (process.waitFor() != 0 || (!"".equals(err) && null != err)) {
                    Log.e("920TERoot", err);
                    okMsgBox(getString(R.string.msgbox_title_nonRooting), getString(R.string.msgbox_desc_nonRooting));
                    return null;
                }
                return reader;
            } catch(Exception e){
                e.printStackTrace();
                okMsgBox(getString(R.string.msgbox_title_nonRooting), getString(R.string.msgbox_desc_nonRooting));
            }
            return null;
        }

        public boolean writeFile(String path, String text){
            try{
                File file = new File(path);
                String tempFile = TEMP_PATH + "/.tempPath";
                String fileString = path;
                if(!file.canWrite()){
                    fileString = tempFile;
                }
                BufferedWriter bw = null;
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileString), Charset.forName("UTF-8")));
                bw.write(text);
                bw.close();
                BufferedReader ret = execute("ls " + getCmdPath(fileString) + " " + getCmdPath(path));
                if(ret == null){
                    return false;
                }
                return true;
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }

        public String getCmdPath(String path){
            return path.replace(" ", "\\ ").replace("'", "\\'");
        }
    }

    //Call-Back 함수 구간
    // -----------------------------------------------------------

    public void callbackRefresh(){ // Refresh 호출 후 check.json 받은 후 호출되는 콜백 함수
        try{
            File checkJSONFile = new File(directories.forDownload, "check.json");

            try{
                String vanillaString = null;
                InputStream inputStream = new FileInputStream(checkJSONFile);
                int fileSize = inputStream.available();
                byte[] buffer = new byte[fileSize];
                inputStream.read(buffer);
                inputStream.close();
                vanillaString = new String(buffer, "UTF-8");

                JSONObject blockAndroid = new JSONObject(vanillaString).getJSONObject("app_version").getJSONObject("android");
                JSONObject blockguiroot = blockAndroid.getJSONObject("gui");
                JSONObject[] blockguis = {blockguiroot.getJSONObject("gui_digitalsky"), blockguiroot.getJSONObject("gui_bilibili")};
                JSONObject blocktext = blockAndroid.getJSONObject("text");
                JSONObject blockUmb = blockAndroid.getJSONObject("umb");
                JSONObject blockSideA = blocktext.getJSONObject("a");
                JSONObject blockSideB = blocktext.getJSONObject("b");

                urls.apks = new String[2];
                urls.apks[getPlatformIndex(platformType.DIGITALSKY)] = blockguis[0].getString("download_link");
                urls.apks[getPlatformIndex(platformType.BILIBILI)] = blockguis[1].getString("download_link");

                versions.latestDigitalSky = blockguis[0].getString("version");
                versions.latestBiliBili = blockguis[1].getString("version");
                versions.latestUmb = blockUmb.getString("version");

                urls.guiNoticeJSON = blockguiroot.getString("changelog");

                urls.sideA = blockSideA.getString("download_link");
                urls.sideB = blockSideB.getString("download_link");
                urls.patchSite = blockUmb.getString("patchSite");

                textesChangeDate = blocktext.getString("date");

                sideA.langSize = blockSideA.getString("langSize");
                sideA.textesSize = blockSideA.getString("textesSize");
                sideB.langSize = blockSideB.getString("langSize");
                sideB.textesSize = blockSideB.getString("textesSize");

                btn_sideA = blockSideA.getString("buttonText");
                btn_sideB = blockSideB.getString("buttonText");

                urls.textNoticeJSON = blocktext.getString("changelog");

                umbURL = blockUmb.getString("download_link");

                try {
                    patchMessage = blockUmb.getString("patchMessage");
                    oneSideMode = blockUmb.getBoolean("oneSideMode");
                }catch(Exception e){
                    oneSideMode = false;
                    patchMessage = getString(R.string.yesnobox_desc_askSide);
                }

                appInfo = blockUmb.getString("appInfo");

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }catch(Exception e) {
            AlertDialog.Builder failedGet = new AlertDialog.Builder(this);
            failedGet.setTitle(getString(R.string.msgbox_title_failedget));
            failedGet.setMessage(getString(R.string.msgbox_desc_failedget));
            failedGet.setPositiveButton(getString(R.string.msgbox_ok_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //finish();
                    //System.exit(0);
                }
            });
            failedGet.show();
        }
        requestNotice();
    }

    public void callbackRequestNotice(){ //공지사항 파일을 다 받은후 호출되는 콜백 함수
        if(!firstTime) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            int i;
            for (i = 0; i < 3; i++) {
                if (navigationView.getMenu().getItem(i).isChecked()) {
                    onNavigationItemSelected(navigationView.getMenu().getItem(i));
                    break;
                }
            }
        }else{
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }
        refreshBar.dismiss();
    }

    public void callbackPatch(platformType p){
        int platform = getPlatformIndex(p);
        setDownloadBar("", getString(R.string.progressdialog_moveFiles), ProgressDialog.STYLE_SPINNER);

        File patchZipFile = new File(directories.forDownload, "patch.zip");
        String forUnzip = directories.forDownload + "/unzip/";

        File newLagFile = new File(forUnzip, patchFileType[0]);
        File newTextesFile = new File(forUnzip, patchFileType[1]);

        if(newLagFile.exists()){
            newLagFile.delete();
        }
        if(newTextesFile.exists()){
            newTextesFile.delete();
        }

        Decompress decompress = new Decompress(patchZipFile.toString(), forUnzip);
        decompress.unzip();

        File targetLangFile = new File(directories.forPatch[platform], patchFileType[0]);
        File targetTextesFile = new File(directories.forPatch[platform], patchFileType[1]);

        if(targetLangFile.exists())
        {
            targetLangFile.delete();
        }
        if(targetTextesFile.exists())
        {
            targetTextesFile.delete();
        }

        try{
            FileInputStream inputStream = new FileInputStream(newLagFile);
            FileOutputStream outputStream = new FileOutputStream(targetLangFile);

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long fileSize = fcin.size();
            fcin.transferTo(0, fileSize, fcout);

            inputStream = new FileInputStream(newTextesFile);
            outputStream = new FileOutputStream(targetTextesFile);

            fcin = inputStream.getChannel();
            fcout = outputStream.getChannel();

            fileSize = fcin.size();
            fcin.transferTo(0, fileSize, fcout);

            inputStream.close();
            outputStream.close();
            fcin.close();
            fcout.close();

            newLagFile.delete();
            newTextesFile.delete();

            okMsgBox(getString(R.string.msgbox_title_patchComplete), getString(R.string.msgbox_desc_patchComplete));
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    //getter, setter 구간
    //------------------------------------------------------------

    public String getPlatformPackageName(platformType type) { //플랫폼별 패키지 이름 불러오기
        switch(type)
        {
            case DIGITALSKY:
                return platformPackageName[0];
            case BILIBILI:
                return platformPackageName[1];
        }
        return null;
    }

    public String getPlatformPackageVersion(platformType type){//플랫폼별 패키지 버전 받아오기, 설치되어 있으면 버전을 그렇지 않으면 null을 반환합니다.
        PackageManager pm = getPackageManager();
        switch (type) {
            case DIGITALSKY:
                try{
                    return pm.getPackageInfo(getPlatformPackageName(platformType.DIGITALSKY), PackageManager.GET_ACTIVITIES).versionName;
                }catch(PackageManager.NameNotFoundException e) {
                    return getString(R.string.source_notInstalled);
                }
            case BILIBILI:
                try{
                    return pm.getPackageInfo(getPlatformPackageName(platformType.BILIBILI), PackageManager.GET_ACTIVITIES).versionName;
                }catch(PackageManager.NameNotFoundException e) {
                    return getString(R.string.source_notInstalled);
                }
        }
        return getString(R.string.source_notInstalled);
    }

    public int getPlatformIndex(platformType p) {
        if(p == platformType.DIGITALSKY)
            return 0;
        return 1;
    }

    public boolean getSuccessConnect(){
        return bSuccessConnect;
    }

    public boolean getCustomServer() { return bCustomServer; }

    public String getCustomServerURL() { return customJSONURL; }

    // --------------------------------------------------------------

    public void setDownloadBar(String title, String msg, int style) {
        downloadBar.setMessage(msg);
        downloadBar.setProgressStyle(style);
    }

    // --------------------------------------------------------------

    public void okMsgBox(String title, String desc){ // OK 버튼만 존재하는 메시지박스를 출력
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(desc);
        builder.setPositiveButton(R.string.msgbox_ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private class DownloadTask extends AsyncTask<String, String, Long> { //범용 다운로드 쓰레드
        private Context context;
        private String fileName;
        private String targetFolder;
        private String DownloadType; //PATCH_FILE, DATA_FILE, NORMAL
        private File targetFile;
        private boolean showProgressBar;
        private platformType platform;

        public DownloadTask(Context cons, String fName, String tFolder, boolean showPBar, String DownType, platformType p) { //현재 Context, 파일명, 다운 폴더, 프로그래스바 표시, 이후 처리용 다운로드 목적
            context = cons;
            fileName = fName;
            targetFolder = tFolder;
            showProgressBar = showPBar;
            DownloadType = DownType;
            platform = p;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgressBar) {
                downloadBar.show();
            }
        }

        @Override
        protected Long doInBackground(String... urls) {
            int count;
            long FileSize = -1;
            InputStream inStream = null;
            OutputStream outStream = null;
            URLConnection URLCon = null;

            try {
                URL url = new URL(urls[0]);
                URLCon = url.openConnection();
                URLCon.connect();

                FileSize = URLCon.getContentLength();

                inStream = new BufferedInputStream(url.openStream(), 8192);
                targetFile = new File(targetFolder, fileName);
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                outStream = new FileOutputStream(targetFile);

                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while ((count = inStream.read(data)) != -1) {
                    downloadedSize += count;
                    if (FileSize > 0) {
                        double per = ((double) downloadedSize / FileSize) * 100;
                        String meg = "Downloading\n" + (int) downloadedSize + "Byte / " + (int) FileSize + "Byte (" + (int) per + "%)";
                        publishProgress((int) ((downloadedSize * 100) / FileSize) + "", meg);
                    }
                    outStream.write(data, 0, count);
                }

                outStream.flush();

                outStream.close();
                inStream.close();

            } catch (Exception e) {
                //Log.e("Error : ", e.getMessage());
                bSuccessConnect = false;
            }

            return FileSize;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (showProgressBar) {
                downloadBar.setIndeterminate(false);
                downloadBar.setMax(100);
                downloadBar.setProgress(Integer.parseInt(values[0]));
                downloadBar.setMessage(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (showProgressBar)
                downloadBar.dismiss();
            switch (DownloadType) {
                case "PATCH_FILE":
                    callbackPatch(platform);
                    break;
                case "REFRESH_FILE":
                    callbackRefresh();
                    break;
                case "TEXT_NOTICE_FILE":
                    DownloadTask dTask = new DownloadTask(context, "posts_gui.json", directories.forDownload, false, "GUI_NOTICE_FILE", null);
                    dTask.execute(urls.guiNoticeJSON);
                    break;
                case "GUI_NOTICE_FILE":
                    callbackRequestNotice();
                    break;
                case "NORMAL":
                default:
                    break;
            }
        }
    }
}

package team.hatsan.grlfonkorpatcher;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity{

    ProgressDialog pbar;
    ProgressDialog loadingBar;
    String newBiliver;
    String newdskyver;
    String upDate;
    String reqURL = "https://frozens.tk/files/check.json";
    String sideAURL = "http://ftp.frozens.tk/files/170730_Android_Text_KR_Side_A_Fix1.zip";
    String sideBURL = "http://ftp.frozens.tk/files/170730_Android_Text_KR_Side_B_Fix1.zip";
    String selectedURL;
    String targetAddr;
    String notice;
    String textesByte;
    String langByte;
    File downloadAddr;
    File resultAddr;
    boolean canBili = false;
    boolean candsky = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadAddr = getExternalCacheDir();
        checkPer();
    }

    public void onBackPressed() {
        super.onBackPressed();
        //if((!pbar.isShowing() && (!loadingBar.isShowing()))){
            this.finish();
            //System.exit(0);
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId())
        {
            case R.id.action_refresh:
                cleaner();
                afterPer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void cleaner()
    {
        TextView contex = (TextView) findViewById(R.id.text_curDate);
        TextView dskytex = (TextView) findViewById(R.id.text_dskyVersion);
        TextView bilitex = (TextView) findViewById(R.id.text_biliVersion);
        TextView newdsky = (TextView) findViewById(R.id.text_newdsky);
        TextView newBili = (TextView) findViewById(R.id.text_newBili);
        TextView noticetex = (TextView) findViewById(R.id.text_notice);

        contex.setText("N/A");
        newdsky.setText("");
        newBili.setText("");
        noticetex.setText("");
    }

    public void afterPer(){
        TextView contex = (TextView) findViewById(R.id.text_curDate);
        TextView dskytex = (TextView) findViewById(R.id.text_dskyVersion);
        TextView bilitex = (TextView) findViewById(R.id.text_biliVersion);
        TextView newdsky = (TextView) findViewById(R.id.text_newdsky);
        TextView newBili = (TextView) findViewById(R.id.text_newBili);
        TextView noticetex = (TextView) findViewById(R.id.text_notice);

        loadingBar = new ProgressDialog(this);
        loadingBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingBar.setMessage("서버로부터 데이터를 불러오는 중입니다.");
        loadingBar.setCancelable(false);
        loadingBar.show();

        postGetText();
        PackageManager pm = this.getPackageManager();

        String verTemp;

        if(isInstalled("com.digitalsky.girlsfrontline.cn", this))
        {
            candsky = true;
            verTemp = getVersion("com.digitalsky.girlsfrontline.cn", this);
            if(verTemp.equals(newdskyver))
            {
                dskytex.setTextColor(Color.BLUE);
            }
            else
            {
                dskytex.setTextColor(Color.RED);
            }
            dskytex.setText(verTemp);
        }
        else
        {
            dskytex.setTextColor(Color.RED);
            dskytex.setText("미설치");
        }
        if(isInstalled("com.digitalsky.girlsfrontline.cn.bili", this))
        {
            canBili = true;
            verTemp = getVersion("com.digitalsky.girlsfrontline.cn.bili", this);
            if(verTemp.equals(newBiliver))
            {
                bilitex.setTextColor(Color.BLUE);
            }
            else
            {
                bilitex.setTextColor(Color.RED);
            }
            bilitex.setText(verTemp);
        }
        else
        {
            bilitex.setTextColor(Color.RED);
            bilitex.setText("미설치");
        }
    }

    public void postGetText()
    {
        DownloadText downloadText = new DownloadText("check.json", 0);
        downloadText.execute(reqURL);
    }

    public void getText()
    {
        try{
            File versionFile = new File(downloadAddr, "check.json");
            String noticeURL;

            try {
                String jsonString = null;
                InputStream istream = new FileInputStream(versionFile);
                int jsonSize = istream.available();
                byte[] jsonBuf = new byte[jsonSize];
                istream.read(jsonBuf);
                istream.close();
                jsonString = new String(jsonBuf, "UTF-8");

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject jsonFirst = jsonObject.getJSONObject("app_version");
                JSONObject jsonAndroid = jsonFirst.getJSONObject("android");
                JSONObject jsondsky = jsonAndroid.getJSONObject("gui_digitalsky");
                JSONObject jsonbili = jsonAndroid.getJSONObject("gui_bilibili");
                JSONObject jsonFinalObject = jsonAndroid.getJSONObject("text");
                JSONObject jsonDownloadObject = jsonFinalObject.getJSONObject("download_link");

                upDate = jsonFinalObject.getString("date");
                newdskyver = jsondsky.getString("version");
                newBiliver = jsonbili.getString("version");
                sideAURL = jsonDownloadObject.getString("a");
                sideBURL = jsonDownloadObject.getString("b");
                noticeURL = jsonFinalObject.getString("changelog");
                langByte = jsonFinalObject.getString("langSize");
                textesByte = jsonFinalObject.getString("textesSize");

                DownloadText noticeDown = new DownloadText("posts.json", 1);
                noticeDown.execute(noticeURL);

                //noticeURL = nojsonObject.getString("link");

                //noticeParser nparser = new noticeParser();
                //nparser.execute(noticeURL);

            }catch(IOException e) {
                e.printStackTrace();
            }

            /*FileReader fr = new FileReader(versionFile);
            BufferedReader bfr = new BufferedReader(fr);

            upDate = bfr.readLine();
            CurrentVersion = bfr.readLine();
            sideAURL = bfr.readLine();
            sideBURL = bfr.readLine();
            notice = bfr.readLine();
            String jsonString = getStringFromUrl*/

        }
        catch(Exception e){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("오류").setMessage("버전 정보를 받아올 수 없습니다.\n" + e.getMessage());
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    public void parsingFunc()
    {
        try {
            File noticeFile = new File(downloadAddr, "posts.json");

            InputStream istream = new FileInputStream(noticeFile);
            int jsonSize = istream.available();
            byte[] jsonBuf = new byte[jsonSize];
            istream.read(jsonBuf);
            istream.close();
            String jsonString = new String(jsonBuf, "UTF-8");

            //jsonString.replace("[", "");
            //jsonString.replace("]", "");
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject nojsonObject = jsonArray.getJSONObject(0);
            JSONObject guidObject = nojsonObject.getJSONObject("content");
            notice = guidObject.getString("rendered");
            notice = notice.replace("<p>", "");
            notice = notice.replace("<strong>", "");
            notice = notice.replace("</p>", "\n");
            notice = notice.replace("</strong>", "");

            TextView nos = (TextView) findViewById(R.id.text_notice);
            nos.setText(notice);

            TextView contex = (TextView) findViewById(R.id.text_curDate);
            TextView newdsky = (TextView) findViewById(R.id.text_newdsky);
            TextView newBili = (TextView) findViewById(R.id.text_newBili);
            TextView noticetex = (TextView) findViewById(R.id.text_notice);

            Double showSizeL = Double.parseDouble(langByte);
            Double showSizeT = Double.parseDouble(textesByte);
            contex.setText(upDate);// + "(" + showSizeL.toString() + "Byte, " + showSizeT.toString() + "Byte)");
            noticetex.setText(notice);
            newdsky.setText(newdskyver);
            newBili.setText(newBiliver);

            loadingBar.dismiss();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeF(InputStream in, OutputStream ou) throws IOException
    {
        int data = 0;
        while((data = in.read()) != -1)
        {
            ou.write(data);
        }
        ou.flush();
    }

    public boolean isInstalled(String packName, Context con)
    {
        PackageManager pm = con.getPackageManager();
        try{
            pm.getPackageInfo(packName, PackageManager.GET_ACTIVITIES);
            return true;
        }catch(PackageManager.NameNotFoundException e){
            return false;
        }
    }

    public String getVersion(String packName, Context con)
    {
        PackageManager pm = con.getPackageManager();
        try{
            PackageInfo pi = pm.getPackageInfo(packName, PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        }catch(PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public void patchbili(View v)
    {
        if(canBili) {
            targetAddr = Environment.getExternalStorageDirectory() + "/Android/data/com.digitalsky.girlsfrontline.cn.bili/files/Android/";
            if (checkFiles() && checkSize(targetAddr)) {
                patch();
            }
        }
    }

    public void patchdsky(View v)
    {
        if(candsky) {
            targetAddr = Environment.getExternalStorageDirectory() + "/Android/data/com.digitalsky.girlsfrontline.cn/files/Android/";
            if (checkFiles() && checkSize(targetAddr)) {
                patch();
            }
        }
    }

    public boolean checkSize(String packAddr)
    {
        File langFile = new File(packAddr, "asset_language.ab");
        File textesFile = new File(packAddr, "asset_textes.ab");
        if(langFile.exists() && textesFile.exists())
        {
            Long langSize = langFile.length();
            Long textesSize = textesFile.length();
            String langSizeStr = langSize.toString();
            String textesSizeStr = textesSize.toString();

            if(langSizeStr.equals(langByte) && textesSizeStr.equals(textesByte))
            {
                return true;
            }
        }
        AlertDialog.Builder alertSize = new AlertDialog.Builder(this);
        alertSize.setTitle("파일 체크 오류");
        alertSize.setMessage("데이터 파일이 없거나 서버와 일치하지 않습니다.\n데이터 파일을 받으시거나 서버가 대응중인지 확인해주세요.");
        alertSize.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertSize.show();
        return false;
    }

    public void checkPer()
    {
        int permissioncheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissioncheck == PackageManager.PERMISSION_DENIED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    AlertDialog.Builder alertPer = new AlertDialog.Builder(this);
                    alertPer.setTitle("권한 요청").setMessage("패치를 위해서는 단말기의 저장 권한을 필요로 합니다.\n계속하시겠습니까?");
                    alertPer.setPositiveButton("예", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                            }
                            dialog.dismiss();
                        }
                    });
                    alertPer.setNegativeButton("아니요", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
                    alertPer.show();
                }
                else
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                }
            }
            else
            {
                afterPer();
            }
        }
        else {
            afterPer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case 1000:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    afterPer();
                }
                else
                {
                    finish();
                    System.exit(0);
                }
        }
    }


    public boolean checkFiles()
    {
        File langFile = new File(targetAddr, "asset_language.ab");
        File textFile = new File(targetAddr, "asset_textes.ab");

        if(langFile.exists())
        {
            if(textFile.exists())
            {
                return true;
            }
        }
        return false;
    }


    public void patch()
    {
        AlertDialog.Builder sideAlert = new AlertDialog.Builder(this);

        sideAlert.setTitle("Side 선택");
        sideAlert.setMessage("패치 Side를 선택해주세요\nA Side : 메인 스토리\nB Side : 이벤트 스토리/가구 설명\n외에는 전부 동일.");
        sideAlert.setNegativeButton("A Side", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedURL = sideAURL;
                dialog.dismiss();
                prog();
            }
        });
        sideAlert.setPositiveButton("B Side", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedURL = sideBURL;
                dialog.dismiss();
                prog();
            }
        });
        //AlertDialog dialog = sideAlert.create();
        sideAlert.show();
    }

    public void prog() {
        pbar = new ProgressDialog(MainActivity.this);
        pbar.setMessage("패치파일 다운로드 중");
        pbar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pbar.setIndeterminate(true);
        pbar.setCancelable(false);

        DownloadTask dtask = new DownloadTask(this);
        dtask.execute(selectedURL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class DownloadText extends AsyncTask<String, String, Long>{
        private Context context;
        String downFileName;
        int call;

        public DownloadText(String Fname, int i){
            downFileName = Fname;
            call = i;
        }

        @Override
        protected Long doInBackground(String... urls) {
            int count;
            long FileSize = -1;
            InputStream inStream = null;
            OutputStream outStream = null;
            URLConnection URLCon = null;

            try{
                URL url = new URL(urls[0]);
                URLCon = url.openConnection();
                URLCon.connect();

                FileSize = URLCon.getContentLength();

                inStream = new BufferedInputStream(url.openStream(), 8192);
                File resultFiles = new File(downloadAddr, downFileName);
                if(resultFiles.exists())
                {
                    resultFiles.delete();
                }
                outStream = new FileOutputStream(resultFiles);

                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while((count = inStream.read(data)) != -1)
                {
                    downloadedSize += count;
                    outStream.write(data, 0, count);
                }

                outStream.flush();

                outStream.close();
                inStream.close();

            }catch(Exception e) {
                e.printStackTrace();
            }
            return FileSize;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            switch(call)
            {
                case 0:
                    getText();
                    break;
                case 1:
                    parsingFunc();
                    break;
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, String, Long> {

        private Context context;

        public DownloadTask(Context con)
        {
            this.context = con;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbar.show();
        }

        @Override
        protected Long doInBackground(String... urls) {
            int count;
            long FileSize = -1;
            InputStream inStream = null;
            OutputStream outStream = null;
            URLConnection URLCon = null;

            try{
                URL url = new URL(urls[0]);
                URLCon = url.openConnection();
                URLCon.connect();

                FileSize = URLCon.getContentLength();

                inStream = new BufferedInputStream(url.openStream(), 8192);
                resultAddr = new File(downloadAddr, "patch.zip");
                if(resultAddr.exists())
                {
                    resultAddr.delete();
                }
                outStream = new FileOutputStream(resultAddr);

                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while((count = inStream.read(data)) != -1){
                    downloadedSize += count;
                    if(FileSize > 0)
                    {
                        double per = ((double)downloadedSize/FileSize) * 100;
                        String meg = "다운로드 중\n" + (int)downloadedSize + "KB / "+(int)FileSize + "KB ("+ (int)per+"%)";
                        publishProgress((int)((downloadedSize * 100) / FileSize) + "", meg);
                    }

                    outStream.write(data, 0, count);
                }

                outStream.flush();

                outStream.close();
                inStream.close();

            }catch(Exception e){
                Log.e("Error : ", e.getMessage());
            }

            return FileSize;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            pbar.setIndeterminate(false);
            pbar.setMax(100);
            pbar.setProgress(Integer.parseInt(values[0]));
            pbar.setMessage(values[1]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            pbar.dismiss();
            moveFiles(resultAddr.toString(), downloadAddr + "/unzip/");
        }
    }

    public void moveFiles(String zipFile, String targetLoc){
        Decompress d = new Decompress(zipFile, targetLoc);
        d.unzip();
        File langFile = new File(targetAddr, "asset_language.ab");
        File textFile = new File(targetAddr, "asset_textes.ab");
        if(langFile.exists())
        {
            langFile.delete();
        }
        if(textFile.exists())
        {
            textFile.delete();
        }
        try{
            FileInputStream inputStream = new FileInputStream(targetLoc + "asset_language.ab");
            FileOutputStream outputStream = new FileOutputStream(targetAddr + "asset_language.ab");

            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long FSize = fcin.size();
            fcin.transferTo(0, FSize, fcout);

            inputStream = new FileInputStream(targetLoc + "asset_textes.ab");
            outputStream = new FileOutputStream(targetAddr + "asset_textes.ab");

            fcin = inputStream.getChannel();
            fcout = outputStream.getChannel();

            FSize = fcin.size();
            fcin.transferTo(0, FSize, fcout);

            fcin.close();
            fcout.close();
            inputStream.close();
            outputStream.close();

            AlertDialog.Builder alertEnd = new AlertDialog.Builder(this);
            alertEnd.setTitle("패치 완료").setMessage("패치가 완료되었습니다.");
            alertEnd.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            }
            });

            alertEnd.show();

        }catch(Exception e) {
            Log.e("move", e.getMessage());
        }
    }

    private class noticeParser extends AsyncTask<String, String, Long>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notice = "";
        }

        @Override
        protected Long doInBackground(String... params) {
            long size = 0;
            try {
                Document doc = Jsoup.connect(params[0]).get();
                Elements elc = doc.select("div.entry-content p");

                for(Element el : elc){
                    notice += el + "\n";
                }

            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return size;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            TextView not = (TextView) findViewById(R.id.text_notice);
            notice = notice.replace("<p>", "");
            notice = notice.replace("<strong>", "");
            notice = notice.replace("</p>", "");
            notice = notice.replace("</strong>", "");
            not.setText(notice);
        }
    }
}

package team.hatsan.grlfonkorpatcher;

import android.support.v4.app.Fragment;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by KNH on 2017-09-13.
 */

public class BaseFragment extends Fragment {

    public String parsingNotice(String fileName)
    {
        String notice = null;
        try {
            File noticeFile = new File(getMainActivity().directories.forDownload, fileName);
            InputStream inputStream = new FileInputStream(noticeFile);

            int jsonSize = inputStream.available();
            byte[] buffer = new byte[jsonSize];
            inputStream.read(buffer);
            inputStream.close();

            String noticeString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(noticeString);
            notice = jsonArray.getJSONObject(0).getJSONObject("content").getString("rendered");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return notice;
    }

    public MainActivity getMainActivity()
    {
        return (MainActivity)getActivity();
    }
}

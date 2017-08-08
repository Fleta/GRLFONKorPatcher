package team.hatsan.grlfonkorpatcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by KNH on 2017-08-03.
 */
public class Decompress {
    private String src;
    private String dest;

    public Decompress(String zipFile, String location) {
        src = zipFile;
        dest = location;
    }

    public void unzip()
    {
        final int BUFFER_SIZE = 4096;
        BufferedOutputStream bufferedOutputStream = null;
        FileInputStream fileInputStream;
        File desdir = new File(dest);
        if(!desdir.exists())
        {
            desdir.mkdir();
        }
        try
        {
            fileInputStream = new FileInputStream(src);
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                String zipEntryName = zipEntry.getName();
                File file = new File(dest + File.separator + zipEntryName);
                if (file.exists())
                {

                } else {
                    if (zipEntry.isDirectory())
                        {
                            file.mkdirs();
                        } else
                        {
                            byte buffer[] = new byte[BUFFER_SIZE];
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
                            int count;
                            while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1)
                            {
                                bufferedOutputStream.write(buffer, 0, count);
                            }
                            bufferedOutputStream.flush();
                            bufferedOutputStream.close();
                        }
                    }
                }
                zipInputStream.close();
                File s = new File(src);
                s.delete();
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

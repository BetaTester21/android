package pe.edu.idat.tarea;

import java.io.File;

import android.os.Environment;

public class FroyoAlbumDirFactory extends AlbumStorageDirFactory{

    @Override
    public File getAlbumStorageDir(String albumName) {
        // TODO Auto-generated method stub
        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}

package pe.edu.idat.tarea;

import java.io.File;

abstract class AlbumStorageDirFactory {

    public abstract File getAlbumStorageDir(String albumName);
}
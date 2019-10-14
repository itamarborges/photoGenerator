package borbi.br.photogenerator.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gabriela on 14/04/2016.
 */
public class ImageUtils {

    /*
    Copia uma imagem do diretório privado do app para o diretório público. Devolve a Uri do caminho público.
     */

    public static final String PICTURE_DIRECTORY = "photos";

    public static Uri copyImageToPublicDirectory(Uri privateFilePath,Context context) throws IOException {
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PICTURE_DIRECTORY);

        // Create the storage directory if it does not exist
        if(!createMediaStorageDir(mediaStorageDir)){
            Log.v(LogUtils.makeLogTag(ImageUtils.class), "failed to create directory");
        }

        File sourceFile = new File(privateFilePath.getPath());
        File destFile = new File(getExternalFileName(mediaStorageDir));

        FileChannel source = new FileInputStream(sourceFile).getChannel();
        FileChannel destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

        return Uri.fromFile(destFile);
    }

    private static String getExternalFileName(File mediaStorageDir){
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return mediaStorageDir.getPath() + File.separator + timeStamp + "_IMG.png";
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(Context context){
        return Uri.fromFile(getOutputMediaFile(context));
    }

    private static File getOutputMediaFile(Context context){
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PICTURE_DIRECTORY);

        // Create the storage directory if it does not exist
        if(!createMediaStorageDir(mediaStorageDir)){
            Log.v(LogUtils.makeLogTag(ImageUtils.class), "failed to create directory");
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile= new File(mediaStorageDir.getPath() + File.separator + timeStamp + "_IMG.png");

        Log.v(LogUtils.makeLogTag(ImageUtils.class), "uri do arquivo: " + mediaFile.getPath());
        if(mediaFile == null){
            Log.v(LogUtils.makeLogTag(ImageUtils.class), "arquivo é null" );
        }else{
            Log.v(LogUtils.makeLogTag(ImageUtils.class), "arquivo não é null");
        }

        return mediaFile;
    }

    private static boolean createMediaStorageDir(File mediaStorageDir) {
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.v(LogUtils.makeLogTag(ImageUtils.class), "failed to create directory");
                return false;
            }
        }
        return true;
    }

    private static File getOutputInternalMediaFile(Context context, CharSequence complementFileName){
        File mediaStorageDir = new File(context.getFilesDir(), PICTURE_DIRECTORY);

        createMediaStorageDir(mediaStorageDir);

        return createFile(mediaStorageDir, complementFileName);
    }

    private static File getOutputExternalMediaFile(Context context, CharSequence complementFileName){
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                PICTURE_DIRECTORY
        );

        createMediaStorageDir(mediaStorageDir);

        return createFile(mediaStorageDir, complementFileName);
    }

    private static File createFile(File mediaStorageDir, CharSequence complementFileName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir .getPath() + File.separator + timeStamp + "_IMG_" + complementFileName + ".png");
        return mediaFile;
    }

    public static Uri saveToExternalStorage(Context context, Bitmap bitmap, CharSequence complementFileName){
        OutputStream outStream = null;

        File destinationInternalImageFile = getOutputExternalMediaFile(context, complementFileName);

        try {
            outStream = new FileOutputStream(destinationInternalImageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                // Eh
            }
        }
        return Uri.fromFile(destinationInternalImageFile);
    }

    public static Uri saveToInternalStorage(Context context, Uri tempUri){
        InputStream in = null;
        OutputStream out = null;

        File sourceExternalImageFile = new File(tempUri.getPath());
        File destinationInternalImageFile = new File(getOutputInternalMediaFile(context, "original").getPath());

        try{
            destinationInternalImageFile.createNewFile();

            in = new FileInputStream(sourceExternalImageFile);
            out = new FileOutputStream(destinationInternalImageFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //Handle error
        }
        finally
        {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    in.close();
                }
            } catch (IOException e) {
                // Eh
            }
        }
        return Uri.fromFile(destinationInternalImageFile);
    }

    /*
    Busca o caminho da imagem na galeria acessível para outros apps, a partir da URI da galeria.
     */
    public static String getPath(Uri uri, Context context) {
        String selectedImagePath = null;
        //1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }

        if(selectedImagePath == null){
            //2:OI FILE Manager --- call method: uri.getPath()
            selectedImagePath = uri.getPath();
        }
        return selectedImagePath;
    }

    public static Bitmap decodeSampledBitmapFromFile(Uri imageUri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageUri.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageUri.getPath(), options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}

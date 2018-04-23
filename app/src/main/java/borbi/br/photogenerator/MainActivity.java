package borbi.br.photogenerator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;

import borbi.br.photogenerator.enums.Order;
import borbi.br.photogenerator.enums.RGB;
import borbi.br.photogenerator.fragments.PhotoFragment;
import borbi.br.photogenerator.interfaces.PictureTaken;
import borbi.br.photogenerator.pojo.ProcessedImage;
import borbi.br.photogenerator.tasks.ReorderTask;
import borbi.br.photogenerator.utils.ImageUtils;

interface TaskStatus {

    public void OnTaskFinished(ProcessedImage processedImage);
    public void OnProgressUpdate();

}

public class MainActivity extends FragmentActivity implements PictureTaken, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    Button mBtnChangeOrder;
    Button mBtnSaveImage;
    Button mBtnCamera;
    Button mBtnGallery;
    String pathPicture = null;
    Order mCurrentOrder = null;
    Integer[] mBitmapColors = null;
    HashMap<Order, Bitmap> mArrayBitmaps = new HashMap<Order, Bitmap>();
    ImageView mImageView;
    PhotoFragment mPhotoFragment;
    TextView mStatusTextView;
    Bitmap mBitmapReordenar = null;
    ReorderTask mReorderTask;
    Order mOrderToShow = Order.Regular;
    Context mContext;
    int[] mPixels;
    int countImageProcessing = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mBtnChangeOrder = (Button) findViewById(R.id.btnChangeOrder);
        mBtnSaveImage = (Button) findViewById(R.id.btnSaveImage);
        mImageView = (ImageView) findViewById(R.id.photoImageView);
        mPhotoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentById(R.id.photoFragment);
        mStatusTextView = (TextView) mPhotoFragment.getView().findViewById(R.id.statusTextView);
        mBtnCamera = (Button) mPhotoFragment.getView().findViewById(R.id.cameraButton);
        mBtnGallery = (Button) mPhotoFragment.getView().findViewById(R.id.galleryButton);

        initializeArrayImages();

        mBtnSaveImage.setEnabled(false);

        mBtnChangeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pathPicture != null) {
                    if (mArrayBitmaps.get(Order.Regular) == null) {
                        mBtnSaveImage.setEnabled(false);
                        reordenarBits();
                    } else {
                        mCurrentOrder = nextSequence(mCurrentOrder);
                        showImage(mArrayBitmaps.get(mCurrentOrder));
                        mStatusTextView.setText("Ordenação: " + getDescriptionOrder(mCurrentOrder));
                    }

                } else {
                    Toast.makeText(getApplication(), "Selecione uma foto antes!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pathPicture != null) {
                    if (canAccessGallery()) {
                        habilitarBotoesTela(false);
                        mBtnSaveImage.setEnabled(false);
                        saveImage();
                        mBtnSaveImage.setEnabled(true);
                        habilitarBotoesTela(true);
                    }
                } else {
                    Toast.makeText(getApplication(), "Selecione uma imagem para salvar!", Toast.LENGTH_LONG).show();
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initializeArrayImages() {
        mArrayBitmaps.clear();
        mCurrentOrder = Order.Regular;
        for (int i = 0; i < 8; i++) {
            mArrayBitmaps.put(mCurrentOrder, null);
            mCurrentOrder = nextSequence(mCurrentOrder);
        }
        mCurrentOrder = Order.Regular;
    }

    private Order nextSequence(Order mOrder) {

        Order mNextOrder = null;

        if (mOrder == null) {

            mNextOrder = Order.Regular;

        } else if (Order.Regular.equals(mOrder)) {

            mNextOrder = Order.RedGreenBlue;

        } else if (Order.RedGreenBlue.equals(mOrder)) {

            mNextOrder = Order.RedBlueGreen;

        } else if (Order.RedBlueGreen.equals(mOrder)) {

            mNextOrder = Order.GreenBlueRed;

        } else if (Order.GreenBlueRed.equals(mOrder)) {

            mNextOrder = Order.GreenRedBlue;

        } else if (Order.GreenRedBlue.equals(mOrder)) {

            mNextOrder = Order.BlueGreenRed;

        } else if (Order.BlueGreenRed.equals(mOrder)) {

            mNextOrder = Order.BlueRedGreen;

        } else if (Order.BlueRedGreen.equals(mOrder)) {

            mNextOrder = Order.Sum;

        } else if (Order.Sum.equals(mOrder)) {

            mNextOrder = Order.Regular;
        }

        return mNextOrder;
    }

    @Override
    public void OnPictureTaken(String path) {
        pathPicture = path;
        initializeArrayImages();
        mBitmapColors = null;
        mOrderToShow = null;
        mCurrentOrder = Order.Regular;
        mBtnSaveImage.setEnabled(false);
        mStatusTextView.setText("Ordenação: " + getDescriptionOrder(mCurrentOrder));
        if (mReorderTask != null) {
            mReorderTask.cancel(true);
        }
    }

    private CharSequence getDescriptionOrder(Order mCurrentOrder) {

        if (RGB.Regular.equals(mCurrentOrder.order0)) {
            return "Normal";
        } else if (RGB.Sum.equals(mCurrentOrder.order0)) {
            return "Somatório";
        } else {
            return mCurrentOrder.order0.toString() + ", " + mCurrentOrder.order1.toString() + ", " + mCurrentOrder.order2.toString();
        }
    }

    public void reordenarBits() {
        if (mBitmapColors == null) {

            mStatusTextView.setText("Processando Imagens... 1/6");
            habilitarBotoesTela(false);

            mBitmapReordenar = BitmapFactory.decodeFile(pathPicture, null);
            mArrayBitmaps.put(Order.Regular, mBitmapReordenar);
            mPixels = new int[mBitmapReordenar.getWidth() * mBitmapReordenar.getHeight()];
            mBitmapReordenar.getPixels(mPixels, 0, mBitmapReordenar.getWidth(), 0, 0, mBitmapReordenar.getWidth(), mBitmapReordenar.getHeight());
            mBitmapColors = new Integer[mPixels.length];

            List<Integer> wrapper = new AbstractList<Integer>() {
                @Override
                public int size() {
                    return mPixels.length;
                }

                @Override
                public Integer get(int i) {
                    return mPixels[i];
                }
            };

            wrapper.toArray(mBitmapColors);

            mCurrentOrder = nextSequence(mCurrentOrder);
            countImageProcessing = 1;
            createRunTask(mCurrentOrder);
        }
    }

    private void createRunTask(Order sortOrder) {
        mReorderTask = new ReorderTask(this, new TaskStatusListener());
        Object[] params = new Object[3];
        params[0] = mBitmapReordenar;
        params[1] = sortOrder;
        params[2] = mBitmapColors;

        mReorderTask.execute(params);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public class TaskStatusListener implements TaskStatus {
        public void OnTaskFinished(ProcessedImage processedImage) {

            mArrayBitmaps.put(processedImage.getOrder(), processedImage.getBitmap());
            mCurrentOrder = nextSequence(mCurrentOrder);
            if (mArrayBitmaps.get(mCurrentOrder) == null) {
                countImageProcessing++;
                mStatusTextView.setText("Processando Imagens... " + String.valueOf(countImageProcessing) + "/7");
                createRunTask(mCurrentOrder);
            } else {
                habilitarBotoesTela(true);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                mBtnSaveImage.setEnabled(true);
                mStatusTextView.setText("Ordenação: " + getDescriptionOrder(mCurrentOrder));
            }
        }

        @Override
        public void OnProgressUpdate() {
        }
    }

    public void showImage(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    private void galleryAddPic(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private boolean canAccessGallery() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Se não tem permissão para acessar a galeria, solicita.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            }

            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    Toast.makeText(mContext, R.string.cameraNoPermission, Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }

    private void saveImage() {
        Uri contentUri = null;
        contentUri = ImageUtils.saveToExternalStorage(mContext, mArrayBitmaps.get(mCurrentOrder), getDescriptionOrder(mCurrentOrder));

        galleryAddPic(contentUri);

        Toast.makeText(getApplication(), "Imagem salva", Toast.LENGTH_SHORT).show();

    }

    public void habilitarBotoesTela(boolean habilitado) {

        mBtnChangeOrder.setEnabled(habilitado);
        mBtnCamera.setEnabled(habilitado);
        mBtnGallery.setEnabled(habilitado);

    }
}

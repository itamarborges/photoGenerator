package borbi.br.photogenerator.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import borbi.br.photogenerator.R;
import borbi.br.photogenerator.interfaces.PictureTaken;
import borbi.br.photogenerator.utils.ImageUtils;
import borbi.br.photogenerator.utils.LogUtils;
import borbi.br.photogenerator.utils.Utils;

public class PhotoFragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int INTENT_CAMERA = 1;
    private static final int INTENT_GALLERY = 2;

    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_GALLERY = 2;

    private Uri mFileUri;
    private Context mContext;
    private Intent mIntentCamera;
    private Intent mPhotoPickerIntent;
    private ImageView mPhotoImageView;

    public PhotoFragment() {
        // Required empty public constructor
    }

    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        mContext = this.getContext();

        Button cameraButton = rootView.findViewById(R.id.cameraButton);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mIntentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraButton.setOnClickListener(this);

        Button galleryButton = rootView.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);

        mPhotoImageView = rootView.findViewById(R.id.photoImageView);

        return rootView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cameraButton: onCameraButton();break;
            case R.id.galleryButton: onGalleryButton();break;
        }
    }

    private void onCameraButton(){
        if(canUseCamera(mIntentCamera)){
            fillIntentCamera();
            startActivityForResult(mIntentCamera, INTENT_CAMERA);
        }
    }

    private void onGalleryButton(){
        if(canAccessGallery()){
            fillIntentGallery();
            startActivityForResult(mPhotoPickerIntent, INTENT_GALLERY);
        }
    }

    private boolean canAccessGallery(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED){
            // Se não tem permissão para acessar a galeria, solicita.
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_GALLERY);

            return false;
        }
        return  true;
    }

    private boolean canUseCamera(Intent intent){
        if(!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            return false;
        }

        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if(list == null || list.isEmpty()){
            return false;
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED){
            // Se não tem permissão para acessar a câmera, solicita.
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fillIntentCamera();
                    startActivityForResult(mIntentCamera, INTENT_CAMERA);
                }else{
                    Toast.makeText(mContext, R.string.cameraNoPermission, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PERMISSION_GALLERY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fillIntentGallery();
                    startActivityForResult(mIntentCamera, INTENT_GALLERY);
                }else{
                    Toast.makeText(mContext, R.string.galleryNoPermission, Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }

    private void fillIntentCamera(){
        if(mFileUri == null) {
            mFileUri = ImageUtils.getOutputMediaFileUri(mContext); // create a file to save the image
        }
        Log.v(LogUtils.makeLogTag(PhotoFragment.class),"uri = " + mFileUri);

        mIntentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri); // set the image file name
    }

    private void fillIntentGallery(){
        mPhotoPickerIntent = new Intent(Intent.ACTION_PICK);
        mPhotoPickerIntent.setType("image/*");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == this.getActivity().RESULT_OK) {
            Uri pathToInternallyStoredImage = null;
            if (requestCode == INTENT_CAMERA) {
                pathToInternallyStoredImage = ImageUtils.saveToInternalStorage(mContext, mFileUri);

            }else if (requestCode == INTENT_GALLERY){
                pathToInternallyStoredImage = ImageUtils.saveToInternalStorage(mContext, Uri.parse(ImageUtils.getPath(data.getData(), mContext)));
            }

            if(pathToInternallyStoredImage != null) {

                ((PictureTaken) getActivity()).OnPictureTaken(pathToInternallyStoredImage.getPath());

                setImage(pathToInternallyStoredImage.toString());
            }

        } else if (resultCode != this.getActivity().RESULT_CANCELED) {
            if (requestCode == INTENT_CAMERA) {
                Toast.makeText(mContext, R.string.cameraFailed, Toast.LENGTH_LONG).show();
            }
        }

    }

    public void setImage(String path) {

        path = (path == null) ? "" : path;
        setImage(Uri.parse(path));
    }

    public void setImage(Uri uri) {

        if (!Utils.isEmptyString(uri.toString())) {
            ;
            mPhotoImageView.setImageBitmap(BitmapFactory.decodeFile(uri.getPath(), null));
            mPhotoImageView.setVisibility(View.VISIBLE);
        } else {
            mPhotoImageView.setImageBitmap(null);
        }

    }

}

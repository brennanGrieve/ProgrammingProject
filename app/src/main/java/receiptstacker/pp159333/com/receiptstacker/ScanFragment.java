package receiptstacker.pp159333.com.receiptstacker;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class ScanFragment extends Fragment {
    SurfaceView cameraView;
    static Context appContext;
    final int RequestCameraPermissionID = 1001;
    TextView textView;
    static CameraSource cameraSource;
    private String rawOCRString;
    private SparseArray<TextBlock> OCRItems;
    private boolean multiCapFlag = false;
    private int multiCapSequence = 1;
    ImageView multiCapIndicator;
    private Receipt multiCapReceipt;


    public static ScanFragment newInstance(Context context) {
        ScanFragment fragment = new ScanFragment();
        appContext = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbSingleton.initDB(getActivity().getApplicationContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView cameraShutter = view.findViewById(R.id.imageViewCameraShutter);
        ImageView multiCap = view.findViewById(R.id.multiCapView);
        multiCapIndicator = view.findViewById(R.id.imageViewMulticapActiveIndicator);
        cameraShutter.setOnClickListener(mOnClickListener);
        multiCap.setOnClickListener(mMultiCapOnClickListener);
        cameraView = view.findViewById(R.id.surfaceView_Camera);
        getCamera(getView());


    }


    CameraSource.PictureCallback pCallBack = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {
            //handle loading the image into the receipt here

            //Receipt receipt = new Receipt("Shirt", "The Warehouse", 9 , new Date(34439393), pic);
           // Receipt receipt = new Receipt(OCRItems, pic);
            //Show dialog
//            ImageTakenDialog customDialog = new ImageTakenDialog(getContext(), receipt);
  //          customDialog.showDialog();
            //Split the procedure for either Single or Multi-Capture operations

            if(!multiCapFlag) {
                Bitmap pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                //Receipt receipt = new Receipt("Shirt", "The Warehouse", 9, new Date(34439393), pic);
                Receipt receipt = new Receipt(OCRItems, pic);
                //Show dialog
                ImageTakenDialog imageTakenDialog = new ImageTakenDialog(getContext(), receipt);
                imageTakenDialog.showDialog();
            }
            if(multiCapFlag){
                //add to the multiCapture object
                Bitmap multiPic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if(multiCapSequence == 1) {
                    ++multiCapSequence;
                    if(multiCapReceipt == null) {
                        //this might need to be changed
                        multiCapReceipt = new Receipt(OCRItems, multiPic);
                    }else{
                        //this could be wrong
                        //the changes in Receipt could have caused this
                        multiCapReceipt.reinitialize(OCRItems, multiPic);
                    }
                }
                else{
                    ++multiCapSequence;
                    //add to lists
                    multiCapReceipt.mergeBitmaps(multiPic);
                    // dont know if you still want this
                    multiCapReceipt.addNewOCR(OCRItems);
                }
            }


        }
    };

    private ImageView.OnClickListener mMultiCapOnClickListener = new ImageView.OnClickListener(){
        @Override
        public void onClick(View v){
            if(!multiCapFlag) {
                multiCapFlag = true;
                multiCapIndicator.setVisibility(View.VISIBLE);
            }
            else{
                //finalize image stitching + commit to db
                multiCapFlag = false;
                if(multiCapReceipt != null) {
                    ImageTakenDialog imageTakenDialog = new ImageTakenDialog(getContext(), multiCapReceipt);
                    imageTakenDialog.showDialog();
                    multiCapReceipt.reset();
                }
                multiCapSequence = 1;
                multiCapIndicator.setVisibility(View.INVISIBLE);
            }
        }
    };

    private ImageView.OnClickListener mOnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Camera takes a picture here
            cameraSource.takePicture(null, pCallBack);
        }
    };

    public void getCamera(View v) {
        cameraView = v.findViewById(R.id.surfaceView_Camera);
        textView = v.findViewById(R.id.textView);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(appContext).build();
    if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Dependencies are not available");
        } else {
            cameraSource = new CameraSource.Builder(appContext, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1080, 1920)
                    .setRequestedFps(30)
                    .setAutoFocusEnabled(true)
                    .build();


            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                     OCRItems = detections.getDetectedItems();
                        if (OCRItems.size() != 0) {
                            textView.post(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i = 0; (i < OCRItems.size()); i++) {
                                        TextBlock item = OCRItems.valueAt(i);
                                        stringBuilder.append(item.getValue());
                                        stringBuilder.append(" ");
                                    }
                                    rawOCRString = stringBuilder.toString();
                                    textView.setText(rawOCRString);
                                }
                            });
                        }
                }
            });
        }
    }

}
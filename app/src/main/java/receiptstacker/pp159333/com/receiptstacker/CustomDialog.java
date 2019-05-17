package receiptstacker.pp159333.com.receiptstacker;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.constraint.Constraints.TAG;

/*
A dialog that shows a receipt for the user. User can decide weather or not they want to
take another photo. This can be used in the browse menu as well with small changes to the button names.
 */

public class CustomDialog{

    private Dialog dialog;
    private Context context;
    private Receipt receipt;
    private Boolean pullDownState;
    private int defaultHeight;

    // Takes a context and a receipt and creates a custom dialog.
    CustomDialog(Context context, Receipt receipt) {
        this.dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_custom_dialog);
        this.context = context;
        this.receipt = receipt;
        pullDownState = true;
        populate(receipt);
    }


    void showDialog() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dialog.getWindow().setLayout((6 * width)/7, (4 * height)/5);
        ConstraintLayout inputBox = dialog.findViewById(R.id.constraintLayout_UserInput);
        defaultHeight = inputBox.getMaxHeight();
        Button okaybutton = dialog.findViewById(R.id.button_keep);
        okaybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReceiptToStorage();

                dialog.dismiss();
            }
        });
        Button tryagain = dialog.findViewById(R.id.button_TryAgain);
        tryagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ImageButton pulldown = dialog.findViewById(R.id.imageButton_PullDown);
        pulldown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pullDownAction();
            }
        });
        dialog.show();
    }

    void pullDownAction (){
        if(pullDownState){
            pullDownState = false;
            ConstraintLayout inputBox = dialog.findViewById(R.id.constraintLayout_UserInput);
            inputBox.setMinHeight(1);
            //Then Hide
        } else{
            pullDownState = true;
            ConstraintLayout inputBox = dialog.findViewById(R.id.constraintLayout_UserInput);
            inputBox.setMaxHeight(defaultHeight);
            //Then Show
        }

    }


    void populate(Receipt receipt){
        TextView pdate = dialog.findViewById(R.id.textView_DateOfPurchase);
        TextView pplace = dialog. findViewById(R.id.textView_PurchaseOrgin);
        TextView pprice = dialog.findViewById(R.id.textview_Price);
        ImageView image = dialog.findViewById(R.id.imageview_ReceiptImage);

        String price;
        price = String.format("%.02f", receipt.getTotalPrice());
        price = "$" + price;

<<<<<<< HEAD
        Date date = receipt.getDateOfPurchase();
        String place = receipt.getBussinessName();

        if(receipt.getDateOfPurchase() != null) {
            pdate.setText(receipt.getDateOfPurchase().toString());
        }
        pplace.setText(receipt.getBussinessName());
=======
        pname.setText(receipt.getProductName());
        pdate.setText(receipt.getDateOfPurchase().toString());
        pplace.setText(receipt.getBusinessName());
>>>>>>> 9a2b462e6255cf34d91747d5383ff4a51f8a56ba
        pprice.setText(price);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(receipt.getImage(), 150, 150, false);
        image.setImageBitmap(scaledBitmap);
    }

    private void saveReceiptToStorage(){
        dbSingleton.initDB(context.getApplicationContext());
        String filename = saveImageToFileSystem(receipt.getImage());
<<<<<<< HEAD
        //This method will need to be changed once commitToDB handles more then 1 string.
        //dbSingleton.commitToDB(filename, receipt.getProductName());
=======
        //This method will need to be changed once saveReceiptToStorage handles more then 1 string.
        dbSingleton.commitToDB(receipt, filename);
>>>>>>> 9a2b462e6255cf34d91747d5383ff4a51f8a56ba
    }

    private String saveImageToFileSystem(Bitmap receiptPic){
        ContextWrapper appWrap = new ContextWrapper(context.getApplicationContext());
        FileOutputStream receiptStream = null;
        File dir = appWrap.getDir("receiptImages", Context.MODE_PRIVATE);
        File newReceiptImage = new File(dir, getCurrentTimeStamp());
        try{
            receiptStream = new FileOutputStream(newReceiptImage);
            receiptPic.compress(Bitmap.CompressFormat.JPEG, 100, receiptStream);
        }catch(Exception FileOpenException){
            FileOpenException.printStackTrace();
        }finally{
            try{
                if (receiptStream != null) {
                    receiptStream.close();
                }
            }catch(IOException fileCloseException){
                fileCloseException.printStackTrace();
            }
        }
        Log.d(TAG, "saveImageToFileSystem: File Path is:" + newReceiptImage.getAbsolutePath());
        return newReceiptImage.getAbsolutePath();
    }



    private String getCurrentTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String timeStamp = simpleDateFormat.format(new Date());
        return timeStamp;
    }
}
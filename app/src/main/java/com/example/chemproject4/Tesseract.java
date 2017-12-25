package com.example.chemproject4;

import android.content.Context;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by nima_ on 13/11/2017. :)
 */
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Delayed;
import java.util.AbstractList;

public class Tesseract extends AppCompatActivity {

    //String[]  letter = new String[10000];
    //int[]     avgX   = new int[10000];
    ArrayList<String> letter = new ArrayList<String>();
    ArrayList<Integer> avgX = new ArrayList<Integer>();
    String datapath = "";
    boolean photo_taken= false;
    ///ImageView mImageView;

    Bitmap image;
    private TessBaseAPI mTess;
    static final int REQUEST_TAKE_PHOTO = 1;
    public String mCurrentPhotoPath;
    private ImageView ImageView_molecule;
    public Bitmap Bitmap_Molecule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView_molecule = (ImageView)findViewById(R.id.mImageView);

        final Button Process_Image_Button = (Button) findViewById(R.id.OCRbutton);
        final Button Take_Photo_Button = (Button) findViewById(R.id.Capture_Photo);

        Process_Image_Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                processImage();
            }
        });
        Take_Photo_Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                dispatchTakePictureIntent();

            }
        });
        //Define image,
        image = BitmapFactory.decodeResource(getResources(), R.drawable.ethene);

        //initialize Tesseract API
        String language = "eng";
        datapath = getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);
    }



    public void processImage( ){
        if (photo_taken ){ // checks if photo_take is equal to true, thus should set bitmap to image taken
            try {
                image = Bitmap_Molecule;
                if (image== null) {
                    Toast.makeText(getApplicationContext(), "error code to be made, bitmap is null!", Toast.LENGTH_SHORT).show();

                }
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "error code to be made, error occured in converting photo taken to bitmap,", Toast.LENGTH_SHORT).show();
            }
        }

        String OCRresult;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        Log.d("UTF8 TEXT", OCRresult);
        Log.d("Box text", mTess.getBoxText(1));
        Log.d("hocr box", mTess.getHOCRText(1));
        string_to_array();

        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        OCRTextView.setText(OCRresult);

    } // todo, get line detection

    private void checkFile(File dir) { /// Checks if the tess two directory exists on phone before copying data to it, if doesnt exist will create directory.

        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }

        }

    }
    private void string_to_array(){
        String str = mTess.getBoxText(1);
        str = str.replaceAll(" 1\n", " \n");// Removes the last 1 from each line of the string
        Log.d("string without spaces", str);
        String str_splitted[] = str.split(" ");
        Log.d("length", String.valueOf(str_splitted.length));
        for (int i = 0; i<(str_splitted.length-1); i+=5){ /// gets an array of letters and the avg x value of letter/
            boolean checker = true;
            Log.d("" , str_splitted[i]);
            ///Log.d("i" , String.valueOf(i));
            Log.d("arrary value", String.valueOf(str_splitted[i].charAt(0)));
            String I = "I";
            if (str_splitted[i].equals(I) ){ //todo fix the checking of I, =, -, / \
                checker = false;

            }
            if (checker == true){
                letter.add(str_splitted[i]);
                avgX.add((Integer.parseInt(str_splitted[i+1])+Integer.parseInt(str_splitted[i+3]))/2);

                Log.d("Letter", String.valueOf(letter.get(i/5)));
                Log.d("avgX", String.valueOf(avgX.get(i/5)));

            }
        }

        Bubblesort(avgX, letter);



/*        Log.d("avgx1", String.valueOf(avgX[1]));
        Log.d("avgx2", String.valueOf(avgX[2]));
        Log.d("avgx3", String.valueOf(avgX[3]));
        Log.d("avgx4", String.valueOf(avgX[4]));
        Log.d("avgx5", String.valueOf(avgX[5]));
        Log.d("avgx6", String.valueOf(avgX[6]));*/
        Log.d("bubble sort", "occrued");
        for(int i = 0; i<avgX.size(); i++){
            Log.d("avgx", String.valueOf(avgX.get(i)));
        }
        for(int i = 0; i<letter.size(); i++){
            Log.d("letter", letter.get(i));
        }
        //Log.d("avgx", String.valueOf(avgX));
        //Log.d("letter", String.valueOf(letter));

        ArrayList<Integer> grouping  = new ArrayList<Integer>();
        for(int i = 0; i < letter.size() - 1; i++) {
            if (avgX.get(i + 1) - avgX.get(i) < 30) {
                continue;
            } else {
                grouping.add(i);
            }
        }
        int i = letter.size() - 1;
        if (avgX.get(i) - avgX.get(i - 1) >= 30) {
            grouping.add(i);
        }

    }
    public void Bubblesort(ArrayList<Integer> avgX, ArrayList<String> Letter ) {
        int i = 0, n = avgX.size();
        boolean swapNeeded = true;
        while (i < n - 1 && swapNeeded) {
            swapNeeded = false;
            for (int j = 1; j < n - i; j++) {
                if (avgX.get(j - 1) > avgX.get(j)) {
                    int temp = avgX.get(j - 1);
                    avgX.set(j - 1, avgX.get(j));
                    avgX.set(j, temp);

                    String Letter_temp = Letter.get(j - 1);
                    Letter.set(j - 1, Letter.get(j));
                    Letter.set(j, Letter_temp);
                    swapNeeded = true;
                }
            }
            if(!swapNeeded) {
                break;
            }
            i++;
        }
    }
    public void get_grouping(ArrayList<Integer> avgX, ArrayList<String> Letter){

        int i = 0, n = avgX.size();
        while (i < n - 1 ) {
            for (int j = 1; j < n - i; j++) {
                if (avgX.get(j) - avgX.get(j - 1) < 30) {
                    int temp = avgX.get(j - 1);
                    avgX.set(j - 1, avgX.get(j));
                    avgX.set(j, temp);

                    String Letter_temp = Letter.get(j - 1);
                    Letter.set(j - 1, Letter.get(j));
                    Letter.set(j, Letter_temp);
                }
            }

            }
            i++;
        }




    private void copyFiles() {
        try {

            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Creating an intent to launch phones camera app part
    public void dispatchTakePictureIntent()  {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
             File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Photo file creation", "error occured while making photo file for photo intent");
                Toast.makeText(getApplicationContext(), "Error code to be made", Toast.LENGTH_SHORT).show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Toast.makeText( getApplicationContext(), photoURI.toString(), Toast.LENGTH_LONG).show();
                photo_taken = true;

            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Error code to be made, no camera app installed in computer ", Toast.LENGTH_SHORT).show();
        }
    }


    public  File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );



        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        datapath = mCurrentPhotoPath;
        return image;


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // Checks if camera intent is completed, then it will compress bitmap.
        // Check which request we're responding to
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                shrink_bitmap();
            }
        }
    }


    public void shrink_bitmap( ) {
        BitmapFactory.Options option = new BitmapFactory.Options();

       Bitmap_Molecule = BitmapFactory.decodeFile(mCurrentPhotoPath) ;
       Log.d("image file", mCurrentPhotoPath);



        if (Bitmap_Molecule != null) { // Checks if bitmap has been created,
            int target_width = ImageView_molecule.getMeasuredWidth(); /// No error
            int target_height = ImageView_molecule.getMeasuredHeight(); /// No error
            Log.d("Test Width", "" + target_width); // for error checking
            Log.d("Test Height", Integer.toString(target_height));
            Log.d("image file",mCurrentPhotoPath);
            Log.d("image", Bitmap_Molecule.toString());
            Log.d("btimap", "bitmap created");

            Bitmap Bitmap_Molecule_compressed = Bitmap.createScaledBitmap(Bitmap_Molecule, target_width, target_height, false);
            Toast.makeText(getApplicationContext(), "Photo successfully compressed!", Toast.LENGTH_SHORT).show();
            ImageView_molecule.setImageBitmap(Bitmap_Molecule_compressed);

        }
        else {
            Log.d("bitmap", "not created!");
            Toast.makeText(getApplicationContext(), "Bitmap not created!, error to be created", Toast.LENGTH_SHORT).show();


            ///image = Bitmap_Molecule_compressed;
        }
    }
}

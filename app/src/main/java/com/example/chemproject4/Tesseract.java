package com.example.chemproject4;

/*
 * Created by nima_on 13/11/2017. :)
 */

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.StrictMath.abs;

public class Tesseract extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    public String mCurrentPhotoPath;
    public Bitmap Bitmap_Molecule;
    //String[]  letter = new String[10000];
    //int[]     avgX   = new int[10000];
    ArrayList<String> letter = new ArrayList<>(); // Array List used for storing the letters, average x+y values.
    ArrayList<Integer> avgX = new ArrayList<>();  // Array List used as it is dynamic, instally it starts with 10 blank elements.
    ArrayList<Integer> avgY = new ArrayList<>();
    ///ImageView mImageView;
    ArrayList<String> error_messages = new ArrayList<>();
    Map<String, String> formulaToName = new HashMap<>();
    boolean photo_taken = false;
    Bitmap image;



    private TessBaseAPI mTess;
    private ImageView ImageView_molecule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView_molecule = (ImageView) findViewById(R.id.mImageView);

        final Button Process_Image_Button = (Button) findViewById(R.id.OCRbutton); /// used to declare the button listiner.
        final Button Take_Photo_Button = (Button) findViewById(R.id.Capture_Photo);

        Process_Image_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    processImage();
                } catch (IOException e) {
                    error_message_writer("error in processing the image");
                }

            }
        });
        Take_Photo_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        //Define image,
        try {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ethene);
        } catch (Exception e) {
            error_message_writer(" error code 1, error in setting the test image"); ///todo error code 1, fix: re install app.
        }

        //initialize Tesseract API
        String language = "eng";
        mCurrentPhotoPath = getFilesDir() + "/tesseract/";

        checkFile(new File(mCurrentPhotoPath + "tessdata/"));
        try {
            mTess = new TessBaseAPI();
            mTess.init(mCurrentPhotoPath, language); // initializes the Api

        } catch (Exception e) {

            error_message_writer("error code 2 , error in starting the OCR engine."); /// todo error code 2, error in starting ocr engine

        }
    }


    public void processImage() throws IOException {

        letter.clear();
        avgY.clear();
        avgX.clear();

        if (photo_taken) { // checks if photo_take is equal to true, thus should set bitmap to image taken
            try {
                image = Bitmap_Molecule;
                if (image == null) {
                    error_message_writer("error code 3, Bitmap wont load "); // todo error code 3, bitmap wont load.
                    Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                error_message_writer("error code 4, error occured in converting photo taken to bitmap"); // todo, error code 4, error code occured in converting photo taken to bitmap
            }
        }

        String OCRresult;
        try {
            mTess.setImage(image);
        } catch (Exception e) {
            error_message_writer("ERROR CODE 5, error setting image. Image hasnt been accessed "); //todo Error code 5, can not set image.
        }

        OCRresult = mTess.getUTF8Text();
        string_to_array();



    }

    public void error_message_writer(String error) {
        Date currentTime = Calendar.getInstance().getTime();
        String error_message_to_write = error + "occured at" + currentTime;
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        Log.d("error", error);


        error_messages.add(error_message_to_write);
    }

    private void error_to_text_file(String error_to_write) {


    }

    private void checkFile(File dir) { /// Checks if the tess two directory exists on phone before copying data to it, if doesnt exist will create directory.


        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String datafilepath = mCurrentPhotoPath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }

        }

    }

    private void string_to_array() throws IOException {
        TextView OCRTextView = (TextView) findViewById(R.id.Chemical_Name);
        TextView formula_textview = (TextView) findViewById(R.id.Formula);
        String image_boxtext = mTess.getBoxText(1);
        String formula;
        image_boxtext = image_boxtext.replaceAll(" 1\n", " ");// Removes the last 1 from each line of the string
        Log.d("string without spaces", image_boxtext);
        String str_splitted[] = image_boxtext.split(" ");
        Log.d("length", String.valueOf(str_splitted.length));
        int itemDeleted = 0;/// Used to check number of elements deleted later on.
        for (int i = 0; i < (str_splitted.length - 1); i += 5) { /// gets an array of letters and the avg x value of letter/
            boolean checker = true;
            Log.d("i", String.valueOf(i));
            Log.d("", str_splitted[i]);

            char ascLetter = str_splitted[i].charAt(0);

            if (ascLetter == 'I' || ascLetter == '=' || ascLetter == '-' || ascLetter == '/' || ascLetter == '\\') { //removes the placation from the text by not adding it to the new array
                checker = false;
                itemDeleted++;
            }

            if (checker == true) {
                letter.add(str_splitted[i]);
                avgX.add((Integer.parseInt(str_splitted[i + 1]) + Integer.parseInt(str_splitted[i + 3])) / 2); // Gets the average x value of the letter as each letter has a diffrent width, so it gives centre line
                avgY.add((Integer.parseInt(str_splitted[i + 2]) + Integer.parseInt(str_splitted[i + 4])) / 2);
                Log.d("Letter", String.valueOf(letter.get((i / 5) - itemDeleted))); // -itemdeleted, so that the elments position are not out of range of array.
                Log.d("avgX", String.valueOf(avgX.get((i / 5) - itemDeleted)));
            }
        }

        Bubblesort(avgX, letter, avgY);
        int max = Collections.max(avgY);
        int min = Collections.min(avgY);
        int middle = (max + min) / 2;
        ArrayList<Boolean> Middle_element = new ArrayList<>();

        for (int i = 0; i < avgY.size(); i++) {
            if (abs(middle - avgY.get(i)) < 30) { // Purpose to get the elements that are in the middle row.
                // 30 allows for variation of centre of Y axis
                Middle_element.add(true);           // true means that that element is the middle one
            } else {
                Middle_element.add(false);
            }
        }

        Log.d("bubble sort", "occrued");

       /* for (int i = 0; i < avgX.size(); i++) {
            Log.d("avgx", String.valueOf(avgX.get(i)));
        }

        for (int i = 0; i < letter.size(); i++) {
            Log.d("letter", letter.get(i));
        }

        Log.d("avgx", String.valueOf(avgX));
        Log.d("letter", String.valueOf(letter));
        */

        ArrayList<Integer> grouping = new ArrayList<>();

        for (int i = 0; i < letter.size() - 1; i++) {   //it groups the elements based on their x axis.
            // it checks if the elements have similar x values.
            if (avgX.get(i + 1) - avgX.get(i) > 30) {

                grouping.add(i);                        //It adds the elements to the group.

            }

        }

        int i = letter.size() - 1;
        if (avgX.get(i) - avgX.get(i - 1) >= 30) { // if there are side elements they will add it to the same group.i

            grouping.add(i);

        }

        if (grouping.get(0) == 0) {

            if (grouping.size() > 1) {  // checks if the first group has only one element, this means that it is connected to element.
                // thus it should be named after the middle group of the element it is connected it to.

                grouping.remove(0);
                Middle_element.set(0, false);

            }
        }
        int len = grouping.size();

        if (grouping.get(len - 1) - grouping.get(len - 2) == 1) {
            grouping.set(len - 2, grouping.get(len - 1));
            int indexTemp = grouping.get(len - 1);
            grouping.remove(len - 1);
            Middle_element.set(indexTemp, false);
        }

        for (int k = 0; k < grouping.size(); k++) {
            Log.d("grouping", String.valueOf(grouping.get(k)));
        }

        String equation = "";

        for (int m = 0; m < grouping.size(); m++) {
            int leftIdx = 0;
            if (m > 0) {
                leftIdx = grouping.get(m - 1) + 1;
            }

            String subEquation = "";

            for (int n = leftIdx; n <= grouping.get(m); n++) {
                if (Middle_element.get(n) == true) {
                    subEquation = letter.get(n);
                    break;
                }
            }

            Map<String, Integer> counting = new HashMap<>();

            for (int n = leftIdx; n <= grouping.get(m); n++) {
                if (Middle_element.get(n) == true) {
                    continue;
                }
                String strTemp = letter.get(n);
                Integer val = counting.get(strTemp);
                if (val != null) {
                    counting.put(strTemp, new Integer(val + 1));
                } else {
                    counting.put(strTemp, 1);
                }
            }

            Integer hCount = counting.get("H");

            if (hCount != null) {
                if (hCount > 0) {// In the naming order a chemical formula, carbon is first, then hydrogen then alphabeitcall. Thus it removes H  and assumes that if C appears it is in the centre.
                    subEquation = subEquation + "H" + String.valueOf(hCount);
                }
                for (Map.Entry<String, Integer> entry : counting.entrySet()) {
                    if (Objects.equals(entry.getKey(), "H")) {
                        continue;
                    }

                    subEquation = subEquation + entry.getKey() + entry.getValue().toString();
                    //System.out.println(entry.getKey() + "/" + entry.getValue());
                }
                //Log.d("equation", subEquation);
                equation += subEquation;
            }
        }

        Log.d("equation", equation);
        // Map<Character, Integer> counting = new HashMap<Character, Integer>();
        workbook_searcher();

        formula = "chemical formula:" + formulaToName.get(equation);
        OCRTextView.setText(formula);
        equation  = "chemical equation:" + equation;
        formula_textview.setText(equation);
    }

    public void Bubblesort(ArrayList<Integer> avgX, ArrayList<String> Letter, ArrayList<Integer> avgY) {
        int i = 0, n = avgX.size();
        boolean more_swaps = true;
        while (i < n - 1 && more_swaps) {
            more_swaps = false;
            for (int j = 1; j < n - i; j++) {
                if (avgX.get(j - 1) > avgX.get(j)) {
                    int temp = avgX.get(j - 1);
                    avgX.set(j - 1, avgX.get(j));
                    avgX.set(j, temp);

                    String Letter_temp = Letter.get(j - 1);
                    Letter.set(j - 1, Letter.get(j));
                    Letter.set(j, Letter_temp);

                    int temp_Y = avgY.get(j - 1);
                    avgY.set(j - 1, avgY.get(j));
                    avgY.set(j, temp_Y);
                    more_swaps = true;
                }
            }
            if (!more_swaps) {
                Log.d("bubble sort", "AvgX, AvgY, letters have been sorted");
                more_swaps = false;
            }
            i++;
        }
    }

    private void workbook_searcher() throws IOException {
        try {
            AssetManager mng = getApplicationContext().getAssets();
            InputStream is = mng.open("database.csv");

            CSVReader csv = new CSVReader(new InputStreamReader(is));
            String[] s;


            while ((s = csv.readNext()) != null) {
                //Log.d((s[0]), s[1]);
                formulaToName.put(s[0], s[1]);
            }
            csv.close();
        } catch (IOException ioe) {
            Log.e("File Read Error: ", "" + ioe.getMessage());
        }

    }

    public Bitmap adjustedContrast(Bitmap Bitmap_to_edit) {
        // image size
        int bitmap_height = Bitmap_to_edit.getHeight();
        int bitmap_width = Bitmap_to_edit.getWidth();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(bitmap_width, bitmap_height, Bitmap_to_edit.getConfig());
        // color information
        int Alpha;
        int pixel;
        // get contrast value
        double contrast = Math.pow((200) / 100, 2);
        int[] Colours = new int[3];
        // scan through all pixels
        for (int x = 0; x < bitmap_width; ++x) {
            for (int y = 0; y < bitmap_height; ++y) {
                // get pixel color
                pixel = Bitmap_to_edit.getPixel(x, y);
                Colours[0] = Color.red(pixel); // array position for red pixel value
                Alpha = Color.alpha(pixel);
                Colours[1] = Color.blue(pixel);
                Colours[2] = Color.green(pixel);
                // apply filter contrast for every channel R, G, B
                for (int i = 0; i < 3; i++) {
                    Colours[i] = (int) (((((Colours[i] / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                    if (Colours[i] < 0) {
                        Colours[i] = 0;
                    } else if (Colours[i] > 255) {
                        Colours[i] = 255;
                    }
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(Alpha, Colours[0], Colours[2], Colours[1]));
            }

        }
        return bmOut;
    }

    private void copyFiles() {
        try {

            String filepath = mCurrentPhotoPath + "/tessdata/eng.traineddata";
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
            error_message_writer("error code 5, error in copying the OCR data. Restart app"); //todo, error code 5 error in copying OCR data. restart app
        }
    }

    //Creating an intent to launch phones camera app part
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // initializes a new intent. Used to take photo of molecule.
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = create_photo_file();
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
                photo_taken = true;
            }
        } else {
            error_message_writer("Error code 6, no camera app installed in computer"); // todo error code 6, Error code to be made, no camera app installed in computer
        }
    }


    public File create_photo_file() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "ChemistryApp_molecule" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
              /* prefix */  imageFileName,
              /* type */   ".jpg",
         /* directory */       storageDir
            );

        } catch (Exception e) {
            error_message_writer("ERROR CODE 6, error in creating photo file"); // todo, error code 6, error in cretaing photo file.
        }

        // Save  file:  the is path used with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
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


    public void shrink_bitmap() {
        BitmapFactory.Options option = new BitmapFactory.Options();

        Bitmap_Molecule = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Log.d("image file", mCurrentPhotoPath);


        if (Bitmap_Molecule != null) { // Checks if bitmap has been created,
            int target_width = ImageView_molecule.getMeasuredWidth(); /// No error
            int target_height = ImageView_molecule.getMeasuredHeight(); /// No error
//            Log.d("Test Width", "" + target_width); // for error checking
//            Log.d("Test Height", Integer.toString(target_height));
//            Log.d("image file", mCurrentPhotoPath);
//            Log.d("image", Bitmap_Molecule.toString());
//            Log.d("btimap", "bitmap created");

            Bitmap Bitmap_Molecule_compressed = Bitmap.createScaledBitmap(Bitmap_Molecule, target_width, target_height, false);
            Toast.makeText(getApplicationContext(), "Photo successfully compressed!", Toast.LENGTH_SHORT).show();
            Bitmap_Molecule_compressed = adjustedContrast(Bitmap_Molecule_compressed);
            ImageView_molecule.setImageBitmap(Bitmap_Molecule_compressed);


        } else {
            Log.d("bitmap", "not created!");
            error_message_writer("error code 6, Bitmap has not been properly created"); //todo, error code 6. Bitmap not created.


            ///image = Bitmap_Molecule_compressed;
        }
    }
}

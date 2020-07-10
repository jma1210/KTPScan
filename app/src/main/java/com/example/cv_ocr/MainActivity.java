package com.example.cv_ocr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean isOCVSetUp = false;
    public static final String TAG = "DEBUG";
    private static final int PICK_IMAGE = 100;
    KTPFields ktp;
    Button b1, b2, b3;
    ImageView iv;
    Uri imguri;
    Bitmap forOcr;

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
                imguri = data.getData();
                iv.setImageURI(imguri);
//                Log.i("Address (URI)",String.valueOf(imageuri));
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.button7);
        b2 = (Button) findViewById(R.id.button8);
        b3 = (Button) findViewById(R.id.button9);
        iv = (ImageView)findViewById(R.id.imageView);
        b1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        openGallery();
                    }
        });
        b2.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        imageProcess();
                    }
            });
        b3.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                    {
                        charRecognition();
                    }
            });
    };
    public void charRecognition()
        {
            ktp = new KTPFields();
            if(forOcr == null)
            {
                Toast toast = Toast.makeText(getApplicationContext(),"No picture available for OCR", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            else
            {
                InputImage img = InputImage.fromBitmap(forOcr, 0);
                TextRecognizer recognizer = TextRecognition.getClient();
                Task<Text> result =
                        recognizer.process(img)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text text) {
                                        processText(text);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                });
            }
        };

    private void processText(Text text)
        {
            List<Text.TextBlock> blocks = text.getTextBlocks();
            List<String> sumLines = new ArrayList<String>();
            if(blocks.size() == 0)
                {
                    Toast toast = Toast.makeText(getApplicationContext(),"No text available for OCR", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            for(int i = 0 ; i < blocks.size() ; ++i)
                {
                    List<Text.Line> lines = blocks.get(i).getLines();
                    for(int j = 0 ; j < lines.size() ; ++j)
                        {
                            sumLines.add(lines.get(j).getText());
                            Log.i("Line "+String.valueOf(i)+","+String.valueOf(j),lines.get(j).getText());
                        }
                }
            if(!sumLines.isEmpty()){ktp.inputFields(sumLines);}
        };
    public void imageProcess()
        {
            Mat org = new Mat();
            if(imguri == null)
                {
                    Toast toast = Toast.makeText(getApplicationContext(),"No picture chosen", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            try
                {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),imguri);
                    Utils.bitmapToMat(bmp,org);
                    Toast toast = Toast.makeText(getApplicationContext(),"Picture loaded", Toast.LENGTH_LONG);
                    toast.show();
                }
            catch(Exception e)
                {
                    Log.e("ERROR OCCURED",String.valueOf(e));
                }
            //Resizing images for faster calculations
            Mat src = new Mat();
            Double ratio = 800.0/org.rows();
            Double newWidth = org.cols()*ratio;
            Double newHeight = org.rows()*ratio;
            Size ssize = new Size(newWidth,newHeight);
            Imgproc.resize(org,src,ssize,0,0,Imgproc.INTER_AREA);

            Mat dstImage = new Mat();
            Mat hsvFrame = new Mat(src.cols(),src.rows(), CvType.CV_8U);
            Mat blueMask = new Mat(src.cols(),src.rows(),CvType.CV_8UC1);

            Imgproc.cvtColor(src,hsvFrame,Imgproc.COLOR_BGR2HSV);
            Scalar lowBlue = new Scalar(0,20,100);
            Scalar highBlue = new Scalar(45,255,255);
            Core.inRange(hsvFrame,lowBlue,highBlue,blueMask);
            boolean isBlur = getBlur(src);
            src.release();
            perspTransform(org,dstImage,blueMask,ratio);
            if(isBlur)
                {
                    Toast toast = Toast.makeText(getApplicationContext(),"Image is too blurry, this might affect OCR performance", Toast.LENGTH_LONG);
                    toast.show();
                }
            if(!dstImage.empty())
            {
                Bitmap bm = Bitmap.createBitmap(dstImage.cols(),dstImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dstImage,bm);
                if(bm.getWidth()<bm.getHeight())bm = rotate(bm,90);
                iv.setImageBitmap(bm);
                forOcr = bm.copy(bm.getConfig(),false);

            }
            else
            {
                Bitmap bm = Bitmap.createBitmap(blueMask.cols(),blueMask.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(blueMask,bm);
                iv.setImageBitmap(bm);
                Log.e("PICTURE NOT MADE","Gambarnya gak kebuat bosqu");
            }

        };

    private Bitmap rotate(Bitmap source,float angle)
    {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), mat, true);
    };
    private boolean getBlur(Mat src)
        {
            double score = 0.0;
            Mat dst = new Mat();
            Mat matGray = new Mat();
            Imgproc.cvtColor(src,matGray,Imgproc.COLOR_BGR2GRAY);
            Imgproc.Laplacian(matGray,dst,3);
            MatOfDouble median = new MatOfDouble();
            MatOfDouble std = new MatOfDouble();
            Core.meanStdDev(dst,median,std);
            score = Math.pow(std.get(0,0)[0],2);
            Log.i("Score",String.valueOf(score));
            return score<500;
        };

    private void perspTransform(Mat src, Mat dst, Mat mask, double ratio)
        {
            Imgproc.blur(mask,mask,new Size(3,3));
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

            if(contours.size()==0)
                {
                    Log.e("Contours not found","No contours found in the picture given");
                    return;
                }

            double maxArea = 0.0;
            int contIdx = 0;

            for(int i = 0; i<contours.size(); ++i)
                {
                    double area = Imgproc.contourArea(contours.get(i));
                    if(maxArea<area)
                        {
                            maxArea = area;
                            contIdx = i;
                        }
                }

            MatOfPoint2f biggCont = new MatOfPoint2f();
            contours.get(contIdx).convertTo(biggCont,CvType.CV_32F);

            MatOfPoint2f approxCont = new MatOfPoint2f();

            double peri = Imgproc.arcLength(biggCont,true);
            Imgproc.approxPolyDP(biggCont,approxCont,peri*0.015,true);

            List<MatOfPoint> drawableContour = new ArrayList<MatOfPoint>();
            if(approxCont.rows() != 4)
                {
                    Log.e("No box contour","The bounding contour found has "+String.valueOf(approxCont.rows())+" lines");
                }

            Moments moments = Imgproc.moments(approxCont);
            double cX = moments.m10/moments.m00;
            double cY = moments.m01/moments.m00;
            Point[] corners = approxCont.toArray();
            Point[] sortedCorners = new Point[4];

            for(Point c : corners)
                {
                    if(c.x<cX && c.y<cY) sortedCorners[0] = new Point(c.x/ratio,c.y/ratio);
                    if(c.x>cX && c.y<cY) sortedCorners[1] = new Point(c.x/ratio,c.y/ratio);
                    if(c.x>cX && c.y>cY) sortedCorners[2] = new Point(c.x/ratio,c.y/ratio);
                    if(c.x<cX && c.y>cY) sortedCorners[3] = new Point(c.x/ratio,c.y/ratio);
                }
            double botWidth     = Math.abs(sortedCorners[2].x-sortedCorners[3].x);
            double topWidth     = Math.abs(sortedCorners[0].x-sortedCorners[1].x);
            double leftHeight   = Math.abs(sortedCorners[0].y-sortedCorners[3].y);
            double rightHeight  = Math.abs(sortedCorners[1].y-sortedCorners[2].y);
            double actWidth     = (botWidth>topWidth)?botWidth:topWidth;
            double actHeight    = (leftHeight>rightHeight)?leftHeight:rightHeight;

            MatOfPoint2f orgCoords = new MatOfPoint2f
                    (
                            sortedCorners[0],
                            sortedCorners[1],
                            sortedCorners[2],
                            sortedCorners[3]
                    );
            MatOfPoint2f finCoords = new MatOfPoint2f
                    (
                            new Point(0,0),
                            new Point(actWidth-1,0),
                            new Point(actWidth-1,actHeight-1),
                            new Point(0,actHeight-1)
                    );

            Mat warpMat = Imgproc.getPerspectiveTransform(orgCoords,finCoords);
            Size dsize = new Size(actWidth,actHeight);
            Imgproc.warpPerspective(src,dst,warpMat,dsize,Imgproc.INTER_LINEAR,Core.BORDER_CONSTANT);
            Core.normalize(dst,dst,0,255,Core.NORM_MINMAX,CvType.CV_8UC3);
            Imgproc.cvtColor(dst,dst,Imgproc.COLOR_BGR2GRAY);

        }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    public void onResume()
    {
        super.onResume();

        if (! isOCVSetUp) { // if OCV hasn't been setup yet, init it
            if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
                Log.i(TAG, "Something's wrong, I can feel it..");
            } else {
                isOCVSetUp = true;
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }
    };
}
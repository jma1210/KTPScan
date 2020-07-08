package com.example.cv_ocr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean isOCVSetUp = false;
    public static final String TAG = "DEBUG";
    private static final int PICK_IMAGE = 100;
    Button b1, b2, b3;
    ImageView iv;
    Uri imguri;

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
//            Imgproc.cvtColor(org,org,Imgproc.COLOR_BGR2RGB);
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
                iv.setImageBitmap(bm);
            }
            else
            {
                Bitmap bm = Bitmap.createBitmap(blueMask.cols(),blueMask.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(blueMask,bm);
                iv.setImageBitmap(bm);
                Log.e("PICTURE NOT MADE","Gambarnya gak kebuat bosqu");
            }

        };

    private boolean getBlur(Mat src)
        {
            Mat laplacian = new Mat();
            Mat matGray = new Mat();
            Mat laplacian8bit = new Mat();
            Imgproc.cvtColor(src,matGray,Imgproc.COLOR_RGB2GRAY);
            Imgproc.Laplacian(matGray,laplacian,CvType.CV_8U);
            laplacian.convertTo(laplacian8bit,CvType.CV_8UC1);

            Bitmap bmp = Bitmap.createBitmap(laplacian8bit.cols(),laplacian8bit.rows(),Bitmap.Config.ARGB_8888);
            int[] pixels = new int[bmp.getWidth()*bmp.getHeight()];
            bmp.getPixels(pixels,0,bmp.getWidth(),0,0,bmp.getWidth(),bmp.getHeight());

            int maxLap = -16777216;
            for(int pixel : pixels)
                {
                    if(pixel > maxLap)maxLap=pixel;
                }
            return maxLap<= -4000000;
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
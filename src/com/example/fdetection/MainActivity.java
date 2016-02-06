package com.example.fdetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Bitmap myBitmap,saveBitmap;
	ImageView iv;
	EditText name;
	Button done;
	int flag,status;
	
	static  final int WIDTH= 128;
	static  final int HEIGHT= 128;;
	public String TAG="FR";
	
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
    private float mRelativeFaceSize   = 0.2f;
    private int  mAbsoluteFaceSize   = 0;
    private static final Scalar    FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    
	private BaseLoaderCallback mLoaderCallBack= new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status){
			switch(status){
			case LoaderCallbackInterface.SUCCESS :
			{
				Log.i(TAG,"OpenCV Loaded Successfully");
				try {
                    InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();

                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    if (mJavaDetector.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        mJavaDetector = null;
                    } else
                        Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    cascadeDir.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                }
				break;
			}
			default :
			{
				super.onManagerConnected(status);
			}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		done=(Button) findViewById(R.id.button5);
		name=(EditText) findViewById(R.id.editText1);
		iv = (ImageView) findViewById(R.id.imageView1);
		name.setVisibility(View.INVISIBLE);
		done.setVisibility(View.INVISIBLE);
		
		BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
		BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
		myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.sae, BitmapFactoryOptionsbfo);
		iv.setImageBitmap(myBitmap);
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==100)
	    {
			myBitmap = (Bitmap)data.getExtras().get("data");
			iv.setImageBitmap(myBitmap);
			saveBitmap=myBitmap;
	    }
	}
	
	public void cameraopen(View v)
	{
		int sa=100;
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, sa);
		flag=1;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallBack);
	}
	
	public void detect(View v)
	{
		  //Bit map image to Mat image conversion
		   Mat tmp = new Mat (myBitmap.getWidth(), myBitmap.getHeight(), CvType.CV_8UC1);
		   Utils.bitmapToMat(myBitmap, tmp);
		 
		   //Mat color image to Mat gray image conversion
		   Mat gray=new Mat(tmp.size(),tmp.type());
		   Imgproc.cvtColor(tmp, gray, Imgproc.COLOR_BGR2GRAY);
		   
		   int height = gray.rows();
	        if (Math.round(height * mRelativeFaceSize) > 0) {
	            mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
	        }
	        
	        //Detection of faces by using gray image
		   MatOfRect faces = new MatOfRect();
	        if (mJavaDetector != null){
	            mJavaDetector.detectMultiScale(gray, faces, 1.1, 2, 2,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
	        }
	        
	        else {
	            Log.e(TAG, "Detection method is not selected!");
	        }
	        
	        //Drawing a rectangle around all detected faces
	        Rect[] facesArray = faces.toArray();
	        for (int i = 0; i < facesArray.length; i++)
	            {
	        	Core.rectangle(tmp, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
	            }
	        
	        // Conversion from mat to Bitmap and showing the detected image
	        Bitmap bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(tmp, bm);
	        iv.setImageBitmap(bm);
	}
	
	public void show(View v)
	{
		switch(flag)
		{
		case 1:
		{
			name.setVisibility(View.VISIBLE);
			done.setVisibility(View.VISIBLE);
			status=1;
			break;
		}
		case 2:
		{
			Toast.makeText(this, "Image Is Already Saved", Toast.LENGTH_SHORT).show();
			break;
		}
		default :
		{
			Toast.makeText(this, "First Click A Image", Toast.LENGTH_SHORT).show();
		}
		}
		
	}
	public void insert(View v)
	{
		name.setVisibility(View.VISIBLE);
		done.setVisibility(View.VISIBLE);
		status=2;
		flag=2;
	}
	public void saveAndGetImage(View v)
	{
		
		String pname=name.getText().toString();
		if(pname.length()>0)
		{
			name.setText("");
			name.setVisibility(View.INVISIBLE);
			done.setVisibility(View.INVISIBLE);
			File imdir=null;
			char c=pname.charAt(0);
			int f=(int)c;
			String first=pname.substring(0,1);;
			if(f>=97 && f<=122 || f>=65 && f<=90)
			{
				File im_dir=getImageDirectory();
				imdir = new File(im_dir,first);
				if (!imdir.exists()) 
				{
					imdir.mkdir();
				}
			switch(status)
			{
			case 1:
			{

				File imfile=new File(imdir,pname);
				String filename=imfile.getPath()+".jpg";
				final String imgName=pname+".jpg";
				FilenameFilter jpgFilter = new FilenameFilter() {
		            @Override
		            public boolean accept(File dir, String name) {
		                return name.equalsIgnoreCase(imgName);
		         }
		        };
		        File[] imageFiles = imdir.listFiles(jpgFilter);
		        int size=imageFiles.length;
		        if(size>0)
		        {
		        	Toast.makeText(this, "An Image is Already Saved of this name..!!", Toast.LENGTH_SHORT).show();
		        	flag=1;
		        }
		        else{
				try
				{
					FileOutputStream out = new FileOutputStream(filename);
	                saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
	                flag=2;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				Toast.makeText(this, "Image has been Saved", Toast.LENGTH_LONG).show();
		        }
			}
			break;
			case 2:
			{
				String s=pname+".jpg";
				File imfile=new File(imdir.getAbsolutePath()+"/"+s);
				String filename=imfile.getPath();
				if(imfile.exists())
				{
					try {
						
						myBitmap = BitmapFactory.decodeFile(filename);
			            iv.setImageBitmap(myBitmap);
			            Toast.makeText(this, "Image has been Inserted", Toast.LENGTH_LONG).show();
			            flag=2;
			        }  
			        catch (Exception e) {
			            e.printStackTrace();
			        }
				}
				else{
					Toast.makeText(this, "Sorry !! There is No Image Of This Name", Toast.LENGTH_SHORT).show();
					name.setVisibility(View.VISIBLE);
					done.setVisibility(View.VISIBLE);
				}
			}
			break;
			default :
				break;
			}
		}
			else{
				Toast.makeText(this, "Please Enter A Valid Name", Toast.LENGTH_SHORT).show();
			}
		}
		else{
			Toast.makeText(this, "Enter Image Name Then Go !!", Toast.LENGTH_SHORT).show();
		}
		
	}
	public File getImageDirectory() {
		File root = Environment.getExternalStorageDirectory();
		File im_dir = new File(root, "facerec");
		if (!im_dir.exists()) 
		{
		im_dir.mkdir();
		}
		return im_dir;
	}
}

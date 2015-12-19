package ocr;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;

public class ControllerOcr {

	private ViewOcr widok;
	private BooleanProperty cameraActive = new SimpleBooleanProperty(false);
	private VideoCapture capture = new VideoCapture();
	
	private ScheduledExecutorService timer;

	public void setWidok(ViewOcr widok) {
		this.widok = widok;
		cameraActive.bind(this.widok.btnStart.selectedProperty());
		cameraActive.addListener((v,ov,nv)->{
			camera(nv);
		});
	}
	
	private void camera(boolean isActive) {
		if(isActive) {
			capture.open(0);
			if(capture.isOpened()) {
				Runnable frameGrabber = new Runnable() {
					@Override
					public void run() {
						Image img = grabFrame();
						widok.getImage().setImage(img);
					}
				};
				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				widok.btnStart.setText("Stop");
			} 
			else
			{
				System.err.println("Nie mogê uruchomiæ kamery !!!");
			}
		}
		else
		{
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
				widok.btnStart.setText("Start");
			} catch (InterruptedException e) {
				System.err.println("Wyst¹pi³ wyj¹tek przy zamykaniu kamery !");
			}
			capture.release();
			widok.getImage().setImage(null);
		}		
	}
	
	private Image grabFrame()
	{
		Image imageToShow = null;
		
		Mat frame		= new Mat();
		Mat grayImg 	= new Mat();
		Mat blurImg		= new Mat();
		Mat binary 		= new Mat();
		
		
		if (capture.isOpened())
		{
			try
			{
				this.capture.read(frame);
				if (!frame.empty())
				{
					Imgproc.blur(frame, blurImg, new Size(7,7));
					Imgproc.cvtColor(blurImg, grayImg, Imgproc.COLOR_BGR2GRAY);
					Imgproc.adaptiveThreshold(grayImg, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 45, 0);
					
					Mat dist = new Mat(binary.size(), CvType.CV_32F);
					Imgproc.distanceTransform(binary, dist, Imgproc.CV_DIST_L2, Imgproc.CV_DIST_MASK_PRECISE);
					
					Mat dibw32f = new Mat(binary.size(), CvType.CV_32F);
					final double SWTHRESH = 8.0; 
					
					Imgproc.threshold(dist, dibw32f, SWTHRESH/2.0, 255, Imgproc.THRESH_BINARY);
					Mat dibw8u = new Mat(binary.size(), CvType.CV_8U);
					dibw32f.convertTo(dibw8u, CvType.CV_8U);
					
					Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
					Mat cont = new Mat(binary.size(), CvType.CV_8U);
					Imgproc.morphologyEx(dibw8u, cont, Imgproc.MORPH_OPEN, kernel);
					
					imageToShow = mat2Image(cont);

				}
			}
			catch (Exception e) {
				System.err.println("Wyst¹pi³ wyj¹tek podczas obróbki obrazu: " + e);
			}
		}
		
		return imageToShow;
	}
	
	private Image mat2Image(Mat frame)
	{
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		try {
			FileOutputStream stream = new FileOutputStream("ocr.png");
			stream.write(buffer.toArray());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
		String tekst = process("ocr.png");
		
		if(tekst.length()>0 && tekst.charAt(0)=='.' && tekst.charAt(tekst.length()-1)=='.') {
			System.out.println(tekst);
		}
		
		return image;
	}
	
    public String process(String file) {
        TessBaseAPI api = new TessBaseAPI();
        if (api.Init(".", "pol") != 0) {
            throw new RuntimeException("Nie mogê zainicjalizowaæ tesseract !");
        }       

        PIX image = null;
        BytePointer outText = null;
        try {
            image = lept.pixRead(file);
            api.SetImage(image);
            outText = api.GetUTF8Text();
            String string = outText.getString("UTF-8");
            if (string != null) {
                string = string.trim();
            }
            return string;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("charset", e);
        } finally {
            if (outText != null) {
                outText.deallocate();
            }
            if (image != null) {
                lept.pixDestroy(image);
            }
            if (api != null) {
                api.End();
            }
        }
    }
	
}

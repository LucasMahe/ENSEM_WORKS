import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
public class AnalyseVideo {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("opencv_ffmpeg2413_64");
	}

	static Mat imag = null;
	static boolean detecte = false;

	public static void main(String[] args) {
		JFrame jframe = new JFrame("Detection de panneaux sur un flux vid?o");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setSize(720, 480);
		jframe.setVisible(true);

		Mat frame = new Mat();
		VideoCapture camera = new VideoCapture("video1.avi");
		Mat PanneauAAnalyser = null;
		int i=0;
		while (camera.read(frame)) {
			//i++;
			//A completer
			if (detecte==true) {
				for (int j=0;j<40;j++) {
					camera.read(frame);
				}
			}
			detecte=false;
			ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));
			vidpanel.setIcon(image);
			Mat transformee=MaBibliothequeTraitementImageEtendue.transformeBGRversHSV(frame);
			//la methode seuillage est ici extraite de l'archivage jar du meme nom 
			Mat saturee=MaBibliothequeTraitementImage.seuillage(transformee, 6, 170, 110);
			Mat objetrond = null;

			//Cr?ation d'une liste des contours ? partir de l'image satur?e
			List<MatOfPoint> ListeContours= MaBibliothequeTraitementImageEtendue.ExtractContours(saturee);
			//Pour tous les contours de la liste
			for (MatOfPoint contour: ListeContours  ){
				objetrond=MaBibliothequeTraitementImageEtendue.DetectForm(frame,contour);
				int indexmax=identifiepanneau(objetrond);
				switch(indexmax){
				case -1:;break;
				case 0:System.out.println("Panneau 30 d?t?ct?");break;
				case 1:System.out.println("Panneau 50 d?t?ct?");break;
				case 2:System.out.println("Panneau 70 d?t?ct?");break;
				case 3:System.out.println("Panneau 90 d?t?ct?");break;
				case 4:System.out.println("Panneau 110 d?t?ct?");break;
				case 5:System.out.println("Panneau interdiction de d?passer d?t?ct?");break;
				}
				if (indexmax>=0) {
					detecte=true;
				}
			}
			vidpanel.repaint();
		}
	}






	public static BufferedImage Mat2bufferedImage(Mat image) {
		MatOfByte bytemat = new MatOfByte();
		Highgui.imencode(".jpg", image, bytemat);
		byte[] bytes = bytemat.toArray();
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}



	public static int identifiepanneau(Mat objetrond){
		double [] scores=new double [6];
		int indexmax=-1;
		if (objetrond!=null){
			scores[0]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref30.jpg");
			scores[1]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref50.jpg");
			scores[2]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref70.jpg");
			scores[3]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref90.jpg");
			scores[4]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref110.jpg");
			scores[5]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"refdouble.jpg");
			
			double scoremax=scores[0];

			for(int j=1;j<scores.length;j++){
				if (scores[j]>scoremax){scoremax=scores[j];indexmax=j;}}	
		}
		return indexmax;
	}


}
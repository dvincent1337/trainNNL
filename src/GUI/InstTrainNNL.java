package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.dvincent1337.neuralNet.NeuralNetwork;
import org.jblas.DoubleMatrix;


public class InstTrainNNL extends JFrame
{
	private JPanel holderPnl;
	private JPanel consolePnl;
	private JPanel imagePnl;
	private JPanel trainPnl;
	private JPanel trainListPnl;
	private JPanel trainButtonPnl;
	private JPanel trainNetPnl;
	
	private JLabel openFileLbl;
	private JLabel blankLbl;
	private JLabel trainLbl;
	private JLabel trainListLbl;
	private JLabel accuracyStaticLbl;
	private JLabel accuracyDynamicLbl;
	private JLabel statusStaticLbl;
	private JLabel statusDynamicLbl;
	
	private JButton openFileBtn;
	private JButton saveLoadImgBtn;
	private JButton removeBtn;
	private JButton clearBtn;
	private JButton trainBtn;
	private JButton loadParmsBtn;
	private JButton saveParmsBtn;
	private JButton createNNBtn;
	private JButton testNNBtn;
		
	private ButtonGroup exampleBgp;
	private JRadioButton goodExampleRdb;
	private JRadioButton badExampleRdb;
	
	private JList<String> exampleLst;
	private DefaultListModel<String> exampleLstModel;
	
	private JSpinner itersSpr;
	
	private BufferedImage 	background;
	private BufferedImage 	originalImage;
	private Vector <BufferedImage> 	selectedExamples;
	private Vector <Boolean>		selectedTypes;
	private Vector <int [][]>		selectedPixels;
	private int [] [] currentRect;
	private final int rectRgbGood = Color.green.getRGB();
	private final int rectRgbBad  = Color.red.getRGB();
	private int x,y,exCount;
	private final boolean debug = true;
	private String recentOpenPath; 
	private String imageFilename;
	private String recentSavePath;
	private String recentLoadPath;
	private String currentImageFilename;
	private String loadedImageFilename;
	private boolean allowSelecting;
	
	private NeuralNetwork NN_L;
	private DoubleMatrix X;
	private DoubleMatrix Y;
	private int NNL_width;
	private int NNL_height;
	private final int extraColor = 0;//black
	
	public InstTrainNNL()
	{
		super();
		buildGUI();
		initGlobalVars();
	}
	
	
	public void buildGUI()
	{
		
		holderPnl = new JPanel(new BorderLayout());
		consolePnl = new JPanel(new GridLayout(2,3));
		imagePnl = new JPanel();
		trainPnl = new JPanel(new BorderLayout());
		trainListPnl = new JPanel(new BorderLayout());
		trainButtonPnl = new JPanel(new GridLayout(2,1));
		trainNetPnl = new JPanel(new GridLayout(5,1));
				
		consolePnl.setBackground(Color.green);
		imagePnl.setBackground(Color.blue);
		trainPnl.setBackground(Color.orange);
		
		//The Image panel
		imagePnl.addMouseMotionListener(new imgMouseMotionListener());
		imagePnl.addMouseListener(new imgMouseListener());
		imagePnl.addComponentListener( new frameResizeListener());

		
		//The console Panel.
		openFileLbl = new JLabel("Open Image File:");
		blankLbl = new JLabel("");
		
		openFileBtn = new JButton("Browse...");
		openFileBtn.addActionListener(new btnListener() );
		saveLoadImgBtn = new JButton("Save/Load Examples");
		saveLoadImgBtn.addActionListener(new btnListener() );

		exampleBgp = new ButtonGroup();
		goodExampleRdb = new JRadioButton("Positive Example",true);
		badExampleRdb = new JRadioButton ("Negitive Example");
		
		exampleBgp.add(goodExampleRdb);
		exampleBgp.add(badExampleRdb);
		consolePnl.add(openFileLbl);
		consolePnl.add(openFileBtn);
		consolePnl.add(goodExampleRdb);
		consolePnl.add(blankLbl);
		consolePnl.add(saveLoadImgBtn);
		consolePnl.add(badExampleRdb);
		
		
		//Training Panel
		trainLbl = new JLabel	 ("   <-----|NN_L|----->");
		trainListLbl = new JLabel("Training Examples:");
		
		exampleLstModel = new DefaultListModel<String>();
		exampleLst = new JList<String>(exampleLstModel);
		exampleLst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exampleLst.setLayoutOrientation(JList.VERTICAL);
		exampleLst.addMouseListener(new listMouseListener());
		
		removeBtn = new JButton("Remove Selected");
		removeBtn.addActionListener(new btnListener());
		
		clearBtn = new JButton("Clear All");
		clearBtn.addActionListener(new btnListener());
		
		accuracyStaticLbl = new JLabel("Accuracy: ");
		accuracyDynamicLbl = new JLabel("");
		trainBtn = new JButton("Train");
		itersSpr = new JSpinner( (SpinnerModel) new SpinnerNumberModel(100,0,20000,1));
		loadParmsBtn = new JButton("Load Weights");
		saveParmsBtn = new JButton("Save Weights");
		statusStaticLbl = new JLabel("Status: ");
		statusDynamicLbl = new JLabel("");

		createNNBtn = new JButton("Create NN");
		createNNBtn.addActionListener(new btnListener());
		testNNBtn = new JButton("Test NN");
		
		trainNetPnl.add(createNNBtn);
		trainNetPnl.add(testNNBtn);
		trainNetPnl.add(trainBtn);
		trainNetPnl.add(itersSpr);
		trainNetPnl.add(accuracyStaticLbl);
		trainNetPnl.add(accuracyDynamicLbl);
		trainNetPnl.add(loadParmsBtn);
		trainNetPnl.add(saveParmsBtn);
		trainNetPnl.add(statusStaticLbl);
		trainNetPnl.add(statusDynamicLbl);
		
		trainButtonPnl.add(removeBtn);
		trainButtonPnl.add(clearBtn);
	
		trainListPnl.add(trainListLbl,BorderLayout.NORTH);
		trainListPnl.add(new JScrollPane(exampleLst),BorderLayout.CENTER);
		trainListPnl.add(trainButtonPnl,BorderLayout.SOUTH);
		
		trainPnl.add(trainLbl, BorderLayout.NORTH);
		trainPnl.add(trainListPnl, BorderLayout.CENTER);
		trainPnl.add(trainNetPnl,BorderLayout.SOUTH);
		
		holderPnl.add(consolePnl,BorderLayout.NORTH);
		holderPnl.add(imagePnl,BorderLayout.CENTER);
		holderPnl.add(trainPnl,BorderLayout.EAST);
		
		add(holderPnl);
		setSize(500,500);
		setVisible(true);
	}
	
	private void initGlobalVars()
	{
		background = null;
		originalImage = null;
		selectedExamples = 	new Vector<BufferedImage>();
		selectedTypes =		new Vector<Boolean>();
		selectedPixels = 	new Vector<int[][]>();
		
		currentRect = new int [4][2];
		
		recentOpenPath = 		"";
		recentSavePath = 		"";
		recentLoadPath =		"";
		currentImageFilename =	"";
		imageFilename = ""; 
		loadedImageFilename="";
		allowSelecting = false;
		exCount = -1;
		
		//Each row of X and Y is an example
		//There are two columns of Y, they are defined as followed.
		// Y = {Y0, Y1} Y0 = 1 && Y1 = 0 if example is a rectangle of text and,
		// Y = {Y0, Y1}	Y0 = 0 && Y1 = 0 if example is not a rectangle of text.
		
		
		NN_L = new NeuralNetwork();
		X = null;
		Y = null;
		NNL_width = -1;
		NNL_height = -1;
	}
	
 	private void paintImage(File imageFile)
	{
		BufferedImage theImg = null;
		try
		{
			theImg = ImageIO.read(imageFile);
			imagePnl.getGraphics().dispose();
			imagePnl.getGraphics().clearRect(0,0,imagePnl.getWidth(),imagePnl.getHeight());
			imagePnl.getGraphics().drawImage(theImg,0,0,null);
			prepareForNewFile();
			originalImage = deepCopy(theImg);
			background = deepCopy(theImg) ;
		}
		catch(Exception e)
		{
			System.err.println("Error! "+ e);
		}
	}
	private void repaintBackground()
	{
		imagePnl.getGraphics().dispose();
		imagePnl.getGraphics().clearRect(0,0,imagePnl.getWidth(),imagePnl.getHeight());
		
		if (background != null)
			imagePnl.getGraphics().drawImage(background,0,0,null);
	}
	
 	private void prepareForNewFile()
 	{
 		background=null;
 	}
 	
 	private void addExample(BufferedImage img, boolean type, int [] [] pixels, boolean isCurrentImage)
 	{
 		//Convert to gray scale.	
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		BufferedImage imgGray= op.filter(img, null);
 		
		Boolean objType = new Boolean(type);
 		String outputStr = "";
 		if (type)
 			outputStr = "+ ";
 		else
 			outputStr = "- ";
		
		//Add the example.
 		if (isCurrentImage)
 		{
	 		selectedExamples.add(imgGray);
	 		selectedTypes.add(objType);
			selectedPixels.add(deepCopy(pixels));
	 		outputStr += imageFilename;
	 		exampleLstModel.addElement(outputStr); 		
	 		exCount++;
 		}
 		else
 		{
 			selectedExamples.add(0,imgGray);
 			selectedTypes.add(0,objType);
 			outputStr +="<" +loadedImageFilename +">";
 			exampleLstModel.add(0, outputStr);
 		}
 		selectLast();

 	}
 	
 	private int[][] deepCopy(int[][] pixels)
	{
 	
 		int[][] copy = new int[pixels.length][];

 		for (int i = 0; i < copy.length; i++) {
 			int[] member = new int[pixels[i].length];
 			System.arraycopy(pixels[i], 0, member, 0, pixels[i].length);
 			copy[i] = member;
 		}

		return copy;
	}

	private void selectLast()
 	{
 		int size = exampleLstModel.size();
 		if (size>0)
 			exampleLst.setSelectedIndex(size-1);
 	}
 	
	private static BufferedImage deepCopy(BufferedImage bi) 
	{
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
 	
	private File openImageFile()
	{
		JFileChooser fileChooser;
		
		if(recentOpenPath !="")
			fileChooser = new JFileChooser(recentOpenPath);
		else
			fileChooser = new JFileChooser();
		
		int status = fileChooser.showOpenDialog(null);
		File selectedFile = null;
		if (status == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = fileChooser.getSelectedFile();
			currentImageFilename = selectedFile.getName();
			openFileLbl.setText("Currently opened: " + currentImageFilename);
			this.setTitle("Train Neural Network: Location < " + selectedFile.getPath() +" >");
		}
		return selectedFile;
		
	}
	
	private class btnListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == openFileBtn)
				openFileBtnAction();
			if (e.getSource() == removeBtn)
				removeBtnAction();
			if (e.getSource() == clearBtn)
				clearBtnAction();
			if (e.getSource() == saveLoadImgBtn )
				saveLoadImgBtnAction();
			if (e.getSource() == createNNBtn)
				createNNBtnAction();
		}
	}
	private void openFileBtnAction()
	{
		File selectedImage = openImageFile();
		if (selectedImage != null)
		{
			paintImage(selectedImage);
			recentOpenPath = selectedImage.getParent();
			imageFilename = selectedImage.getName();
			allowSelecting = true;
			exCount =0;
			selectedPixels.removeAllElements();
		}
	}
	private void removeBtnAction()
	{
		int index = exampleLst.getSelectedIndex();
		//See if the example was preloaded or this current image
		int size = exampleLstModel.size();
		int preloaded = size - exCount;
		boolean isCurrent=false;
		if ((index+1) > preloaded)
			isCurrent = true;
	
		if (index >=0)
		{
			//remove the example from:
			//  selectedExamples
			//  selectedTypes
			//  the JList
			selectedExamples.remove(index);
			selectedTypes.remove(index);
			exampleLstModel.remove(index);
			if (isCurrent)
			{
				selectedPixels.remove(index - (preloaded));
				exCount--;
				BufferedImage newBg = deepCopy(originalImage);
				Iterator<int [] []> iter = selectedPixels.iterator();
				int counter = 0;
				while (iter.hasNext())
				{
					int [] [] rect = iter.next();

					if (debug)
					{
						String points ="";
						for ( int i = 0; i< 4; i++)
						{
							points += rect[i][0]+"," + rect[i][1]+";";
						}
						System.out.println("ReDrawing rect: "+points);
						//printSavedSelections();
					}
					
					int rectRgb = rectRgbGood;
					if (selectedTypes.get((preloaded) + counter) == false)
						rectRgb = rectRgbBad;
					newBg = drawRect(rect,rectRgb, newBg);
					counter++;
				}
				background = deepCopy(newBg);
				repaintBackground();
			}
			if (size==0 || exCount==0)
			{
				background = deepCopy(originalImage);
				repaintBackground();
			}
			if (size >0)
				selectLast();
		}
	}
	private void clearBtnAction()
	{
		// remove all from:
		// selectedExamples
		// selectedTypes
		// the Jlist
		//remember to reset the image.
		String [] options = {"Yes", "No"};
		int n = JOptionPane.showOptionDialog(null,"Are you sure you want to clear all the exampels?",
											"Warning",
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null,
											options,	
											null);
		if (n == 0)
		{
			selectedExamples.removeAllElements();
			selectedTypes.removeAllElements();
			exampleLstModel.removeAllElements();
			exCount = 0;
			selectedPixels.removeAllElements();
			background = deepCopy(originalImage);
			repaintBackground();
		}
	}
	private void saveLoadImgBtnAction()
	{
		String [] options = {"Save", "Load"};
		String message = "Do you want to save examples or load examples?\n";
		int n = JOptionPane.showOptionDialog(null,message,
											"Load or Save?",
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null,
											options,	
											null);
		if (n == 0)
		{
			//Save the file
			saveExamples();
		}
		else if (n==1)
		{
			loadExamples();
		}
		
	}
	private void createNNBtnAction()
	{
		JTextField 	widthFld = new JTextField(8);
		JTextField	heightFld = new JTextField(8);

		JTextField	hiddenFld = new JTextField(15);
		JLabel		headerLbl = new JLabel("Select the 'NeuralNet' Space");
		JLabel 		widthLbl = new JLabel("Width(px): ");
		JLabel		heightLbl = new JLabel("Heigh(px): ");
		JLabel		hiddenLbl = new JLabel("Neurons in hidden layers: (#.#.#.#) ect: ");
		JPanel 		inputPnl= new JPanel(new GridLayout(2,2));
		inputPnl.add(widthLbl);
		inputPnl.add(widthFld);
		inputPnl.add(heightLbl);
		inputPnl.add(heightFld);
		JPanel inputHolderPnl = new JPanel(new GridLayout(4,1));
		inputHolderPnl.add(headerLbl);
		inputHolderPnl.add(inputPnl);
		inputHolderPnl.add(hiddenLbl);
		inputHolderPnl.add(hiddenFld);
		JOptionPane.showMessageDialog(null,inputHolderPnl,"Enter NN_L Properties ",JOptionPane.PLAIN_MESSAGE);
		
		int w = 0,h=0;
		int [] topology = null;
		char tokenChar = '.';
		String tokenStr = "\\.";
		String hiddenInput="";
		
		boolean enoughInfo = true;
		
		try
		{
			w = Integer.parseInt(widthFld.getText());
			h = Integer.parseInt(heightFld.getText());
			hiddenInput = hiddenFld.getText();
			if (hiddenInput == null)
				enoughInfo = false;

			if (!(hiddenInput.indexOf(tokenChar) >0))
					enoughInfo=false;

		}
		catch (NumberFormatException e)
		{
			enoughInfo = false;
		}
		
		if (enoughInfo)
		{
			String [] tokens = hiddenInput.split(tokenStr);
			topology = new int[tokens.length+2];
			topology[0]=w*h;
			for (int i = 0; i<(tokens.length);i++)
			{
				try
				{
					topology[i+1] = Integer.parseInt(tokens[i]);
				}
				catch (NumberFormatException e)
				{} ///Ignore this.
			}
			topology[topology.length-1] = 2;
			if (debug)
			{
				System.out.print("Topology Recieved: ");
				for (int i = 0 ; i<topology.length;i++)
				{
					System.out.print(topology[i] + ";");
				}
				System.out.println();
			}
			statusDynamicLbl.setText("Creating NeuralNet...");
			
			//Update the neural network. (topology, and weights)
			NN_L.setTopology(topology);
			NN_L.initWeights();
			NNL_width = w;
			NNL_height = h;
			
			
			statusDynamicLbl.setText("Created NeuralNetwork");
		}
		
	}
	
	private File saveExamplesFileChooser()
	{
		JFileChooser fileChooser;
		
		if(recentSavePath !="")
			fileChooser = new JFileChooser(recentSavePath);
		else
			fileChooser = new JFileChooser();
		
		int status = fileChooser.showSaveDialog(null);
		File selectedFile = null;
		if (status == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = fileChooser.getSelectedFile();
		}
		return selectedFile;
		
	}
	
	private File loadExamplesFileChooser() 
	{
		JFileChooser fileChooser;
		
		if(recentLoadPath !="")
			fileChooser = new JFileChooser(recentLoadPath);
		else
			fileChooser = new JFileChooser();
		
		int status = fileChooser.showOpenDialog(null);
		File selectedFile = null;
		if (status == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = fileChooser.getSelectedFile();
		}
		return selectedFile;
		
	}
	
	private void saveExamples()
	{
		File saveFile = saveExamplesFileChooser();
		//Convert to the array.
		if (saveFile!=null)
		{
			int sizeExamples = selectedExamples.size();
			int sizeTypes 	 = selectedExamples.size();
			if (sizeExamples==sizeTypes)
			{
				//find the size of the array
				int len = 0;
				for (int i = 0; i < sizeExamples;i++)
				{
					int nc,nr;
					nc = selectedExamples.get(i).getHeight();
					nr = selectedExamples.get(i).getWidth();
					// we will have: {-1,t,nc,nr,c & r}
					len +=4+nr*nc;
				}
			
				int [] data = new int [len];
				
				int offset = 0;
				
				//loop through the examples.
				for (int i = 0; i< sizeExamples;i++)
				{
					BufferedImage currentImg = selectedExamples.get(i);
					data[offset] 	= -1;
					int type = 0;
					if (selectedTypes.get(i))
						type = 1;
					int w= currentImg.getWidth() , h = currentImg.getHeight();
					data[offset+1]	= type;
					data[offset+2]	= w;
					data[offset+3]	= h;
					int [] [] imgArray = bufferedImgToIntArray(currentImg);
					//fill in the pixel values
					int count = 0;
					for (int c = 0; c<w; c++)
					{
						for (int r =0; r< h ; r++)
						{	
							data[offset + 4 + count] = imgArray[c][r];
							count++;
							
						}
					}
					offset = offset + 4 + count;
					
				}
				//Going to write the data to the selected file
				try 
				{
					ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(saveFile.getPath()));
					stream.writeObject(data);
					stream.close();
					 recentSavePath = saveFile.getParent();
					 if (debug)
						 System.out.println("File Saved!");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
			}
			else
			{
				//ERROR THIS SHOULD NOT HAPPEN
				if (debug)
				{
					System.out.println("ERROR: selectedExamples: "+ sizeExamples + " selectedTypes: " + sizeTypes);
				}
			}
		}
	}
	
	private void loadExamples()
	{
		File loadFile = loadExamplesFileChooser();
		if (loadFile != null)
		{
			loadedImageFilename = loadFile.getName();
			try
			{
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(loadFile.getPath()));
				int [] data = (int []) stream.readObject();
				stream.close();
				//Loop through the data. again the format is {-1,t,nc,nr,cxr}
				for (int i = 0; i < data.length;)
				{
					if (data[i]==-1) //We have us an example
					{
						i++;	//Move the iterator over every time an element is accessed.
						int type = data[i]; i++;
						int nc = data[i];i++;
						int nr = data[i];i++;
						//and data time.
						int [] [] imgData = new int[nc][nr];
						for (int x = 0;x<nc;x++)
						{
							for (int y = 0; y<nr;y++)
							{
								imgData[x][y] = data[i];
								i++;
							}
						}
						boolean dataType = false;
						if (type == 1)
							dataType=true;
						addExample(intArrayToBufferedImage(imgData) , dataType,null,false	);
						
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static int [] [] bufferedImgToIntArray(BufferedImage image)
	{
		//NOTE: ASSUMES THAT THE IMAGE IS IN 'TYPE_BYTE_GRAY' (Gray scale bufferedImage)
		WritableRaster rast = image.getRaster();
		int w = image.getWidth(), h = image.getHeight();
		int [] [] pixelArray = new int [w][h];
		for (int i = 0; i<w;i++)
		{
			for (int j = 0; j<h;j++)
			{
				pixelArray[i][j] = rast.getSample(i, j, 0);
			}
		}
		//The array is in the format array[column][row] or array[x][y] with xy coordinate plane
		return pixelArray;
	}
	private static BufferedImage intArrayToBufferedImage(int [] [] pixelArray)
	{
		int width = pixelArray.length;
		int height = pixelArray[0].length;
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rast = img.getRaster();
		int [] a = new int[1];
		//Set the new image
		for (int i = 0; i< width; i++)
		{
			for (int j = 0; j< height;j++)
			{
				a[0] = pixelArray[i][j];
				rast.setPixel(i, j, a);
			}
		}
		return deepCopy(img);
	}
	
	private void addNNLExample(BufferedImage image, boolean type )
	{
		//Modifies the X and Y DoubleMatrix. assumes that X and Y were initilized. 
		// Also assumes that widthNNL and heightNNL are defined.
		//Takes the image and adds it to the X and Y training examples.
		
		
		
		
		
	}
	private DoubleMatrix generateOutputMatrix(boolean exampleType,int num)
	{
		// For postive example use: {1,0} for negative example use {0,1}
		double [][] data = new double[1][2];
		if (exampleType)
		{
			//positive
			data[0][0]=1;
			data[0][1]=0;
		}
		else
		{
			//negative
			data[0][0]=0;
			data[0][1]=1;
		}
		DoubleMatrix output = new DoubleMatrix(data);
		for (int i = 0;i<(num-1);i++)
		{
			output = DoubleMatrix.concatVertically(output, new DoubleMatrix(data));
		}
		return output;
	}

	private DoubleMatrix generateInputMatrix(BufferedImage image)
	{
		int[][] pixleArray = bufferedImgToIntArray(image);
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		//Decide on how to scale the image.
		float Pw = (float) imageWidth / NNL_width;
		float Ph = (float) imageHeight / NNL_height;
		
		float Zw = Math.abs(1-Pw);
		float Zh = Math.abs(1-Ph);
		
		boolean scaleVertically = true; //Scale Vertically unless horizontally is better.
		if (Zw<Zh)
			scaleVertically = false;
		
		if (scaleVertically)
		{
			//Scaling vertically. make sure that the new image's height matches NNL_height
			
		}
		return null;//RETURN NEW DATA
	}
	
	private void addToMatracies(DoubleMatrix inputExample, DoubleMatrix outputExample)
	{
		//See if they are initilized 
		if (X == null)
		{
			X = new DoubleMatrix(inputExample.toArray()); 
			Y = new DoubleMatrix(outputExample.toArray());
		}
		else if (X.getColumns() == NNL_width*NNL_height && Y.getColumns() == NN_L.getTopology()[NN_L.getTopology().length-1] )
		{
			X = DoubleMatrix.concatVertically(X,inputExample);
			Y = DoubleMatrix.concatVertically(Y, outputExample);
		}
		else
		{
			if (debug)
			{
				System.out.println("ERROR IN addToMatracies: sizes of X or sizes of Y are not correct.");
			}
		}
	}
	
	private static double [] []  intArrayToDoubleArray(int [] [] inputArray)
	{
		int rows = inputArray.length;
		int columns = inputArray[0].length;
		double [][] output = new double[rows][columns];
		for (int i = 0; i<rows;i++)
		{
			for (int j = 0; j<columns;j++)
			{
				output[i][j] = inputArray[i][j]  ;
			}
		}
		return output;
	}
	private static double [] [] padWithExtraColor(int[] [] inputArray, int newWidth, int newHeight,int offsetX,int offsetY, int colorValue)
	{
		//the dimentions of inputarray size;
		int inWidth = inputArray.length;
		int inHeight = inputArray[0].length;
		
		double [] []  outputArray = new double[newWidth][newHeight];
		for (int i = 0; i < newWidth;i++)
		{
			for (int j = 0; j < newHeight;j++)
			{
				 if (i<= offsetX || i <= offsetY || i >= inWidth || j >= inHeight)
					 outputArray[i][j] = colorValue;
				 else
					 outputArray[i][j] = inputArray[i][j];
				 
			}
		}
		return outputArray;
	}
	private static double [] [] padWithExtraColor(double[] [] inputArray, int newWidth, int newHeight, int colorValue)
	{
		//the dimentions of inputarray size;
		int inWidth = inputArray.length;
		int inHeight = inputArray[0].length;
		
		double [] []  outputArray = new double[newWidth][newHeight];
		for (int i = 0; i < newWidth;i++)
		{
			for (int j = 0; j < newWidth;j++)
			{
				 if (i >= inWidth || j >= inHeight)
					 outputArray[i][j] = colorValue;
				 else
					 outputArray[i][j] = inputArray[i][j];
				 
			}
		}
		return outputArray;
	}
	private static double [][] from2DArrayTo1D(double [] [] inputArray)
	{
		// put each row into one row.
		int width = inputArray.length;
		int height = inputArray[0].length;
		int len = width*height;
		double [][] output= new double[1][len];
		int count = 0;
		for (int i = 0; i<width;i++)
		{
			for (int j = 0; j<height;j++)
			{
				output[0][count]=inputArray[i][j];
				count++;
			}
		}
		return output;
		
	}
	
	private class imgMouseMotionListener implements MouseMotionListener
	{
		public void mouseDragged(MouseEvent e)
		{
			if (allowSelecting)
			{
				int w,h;
				w = e.getX();
				h = e.getY();
				int [] xPoints = new int[4];
				int [] yPoints = new int[4];
	
				currentRect[0][0]=x;
				currentRect[0][1]=y;
				currentRect[1][0]=w;
				currentRect[1][1]=y;
				currentRect[2][0]=w;
				currentRect[2][1]=h;
				currentRect[3][0]=x;
				currentRect[3][1]=h;
	
				int i;
				for (i = 0; i < 4 ;i++)
					xPoints[i] = currentRect[i][0];
				for (i = 0; i < 4; i++)
					yPoints[i]= currentRect[i][1];
				
				repaintBackground();
				imagePnl.getGraphics().drawPolygon(xPoints,yPoints,4);
			}
		}

		public void mouseMoved(MouseEvent e)
		{
		}
	}
	private class imgMouseListener implements MouseListener
	{

		public void mouseClicked(MouseEvent e)
		{			
		}

		public void mouseEntered(MouseEvent e)
		{			
		}

		public void mouseExited(MouseEvent e)
		{			
		}

		public void mousePressed(MouseEvent e)
		{			
			if (allowSelecting)
			{
				x = e.getX();
				y = e.getY();
			}
		}

		public void mouseReleased(MouseEvent e)
		{			
			if (allowSelecting)
			{
				int rgb= rectRgbGood;
				boolean type = true;
				if (badExampleRdb.isSelected())
				{
					rgb = rectRgbBad;
					type = false;
				}
				/*
				rectCollection.add(deepCopy(currentRect, 4,2));
				rectCollectionType.add(new Boolean(type));
				*/
				background = drawRect(currentRect,rgb,background);				
				repaintBackground();
				addExample(getImg(currentRect), type, currentRect,true);
				if (debug)
				{
					String points ="";
					for ( int i = 0; i< 4; i++)
					{
						points += currentRect[i][0]+"," + currentRect[i][1]+";";
					}
					System.out.println("Last rect: "+points);
					//printSavedSelections();
				}
			}
		}
		
	}
	private BufferedImage getImg(int [] [] rect)
	{
		//get the initial X and Y values (Smallest) (xl -x low) (xh - x high)
		int xl = rect[0][0];
		int yl = rect[0][1];
		int xh = xl;
		int yh = yl;
		for (int i = 0; i <3; i++ )
		{
			//current x and y.
			int xc = rect[i][0];
			int yc = rect[i][1];
			
			// find smallest
			if (xc<xl )
				xl = xc;
			if (yc<yl)
				yl = yc;
			// find biggest
			if (xc > xh)
				xh = xc;
			if (yc > yh)
				yh = yc;
		}
		int width = xh -xl;
		int height = yh - yl;
		return originalImage.getSubimage(xl,yl,width,height);
		
		
	}
	
	private BufferedImage drawRect(int [] [] rect,int rgb, BufferedImage image)
	{
		BufferedImage img = deepCopy(image);
		img = addLine(rect,0,1,rgb,img);//Horizontal
		img = addLine(rect,1,2,rgb,img);//Vertical
		img = addLine(rect,2,3,rgb,img);//Horizontal
		img = addLine(rect,3,0,rgb,img);//Vertical
		return deepCopy(img);
		
	}
	private BufferedImage addLine(int [] [] rect,int point1,int point2,int rgb,BufferedImage img)
	{
		
		int xc,yc,orientation;
		if (rect[point1][1]-rect[point2][1]==0)
		{
			orientation = 0;
		}
		else if(rect[point1][0]-rect[point2][0]==0)
		{
			orientation =1;
		}
		else
		{
			orientation = 2;
		}
		if (orientation ==0)//Horizontal
		{
			int a = rect[point1][0];
			int b = rect[point2][0];
			if(a>b)
			{
				int temp = a;
				a=b;
				b= temp;
			}
			yc = rect[point1][1];
	
			for(xc=a; xc<= b;xc++)
			{
					img.setRGB(xc, yc, rgb);
			}
		}
		else //Vertical
		{
			int a = rect[point1][1];
			int b = rect[point2][1];
			if(a>b)
			{
				int temp = a;
				a=b;
				b= temp;
			}			
			xc = rect[point1][0];
			for (yc = a; yc<=b; yc++)
			{
				img.setRGB(xc, yc, rgb);
			}
		}
		return img;
	}
	
	private class frameResizeListener implements ComponentListener
	{

		@Override
		public void componentHidden(ComponentEvent arg0)
		{
			// TODO Auto-generated method stub
			
		}

		public void componentMoved(ComponentEvent arg0)
		{
			repaintBackground();			
		}

		public void componentResized(ComponentEvent arg0)
		{
			repaintBackground();
		}

		public void componentShown(ComponentEvent arg0)
		{
			repaintBackground();			
		}
	}
	
	private class listMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			JList<?> list = (JList<?>)e.getSource();
			if (e.getClickCount() == 2)
			{
				int index = list.locationToIndex(e.getPoint());
				ImageIcon icon = new ImageIcon();
				icon.setImage(selectedExamples.get(index));
				JOptionPane.showMessageDialog(null,icon);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseExited(MouseEvent e)
		{
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mousePressed(MouseEvent e)
		{
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent e)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String [] args)
	{
		System.out.println("Hi");
		InstTrainNNL frame = new InstTrainNNL();
		frame.setTitle("Train Neural Network: Location");
		frame.setSize(1600,960);
		
	}
}

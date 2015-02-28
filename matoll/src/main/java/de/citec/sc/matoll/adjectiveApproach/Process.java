package de.citec.sc.matoll.adjectiveApproach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/*
 * Good description for using Weka with Java:
 * http://stackoverflow.com/questions/21674522/get-prediction-percentage-in-weka-using-own-java-code-and-a-model/21678307#21678307
 */
public class Process {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String path_annotatedFiles = "/Users/swalter/Documents/annotatedAdjectives/";
		String path_raw_files = "/Users/swalter/Documents/plainAdjectives/";
		String path_to_write_arff = "/Users/swalter/Desktop/adjective.arff";
		String path_weka_model = path_to_write_arff.replace(".arff", ".model");
		Classifier cls = new SMO();
		
		/*
		 * Create Raw Data
		 */
		
		
		/*
		 * Generate ARFF File
		 */
		try {
			GenerateArff.run(path_annotatedFiles, path_raw_files, path_to_write_arff);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Generate model
		 */
		try {
			generateModel(cls,path_to_write_arff);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Load model for prediction
		 */
		Prediction prediction = new Prediction(path_weka_model);
		
		/*
		 * Load instances to predict.
		 */
		 ArffLoader loader = new ArffLoader();
		 loader.setFile(new File(path_to_write_arff));
		 Instances structure = loader.getStructure();
		 structure.setClassIndex(structure.numAttributes() - 1);
		 
		 Instance current;
		 while ((current = loader.getNextInstance(structure)) != null){
			 /*
			  * predict
			  */
			 List<String> result = prediction.predict(current);
			 System.out.println("Prediction:"+result.get(0));
			 System.out.println("Distribution:"+result.get(1));
			 
		 }
		 
		 
		 
		
	}

	private static void generateModel(Classifier cls,String path_to_arff) throws FileNotFoundException, IOException {
		 Instances inst = new Instances(new BufferedReader(new FileReader(path_to_arff)));
		 inst.setClassIndex(inst.numAttributes() - 1);
		 try {
			cls.buildClassifier(inst);
			// serialize model
			 ObjectOutputStream oos = new ObjectOutputStream(
			                            new FileOutputStream(path_to_arff.replace(".arff", ".model")));
			 oos.writeObject(cls);
			 oos.flush();
			 oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		
	}

}
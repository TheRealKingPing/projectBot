package projectBot;

import org.apache.commons.lang3.Validate;

//usless
public class NeuralNetwork {
	private int learningIterations;
	private int learningRate;
	private int hiddenUnits;
	private byte target = 0; 
	private byte[] weights = null;
	
	NeuralNetwork(String _name, int _learningIterations, int _hiddenUnits) {
		Validate.notNull(_name, "name can't be null");
		Validate.notNull(_learningIterations, "learning iterations can't be null");
		Validate.notNull(_hiddenUnits, "hidden units can't be null");
		learningIterations = _learningIterations;	
		hiddenUnits = _hiddenUnits;
	}
	
	public void learn() {
		 forwardPropagation();
	}
	
	private double forwardPropagation() {
		//beispiel /todo: to float?
		byte[] input = {1, 1};		
		
		double[][] weight = {
				{0.8, 0.4, 0.3},
				{0.2, 0.9, 0.5}				
		};			

		//hidden sum (units)
		double hiddenSum1 = input[0] * weight[0][0] + input[1] * weight[1][0];
		double hiddenSum2 = input[0] * weight[0][1] + input[1] * weight[1][1];
		double hiddenSum3 = input[0] * weight[0][2] + input[1] * weight[1][2];
		
		//Sigmoid of hidden sum (units)
		double sHiddenSum1 = 1 / (1 + Math.exp(-hiddenSum1));
		double sHiddenSum2 = 1 / (1 + Math.exp(-hiddenSum2));
		double sHiddenSum3 = 1 / (1 + Math.exp(-hiddenSum3));
		
		//hidden-to-outer weights
		double[] hTOWeight = {0.3, 0.5, 0.9};
		
		double outputSum = sHiddenSum1 * hTOWeight[0] + sHiddenSum2 * hTOWeight[1] + sHiddenSum3 * hTOWeight[2];
		
		double calculated = 1 / (1 + Math.exp(-outputSum));
		
		double difference = target - calculated;

		return difference;
	}
	
	private void backPropagation() {
		//beispiel
		
		
	}
	
	private void getRandomWeight() {
		
	}
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class SumOfSelections {

	private boolean U[][];
	private int n, K, t[], SN, SP;
	
	/* Determines which approach will be taken. False by default. */
	private boolean topdownMode; 
	
	private File inputFile, outputFile;
	private PrintWriter out;
	
	/**
	 * Constructor for an instance of SumOfSelections. 
	 * Makes sure that the input file can be read, and that the output file can be created or deleted.
	 * @param infname	filename of the input file
	 * @param outfname	filename of the output file
	 */
	public SumOfSelections(String infname, String outfname) {
		inputFile = new File(infname);
		if (!inputFile.canRead()) {
			System.out.println("File " + infname + " could be not found, or cannot be read!");
			System.exit(0);
		}
		outputFile = new File(outfname);
		try {
			if ((outputFile.exists() && !outputFile.delete()) || !outputFile.createNewFile()) {
				System.out.println("File " + outfname + " could not be deleted!");
				System.exit(0);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		try {
			out = new PrintWriter(outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		topdownMode = false;
	}
	
	/**
	 * Sets the mode of the SumOfSelection solver.
	 * @param b if true, the solver will use the top-down approach. If false, it will use the bottom-up approach.
	 */
	public void setMode(boolean b) {
		topdownMode = b;
	}
	
	/**
	 * Calculates SN and SP and initializes the U array.
	 */
	private void initialize() {
		SN = 0; SP = 0;
		
		/* Adds each integer to the correct accumulator variable. */
		for (int i : t) {
			if (i < 0)
				SN += i;
			else 
				SP += i;
		}
		
		/* Creates a new array with the correct dimensions. */
		U = new boolean[n][SP - SN + 1];
		
		/* Initializes the values of the U array. */
		for (int i = 0; i < U.length; i++)
			for(int j = 0; j < U[0].length; U[i][j++] = false);
		U[0][0 - SN] = true;
		U[0][t[0] - SN] = true;
	}
	
	/**
	 * Finds all possible sum of selections.
	 * This is the bottom-up approach of the SOS solver.
	 */
	private void findAllSelections() {
		for (int i = 1; i < U.length; i++) {
			for (int j = 0; j < U[0].length; j++) {
				/* Checks if j - t[i] is within array boundaries. */
				if (j - t[i] < 0 || j - t[i] > SP - SN)
					U[i][j] = U[i - 1][j];
				else
					U[i][j] = U[i - 1][j] || U[i - 1][j - t[i]];
				}
		}
	}
	
	/**
	 * Recursively calculates if the sum j - SN is possible.
	 * This is the top-down approach of the SOS solver.
	 * @param i index value of the first dimension in the U array
	 * @param j index value of the second dimension in the U array and part of the sum j - SN
	 * @return true if j - SN is a possible sum, false otherwise.
	 */
	private boolean findSelectionTD(int i, int j) {
		/* Return true if j - SN has already been calculated to be a possible sum. */
		if (U[i][j]) return true; 
		/* We cannot leave of the array boundaries, so return false. */
		if (i == 0) return false;
		
		boolean b = false;
		
		/* Checks if j - t[i] is within array boundaries. */
		if (j - t[i] >= 0 && j - t[i] <= SP - SN) 
			/* Calculate if j - t[i] - SN is a possible sum. */
			b = findSelectionTD(i - 1, j - t[i]);
		/* Checks if j - t[i] - SN was calculated to be a possible sum,
		 * and if it wasn't, decrement the i index and do a recursive call.
		 * Note that if b evaluates to true, findSelectionTD(i - 1, j) will not be called. */
		b = b || findSelectionTD(i - 1, j);
		
		/* Save the calculation result to the array. */
		U[i][j] = b;
		
		return b;
	}
	
	/**
	 * Generates a String showing which t-integers are in the selection for K and which integers are not, by backtracing the selection.
	 * @return a String showing which t-integers are in the selection for K and which integers are not.
	 */
	private String outputSelection() {
		int i, j;
		
		/* Array determining which t-integers are in the selection. */
		boolean b[] = new boolean[n];
		for (i = 0; i < n; i++)
			b[i] = false;
		
		i = n; j = K - SN;

		/* Backtraces the selection. */
		do {
			for (; i > 0 && U[i - 1][j]; i--);
			b[i] = U[i][j];
			j -= t[i--];
		} while(j + SN != 0 && i >= 0 && j > 0 && j < U[0].length && U[i][j]); /* Checks that we've not reached the sum 0, the end of the array, and
		 																		that j is within the array boundaries*/
		String s = "";
		/* Generates the String. */
		for (i = 0; i < n; i++) 
			s += "(" + t[i] + "," + (b[i] ? 1 : 0) + ")";
		
		return s;
	}
	
	/**
	 * Calculates if the sum K is possible, and outputs the result plus one of the selections if it was determined to be possible.
	 */
	private void findSelection() {
		/* Must initialize the system first. */
		initialize();
		
		/* Call the method for the selected mode. */
		if (topdownMode) 
			findSelectionTD(n - 1, K - SN);
		else 
			findAllSelections();
		
		/* Outputs the result to the output file. */
		String s;
		if (U[n - 1][K - SN]) 
			s = "YES " + K + " " + outputSelection();
		else
			s = "NO " + K;
		
		out.println(s);
		out.flush();
	}
	
	/**
	 * Processes the input file
	 */
	public void readInputFile() {
		Scanner s = null;
		try {
			s = new Scanner(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		/* Reads a line from the input file into the system and runs the SOS solver. */
		while (s.hasNext()) {
			/* Read and assign the integers values in the input to the appropriate variables. */
			n = s.nextInt(); 
			K = s.nextInt();
			t = new int[n];
			for (int i = 0; i < n; i++) 
				t[i] = s.nextInt();
			
			/* Runs the SOS solver. */
			findSelection();
		}
		s.close();
		out.close();
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			SumOfSelections SOS = new SumOfSelections(args[0], args[1]);
			SOS.readInputFile();
		} else if (args.length == 3) {
			if (args[2].equals("-td") || args[2].equals("-topdown")) {
				SumOfSelections SOS = new SumOfSelections(args[0], args[1]);
				SOS.setMode(true);
				SOS.readInputFile();
			} else {
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				System.err.println("Invalid third argument:\n" + stack[stack.length - 1].getClassName() + " input_filename output_filename {-td | -topdown}");
			}
		} else {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			System.err.println("Invalid number of arguments:\n" + stack[stack.length - 1].getClassName() + " input_filename output_filename {-td | -topdown}");
		}
	}

}

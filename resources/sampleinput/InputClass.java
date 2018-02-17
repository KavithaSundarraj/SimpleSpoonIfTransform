package sampleinput;

import IfTransformExample.Ifind;

public class InputClass implements Ifind {
	
	public String find() {
		boolean cond=true;
		String res="result";
		if(cond)
			System.out.println("Sample output");
		if(cond)
			return "success";
		else
			return res;
		
		}
}

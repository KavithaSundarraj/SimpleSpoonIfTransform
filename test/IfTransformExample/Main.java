package IfTransformExample;

import java.lang.reflect.Method;
import java.util.List;

import spoon.reflect.Factory;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtSimpleType;
import spoonloader.SpoonLoader;

public class Main {

	public static void main(String[] args) throws Exception {
		SpoonLoader loader = new SpoonLoader();
		loader.addSourcePath("resources/sampleinput");
		
		// Execute original class
		System.out.println("****Original Class****");
		Class<Ifind> inputClass = loader.load("sampleinput.InputClass");
		printCode(loader.mirror(inputClass));
		execute(inputClass);
		
		// Modify class
		modify(inputClass, "return \"success\"", loader.getFactory());
		loader.compile();
		
		// Execute modified class
		System.out.println("****Transformed Class****");
		Class<Ifind> modifiedClass = loader.load("sampleinput.InputClass");
		printCode(loader.mirror(modifiedClass));
		execute(modifiedClass);
	}

	/**
	 * Run {@link Ifind#find()} on an instance of the given class
	 */
	private static void execute(Class<Ifind> inputClass) throws Exception {
		Ifind instance = inputClass.newInstance();
		System.out.println(instance.find());
	}
	
	/**
	 * Modify {@link Ifind#find()} to include the given statement
	 */
	private static void modify(
			Class<Ifind> inputClass, 
			String newStatementString, 
			Factory factory) throws Exception {		
		CtStatement newStatement = 
			factory.Code().createCodeSnippetStatement(newStatementString);
		Method method = inputClass.getMethod("find");
		CtExecutable<?> methodDecl = 
			factory.Method().createReference(method).getDeclaration();			
		List<CtStatement> statements = methodDecl.getBody().getStatements();	
		for(int i=0;i<statements.size();i++)
		{
			String temp=statements.get(i).toString();
			if(temp.contains("if")&& temp.contains("return"))
				statements.remove(i);
			}
			statements.add(newStatement);
   	}

	/**
	 * Print the source code of the given type
	 */
	private static void printCode(CtSimpleType<Ifind> ctSimpleType) {
		System.out.println(ctSimpleType);
	}
}

package spoonloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import spoon.processing.Builder;
import spoon.reflect.Factory;
import spoon.reflect.declaration.CtSimpleType;
import spoon.support.ByteCodeOutputProcessor;
import spoon.support.DefaultCoreFactory;
import spoon.support.JavaOutputProcessor;
import spoon.support.StandardEnvironment;

/**
 * 
 * @author kavitha
 *
 */
public class SpoonLoader {
	
	private static final String DEFAULT_MODEL_BIN_DIR = "spoon/bin";
	private static final String DEFAULT_MODEL_SRC_DIR = "spoon/src";
	
	private Factory factory;
	private String modelBinDir = DEFAULT_MODEL_BIN_DIR;
	private String modelSrcDir = DEFAULT_MODEL_SRC_DIR;

	public SpoonLoader() {
		factory = createFactory();
	}
	
	/**
	 * @return the {@link Factory} used to create mirrors
	 */
	public Factory getFactory() {
		return factory;
	}
	
	/**
	 * Set the temporary directory in which this class outputs source files
	 */
	public void setModelSrcDir(String modelSrcDir) {
		this.modelSrcDir = modelSrcDir;
	}

	/**
	 * Get the temporary directory in which this class outputs source files
	 */
	public String getModelSrcDir() {
		return modelSrcDir;
	}
	
	/**
	 * Set the temporary directory in which this class outputs compiled class files
	 */
	public void setModelBinDir(String modelBinDir) {
		this.modelBinDir = modelBinDir;
	}
	
	/**
	 * Get the temporary directory in which this class outputs compiled class files
	 */
	public String getModelBinDir() {
		return modelBinDir;
	}

	/**
	 * @return a new Spoon {@link Factory}
	 */
	protected Factory createFactory() {
		StandardEnvironment env = new StandardEnvironment(); 
		Factory factory = new Factory(new DefaultCoreFactory(), env);

		// environment initialization
		env.setComplianceLevel(6);
		env.setVerbose(false);
		env.setDebug(false);
		env.setTabulationSize(5);
		env.useTabulations(true);
		env.useSourceCodeFragments(false);	
		
		return factory;
	}
	
	/**
	 * Add directories or JAR files in which to search for input source code
	 */
	public void addSourcePath(String sourcePath) {
		addSourcePath(new File(sourcePath));
	}
	
	/**
	 * Add directories or JAR files in which to search for input source code
	 */
	public void addSourcePath(File sourcePath) {
		buildModel(sourcePath);
		compile();
	}
	
	/**
	 * Build the internal model (i.e. abstract syntax tree) of the source code
	 */
	protected void buildModel(File sourcePath) {
		Builder builder = getFactory().getBuilder();		
		
		try {
			builder.addInputSource(sourcePath);
			builder.build();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	/**
	 * Create class files of the internal model in {@link #modelBinDir}
	 */
	public void compile() {
		JavaOutputProcessor fileOutput = new JavaOutputProcessor(new File(modelSrcDir));
		ByteCodeOutputProcessor classOutput = new ByteCodeOutputProcessor(fileOutput, new File(modelBinDir));
		classOutput.setFactory(getFactory());
		classOutput.init();
		for (CtSimpleType<?> type : getFactory().Class().getAll()) {
			classOutput.process(type);
		}
		classOutput.processingDone();
	}
	
	/**
	 * @return the class with the given qualified name. 
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> load(String qualifiedName) {
		try {
			URL url = new File(modelBinDir).toURI().toURL();
			URLClassLoader cl = new URLClassLoader(new URL[] {url});
			Thread.currentThread().setContextClassLoader(cl);
			return (Class<T>)(cl.loadClass(qualifiedName));
		} 
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} 
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return the reflective instance of the class with the given qualified name
	 */
	public <T> CtSimpleType<T> mirror(String qualifiedName) {
		Class<T> clazz = load(qualifiedName);
		return mirror(clazz);
	}
	
	/**
	 * @return the reflective instance of the given class
	 */
	public <T> CtSimpleType<T> mirror(Class<T> clazz) {
		return getFactory().Type().get(clazz);
	}
}

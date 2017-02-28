package javar;

import java.io.File;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.jar.JarFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.ParseException;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class RenjinTestRun {

	RenjinScriptEngineFactory factory;
	ScriptEngine engine;

	@Before
	public void setup() {
		factory = new RenjinScriptEngineFactory();
		engine = factory.getScriptEngine();
	}

	@Test
	public void testRenjin0() {
		try {
			engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
			engine.eval("print(df)");
			engine.eval("print(lm(y ~ x, df))");
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}

	}

	// http://docs.renjin.org/en/latest/library/moving-data-between-java-and-r-code.html
	@Test
	public void testRenjin1() {
		try {
			// evaluate Renjin code from String:
			SEXP res = (SEXP) engine.eval("a <- 2; b <- 3; a*b");

			// print the result to stdout:
			System.out.println("The result of a*b is: " + res);
			// determine the Java class of the result:
			Class objectType = res.getClass();
			System.out.println("Java class of 'res' is: " + objectType.getName());
			// use the getTypeName() method of the SEXP object to get R's type
			// name:
			System.out.println("In R, typeof(res) would give '" + res.getTypeName() + "'");
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	@Test
	public void testRenjin3() {
		try {
			Vector x = (Vector) engine.eval("x <- c(6, 7, 8, 9)");
			System.out.println("The vector 'x' has length " + x.length());
			for (int i = 0; i < x.length(); i++) {
				System.out.println("Element x[" + (i + 1) + "] is " + x.getElementAsDouble(i));
			}
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	@Test
	public void testRenjin4() {
		try {
			Vector res = (Vector) engine.eval("matrix(seq(9), nrow = 3)");
			if (res.hasAttributes()) {
				AttributeMap attributes = res.getAttributes();
				Vector dim = attributes.getDim();
				if (dim == null) {
					System.out.println("Result is a vector of length " + res.length());

				} else {
					if (dim.length() == 2) {
						System.out.println(
								"Result is a " + dim.getElementAsInt(0) + "x" + dim.getElementAsInt(1) + " matrix.");
					} else {
						System.out.println("Result is an array with " + dim.length() + " dimensions.");
					}
				}
			}
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	@Test
	public void testRenjin5() {
		try {
			Vector res = (Vector) engine.eval("matrix(seq(9), nrow = 3)");
			try {
				Matrix m = new Matrix(res);
				System.out.println("Result is a " + m.getNumRows() + "x" + m.getNumCols() + " matrix.");
			} catch (IllegalArgumentException e) {
				System.out.println("Result is not a matrix: " + e);
			}
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	@Test
	public void testRenjin6() {
		try {
			ListVector model = (ListVector) engine.eval("x <- 1:10; y <- x*3; lm(y ~ x)");
			Vector coefficients = model.getElementAsVector("coefficients");
			// same result, but less convenient:
			// int i = model.indexOfName("coefficients");
			// Vector coefficients = (Vector)model.getElementAsSEXP(i);

			System.out.println("intercept = " + coefficients.getElementAsDouble(0));
			System.out.println("slope = " + coefficients.getElementAsDouble(1));
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	/**
	 * 
	 * ParseException: an exception thrown by Renjin’s R parser due to a syntax
	 * error and EvalException: an exception thrown by Renjin when the R code
	 * generates an error condition, for example by the stop() function.
	 */
	@Test
	public void testRenjin7() {
		try {

			engine.eval("x <- 1 +/ 1");
		} catch (ParseException e) {
			System.out.println("R script parse error: " + e.getMessage());
			Assert.assertEquals(ParseException.class, e.getClass());
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}

		try {
			engine.eval("stop(\"Hello world!\")");
		} catch (EvalException e) {
			// getCondition() returns the condition as an R list:
			Vector condition = (Vector) e.getCondition();
			// the first element of the string contains the actual error
			// message:
			String msg = condition.getElementAsString(0);
			System.out.println("The R script threw an error: " + msg);

			Assert.assertEquals(EvalException.class, e.getClass());
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	@Test
	public void testRenjin8() {
		try {
			engine.put("x", 4);
			engine.put("y", new double[] { 1d, 2d, 3d, 4d });
			engine.put("z", new DoubleArrayVector(1, 2, 3, 4, 5));
			engine.put("hashMap", new java.util.HashMap());
			// some R magic to print all objects and their class with a
			// for-loop:
			engine.eval("for (obj in ls()) { " + "cmd <- parse(text = paste('typeof(', obj, ')', sep = ''));"
					+ "cat('type of ', obj, ' is ', eval(cmd), '\\n', sep = '') }");
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}

	// custom sessions
	// 1 ScriptEngine instance = 1 r session
	// http://docs.renjin.org/en/latest/library/execution-context.html#file-system
	@Test
	public void testRenjin9() {
		try {
			
			// a new engine from a session
			Session session = new SessionBuilder().withDefaultPackages().build();

			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
			RenjinScriptEngine engine = factory.getScriptEngine(session);
			
			// other session from a custom file providing
			/*
			 * The renjin-appengine module provides a more complex example. 
			 * There, the AppEngineContextFactory class prepares a FileSystemManager that is configured 
			 * with a AppEngineLocalFileSystemProvider subclass that provides
			 *  read-only access to the servlet’s directory. 
			 *  This allows R scripts access to “/WEB-INF/data/model.R”, 
			 *  which is translated into the absolute path at runtime.
			 */
			
			DefaultFileSystemManager fsm = new DefaultFileSystemManager();
			fsm.addProvider("jar", new JarFileProvider());
			fsm.addProvider("file", new DefaultLocalFileProvider());
			fsm.addProvider("res", new ResourceFileProvider());
			fsm.addExtensionMap("jar", "jar");
			fsm.setDefaultProvider(new UrlFileProvider());
			fsm.setBaseFile(new File("/"));
			fsm.init();

			Session session2 = new SessionBuilder()
			  .withDefaultPackages()
			  .setFileSystemManager(fsm)
			  .build();

			RenjinScriptEngineFactory factory2 = new RenjinScriptEngineFactory();
			RenjinScriptEngine engine2 = factory.getScriptEngine(session);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	}
	
	/**
	 * loading packages
	 * http://docs.renjin.org/en/latest/library/execution-context.html#package-loading
	 * faltan remote repositoru , aether packageloader
	 */
//	@Test
//	public void testRenjin10() {
//		try {
//			RemoteRepository internalRepo = new RemoteRepository.Builder(
//				    "internal", /* id */
//				    "default",  /* type */
//				    "https://repo.acme.com/content/groups/public/").build();
//
//				List<RemoteRepository> repositories = new ArrayList<>();
//				repositories.add(internalRepo);
//				repositories.add(AetherFactory.renjinRepo());
//				repositories.add(AetherFactory.mavenCentral());
//
//				ClassLoader parentClassLoader = getClass().getClassLoader();
//
//				AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, repositories);
//
//				Session session = new SessionBuilder()
//				    .withDefaultPackages()
//				    .setPackageLoader(loader)
//				    .build();
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail("b00m renjin no funciono!");
//		}
//	}

}

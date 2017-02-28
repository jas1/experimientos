package javar;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;
import org.renjin.script.RenjinScriptEngineFactory;

public class RenjinTestRun {

	@Test
	public void testRenjin0(){
		
	    try {
			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
			// create a Renjin engine:
			ScriptEngine engine = factory.getScriptEngine();
			
			engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
			engine.eval("print(df)");
			engine.eval("print(lm(y ~ x, df))");
		} catch (ScriptException e) {
			e.printStackTrace();
			Assert.fail("b00m renjin no funciono!");
		}
	    
	}
	
}

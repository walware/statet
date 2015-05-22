package de.walware.statet.r.core.rsource.ast;

import org.junit.Assert;
import org.junit.Test;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.RAst.ReadedFCallArgs;


public class RArgumentMatchingTest {
	
	
	private final RScanner scanner= new RScanner(IModelManager.MODEL_FILE);
	
	private final StringParserInput input= new StringParserInput();
	
	
	@Test
	public void simple() {
		final ArgsDefinition argsDef = new ArgsDefinition("test1", "test2");
		
		assertArgs(argsDef, "1, 2", new int[] { 0, 1 });
		assertArgs(argsDef, "test1=1, 2", new int[] { 0, 1 });
		assertArgs(argsDef, "1, test1=2", new int[] { 1, 0 });
		assertArgs(argsDef, "test2=1, 2", new int[] { 1, 0 });
		assertArgs(argsDef, "test1=1, test2=2", new int[] { 0, 1 });
		assertArgs(argsDef, "test2=1, test1=2", new int[] { 1, 0 });
	}
	
	@Test
	public void empty1() {
		final ArgsDefinition argsDef = new ArgsDefinition("test1", "test2");
		
		assertArgs(argsDef, ", 2", new int[] { 0, 1 });
		assertArgs(argsDef, "test1=, 2", new int[] { 0, 1 });
		assertArgs(argsDef, ", test1=2", new int[] { 1, 0 });
		assertArgs(argsDef, "test2=, 2", new int[] { 1, 0 });
		assertArgs(argsDef, "test1=, test2=2", new int[] { 0, 1 });
		assertArgs(argsDef, "test2=, test1=2", new int[] { 1, 0 });
	}
	
	@Test
	public void empty2() {
		final ArgsDefinition argsDef = new ArgsDefinition("test1", "test2");
		
		assertArgs(argsDef, "1, ", new int[] { 0, 1 });
		assertArgs(argsDef, "test1=1, ", new int[] { 0, 1 });
		assertArgs(argsDef, "1, test1=", new int[] { 1, 0 });
		assertArgs(argsDef, "test2=1, ", new int[] { 1, 0 });
		assertArgs(argsDef, "test1=1, test2=", new int[] { 0, 1 });
		assertArgs(argsDef, "test2=1, test1=", new int[] { 1, 0 });
	}
	
	@Test
	public void partialMatch() {
		final ArgsDefinition argsDef = new ArgsDefinition("b", "aaa", "aa");
		
		assertArgs(argsDef, "aaa=1, a=2, 3", new int[] { 1, 2, 0 });
		assertArgs(argsDef, "aaa=1, a=2, a=3", new int[] { 1, 2, -1 });
		assertArgs(argsDef, "a=1, 2", new int[] { -1, 0 });
	}
	
	
	private void assertArgs(final ArgsDefinition argsDef, final String code, final int[] expected) {
		final Args callArgs = this.scanner.scanFCallArgs(this.input.reset(code).init(), true);
		
		final ReadedFCallArgs readedArgs = RAst.readArgs(callArgs, argsDef);
		
		Assert.assertArrayEquals(expected, readedArgs.argsNode2argsDef);
	}
	
}

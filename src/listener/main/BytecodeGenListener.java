package listener.main;

import com.sun.corba.se.impl.io.TypeMismatchException;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ParamsContext;

import java.io.FileOutputStream;
import java.io.IOException;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();
	
	int tab = 0;
	int label = 0;
	String globalDecl = "";
	String globalDeclWithInit = "";
	// program	: decl+
	
	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		symbolTable.initFunDecl();
		
		String fname = getFunName(ctx);
		ParamsContext params;
		
		if (fname.equals("main")) {
			symbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			symbolTable.putFunSpecStr(ctx);
			params = (MiniCParser.ParamsContext) ctx.getChild(3);
			symbolTable.putParams(params);
		}		
	}

	
	// var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		
		if (isArrayDecl(ctx)) {
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
			globalDecl += ".field static " + varName + " [I\n";
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
			globalDecl += ".field static " + varName + " I\n";
		}
		else  { // simple decl
			symbolTable.putGlobalVar(varName, Type.INT);
			globalDecl += ".field static " + varName + " I" +
					"\n";
		}
	}

	
	@Override
	public void enterLocal_decl(MiniCParser.Local_declContext ctx) {			
		if (isArrayDecl(ctx)) {
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));	
		}
		else  { // simple decl
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}	
	}

	
	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		String classProlog = getFunProlog();
		classProlog = classProlog.replace("Object\n", "Object\n" + globalDecl);
		String fun_decl = "", var_decl = "";
		
		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}
		String expr = classProlog + var_decl + fun_decl;
		expr = expr.replace(".method public static main([Ljava/lang/String;)V\n\t.limit stack 32\n\t.limit locals 32\n",
				".method public static main([Ljava/lang/String;)V\n\t.limit stack 32\n\t.limit locals 32\n" +	globalDeclWithInit);
		newTexts.put(ctx, expr);
		
		System.out.println(newTexts.get(ctx));
		try {
			FileOutputStream outputStream = new FileOutputStream("test.j");
			outputStream.write(newTexts.get(ctx).getBytes());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	
	// decl	: var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if(ctx.getChildCount() == 1)
		{
			if(ctx.var_decl() != null)				//var_decl
				decl += newTexts.get(ctx.var_decl());
			else							//fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}
	
	// stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		StringBuilder stmt = new StringBuilder("");
		if(ctx.getChildCount() > 0)
		{
			if(ctx.expr_stmt() != null)				// expr_stmt
				stmt.append(newTexts.get(ctx.expr_stmt()));
			else if(ctx.compound_stmt() != null)	// compound_stmt
				stmt.append(newTexts.get(ctx.compound_stmt()));
			// <(0) Fill here>
			else if(ctx.if_stmt() != null)
				stmt.append(newTexts.get(ctx.if_stmt()));
			else if(ctx.while_stmt() != null)
				stmt.append(newTexts.get(ctx.while_stmt()));
			else if(ctx.return_stmt() != null)
				stmt.append(newTexts.get(ctx.return_stmt()));
		}
		newTexts.put(ctx, stmt.toString());
	}
	
	// expr_stmt	: expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() == 2)
		{
			stmt += newTexts.get(ctx.expr());	// expr
		}
		newTexts.put(ctx, stmt);
	}
	
	
	// while_stmt	: WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
			// <(1) Fill here!>
		StringBuilder stmt = new StringBuilder("");
		String condExpr= newTexts.get(ctx.expr());
		String loopstmt = newTexts.get(ctx.stmt());

		String lLoop = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		stmt.append(lLoop)
				.append(":\n")
				.append(condExpr)
				.append("ifne ")
				.append(lend)
				.append("\n")
				.append(loopstmt)
				.append("goto ")
				.append(lLoop)
				.append("\n")
				.append(lend)
				.append(":")
				.append("\n");

		newTexts.put(ctx, stmt.toString());
	}

	@Override
//	type_spec IDENT '(' params ')' compound_stmt ;
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
			// <(2) Fill here!>
		StringBuilder stmt = new StringBuilder("");
		String fname = getFunName(ctx);
		symbolTable.putFunSpecStr(ctx);
		String header = funcHeader(ctx, fname);
		if(ctx.compound_stmt().stmt().get(ctx.compound_stmt().stmt().size() - 1).return_stmt() != null) {
//			has return
			stmt.append(header)
					.append(newTexts.get(ctx.compound_stmt()))
					.append(".end method\n");
		}
		else {
//			not has return
			stmt.append(header)
					.append(newTexts.get(ctx.compound_stmt()))
					.append("return\n.end method\n");
		}
		newTexts.put(ctx, stmt.toString());
	}
	

	private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) {
		return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"	
				+ "\t" + ".limit stack " 	+ getStackSize(ctx) + "\n"
				+ "\t" + ".limit locals " 	+ getLocalVarSize(ctx) + "\n";
				 	
	}

	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		String varDecl = "";
		
		if (isDeclWithInit(ctx)) {
			globalDeclWithInit += "ldc " + ctx.LITERAL().getText()
					+"\nputstatic Test/" + varName + " I\n";
			// v. initialization => Later! skip now..: 
		}
//		type_spec IDENT '[' LITERAL ']'
		if (isArrayDecl(ctx)) {
			globalDeclWithInit += "ldc " + ctx.LITERAL().getText() + "\n"
					+ "newarray int\n"
					+ "putstatic Test/" + varName + " [I\n";
		}
		newTexts.put(ctx, varDecl);
	}
	
	
	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String varDecl = "";
		String vId = symbolTable.getVarId(ctx);
		
		if (isDeclWithInit(ctx)) {
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
					+ "istore " + vId + "\n";
		}

		if(isArrayDecl(ctx)) {
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
					+ "newarray int\n"
					+ "astore " + vId + "\n";
		}
		newTexts.put(ctx, varDecl);
	}

	
	// compound_stmt	: '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// <(3) Fill here>
		StringBuilder stmt = new StringBuilder("");
		for(MiniCParser.Local_declContext localCtx : ctx.local_decl()) {
			stmt.append(newTexts.get(localCtx));
		}

		for(MiniCParser.StmtContext stmtCtx : ctx.stmt()) {
			stmt.append(newTexts.get(stmtCtx));
		}

		newTexts.put(ctx, stmt.toString());
	}

	// if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));

		String lend = symbolTable.newLabel();
		String lelse = symbolTable.newLabel();


		if(noElse(ctx)) {
			stmt += condExpr
				+ "ifne " + lend + "\n"
				+ thenStmt
				+ lend + ":"  + "\n";
		}
		else {
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr
					+ "ifne " + lelse + "\n"
					+ thenStmt
					+ "goto " + lend + "\n"
					+ lelse + ":\n" + elseStmt
					+ lend + ":"  + "\n";
		}

		newTexts.put(ctx, stmt);
	}
	
	
	// return_stmt	: RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
			// <(4) Fill here>
		StringBuilder stmt = new StringBuilder("");
		String returnVar = ctx.expr().IDENT().getText();
		String id = symbolTable.getVarId(returnVar);
		if(ctx.expr() == null) {
			newTexts.put(ctx, "return\n");
		}
		else if(symbolTable.getVarType(returnVar) == Type.INT){
			stmt.append("iload ")
					.append(id)
					.append("\nireturn\n");
			newTexts.put(ctx, stmt.toString());
		}
		else {
			//array
			if(!id.contains("Test/")) {
				//local
				stmt.append("aload ")
						.append(id)
						.append("\nareturn\n");
			}
			else {
				//global
				stmt.append("getstatic ")
						.append(id)
						.append(" [I\nareturn\n");
			}
			newTexts.put(ctx, stmt.toString());
		}
	}

	
	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String expr = "";

		if(ctx.getChildCount() <= 0) {
			newTexts.put(ctx, ""); 
			return;
		}		
		
		if(ctx.getChildCount() == 1) { // IDENT | LITERAL
			if(ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if(symbolTable.getVarType(idName) == Type.INT) {
					String varId = symbolTable.getVarId(idName);
					if(varId.indexOf(0) != 'G') {
						//local
						expr += "iload " + varId + " \n";
					}
					else {
						//global
						varId = varId.replaceFirst("G", "");
						expr += "getfield " + varId + "\n";
					}
				}
				//else	// Type int array => Later! skip now..
				else if(symbolTable.getVarType(idName) == Type.INTARRAY) {
					String varId = symbolTable.getVarId(idName);
					if(!varId.contains("Test/")) {
						//local
						expr += "aload " + varId + " \n";
					}
					else {
						//global
						expr += "getstatic " + varId;
						if(symbolTable.getVarType(idName) == Type.INT) {
							expr += " I\n";
						}
						else if(symbolTable.getVarType(idName) == Type.INTARRAY) {
							expr += " [I\n";
						}
					}
				}
				//	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
				} else if (ctx.LITERAL() != null) {
					String literalStr = ctx.LITERAL().getText();
					expr += "ldc " + literalStr + " \n";
				}
			}
		else if(ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx, expr);
		}
		else if(ctx.getChildCount() == 3) {	 
			if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
				expr = newTexts.get(ctx.expr(0));
				
			} else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
				String varId = symbolTable.getVarId(ctx.IDENT().getText());
				if(!varId.contains("Test/")) {
					//local
					expr = newTexts.get(ctx.expr(0))
							+ "istore_" + varId + " \n";
				}
				else {
					//global
					String idName = ctx.IDENT().getText();
					expr = newTexts.get(ctx.expr(0))
							+ "putstatic " + varId;
					if(symbolTable.getVarType(idName) == Type.INT) {
						expr += " I\n";
					}
					else if(symbolTable.getVarType(idName) == Type.INTARRAY) {
						expr += " [I\n";
					}
				}
			} else { 											// binary operation
				expr = handleBinExpr(ctx, expr);
			}
		}
		// IDENT '(' args ')' |  IDENT '[' expr ']'
		else if(ctx.getChildCount() == 4) {
			if(ctx.args() != null){		// function calls
				expr = handleFunCall(ctx, expr);
			} else { // expr
				// Arrays:
				String idName = ctx.IDENT().getText();
				String varId = symbolTable.getVarId(idName);
				if(!varId.contains("Test/")) {
					expr += "aload " + varId + "\n";
				}
				else {
					//global
					expr = "getstatic " + varId;
					if(symbolTable.getVarType(idName) == Type.INT) {
						expr += " I\n";
					}
					else if(symbolTable.getVarType(idName) == Type.INTARRAY) {
						expr += " [I\n";
					}
				}
				expr += newTexts.get(ctx.expr(0))
								+ "iaload\n";
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays:
			String idName = ctx.IDENT().getText();
			String varId = symbolTable.getVarId(idName);
			if(!varId.contains("Test/")) {
				//local
				expr += "aload " + varId + "\n";
			}
			else {
				//global
				expr += "getstatic " + varId + " [I\n";
			}
			expr += newTexts.get(ctx.expr(0))
					+ newTexts.get(ctx.expr(1))
					+ "iastore\n";
		}
		newTexts.put(ctx, expr);
	}


	private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();
		String id = symbolTable.getVarId(ctx.expr(0).IDENT().getText());
		Type type = symbolTable.getVarType(ctx.expr(0).IDENT().getText());
		if(type == Type.INTARRAY && ctx.expr(0).children.size() != 4) {
			//not int && not IDENT[expr]
			System.out.println("Invalid Operand : " + ctx.getText());
			System.exit(-1);
		}
		if(type == Type.INT) {
			expr += newTexts.get(ctx.expr(0));
			switch(ctx.getChild(0).getText()) {
				case "-":
					expr += "ineg \n"; break;
				case "--":
					expr += "ldc 1" + "\n"
							+ "isub" + "\n"
							+ "istore " + id + "\n";
					break;
				case "++":
					expr += "ldc 1" + "\n"
							+ "iadd" + "\n"
							+ "istore " + id  + "\n";
					break;
				case "!":
					expr += "ifeq " + l2 + "\n"
							+ l1 + ":\n" + "ldc 0" + "\n"
							+ "goto " + lend + "\n"
							+ l2 + ":\n" + "ldc 1" + "\n"
							+ lend + ": " + "\n";
					break;
			}
		}
		else {
			switch(ctx.getChild(0).getText()) {
				case "-":
					expr += newTexts.get(ctx.expr(0)) +
							"ineg \n";
					break;
				case "--":
					if(id.contains("Test/")) {
						expr += "getstatic " + id + " [I\n";
					}
					else {
						expr += "aload " + id + "\n";
					}
					expr += "ldc " + ctx.expr(0).expr(0).getText() + "\n" +
							newTexts.get(ctx.expr(0)) +
							"ldc 1" + "\n"
							+ "isub" + "\n"
							+ "iastore\n";
					break;
				case "++":
					if(id.contains("Test/")) {
						expr += "getstatic " + id + " [I\n";
					}
					else {
						expr += "aload " + id + "\n";
					}
					expr += "ldc " + ctx.expr(0).expr(0).getText() + "\n" +
							newTexts.get(ctx.expr(0)) +
							"ldc 1" + "\n"
							+ "iadd" + "\n"
							+ "iastore\n";
					break;
				case "!":
					expr += newTexts.get(ctx.expr(0)) +
							"ifeq " + l2 + "\n"
							+ l1 + ":\n" + "ldc 0" + "\n"
							+ "goto " + lend + "\n"
							+ l2 + ":\n" + "ldc 1" + "\n"
							+ lend + ": " + "\n";
					break;
			}
		}
		return expr;
	}


	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2;
		String lend = symbolTable.newLabel();
		Type type1 = symbolTable.getVarType(ctx.expr(0).getText());
		Type type2 = symbolTable.getVarType(ctx.expr(1).getText());
		if( (type1 == Type.INTARRAY && ctx.expr(0).children.size() != 4) ||
				(type2 == Type.INTARRAY && ctx.expr(1).children.size() != 4)) {
			System.out.println("Invalid Operand : " + ctx.getText());
			System.exit(-1);
		}

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));
		
		switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "imul \n"; break;
			case "/":
				expr += "idiv \n"; break;
			case "%":
				expr += "irem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "iadd \n"; break;
			case "-":
				expr += "isub \n"; break;
				
			case "==":
				expr += "isub " + "\n"
						+ "ifeq " + l1 + "\n"
						+ "ldc 1" + "\n"
						+ "goto " + lend + "\n"
						+ l1 + ":\n" + "ldc 0" + "\n"
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "isub " + "\n"
						+ "ifne " + l1 + "\n"
						+ "ldc 1" + "\n"
						+ "goto " + lend + "\n"
						+ l1 + ":\n " + "ldc 0" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<=":
				// <(5) Fill here>
				expr += "if_icmple " + l1 + "\n"
						+ "ldc 1\ngoto " + lend + "\n"
						+ l1 + ":\nldc 0\n"
						+ lend + ": \n";
				break;
			case "<":
				// <(6) Fill here>
				expr += "if_icmplt " + l1 + "\n"
						+ "ldc 1\ngoto " + lend + "\n"
						+ l1 + ":\nldc 0\n"
						+ lend + ": \n";
				break;

			case ">=":
				// <(7) Fill here>
				expr += "if_icmpge " + l1 + "\n"
						+ "ldc 1\ngoto " + lend + "\n"
						+ l1 + ":\nldc 0\n"
						+ lend + " : \n";
				break;

			case ">":
				// <(8) Fill here>
				expr += "if_icmpgt " + l1 + "\n"
						+ "ldc 1\ngoto " + lend + "\n"
						+ l1 + ":\nldc 0\n"
						+ lend + " : \n";
				break;

			case "and":
				l2 = symbolTable.newLabel();
				expr +=  "ifne "+ l1 + "\n"
						+ "ifne " + l2 + "\n"
						+ "ldc 0\ngoto " + lend + "\n"
						+ l1 + ":\npop\n"
						+ l2 + ":\nldc 1\n"
						+ lend + ": \n";
				break;
			case "or":
				// <(9) Fill here>
				l2 = symbolTable.newLabel();
				expr +=  "ifeq "+ l1 + "\n"
						+ "ifeq " + l2 + "\n"
						+ "ldc 1\ngoto " + lend + "\n"
						+ l1 + ":\npop\n"
						+ l2 + ":\nldc 0\n"
						+ lend + ": \n";
				break;

		}
		return expr;
	}
	private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
		String fname = getFunName(ctx);		
		String funSpecStr = symbolTable.getFunSpecStr(fname);
		String formalArgType = funSpecStr.substring(funSpecStr.indexOf("(") + 1, funSpecStr.indexOf(")"));
		MiniCParser.ArgsContext args = ctx.args();
		StringBuilder actualArgType = new StringBuilder();
		//parameter type check
		for(MiniCParser.ExprContext exprContext : args.expr()) {
			if(exprContext.IDENT() != null) {
				Type type = symbolTable.getVarType(exprContext.IDENT().getText());
				if(type == Type.INT) {
					actualArgType.append("I");
				}
				else if(type == Type.INTARRAY){
					if(exprContext.children.size() == 4)
						actualArgType.append("I");
					else {
						actualArgType.append("[I");
					}
				}
			}
			if(exprContext.LITERAL() != null) {
				actualArgType.append("I");
			}
		}
		//Invalid Parameter Input then System Exit
		if(!formalArgType.equals(actualArgType.toString())) {
			System.out.println("Invalid argument : " + ctx.getText());
			System.exit(-1);
		}
		if (fname.equals("_print")) {		// System.out.println	
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
			  		+ newTexts.get(ctx.args()) 
			  		+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {	
			expr = newTexts.get(ctx.args()) 
					+ "invokestatic " + getCurrentClassName()+ "/" + symbolTable.getFunSpecStr(fname) + "\n";
		}	
		
		return expr;
			
	}

	// args	: expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "";
		
		for (int i=0; i < ctx.expr().size() ; i++) {
			argsStr += newTexts.get(ctx.expr(i)) ; 
		}		
		newTexts.put(ctx, argsStr);
	}

}

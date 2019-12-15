package listener.main;

import java.util.HashMap;
import java.util.Map;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.Var_declContext;

import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}
	
	static public class VarInfo {
		Type type; 
		int id;
		int initVal;
		
		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}
	
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function 
	
		
	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;
		_lsymtable.clear();
	}
	
	void putLocalVar(String varname, Type type){
		//<Fill here>
		VarInfo localVarInfo = new VarInfo(type, _localVarID++);
		_lsymtable.put(varname, localVarInfo);
	}
	
	void putGlobalVar(String varname, Type type){
		//<Fill here>
		VarInfo globalVarInfo = new VarInfo(type, _globalVarID++);
		_gsymtable.put(varname, globalVarInfo);
	}
	
	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo localVarInfo = new VarInfo(type, _localVarID++, initVar);
		_lsymtable.put(varname, localVarInfo);
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo glovalVarInfo = new VarInfo(type, _globalVarID++, initVar);
		_gsymtable.put(varname, glovalVarInfo);
	}
	
	void putParams(MiniCParser.ParamsContext params) {
		for(MiniCParser.ParamContext ctx : params.param()) {
			Type type = null;
			if(BytecodeGenListenerHelper.getTypeText((MiniCParser.Type_specContext)ctx.getChild(0)).equals("V")) {
				type = Type.VOID;
			}
			else if(ctx.children.size() == 2){
				type = Type.INT;
			}
			else {
				type = Type.INTARRAY;
			}
			putLocalVar(ctx.IDENT().getText(), type);
		}
	}
	
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}
	
	public String getFunSpecStr(String fname) {		
		// <Fill here>
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		String fname = ctx.getChild(1).getText();
		return _fsymtable.get(fname).sigStr;
	}
	
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		if(fname.equals("main")) {
			String res = getFunSpecStr(fname);
			return res;
		}
		if(fname.equals("_print")) {
			String res = getFunSpecStr("_print");
			return res;
		}
		String argtype = BytecodeGenListenerHelper.getParamTypesText((MiniCParser.ParamsContext) ctx.getChild(3));
		String rtype = BytecodeGenListenerHelper.getTypeText((MiniCParser.Type_specContext) ctx.getChild(0));
		StringBuilder res = new StringBuilder("");
		
		// <Fill here>	
		
		res.append(fname)
				.append("(")
				.append(argtype)
				.append(")")
				.append(rtype);
		
		FInfo finfo = new FInfo();
		finfo.sigStr = res.toString();
		_fsymtable.put(fname, finfo);
		
		return res.toString();
	}
	
	String getVarId(String name){
		// <Fill here>
		VarInfo lvar = _lsymtable.get(name);
		if (lvar != null) {
			return Integer.toString(lvar.id);
		}

		VarInfo gvar = _gsymtable.get(name);
		if (gvar != null) {
			//global variable
			return "Test/" + name;
		}

		return null;
	}
	
	Type getVarType(String name){
		VarInfo lvar = _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}
		
		VarInfo gvar = _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}
		
		return Type.ERROR;	
	}
	String newLabel() {
		return "label" + _labelID++;
	}
	
	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		return getVarId(ctx.IDENT().getText());
	}

	// local
	public String getVarId(Local_declContext ctx) {
//		String sname = "";
//		sname += getVarId(ctx.IDENT().getText());
		return getVarId(ctx.IDENT().getText());
	}
	
}


package Parse;

import java.util.ArrayList;
import Absyn.*;
import java.util.Optional;
import Parse.antlr_build.Parse.*;
import org.antlr.v4.runtime.ParserRuleContext;


/*
 * Hello, I assume that you have read the material in gParser.g4
 *
 * This file is your "Visitor". 
 *
 * Your job is to write visit functions for each parse rule in the gParser.g4 
 * file. Each visit function needs to return the corresponding Absyn node.
 *
 * The driver file you have been provided will print whatever is returned from
 * this visitor. If you successfully return the Absyn nodes, you will see them 
 * print in the terminal.
 *
 * If you get stuck of lost: Each context object can be found
 * in gParser.java. Just search "Context".
 *
 * 
*/

public class ASTBuilder extends gParserBaseVisitor<Absyn> {

   @Override
   public Absyn visitProgram(gParser.ProgramContext ctx) {
      DeclList decls = new DeclList(0);
      for (gParser.DeclarationContext dctx : ctx.declaration()) {
        decls.list.add((Decl)visit(dctx));
      }
      return decls;
   }

   @Override
   public Absyn visitVarDecl(gParser.VarDeclContext ctx) {
      Type t = (Type) visit(ctx.type());
      String name = ctx.ID().getText();
      Exp init;
      if (ctx.initialization().ASSIGN() != null) {
         init = (Exp) visit(ctx.initialization().initializer());
      } else {
         init = new EmptyExp(0);
      }
      return new VarDecl(ctx.getStart().getLine(), t, name, init);
   }

   @Override
   public Absyn visitType(gParser.TypeContext ctx) {
      boolean constant = ctx.CONST() != null;
      String name = getTypeName(ctx.type_name());
      int pointerCount = ctx.STAR() != null ? ctx.STAR().size() : 0;
      DeclList brackets = ctx.brackets_list() != null
         ? (DeclList) visit(ctx.brackets_list())
         : new DeclList(0);
      return new Type(ctx.getStart().getLine(), constant, name, pointerCount, brackets);
   }

   private String getTypeName(gParser.Type_nameContext ctx) {
      if (ctx.VOID() != null) return "void";
      if (ctx.INT() != null) return "int";
      if (ctx.STRING() != null) return "string";
      if (ctx.ID() != null) return ctx.ID().getText();
      return "int";
   }

   @Override
   public Absyn visitEmptyArrayBrackets(gParser.EmptyArrayBracketsContext ctx) {
      DeclList list = new DeclList(ctx.getStart().getLine());
      int n = ctx.LSQUARE().size();
      for (int i = 0; i < n; i++) {
         list.list.add(new ArrayType(ctx.getStart().getLine(), new EmptyExp(0)));
      }
      return list;
   }

   @Override
   public Absyn visitExprArrayBrackets(gParser.ExprArrayBracketsContext ctx) {
      DeclList list = new DeclList(ctx.getStart().getLine());
      for (int i = 0; i < ctx.expr().size(); i++) {
         list.list.add(new ArrayType(ctx.getStart().getLine(), (Exp) visit(ctx.expr(i))));
      }
      return list;
   }

   @Override
   public Absyn visitInitializer(gParser.InitializerContext ctx) {
      if (ctx.expr() != null) {
         return (Exp) visit(ctx.expr());
      }
      ExpList list = new ExpList(ctx.getStart().getLine());
      for (gParser.InitializerContext ic : ctx.initializer()) {
         list.list.add((Exp) visit(ic));
      }
      return list;
   }

   @Override
   public Absyn visitDecLit(gParser.DecLitContext ctx) {
      int value = Integer.parseInt(ctx.DECIMAL_LITERAL().getText());
      return new DecLit(ctx.getStart().getLine(), value);
   }

   @Override
   public Absyn visitID(gParser.IDContext ctx) {
      return new ID(ctx.getStart().getLine(), ctx.ID().getText());
   }

}



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
      TypeName tn = (TypeName) visit(ctx.type_name());
      String name = tn.name;
      int pointerCount = ctx.STAR() != null ? ctx.STAR().size() : 0;
      DeclList brackets = ctx.brackets_list() != null
         ? (DeclList) visit(ctx.brackets_list())
         : new DeclList(0);
      return new Type(ctx.getStart().getLine(), constant, name, pointerCount, brackets);
   }

   @Override
   public Absyn visitType_name(gParser.Type_nameContext ctx) {
      String name = getTypeName(ctx);
      return new TypeName(ctx.getStart().getLine(), name);
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

   @Override
   public Absyn visitFunDecl(gParser.FunDeclContext ctx) {
      Type returnType = (Type) visit(ctx.type());
      String name = ctx.ID().getText();
      DeclList params = ctx.parameters() != null
         ? (DeclList) visit(ctx.parameters())
         : new DeclList(ctx.getStart().getLine());
      Stmt body = (Stmt) visit(ctx.statement());
      return new FunDecl(ctx.getStart().getLine(), returnType, name, params, body);
   }

   @Override
   public Absyn visitParameters(gParser.ParametersContext ctx) {
      DeclList list = new DeclList(ctx.getStart().getLine());
      for (int i = 0; i < ctx.type().size(); i++) {
         Type t = (Type) visit(ctx.type(i));
         String paramName = ctx.ID(i).getText();
         list.list.add(new Parameter(ctx.getStart().getLine(), t, paramName));
      }
      return list;
   }

   @Override
   public Absyn visitTypedefDecl(gParser.TypedefDeclContext ctx) {
      Type t = (Type) visit(ctx.type());
      String name = ctx.ID().getText();
      return new Typedef(ctx.getStart().getLine(), t, name);
   }

   @Override
   public Absyn visitStructOrUnionDecl(gParser.StructOrUnionDeclContext ctx) {
      String name = ctx.ID(0).getText();
      DeclList body = new DeclList(ctx.getStart().getLine());
      for (int i = 0; i < ctx.type().size(); i++) {
         Type t = (Type) visit(ctx.type(i));
         String memberName = ctx.ID(i + 1).getText();
         if (ctx.STRUCT() != null) {
            body.list.add(new StructMember(ctx.getStart().getLine(), t, memberName));
         } else {
            body.list.add(new UnionMember(ctx.getStart().getLine(), t, memberName));
         }
      }
      if (ctx.STRUCT() != null) {
         return new StructDecl(ctx.getStart().getLine(), name, body);
      } else {
         return new UnionDecl(ctx.getStart().getLine(), name, body);
      }
   }

   @Override
   public Absyn visitCompStmt(gParser.CompStmtContext ctx) {
      DeclList declList = new DeclList(ctx.getStart().getLine());
      for (gParser.DeclarationContext dctx : ctx.declaration()) {
         declList.list.add((Decl) visit(dctx));
      }
      StmtList stmtList = new StmtList(ctx.getStart().getLine());
      for (gParser.StatementContext sctx : ctx.statement()) {
         stmtList.list.add((Stmt) visit(sctx));
      }
      return new CompStmt(ctx.getStart().getLine(), declList, stmtList);
   }

   @Override
   public Absyn visitIfStmt(gParser.IfStmtContext ctx) {
      Exp cond = (Exp) visit(ctx.expr());
      Stmt thenStmt = (Stmt) visit(ctx.statement());
      return new IfStmt(ctx.getStart().getLine(), cond, thenStmt, new EmptyStmt(ctx.getStart().getLine()));
   }

   @Override
   public Absyn visitIfElseStmt(gParser.IfElseStmtContext ctx) {
      Exp cond = (Exp) visit(ctx.expr());
      Stmt thenStmt = (Stmt) visit(ctx.statement(0));
      Stmt elseStmt = (Stmt) visit(ctx.statement(1));
      return new IfStmt(ctx.getStart().getLine(), cond, thenStmt, elseStmt);
   }

   @Override
   public Absyn visitWhileStmt(gParser.WhileStmtContext ctx) {
      Exp cond = (Exp) visit(ctx.expr());
      Stmt body = (Stmt) visit(ctx.statement());
      return new WhileStmt(ctx.getStart().getLine(), cond, body);
   }

   @Override
   public Absyn visitExprStmt(gParser.ExprStmtContext ctx) {
      Exp e = (Exp) visit(ctx.expr());
      return new ExprStmt(ctx.getStart().getLine(), e);
   }

   @Override
   public Absyn visitReturnStmt(gParser.ReturnStmtContext ctx) {
      Exp value = (Exp) visit(ctx.initializer());
      return new ReturnStmt(ctx.getStart().getLine(), value);
   }

   @Override
   public Absyn visitBreakStmt(gParser.BreakStmtContext ctx) {
      return new BreakStmt(ctx.getStart().getLine());
   }

}


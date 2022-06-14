package com.zain.zag;
import java.util.List;

abstract class Stmt {
	interface Visitor<R>{
		 R visitExpressionStmt(Expression stmt);
 		 R visitPrintStmt(Print stmt);
 		 R visitVarStmt(Var stmt);
 		 R visitBlockStmt(Block stmt);
 		 R visitIfStmt(If stmt);
 		 R visitWhileStmt(While stmt);
 	}
	static class Expression extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitExpressionStmt(this);
		}

		final Expr expression;

		Expression( Expr expression) {
			this.expression = expression;
		}
	}
	static class Print extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitPrintStmt(this);
		}

		final Expr expression;

		Print( Expr expression) {
			this.expression = expression;
		}
	}
	static class Var extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitVarStmt(this);
		}

		final Token name;
		final Expr initializer;

		Var( Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}
	}
	static class Block extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitBlockStmt(this);
		}

		final List<Stmt> statements;

		Block( List<Stmt> statements) {
			this.statements = statements;
		}
	}
	static class If extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitIfStmt(this);
		}

		final Expr condition;
		final Stmt ifCondition;
		final Stmt elseCondition;

		If( Expr condition, Stmt ifCondition, Stmt elseCondition) {
			this.condition = condition;
			this.ifCondition = ifCondition;
			this.elseCondition = elseCondition;
		}
	}
	static class While extends Stmt{

		@Override
		<R> R accept(Visitor<R> visitor) {
				return visitor.visitWhileStmt(this);
		}

		final Expr condition;
		final Stmt body;

		While( Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}
	}

	abstract <R> R accept(Visitor<R> visitor);
}
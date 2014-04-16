package com.apigee.cs.ast;

/**
 * AstException is a base RuntimeException that is used for all AST code.  We just wrap
 * checked exceptions with AstException to keep code clean and simple.  Because in 95% of cases
 * checked exceptions suck.
 * @author rob
 *
 */
public class AstException extends RuntimeException {


	private static final long serialVersionUID = 6461572880235470747L;

	public AstException() {
		super();
		
	}

	public AstException(String message, Throwable cause) {
		super(message, cause);
	
	}

	public AstException(String message) {
		super(message);
	
	}

	public AstException(Throwable cause) {
		super(cause);
	
	}

}

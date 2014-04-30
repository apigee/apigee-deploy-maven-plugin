/**
 * Copyright (C) 2014 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.utils;

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

/**
 * Copyright (C) 2014 Apigee Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.rest;

/**
 * Immutable value object to describe an installable bundle including the name, type and revision number.
 */
public class Bundle {

	private String name;

	private Type type;

	private Long revision;


	public Bundle(String name) {
		this(name, Type.APIPROXY);
	}

	public Bundle(String name, Type type) {
		this(name, type, null);
	}

	public Bundle(String name, Type type, Long revision) {
		this.name = name;
		this.type = type;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Bundle)) {
			return false;
		}

		Bundle other = (Bundle) obj;
		return (this.name.equals(other.name) && this.type.equals(other.type))
				&& (
				(this.revision == null && other.revision == null) ||
						(this.revision != null && this.revision.equals(other.revision)) ||
						(other.revision != null && other.revision.equals(this.revision)));
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Long getRevision() {
		return revision;
	}

	@Override
	public Bundle clone() {
		return new Bundle(this.name, this.type, this.revision);
	}

	public Bundle clone(Long revision) {
		return new Bundle(this.name, this.type, revision);
	}

	public String getPathName() {
		return this.type.getPathName();
	}

	public enum Type {

		APIPROXY("apis"),

		SHAREDFLOW("sharedflows");

		private final String pathName;

		Type(String pathName) {
			this.pathName = pathName;
		}

		public static Type valueOfName(String name) {
			for (Type type : Type.values()) {
				if (type.pathName.equalsIgnoreCase(name)) {
					return type;
				}
			}
			return null;
		}

		protected String getPathName() {
			return pathName;
		}
	}

}

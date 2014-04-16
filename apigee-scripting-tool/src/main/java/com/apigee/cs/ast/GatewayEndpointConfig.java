package com.apigee.cs.ast;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sonoa.security.util.EncryptionUtil;

@XmlRootElement(namespace = XmlConstants.NS)

/**
 * GatewayEndpointConfig provides a means of resolving configuration (indirectly) to Gateway/SMS endpoints.  We then use this
 * information to configure SOI.
 * @author rob
 *
 */
public class GatewayEndpointConfig {

	Logger log = LoggerFactory.getLogger(GatewayEndpointConfig.class);
	private String hostname = "myhost";
	private int port = 9080;
	private boolean secure;
	private String username;

	private String password;
	private String encryptedPassword;

	private String sshUsername;
	private String sshPassword;
	private int sshPort = 22;
	private String id=UUID.randomUUID().toString();
	private String description;

	@XmlElement(namespace = XmlConstants.NS)
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets password in plain text.
	 * @return
	 */
	protected String getCTP() {
		if (password == null) {
			return null;
		} else if (password.startsWith("{DES}")) {

			return EncryptionUtil.decrypt(password.substring("{DES}".length()));
		} else {
			String cipherText = EncryptionUtil.encrypt(password);
			log.warn("Please consider using encrypted password: {DES}"
					+ cipherText);
			return password;
		}
	}

	/** 
	 * Constructs and returns a URL to connect to the SOI endpoint.
	 * @return
	 */
	public String getManagementUrl() {
		if (getHostname() == null || getHostname().trim().length() < 1) {
			throw new AstException(
					"Management hostname not set.  Consider setting AST_HOSTNAME or passing -Dast.hostname");
		}
		return (isSecure() ? "https://" : "http://") + getHostname() + ":"
				+ getPort();
	}

	public void validateConfig() {

		if (isEmpty(getPassword())) {
			throw new AstException("ast.password not set");
		}
		if (isEmpty(getUsername())) {
			throw new AstException("ast.username not set");
		}

	}

	boolean isEmpty(String s) {

		return s == null || s.trim().length() == 0;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public String getSshUsername() {
		return sshUsername;
	}

	public void setSshUsername(String sshUsername) {
		this.sshUsername = sshUsername;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public String getSshPassword() {
		return sshPassword;
	}

	public void setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
	}

	@XmlElement(namespace = XmlConstants.NS)
	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	@XmlElement(namespace = XmlConstants.NS)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

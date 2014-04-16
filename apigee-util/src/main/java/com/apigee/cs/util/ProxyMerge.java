package com.apigee.cs.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ProxyMerge {


	private static Node getTopLevelFaultRules(Document doc){
		NodeList childNodes = doc.getElementsByTagName("FaultRules");
		if(childNodes == null || childNodes.getLength() < 1){
			return null;
		}
		for(int i=0;i<childNodes.getLength();i++){
			Node child = childNodes.item(i);
			if(child.getParentNode() != null && child.getParentNode().getNodeName() != null){
				if(child.getParentNode().getNodeName().equals("ProxyEndpoint") 
						|| child.getParentNode().getNodeName().equals("TargetEndpoint")){
					return child;
				}
			}
		}
		return null;
	}

	private static final Node mergeFlowElements(Node a, Node b, Document bDoc, Node bLastNode){

		if(a == null && b == null){
			return bLastNode;
		}
		if(b == null && a != null){
			Node importedA = bDoc.importNode(a, true);
			bLastNode.getParentNode().insertBefore(importedA, bLastNode);
			return importedA;
		}
		if(b != null && a == null){
			return b;
		}

		Node aDesc = DOMUtil.getChildElementWithName(a,"Description");
		Node aRequest = DOMUtil.getChildElementWithName(a,"Request");
		Node aResponse = DOMUtil.getChildElementWithName(a,"Response");

		Node bDesc = DOMUtil.getChildElementWithName(b,"Description");
		Node bRequest = DOMUtil.getChildElementWithName(b,"Request");
		Node bResponse = DOMUtil.getChildElementWithName(b,"Response");

		bLastNode = DOMUtil.mergeSubElements(aResponse, bResponse, bDoc,null,b);	
		bLastNode = DOMUtil.mergeSubElements(aRequest, bRequest, bDoc, bLastNode,b);	
		bLastNode = DOMUtil.mergeSubElements(aDesc, bDesc, bDoc, bLastNode,b);	

		return bLastNode;
	}

	private static final void mergeEndPoints(File a, File b, File output){

		InputStream inputStreamA = null, inputStreamB = null;
		OutputStream outputStream = null;
		try{
			inputStreamA = new FileInputStream(a);
			Document docA = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStreamA);

			inputStreamB = new FileInputStream(b);
			Document docB = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStreamB);

			outputStream = new FileOutputStream(output);

			Node aPreFlow = DOMUtil.getFirstChildElement(docA,"PreFlow");
			Node aPostFlow = DOMUtil.getFirstChildElement(docA,"PostFlow");
			Node aFlows = DOMUtil.getFirstChildElement(docA,"Flows");
			Node aFaultRules = getTopLevelFaultRules(docA);

			Node bProxyEndpoint = DOMUtil.getFirstChildElement(docB,"ProxyEndpoint");
			Node bTargetEndpoint = DOMUtil.getFirstChildElement(docB,"TargetEndpoint");
			Node bPreFlow = DOMUtil.getFirstChildElement(docB,"PreFlow");
			Node bPostFlow = DOMUtil.getFirstChildElement(docB,"PostFlow");
			Node bFlows = DOMUtil.getFirstChildElement(docB,"Flows");
			Node bFaultRules = getTopLevelFaultRules(docB);

			Node bLastNode = bProxyEndpoint == null? DOMUtil.getFirstChildElement(docB,"HTTPTargetConnection"):DOMUtil.getFirstChildElement(docB,"HTTPProxyConnection");
			Node bEndPoint = bProxyEndpoint == null? bTargetEndpoint : bProxyEndpoint;
			bLastNode = DOMUtil.mergeSubElements(aFaultRules, bFaultRules, docB, bLastNode,bEndPoint); 
			bLastNode = DOMUtil.mergeSubElements(aFlows,bFlows, docB, bLastNode,bEndPoint);
			bLastNode = mergeFlowElements(aPostFlow,bPostFlow,docB, bLastNode);
			bLastNode = mergeFlowElements(aPreFlow,bPreFlow,docB, bLastNode);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(outputStream);
			Source input = new DOMSource(docB);
			transformer.transform(input, result);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(inputStreamA != null){try{inputStreamA.close();}catch(Exception ignore){}}
			if(inputStreamB != null){try{inputStreamB.close();}catch(Exception ignore){}}	
			if(outputStream != null){try{outputStream.flush();outputStream.close();}catch(Exception ignore){}}			
		}

	}

	private static final void mergeEndpointXMLs(File xmlA, List<File> xmlBList, File targetDir) throws IOException{

		for(File xmlB:xmlBList){
			//mergeEndPointXMLs(proxyAProxyEndpoint,proxyBProxyEndpointList,targetDir);
			File targetXML = new File(targetDir,xmlB.getName()); 
			if(targetXML.exists()) targetXML.delete();
			targetXML.createNewFile();
			mergeEndPoints(xmlA, xmlB, targetXML);
		}

	}

	private static final List<File> getListOfXmls(File dir, String excludeFileNames){
		List<File> xmlList = new ArrayList<File>();
		File[] fileList = dir.listFiles();
		for(File f:fileList){
			System.out.println("Adding [" + f.getName() + "] to merge, exclude [" + excludeFileNames + "]");
			if(f.getName().toLowerCase().endsWith(".xml") && !excludeFileNames.toLowerCase().contains(f.getName().toLowerCase())) 
				xmlList.add(f);
		}
		System.out.println("Final File list:[" + xmlList + "]");
		return xmlList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try{
			if(args.length < 3){
				System.out.println("Usage: ProxyMerge proxyADir proxyBDir dirForAMergedWithB excludeFileList");
				return;
			}

			File proxyA = new File(args[0]);
			File proxyB = new File(args[1]);
			File mergedProxy = new File(args[2]);
			String excludeFileList = (args.length > 3)? args[3] : "";

			if(args[0].equals(args[2]) || args[1].equals(args[2])){
				System.out.println("Merged proxy directory should be different from proxy A and proxy B");
				return;
			}

			if(proxyA.isFile() || proxyB.isFile() || mergedProxy.isFile()){
				System.out.println("All Input parameters should be top level directories for Proxies");
				return;
			}

			if(mergedProxy.exists()){
				System.out.println("directory for merged proxy already exists. Deleting it...");
				FileUtil.recursiveDelete(mergedProxy);
			}
			System.out.println("Starting the merge of \n proxy @ '" + args[0] + "' \n with proxy @ '" 
					+ args[1] + "'. \n Merged Proxy will be stored @ '" + args[2] + "'");

			mergedProxy.mkdir();
			File proxyAResources = new File(args[0] + "/apiproxy/resources");
			File proxyASteps = new File(args[0] + "/apiproxy/stepdefinitions");

			File mergedProxyResources = new File(args[2] + "/apiproxy/resources");
			File mergedProxySteps = new File(args[2] + "/apiproxy/stepdefinitions");		

			FileUtil.copyFolder(proxyB, mergedProxy);
			if(excludeFileList != null && excludeFileList.equalsIgnoreCase("NOMERGE") == false) {
				FileUtil.copyFolder(proxyAResources,mergedProxyResources);
				FileUtil.copyFolder(proxyASteps,mergedProxySteps);

				File proxyAProxyEndpoint = new File(args[0] + "/apiproxy/proxies/default.xml");
				File proxyATargetEndpoint = new File(args[0] + "/apiproxy/targets/default.xml");

				List<File> proxyBProxyEndpointList  = getListOfXmls(new File(args[1] + "/apiproxy/proxies"),excludeFileList);
				List<File> proxyBTargetEndpointList = getListOfXmls(new File(args[1] + "/apiproxy/targets"),excludeFileList);

				if(proxyAProxyEndpoint.exists()){
					File targetDir = new File(args[2] + "/apiproxy/proxies");
					if(proxyBProxyEndpointList.isEmpty()){
						//FileUtil.copyFile(proxyAProxyEndpoint, new File(targetDir,proxyAProxyEndpoint.getName()));
					}else{
						mergeEndpointXMLs(proxyAProxyEndpoint,proxyBProxyEndpointList,targetDir);
					}
				}

				if(proxyATargetEndpoint.exists()){
					File targetDir = new File(args[2] + "/apiproxy/targets");
					if(proxyBTargetEndpointList.isEmpty()){
						//FileUtil.copyFile(proxyATargetEndpoint, new File(targetDir,proxyATargetEndpoint.getName()));
					}else{
						mergeEndpointXMLs(proxyATargetEndpoint,proxyBTargetEndpointList,targetDir);
					}
				}
			}else {
				System.out.println("*******************************************************************");
				System.out.println("Proxy [" + args[1] + "] marked for NOMERGE, Skipping merge steps...");
				System.out.println("*******************************************************************");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}

package com.apigee.cs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

	
	public static final void recursiveDelete(File f){
		if(f == null)
			return;
		File[] containedFiles = f.listFiles();
		if(containedFiles != null && containedFiles.length > 0){
			for(File cF:containedFiles){
				recursiveDelete(cF);
			}
		}
		f.delete();
	}
	
	
	 public static final void copyFolder(File src, File dest)
		    	throws Exception{
		 
		    	if(src.isDirectory()){
		 
		    		//if directory not exists, create it
		    		if(!dest.exists()){
		    		   dest.mkdir();
		    		   
		    		}
		 
		    		//list all the directory contents
		    		String files[] = src.list();
		 
		    		for (String file : files) {
		    		   //construct the src and dest file structure
		    		   File srcFile = new File(src, file);
		    		   File destFile = new File(dest, file);
		    		   //recursive copy
		    		   copyFolder(srcFile,destFile);
		    		}
		 
		    	}else{
		    		
		    		if(dest.exists()){
		    		  System.err.println("file: '" + src.getPath() + "' already exists in the destination");	
		    		}else{
		    		//if file, then copy it
		    		//Use bytes stream to support all file types
		    			copyFile(src,dest);
		    		}   
		    	}
		    }
	
	public static final void copyFile(File src, File dest) throws Exception{
	    InputStream in = null;
        OutputStream out = null;
        try{
	        in = new FileInputStream(src);
	        out = new FileOutputStream(dest); 
	
	        byte[] buffer = new byte[1024];
	
	        int length;
	        //copy the file content in bytes 
	        while ((length = in.read(buffer)) > 0){
	    	   out.write(buffer, 0, length);
	        }
	        

        }finally{
        	try{in.close();}catch(Exception ignore){}
        	try{out.close();}catch(Exception ignore){}
        }
	}

}

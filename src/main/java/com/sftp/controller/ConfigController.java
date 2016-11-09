package com.sftp.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcraft.jsch.SftpException;
import com.sftp.util.RemoteShellTool;
import com.sftp.util.SFTPUtil;
import com.sftp.util.ZipFileUtil;


@Controller
@RequestMapping("/config")
public class ConfigController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String host ="192.168.117.128"; //10.200.1.201
	
	private String username ="root";
	
	private String password = "root";//3PJhDE
	
	private int port = 22;
	
	private String serverPath = new File(getClass().getResource("/").getFile()).getParentFile().getParentFile().getAbsolutePath() + "/temp/server/";
	
	private String templatePath = getClass().getResource("/").getFile() + "/template/";
	
	private String savePath = new File(getClass().getResource("/").getFile()).getParentFile().getParentFile().getAbsolutePath() + "/temp/save/";
	
	@RequestMapping("/create")
	public @ResponseBody JSONObject create(String name,String s_port,String c_port,boolean flag) throws ParserConfigurationException, SAXException, IOException{
		JSONObject result = new JSONObject();
		SFTPUtil sftp = new SFTPUtil(username, password, host, port);
		sftp.login();//登录
		try {
			if(!flag){
				Vector vectors = sftp.listFiles("/opt/apache-tomcat-payment*");
				logger.info("size:" + vectors.size());
				logger.info("====开始拉取文件=======");
				File serverFile = new File(serverPath);
				serverFile.deleteOnExit();
				serverFile.mkdirs();
				for(int i = 0; i<vectors.size();i++){
					String[] names = vectors.get(i).toString().split(" ");
					String fileName = names[names.length -1];//文件名称 
					String prefix = fileName.split("_")[1];
					try {
						sftp.download("/opt/" + fileName + "/conf/", "server.xml", serverPath + prefix + "_server.xml");
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
				}
				logger.info("====结束拉取文件=======");
				File[] files = new File(serverPath).listFiles();//列出所有文件
				
				if(files != null){
					DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				    DocumentBuilder  builder = builderFactory.newDocumentBuilder();
					for(File f :files){
					    Document doc = builder.parse(f);
					    String applicationName = f.getName().split("_")[0];
					    Element root = doc.getDocumentElement();
					    Node node = root.getElementsByTagName("Connector").item(0);
					    String content = applicationName + "_" + root.getAttribute("port") + "_" + node.getAttributes().getNamedItem("port").getNodeValue();
					    if(content.contains(name) || content.contains(s_port) || content.contains(c_port)){
					    	result.put("flag", false);
					    	result.put("message", "applicationName:" + applicationName + ",serverPort:" + root.getAttribute("port") + ",c_port:" 
					    			+ node.getAttributes().getNamedItem("port").getNodeValue());
					    	return result;
					    }
					}
				}
			}
			
			logger.info("====处理文件开始=======");
			boolean f = dealFile(name,s_port,c_port);
			
			result.put("flag", true);
	    	result.put("message", "生成成功");
	    	ZipFileUtil.compressFoldToZip(savePath + name, savePath + name + ".zip");
			logger.info("====处理文件结束：" + f);
		} catch (SftpException e) {
			e.printStackTrace();
		}
		sftp.logout();//登出
		return result;
	}


	private boolean dealFile(String name, String s_port, String c_port) {
		File template = new File(templatePath);
		for(File file :template.listFiles()){
			File destFile = new File(savePath + name + File.separator + file.getName().replace("{0}", name));
			logger.info("destFile:" + destFile.getName());
			if(file.isDirectory()){
				try {
					copy(destFile,file,s_port,c_port,name);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}else if(file.isFile()){//处理重启脚本
				try {
					BufferedReader reader = new BufferedReader( new FileReader(file));
					String content = "",readLine;
					while((readLine = reader.readLine())!= null){
						content += readLine + "\n";
					}
					reader.close();
					logger.info("读取的文件内容：" + content);
					BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
					writer.append(content.replaceAll("\\{0\\}", name));
					writer.flush();
					writer.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return true;
	}


	private void copy(File destFile, File sourceFile, String s_port, String c_port,String name) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		destFile.deleteOnExit();
		destFile.mkdirs();
		for(File file :sourceFile.listFiles()){
			if(file.isFile()){
				FileUtils.copyFile(file, new File(destFile.getAbsoluteFile() + File.separator + file.getName()));
				if(file.getName().equals("server.xml")){//处理xml中的内容
					File serverXml = new File(destFile.getAbsoluteFile() + File.separator + file.getName());
					DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				    DocumentBuilder  builder = builderFactory.newDocumentBuilder();
				    Document document = builder.parse(serverXml);
				    Element root = document.getDocumentElement();
				    root.setAttribute("port", s_port);
				    Node node = root.getElementsByTagName("Connector").item(0);
				    node.getAttributes().getNamedItem("port").setNodeValue(c_port);
				    NodeList contexts = root.getElementsByTagName("Context");
				    for(int i = 0; i<contexts.getLength();i++){
				    	Node context = contexts.item(i).getAttributes().getNamedItem("docBase");
				    	logger.info("===docbase====" + context.getNodeValue().replace("{0}", name));
				    	context.setNodeValue(context.getNodeValue().replace("{0}", name));
				    }
				    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		            Transformer transformer = transformerFactory.newTransformer();
		            DOMSource source = new DOMSource(document);
		            StreamResult result = new StreamResult(serverXml);
		            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		            transformer.transform(source, result);
		            logger.info("XML file updated successfully");
				}
			}else if(file.isDirectory()){
				copy(new File(destFile.getAbsoluteFile() + File.separator + file.getName()),file,s_port,c_port,name);
			}
		}
	}
	
	@RequestMapping("/upload")
	public @ResponseBody JSONObject upload(String name){
		JSONObject result = new JSONObject();
		File file = new File(savePath + name);
		if(file.exists()){
			SFTPUtil sftp = new SFTPUtil(username, password, host, port);
			sftp.login();//登录
			try {
				sftp.upload("/opt", savePath + name + ".zip");
			} catch (FileNotFoundException | SftpException e) {
				e.printStackTrace();
			}
			sftp.logout();
			 RemoteShellTool tool = new RemoteShellTool(host, username,  
		                password, "utf-8"); 
			logger.info(("start===="));
			logger.info(tool.exec("cd /opt && rm -rf apache-tomcat-payment_" + name + " && rm -rf restart_payment_" + name + ".sh"));
			logger.info(("middle===="));
			logger.info(tool.exec("cd /opt && unzip " + name + ".zip"));
			logger.info(("end===="));
			logger.info(tool.exec("cd /opt && rm -rf " + name + ".zip"));
			result.put("flag", true);
			result.put("message", "文件上传成功");
		}else{
			result.put("flag", false);
			result.put("message", "文件上传失败！");
		}
		return result;
	}
	
	
	@RequestMapping("/start")
	public @ResponseBody JSONObject start(String name){
		JSONObject result = new JSONObject();
		 RemoteShellTool tool = new RemoteShellTool(host, username,  
	                password, "utf-8"); 
		logger.info(tool.exec("cd /opt && sh ./restart_payment_" + name + ".sh"));
		result.put("flag", true);
		return result;
	}
	
}

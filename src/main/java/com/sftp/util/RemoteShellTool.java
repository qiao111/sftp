package com.sftp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
  
public class RemoteShellTool {  
  
	private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection conn;  
    private String ipAddr;  
    private String charset = Charset.defaultCharset().toString();  
    private String userName;  
    private String password;  
  
    public RemoteShellTool(String ipAddr, String userName, String password,  
            String charset) {  
        this.ipAddr = ipAddr;  
        this.userName = userName;  
        this.password = password;  
        if (charset != null) {  
            this.charset = charset;  
        }  
    }  
  
    public boolean login() throws IOException {  
        conn = new Connection(ipAddr);  
        conn.connect(); // 连接  
        return conn.authenticateWithPassword(userName, password); // 认证  
    }  
  
    public String exec(String cmds) {  
        InputStream in = null;  
        String result = "";  
        try {  
            if (this.login()) {  
            	logger.info("登录成功");
                Session session = conn.openSession(); // 打开一个会话  
                logger.info("执行命令：" + cmds);
                session.execCommand(cmds);  
                in = session.getStdout();  
                result = this.processStdout(in, this.charset);  
                logger.info("执行结果：" + result);
                session.close();  
                conn.close();  
            }  
        } catch (IOException e1) {  
            e1.printStackTrace();  
        }  
        return result;  
    }  
  
    public String processStdout(InputStream in, String charset) {  
      
        byte[] buf = new byte[1024];  
        StringBuffer sb = new StringBuffer();  
        try {  
            while (in.read(buf) != -1) {  
                sb.append(new String(buf, charset));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return sb.toString();  
    }  
  
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  
  
        RemoteShellTool tool = new RemoteShellTool("192.168.27.41", "hadoop",  
                "hadoop", "utf-8");  
  
        String result = tool.exec("./test.sh xiaojun");  
        System.out.print(result);  
  
    }  
  
}  
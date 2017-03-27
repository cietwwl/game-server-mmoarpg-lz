package com.pwrd.windows;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.alibaba.fastjson.JSONObject;
import com.pwrd.excel.CustomRun;

public class DropDragJFrame extends JFrame implements DropTargetListener{
	
	public JTextArea showMsg;
	public String path = System.getProperty("user.dir");
	
	public DropDragJFrame(){
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		showMsg.setText("\n \t生成 <客户端> <服务器> <手机端> 文件");
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		showMsg.setText("");
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		JSONObject json = MainWindow.readConfigFile();
		String pathServer = json.getString("serverPath");
		String pathClient = json.getString("clientPath");
		String pathPhone = json.getString("phonePath");

		String entityPackage = json.getString("entityPackage");
		String pathConf = json.getString("confPath");
		String pathFreemarker = json.getString("freemakerPath");
		
		
		showMsg.setText("");
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            
            try {
				@SuppressWarnings("unchecked")
				List<File> list = (List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
				File[] files = (File[])list.toArray();
				//客户端
				CustomRun client = new CustomRun("client", showMsg, files, path + "/" + pathConf, path + "/" + pathClient,
						path + "/" + pathFreemarker, entityPackage);
				client.run();
				
				//服务器
				CustomRun server = new CustomRun("server", showMsg, files, path + "/" + pathConf, path + "/" + pathServer,
						path + "/" + pathFreemarker, entityPackage);
				server.run();
				
				//手机端
				CustomRun phone = new CustomRun("phone", showMsg, files, path + "/" + pathConf, path + "/" + pathPhone,
						path + "/" + pathFreemarker, entityPackage);
				phone.run();
				
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
        }
	}
}

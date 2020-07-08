package com.efel.imgrecoverservice.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.efel.imgrecoverservice.config.AppConfiguration;
import com.efel.imgrecoverservice.domain.PicImage;
import com.efel.imgrecoverservice.domain.RecoverLog;
import com.efel.imgrecoverservice.repo.ImageRepository;
import com.efel.imgrecoverservice.repo.RecoverLogRepository;
import com.efel.imgrecoverservice.web.IndexController;

@Service
public class ImageServiceImpl implements ImageService{
	
	private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
	
	private static final String CONTEXT_PATH = "/iupload/";
	private static final String COMPRESS_DIR = "small";
	private static final String WALMART_ACCOUNT_ID = "1121";
	private static final String WALMART_ACCOUNT_NAME = "walmart_iefiel";
	private static final String WALMART_ACCOUNT_PLATFORM = "WALMART";
	private static final String WALMART_FLAG = "wal";
	private static final Pattern SKU_PATTERN = Pattern.compile("^[0-9]{8}");

	@Autowired
	private AppConfiguration appCfg;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private RecoverLogRepository recoverLogRepository;

	@Override
	public String process() throws Exception {
		logger.info("配置信息：" + appCfg);
		File dir = new File(appCfg.getImageSrcDir());
		File[] files = dir.listFiles();
		int size = files.length;
		File file = null;
		long t1 = System.currentTimeMillis();
		int startIdx = appCfg.getStartIndex();
		for(int i=startIdx;i<size;i++) {
			file = files[i];
			if(file.isDirectory() || !file.exists()) continue;
//			System.out.println("name=" + file.getName() + ";abs path=" + file.getAbsolutePath() + ";length=" + file.length());
			saveImageInfo(file,i);
		}
		long t2 = System.currentTimeMillis();
		return (t2 - t1) + "";
	}
	
	private void saveImageInfo(File file,int idx) throws Exception{
		logger.info("----开始处理第[" + idx + "]张图片,name=" + file.getName());
		try {
			PicImage img = constructImgageInfo(file);
			imageRepository.save(img);
		}catch (Exception e) {
			logger.error("处理图片失败第[" + idx + "]张图,name=" + file.getName(),e);
			try {
				RecoverLog errLog = new RecoverLog();
				errLog.setFileName(file.getName());
				String msg = e.getMessage();
				errLog.setMessage(msg != null && msg.length() > 2000 ? msg.substring(0, 2000) : msg);
				recoverLogRepository.save(errLog);
			}catch (Exception ee) {
				logger.error("失败时处理失败信息失败，name=" + file.getName(),ee); 
			}
			
		}
		logger.info("---结束处理第[" + idx + "]张图片,name=" + file.getName());
	}
	
	private PicImage constructImgageInfo(File file) throws Exception{
		PicImage img = new PicImage();
		String name = file.getName();
		img.setName(name);
		img.setUrl(appCfg.getUrlPrefix() + CONTEXT_PATH + name);
		img.setExtension(getFileExtension(name));
		img.setAbsolutePath(file.getAbsolutePath());
		img.setRelativePath(name);
		img.setUploadDate(new Date(file.lastModified()));
		img.setSmallAbsolutePath(getSmallAbsPath(file));
		img.setSmallRelativePath(getSmallRelativePath(file));
		img.setSmallUrl(appCfg.getUrlPrefix() + CONTEXT_PATH + img.getSmallRelativePath());
		if(isMatchWalmart(name)) { // walmart
			img.setAccountId(WALMART_ACCOUNT_ID);
			img.setAccountName(WALMART_ACCOUNT_NAME);
			img.setPlatform(WALMART_ACCOUNT_PLATFORM);
		}
		setImageWidthHeightAndSize(img,file);
		return img;
	}
	
	private String getFileExtension(String fileName) {
		if (fileName == null || "".equals(fileName)) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}
	
	private String getSmallAbsPath(File srcFile) {
		String relativePath = getSmallRelativePath(srcFile);
		return srcFile.getParentFile().getAbsolutePath() + File.separator + relativePath;
	}
	private String getSmallRelativePath(File srcFile) {
		String fileName = srcFile.getName(), name = null, suffix = null;
		name = fileName.substring(0, fileName.lastIndexOf("."));
		suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		// 相对路径
		return COMPRESS_DIR + File.separator + name + "_small." + suffix;
	}
	
	protected void setImageWidthHeightAndSize(PicImage vo,File file) throws Exception{
		if(file == null || !file.exists()){
			throw new RuntimeException("设置图片的宽、高、大小失败，图片文件不存在");
		}
		BufferedImage image = null;
		try{
			image = ImageIO.read(file);
			vo.setWidth(image.getWidth());
			vo.setHeight(image.getHeight());
			vo.setLength(file.length());
		}catch(Exception e){
			logger.error("获取图片" + file.getName() + "的尺寸大小失败", e);
			throw new RuntimeException(e);
		}finally{
			if(image != null){
				image.flush();
			}
			image = null;
		}
 	}
	
	private boolean isMatchWalmart(String name) {
		return isMatchSku(name) && name.toLowerCase().contains(WALMART_FLAG);
	}
	
	// 是否符合SKU
	private boolean isMatchSku(String name) {
		return SKU_PATTERN.matcher(name).lookingAt();
	}
	
}

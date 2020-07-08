package com.efel.imgrecoverservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efel.imgrecoverservice.service.ImageService;

@RestController
@RequestMapping
public class IndexController {
	
	private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

	
	@Autowired
	private ImageService imageService;
	
	@RequestMapping
	public String index() {
		return "hello recover images service";
	}
	
	@RequestMapping("/process")
	public String process() {
		try {
			String time = imageService.process();
			String msg =  "处理完成：耗时=" + time + " 毫秒 ";
			logger.info(msg);
			return msg;
		}catch (Exception e) {
			logger.error("处理失败",e);
		}
		return "fail";
	}

}

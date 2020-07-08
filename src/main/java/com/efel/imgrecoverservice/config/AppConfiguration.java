package com.efel.imgrecoverservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@NoArgsConstructor
public class AppConfiguration {

	private String imageSrcDir;
	private String urlPrefix;
	private int startIndex = 0;
	
	
	@Override
	public String toString() {
		return "imageSrcDir=" + imageSrcDir + ";urlPrefix=" + urlPrefix + ";startIndex = " + startIndex;
	}
}

package com.gwall.core.eye.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * jackson转换工具类
 * @author yangkai
 *
 */
public class JacksonUtils {
	public static final Logger LOGGER = Logger.getLogger(JacksonUtils.class);
	private static ObjectMapper mapper = new ObjectMapper() ;
	
	/**
	 * 把对象序列为字符串
	 * @param value
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String toJson(Object value) {
		try {
			return mapper.writeValueAsString(value) ;
		} catch (JsonProcessingException e) {
			LOGGER.info(null, e);
		}
		
		return null ;
	}
	
	/**
	 * 把对象序列化进outputstream
	 * @param value
	 * @param outputStream
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static void toOutputStream(Object value , OutputStream outputStream) {
		try {
			mapper.writeValue(outputStream, value);
		} catch (IOException e) {
			LOGGER.info(null, e);
		}
	}
	
	/**
	 * 把json字符串反序列化为对象
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJson(String json,Class<T> clazz) {
		T jackson = null;
		if (StringUtils.isNotEmpty(json)) {
			try {
				jackson = mapper.readValue(json, clazz) ;
			} catch (IOException e) {
				LOGGER.info(null, e);
			}
		}
		
		return jackson ;
	}
}

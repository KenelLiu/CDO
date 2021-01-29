package com.cdoframework.cdolib.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cdoframework.cdolib.base.Return;

/**
 * 缓存工具类
 * 
 * @author zzj
 *
 */
public class CacheUtil {

	/**
	 * 处理缓存
	 * 
	 * @param request
	 * @param response
	 */
	public static void handleReqCache(HttpServletRequest request, HttpServletResponse response, Return ret ) {
		String reqCache = request.getParameter("reqCache");
		long time = getCacheTime(reqCache);
		if (time <= 0L || ret == null || ret.getCode() != 0) {
			//异步请求，默认不缓存，请求返回前的最后设置，会子类覆盖之前的设置。
			response.setHeader("Cache-control","no-cache,no-store");
		}else {
			response.setHeader("Cache-Control", "max-age=" + time);
		}
	}

	/**
	 * 获取缓存的时间
	 * 
	 * 月按照30天计算
	 * 年按照365天计算
	 * 
	 * @param reqCache 待处理参数
	 * @return 缓存时间(秒)
	 */
	private static long getCacheTime(String reqCache) {
//		if (StringUtils.isBlank(reqCache)) {
//			return 0L;
//		}
//		if (StringUtils.isNumeric(reqCache)) {
//			return NumberUtils.toLong(reqCache);
//		}
//		reqCache = reqCache.toUpperCase();
//		if (Pattern.matches("([0-9]+)H+", reqCache)) {//时			
//			return NumberUtils.toLong(StringUtils.substringBefore(reqCache, "H")) * 60 * 60;
//		} else if (Pattern.matches("([0-9]+)D+", reqCache)) {//天
//			return NumberUtils.toLong(StringUtils.substringBefore(reqCache, "D")) * 60 * 60 * 24;
//		} else if (Pattern.matches("([0-9]+)W+", reqCache)) {//周
//			return NumberUtils.toLong(StringUtils.substringBefore(reqCache, "W")) * 60 * 60 * 24 * 7;
//		} else if (Pattern.matches("([0-9]+)M+", reqCache)) {//月
//			return NumberUtils.toLong(StringUtils.substringBefore(reqCache, "M")) * 60 * 60 * 24 * 30;
//		} else if (Pattern.matches("([0-9]+)Y+", reqCache)) {//年
//			return NumberUtils.toLong(StringUtils.substringBefore(reqCache, "Y")) * 60 * 60 * 24 * 365;
//		}
		return 0L;
	}
}

package org.pigeon.http;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.pigeon.string.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class CatchUtil {

	private static Logger logger = Logger.getLogger(CatchUtil.class);

	/**
	 * 设置请求参数
	 * @param map a
	 * @return
	 */
	private static List<NameValuePair> setHttpParams(Map<String, Object> map) {
		if(map == null){
			return null;
		}
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		Set<Map.Entry<String, Object>> set = map.entrySet();
		for (Map.Entry<String, Object> entry : set) {
			formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
		}
		return formParams;
	}

    /**
     * 设置请求参数
     * @param list
     * @return
     */
    private static List<NameValuePair> setHttpParams(List<String[]> list) {
        if(list == null){
            return null;
        }
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (String [] obj : list) {
            if(obj != null && obj.length == 2)
				formParams.add(new BasicNameValuePair(StringUtils.toString(obj[0]), StringUtils.toString(obj[1])));
        }
        return formParams;
    }

	/**
	 * 获得响应HTTP实体内容
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private static String GetHttpEntityContent(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream is = entity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = br.readLine();
			StringBuilder sb = new StringBuilder();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		}
		return "";
	}


	public static String post(String url, Map<String, Object> map) throws ClientProtocolException, IOException{

		return post(url, map, null);

	}
	/**
	 * 远程调用服务POST方法
	 * @param url 服务地址
	 * @param map 传递参数
	 * @return 服务器返回点JSON字符串
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String post(String url, Map<String, Object> map, Map<String, Object> headers) throws ClientProtocolException, IOException {


		return post(url, null, map, headers, false, null);
	}

	public static String post(String url, String msg) throws ClientProtocolException, IOException{

		return post(url, msg, null, false, null);
	}
	public static String post(String url, String msg, Map<String, Object> headers) throws ClientProtocolException, IOException{

		return post(url, msg, headers, false, null);
	}
	//	    public static String post(String url, String msg, Map<String, String> headers, boolean needDecode, String charset) {
//
//		return post(url, msg, headers, needDecode, charset);
//	    }
	public static String post(String url, String msg, Map<String, Object> headers, boolean needDecode, String charset) throws ClientProtocolException, IOException {

		return post(url, msg, null, headers, needDecode, charset);
	}

	public static String post(String url, String msg, Map<String, Object> postParams, Map<String, Object> headers, boolean needDecode, String charset) throws ClientProtocolException, IOException {

		return post(url, msg, postParams, headers, needDecode, charset, false);
	}
		/**
		 * msg优先
		 */
	public static String post(String url, String msg, Map<String, Object> postParams, Map<String, Object> headers, boolean needDecode, String charset, boolean multipart) throws ClientProtocolException, IOException {

		return post(new DefaultHttpClient(), url, msg, postParams,headers, needDecode, charset, multipart);
	}

    /**
     * msg优先
     */
    public static String post(HttpClient httpclient, String url, String msg, Map<String, Object> postParams, Map<String, Object> headers, boolean needDecode, String charset, boolean multipart) throws ClientProtocolException, IOException {

        //HttpClient httpclient = new DefaultHttpClient();  ;

        if(url.toLowerCase().startsWith("https://")){

            HttpClientSendPost.enableSSL(httpclient);
        }




        httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); //超时设置
        httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);//连接超时

        httpclient.getParams().setParameter("http.useragent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2");


        HttpPost httppost = new HttpPost(url);


        if(headers != null){

            for(Map.Entry<String, Object> entry : headers.entrySet()){
                //httppost.addHeader(entry.getKey(), entry.getValue().toString());
                httppost.addHeader( entry.getKey(), entry.getValue().toString());
            }
        }


        if(msg != null){

            StringEntity stringEntity = new StringEntity(msg,  "UTF-8");
            httppost.setEntity(stringEntity);
            //stringEntity.writeTo(System.out);
        }else{

            if(multipart){


                if(postParams != null && postParams.size() > 0){

                    MultipartEntity entity = new MultipartEntity();


                    for(Map.Entry<String, Object> entry: postParams.entrySet()){
                        entity.addPart(entry.getKey(), new StringBody(StringUtils.toString(entry.getValue())));
                    }


                   // entity.writeTo(System.out);
                    httppost.setEntity(entity);
                }

            }else{

                List<NameValuePair> formparams = setHttpParams(postParams);
                UrlEncodedFormEntity param = new UrlEncodedFormEntity(formparams, "UTF-8");

                httppost.setEntity(param);
                //param.writeTo(System.out);
            }




        }





        HttpResponse response = httpclient.execute(httppost);


        return processEntry(response.getEntity(), response,  needDecode,  charset);
    }

    /**
     * msg优先
     */
    public static String post(HttpClient httpclient, String url, String msg, List<String[]> postParams, Map<String, Object> headers, boolean needDecode, String charset, boolean multipart) throws ClientProtocolException, IOException {

        //HttpClient httpclient = new DefaultHttpClient();  ;

        if(url.toLowerCase().startsWith("https://")){

            HttpClientSendPost.enableSSL(httpclient);
        }




        httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); //超时设置
        httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);//连接超时

        httpclient.getParams().setParameter("http.useragent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2");


        HttpPost httppost = new HttpPost(url);


        if(headers != null){

            for(Map.Entry<String, Object> entry : headers.entrySet()){
                //httppost.addHeader(entry.getKey(), entry.getValue().toString());
                httppost.addHeader( entry.getKey(), entry.getValue().toString());
            }
        }


        if(msg != null){

            StringEntity stringEntity = new StringEntity(msg,  "UTF-8");
            httppost.setEntity(stringEntity);
            //stringEntity.writeTo(System.out);
        }else{

            if(multipart){


                if(postParams != null && postParams.size() > 0){

                    MultipartEntity entity = new MultipartEntity();


                    for(String[] obj: postParams){
                        if(obj != null && obj.length == 2)
                        entity.addPart(StringUtils.toString(obj[0]), new StringBody(StringUtils.toString(obj[0])));
                    }


                    // entity.writeTo(System.out);
                    httppost.setEntity(entity);
                }

            }else{

                List<NameValuePair> formparams = setHttpParams(postParams);
                UrlEncodedFormEntity param = new UrlEncodedFormEntity(formparams, "UTF-8");

                httppost.setEntity(param);
                //param.writeTo(System.out);
            }




        }





        HttpResponse response = httpclient.execute(httppost);


        return processEntry(response.getEntity(), response,  needDecode,  charset);
    }






	/**
	 * HTTP DELETE方法进行删除操作
	 * @param url
	 * @param map
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String delete(String url, Map<String, Object> map) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete httpdelete= new HttpDelete();
		List<NameValuePair> formparams = setHttpParams(map);
		String param = URLEncodedUtils.format(formparams, "UTF-8");
		httpdelete.setURI(URI.create(url + "?" + param));
		HttpResponse response = httpclient.execute(httpdelete);
		String httpEntityContent = GetHttpEntityContent(response);
		httpdelete.abort();
		return httpEntityContent;
	}
	public static String  put(String url, Map<String, Object> params) throws ClientProtocolException, IOException{

		//url = JETSUM_PLATFORM_SERVER+url;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPut httpput = new HttpPut(url);

		if(params != null){

			List<NameValuePair> formparams = setHttpParams(params);
			UrlEncodedFormEntity param = new UrlEncodedFormEntity(formparams, "UTF-8");
			httpput.setEntity(param);

		}

		HttpResponse response = httpclient.execute(httpput);
		String httpEntityContent = GetHttpEntityContent(response);
		httpput.abort();
		return httpEntityContent;

	}

	private static String processEntry(HttpEntity entity, HttpResponse response, boolean needEncode, String charset) throws IllegalStateException, IOException{

		String value = null;


		Header acceptEncodingObj = response.getFirstHeader("Content-Encoding");
		String  acceptEncoding = null;

		if(acceptEncodingObj !=null){
			acceptEncoding = acceptEncodingObj.getValue();

			StringBuffer sb =new StringBuffer();



			if(acceptEncoding.toLowerCase().indexOf("gzip") > -1){

				//建立gzip解压工作流

				InputStream is = entity.getContent();

				GZIPInputStream gzin = new GZIPInputStream(is);

				//流编码
				String streamCharset = "ISO-8859-1";
				Header  streamCharsetHeader =  response.getFirstHeader("Content-Type");

				if(streamCharsetHeader != null){

					String streamCharsetTemp = streamCharsetHeader.getValue();
					if(streamCharsetTemp != null){

						streamCharsetTemp = CatchUtil.getValue(streamCharsetTemp, ".*?charset=(.*?)$", 1);

						if(streamCharsetTemp != null){

							streamCharset = streamCharsetTemp;
						}
					}
				}
				InputStreamReader isr = null;

				try {
					isr = new InputStreamReader(gzin, streamCharset); // 设置读取流的编码格式，自定义编码
				} catch (Exception e) {

					e.printStackTrace();
					isr = new InputStreamReader(gzin, "ISO-8859-1");
				}

				BufferedReader br = new BufferedReader(isr);

				String tempbf;

				try {
					while((tempbf=br.readLine())!=null){

						sb.append(tempbf);

						sb.append("\r\n");

					}
				} catch (Exception e) {
					// TODO: handle exception
				}finally{
					try {
						isr.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					try {
						gzin.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}


				}

				value = sb.toString();

			}
		}


		if(value == null){
			value  = EntityUtils.toString(entity);
		}

		if(value != null && needEncode){// content="text/html; charset=utf-8" />

			String entryCharset = null;

			String contentType = entity.getContentType().getValue();
			if(charset != null){
				entryCharset = charset;
			}else{

				entryCharset = CatchUtil.getValue(value, "charset=\"?([^\"]+)\"? */?>", 1);
			}

			if(entryCharset == null){
				entryCharset = CatchUtil.getValue(value, "encoding=\"?(.*?)\"?\\?>", 1);
			}



			//chart = chart.replaceAll("charset=\"?([^\"]+)\" */?>", "$1");
			if(entryCharset != null && contentType != null && contentType.toLowerCase().contains(entryCharset.toLowerCase())){
				return value;
			}else{
				if(entryCharset == null){
					entryCharset = "utf8";
				}
				return new String(value.getBytes("ISO-8859-1") , entryCharset);
			}

		}else {
			return value;
		}
	}

	public static String getRedirectUrl(String url) {

		DefaultHttpClient httpclient = new DefaultHttpClient();

		httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); //超时设置
		httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);//连接超时
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		httpclient.getParams().setParameter("http.useragent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2");
		//List<BasicHeader> headers = new ArrayList<BasicHeader>();
		//headers.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
		// httpclient.getParams().setParameter("http.default-headers", headers);
		//httpclient.getParams().setIntParameter(HttpConnectionParams., 5000);//连接超时

		httpclient.setHttpRequestRetryHandler(new Retry());
		//HttpPost httpPost = new HttpPost(url);
		HttpGet get = new HttpGet(url);
		//httpPost.getParams().setBooleanParameter(arg0, arg1)

		logger.debug("请求地址：" + get.getURI());
		HttpResponse response = null;
		String key = "Location";
		try {
			response = httpclient.execute(get);

			Header[] headers = response.getAllHeaders();
			if(headers != null && headers.length > 0){

				for(Header header : headers){

					if(key.equals(header.getName())){
						return header.getValue();
					}
				}
			}
			// System.out.println(Arrays.toString(response.getAllHeaders()));
		} catch (IOException ex) {
			logger.error("Util#httpclient.execute(httpost) " + ex.toString());
			//ex.printStackTrace();
		} catch (Exception ex) {
			logger.error("Util#httpclient.execute(httpost) " + ex.toString());
			//ex.printStackTrace();
		}

		return null;
	}


	/**
	 * 获取远程地址的返回代码
	 * @param url 远程地址，例如http://pay.mapbar.com/index.html
	 * @return  返回值,当失败是返回null
	 */
	public static String getSrcAndSetParentUrl(String url , boolean needEncode,  String parentUrl) {

		return getSrc(url, needEncode, null, parentUrl);

	}
	/**
	 * 获取远程地址的返回代码
	 * @param url 远程地址，例如http://pay.mapbar.com/index.html
	 * @return  返回值,当失败是返回null
	 */
	public static String getSrc(String url , boolean needEncode) {

		return getSrc(url, needEncode, null);

	}


	/**
	 * 获取远程地址的返回代码
	 * @param url 远程地址，例如http://pay.mapbar.com/index.html
	 * @return  返回值,当失败是返回null
	 */
	public static String getSrc(String url , boolean needEncode, int timeout) {

		return getSrc(url, needEncode, null, null, timeout);

	}

	/**
	 * 获取远程地址的返回代码
	 * @param url 远程地址，例如http://pay.mapbar.com/index.html
	 * @return  返回值,当失败是返回null
	 */
	public static String getSrc(String url , boolean needEncode, String charset) {

		return getSrc(url, needEncode, charset, null);

	}



	public static String getSrc(HttpClient httpclient, String url , boolean needEncode, String charset, String parentUrl){

		return getSrc(httpclient, url, needEncode, charset, parentUrl, 10000);
	}

    /**
     * 获取远程地址的返回代码
     * @param url 远程地址，例如http://pay.mapbar.com/index.html
     * @return  返回值,当失败是返回null
     */
    public static String getSrc(HttpClient httpclient, String url , boolean needEncode, String charset, String parentUrl, int timeout) {

//		if(url != null && url.startsWith("file://")){
//
//			logger.debug("read local file " + url);
//			return FileUtil.readFile(url.replace("file://", ""));
//		}
        //设置代理

        httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, timeout); //超时设置
        httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);//连接超时

        httpclient.getParams().setParameter("http.useragent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2");
        //List<BasicHeader> headers = new ArrayList<BasicHeader>();
        //headers.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
        // httpclient.getParams().setParameter("http.default-headers", headers);
        //httpclient.getParams().setIntParameter(HttpConnectionParams., 5000);//连接超时


        if(httpclient instanceof DefaultHttpClient)
            ((DefaultHttpClient)httpclient).setHttpRequestRetryHandler(new Retry());
        //HttpPost httpPost = new HttpPost(url);
        HttpGet get = new HttpGet(url);
        //httpPost.getParams().setBooleanParameter(arg0, arg1)

        if(parentUrl != null)
            get.addHeader("Referer", parentUrl);

        logger.debug("请求地址：" + get.getURI());
        HttpResponse response = null;
        try {
            response = httpclient.execute(get);
        } catch (IOException ex) {
            logger.error("Util#httpclient.execute(httpost) " + ex.toString());
            //ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Util#httpclient.execute(httpost) " + ex.toString());
            //ex.printStackTrace();
        }


        if (response == null) {
            logger.error("获取接口数据异常,返回值为空,url=" + url);
            return null;
        } else {
            logger.debug(response.getStatusLine() + "," + get.getURI());
        }

        //获取代码
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            if (entity != null) {

                return processEntry(entity, response, needEncode, charset);
                //System.out.println(sb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取接口返回值异常：" + e.toString() + ",url=" + url);
        } finally {
            //关闭
//            try {
//                httpclient.getConnectionManager().shutdown();
//
//            } catch (Exception e) {
//                logger.error("关闭httpclient异常" + e.toString());
//            }
//            httpclient = null;
//            httpPost = null;
//            response = null;
//            entity = null;

        }

        return null;

    }


    /**
     * 获取远程地址的返回代码
     * @param url 远程地址，例如http://pay.mapbar.com/index.html
     * @return  返回值,当失败是返回null
     */
    public static void download( String url , File outputFile, String parentUrl) {

//		if(url != null && url.startsWith("file://")){
//
//			logger.debug("read local file " + url);
//			return FileUtil.readFile(url.replace("file://", ""));
//		}
        //设置代理

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); //超时设置
        httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);//连接超时

        httpclient.getParams().setParameter("http.useragent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2");
        //List<BasicHeader> headers = new ArrayList<BasicHeader>();
        //headers.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
        // httpclient.getParams().setParameter("http.default-headers", headers);
        //httpclient.getParams().setIntParameter(HttpConnectionParams., 5000);//连接超时


        if(httpclient instanceof DefaultHttpClient)
            ((DefaultHttpClient)httpclient).setHttpRequestRetryHandler(new Retry());
        //HttpPost httpPost = new HttpPost(url);
        HttpGet get = new HttpGet(url);
        //httpPost.getParams().setBooleanParameter(arg0, arg1)

        if(parentUrl != null)
            get.addHeader("Referer", parentUrl);

        logger.debug("请求地址：" + get.getURI());
        HttpResponse response = null;
        try {
            response = httpclient.execute(get);
        } catch (IOException ex) {
            logger.error("Util#httpclient.execute(httpost) " + ex.toString());
            //ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Util#httpclient.execute(httpost) " + ex.toString());
            //ex.printStackTrace();
        }


        if (response == null) {
            logger.error("获取接口数据异常,返回值为空,url=" + url);
        } else {
            logger.debug(response.getStatusLine() + "," + get.getURI());
        }

        //获取代码
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            if (entity != null) {

                OutputStream outputStream = new FileOutputStream(outputFile);
                InputStream inputStream = entity.getContent();

                byte buffer[] = new byte[1024 * 10];
                int len = 0;
                while((len = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, len);
                }

                outputStream.close();
                inputStream.close();
                //System.out.println(sb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取接口返回值异常：" + e.toString() + ",url=" + url);
        } finally {
            //关闭
//            try {
//                httpclient.getConnectionManager().shutdown();
//
//            } catch (Exception e) {
//                logger.error("关闭httpclient异常" + e.toString());
//            }
//            httpclient = null;
//            httpPost = null;
//            response = null;
//            entity = null;

        }


    }

	public static String getSrc(String url , boolean needEncode, String charset, String parentUrl) {
		return  getSrc(url, needEncode, charset, parentUrl, 10000);
	}
	/**
	 * 获取远程地址的返回代码
	 * @param url 远程地址，例如http://pay.mapbar.com/index.html
	 * @return  返回值,当失败是返回null
	 */
	public static String getSrc(String url , boolean needEncode, String charset, String parentUrl, int timeout) {


        if(url.startsWith("https:")){

            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] arg0,
                                                   String arg1) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] arg0,
                                                   String arg1) throws CertificateException {
                    }
                };
                ctx.init(null, new TrustManager[] { tm }, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx);
                ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", ssf,443));

                ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(defaultHttpClient.getParams(), registry);
                defaultHttpClient =  new DefaultHttpClient(mgr, defaultHttpClient.getParams());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }

            return getSrc(defaultHttpClient, url, needEncode, charset, parentUrl);

        }else {
            return getSrc(new DefaultHttpClient(), url, needEncode, charset, parentUrl, timeout);
        }


	}

	public static String getValue(String src , String regex , int id){

		if(src != null && regex != null){


			Matcher matcher = Pattern.compile(regex,Pattern.DOTALL).matcher(src);



			if(matcher != null){

				if(matcher.find()){

					return matcher.group(id);
				}
			}
		}
		return null;
	}
	public static Map<String, String> getValues(String src , String regex,int urlId , int titleId){

		if(src != null && regex != null){


			Matcher matcher = Pattern.compile(regex).matcher(src);
			if(matcher != null){
				Map<String, String> map  = new LinkedHashMap<String, String>();
				while(matcher.find()){

					map.put(matcher.group(urlId), matcher.group(titleId));
				}
				return map;
			}

		}
		return null;
	}

	public static List<String> getValues(String src , String regex, int regexId){

		if(src != null && regex != null){


			Matcher matcher = Pattern.compile(regex,Pattern.DOTALL).matcher(src);
			if(matcher != null){
				List<String> list = new ArrayList<String>();
				while(matcher.find()){
					String str = matcher.group(regexId);
					//str = str.replace("<li class=\"v_title\">", "");
					list.add(str);
				}
				return list;
			}

		}
		return null;
	}

	public static List<String[]> getValues(String src , String regex, int regexId[]){

		//System.out.println(src);
		//System.out.println(regex);

		if(src != null && regex != null)
		{
			Matcher matcher = Pattern.compile(regex,Pattern.DOTALL).matcher(src);
			if(matcher != null)
			{
				List<String[]> list = new ArrayList<String[]>();
				String cur[];
				int len = regexId.length;
				while(matcher.find()){

					cur = new String[regexId.length];
					for(int i = 0; i < len; i++){

						if(regexId[i] == -1){
							cur[i] = StringUtils.NULL_STRING;
						}else{
							String tmp = matcher.group(regexId[i]);
							cur[i] = tmp;
						}

					}
					list.add(cur);
					//break;
				}
				return list;
			}

		}
		return null;
	}
	private static HttpClient gettHttpClient() {
		HttpClient httpclient = null;
		if(httpclient == null){
			try {
				X509TrustManager tm = new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] arg0,
					                               String arg1) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] arg0,
					                               String arg1) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				sslcontext.init(null, new TrustManager[] { tm }, null);
				SSLSocketFactory ssf = new SSLSocketFactory(sslcontext);
				ClientConnectionManager ccm = new DefaultHttpClient().getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				//sr.register(new Scheme("https", 8443, ssf));
				HttpParams params = new BasicHttpParams();
				params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
				httpclient = new DefaultHttpClient(ccm,params);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return httpclient;
	}
}

class Retry implements HttpRequestRetryHandler{

	public boolean retryRequest(IOException arg0, int executionCount, HttpContext arg2) {

		if (executionCount >= 3) {
			// 超过最大次数则不需要重试

			return false;
		}else {
			System.out.println("重试：" + executionCount);
			return true;
		}


	}


}

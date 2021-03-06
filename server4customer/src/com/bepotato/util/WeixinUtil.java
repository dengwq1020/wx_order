package com.bepotato.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.bepotato.menu.Button;
import com.bepotato.menu.ClickButton;
import com.bepotato.menu.Menu;
import com.bepotato.menu.ViewButton;
import com.bepotato.po.AccessToken;
import com.bepotato.po.AccessTokenDao;
import com.bepotato.po.Scene;
import com.bepotato.po.Ticket;

import net.sf.json.JSONObject;

public class WeixinUtil {

	public static String APPID="wxbb03dd28e2c0154d";
	public static String APPSECRET="3b150731c35ceda61dc517e802d79141";
	public static String ACCESS_TOKEN_URL ="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	public static String CREATE_MENU_URL ="https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	public static String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=1#wechat_redirect";
	public static String DELETE_MENU_URL ="https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
	public static String TICKET_URL="https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=TOKEN";
	public static String QRCODE_URL="https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";
	public static final String MY_URL ="http://119.29.26.47";
	
	/**
	 * get����
	 * @param url
	 * @return
	 */
	public static JSONObject doGetStr(String url){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet =new HttpGet(url);
		JSONObject jsonObject = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity,"utf-8");
				jsonObject = JSONObject.fromObject(result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * post����
	 * @param url
	 * @param outStr
	 * @return
	 */
	public static JSONObject doPostStr(String url,String outStr){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		JSONObject jsonObject =null;
		try {
			httpPost.setEntity(new StringEntity(outStr, "utf-8"));
			HttpResponse response = httpClient.execute(httpPost);
			String result = EntityUtils.toString(response.getEntity(),"utf-8");
			jsonObject = JSONObject.fromObject(result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public static AccessToken getAccessToken(){
		AccessToken token = new AccessToken();
		String url = ACCESS_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);
		JSONObject jsonObject = doGetStr(url);
		if (jsonObject != null) {
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
			token.setCreateTime(new Timestamp(new Date().getTime()));
			System.out.println("成功获得最新token:"+token.getToken());
		}
		return token;
	}
	
	/**
	 **初始化菜单װ
	 * @return
	 */
	public static Menu initMenu(){
		Menu menu = new Menu();
		ViewButton button = new ViewButton();
		button.setName("进入比逗");
		button.setType("view");
		button.setUrl(CreateURL(MY_URL+"/pages/home.jsp"));
		
		menu.setButton(new Button[]{button});
		return menu;
	}
	
	/**
	 * 创建菜单
	 * @param menu
	 * @return
	 */
	public static int createMenu(String menu){
		int result =0;
		AccessTokenDao atDao = new AccessTokenDao();
		atDao.checkToken();
		String token = atDao.getAccessTokenBySQL().getToken();
		String url = CREATE_MENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObject = doPostStr(url, menu);
		if (jsonObject != null) {
			result = jsonObject.getInt("errcode");
		}
		return result;
	}
	
	public static int deleteMenu(String token){
		int result = 0;
		String url = DELETE_MENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObject = doGetStr(url);
		if (jsonObject != null) {
			result = jsonObject.getInt("errcode");
		}
		return result;
	}
	
	public static String CreateURL(String url){
		String redirect_url = null;
		String urltemp =null;
		try {
			redirect_url = URLEncoder.encode(url,"utf-8");
			urltemp = AUTHORIZE_URL.replace("APPID", APPID).replace("REDIRECT_URI", redirect_url).replace("SCOPE", "snsapi_base");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return urltemp;
	}
	
	public static JSONObject getTicketJsonObj(int scene_id,int expire_seconds){
		Ticket t = new Ticket();
		t.setAction_name(Ticket.QR_SCENE);
		Scene scene = new Scene();
		scene.setScene_id(scene_id);
		t.setAction_info(scene);
		t.setExpire_seconds(expire_seconds);
		String ticket = JSONObject.fromObject(t).toString();
		System.out.println(ticket);
		AccessTokenDao atDao = new AccessTokenDao();
		atDao.checkToken();
		String token = atDao.getAccessTokenBySQL().getToken();
		String url = TICKET_URL.replace("TOKEN", token);
		return doPostStr(url, ticket);
	}
	
	public static String getQrcodeUrl(JSONObject ticketObj){
		String ticketString = ticketObj.getString("ticket");
		String url =null;
		try {
			url = QRCODE_URL.replace("TICKET", URLEncoder.encode(ticketString,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
}

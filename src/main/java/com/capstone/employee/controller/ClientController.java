package com.capstone.employee.controller;

import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.capstone.employee.entity.Employee;

@RestController
@RequestMapping("/employee/client")
public class ClientController {
	
	private static final String SECRET_KEY = "MySecretKey";
	private static final String SALTVALUE = "abcdefg";

	@GetMapping("/get/restcontroller/{ID}")
	public Employee getByIDRest(@PathVariable int ID) throws Exception {

		String url = "https://localhost:8445/employee/get/by/" + ID;
		RestTemplate restTemplate = new RestTemplate(getRequestFactory());
		Employee employee = restTemplate.getForObject(url, Employee.class);
		System.out.println(employee);
		
		String decrypted=ClientController.decrypt(employee.getDateOfBirth());
		System.out.println(decrypted);
		
		employee.setDateOfBirth(decrypted);

		return employee;

	}

	private static SimpleClientHttpRequestFactory getRequestFactory() {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} };

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(5000);
		requestFactory.setReadTimeout(5000);
		return requestFactory;
	}
	
	public static String decrypt(String strToDecrypt) throws Exception {

		/* Declare a byte array. */
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		/* Create factory for secret keys. */
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		/* PBEKeySpec class implements KeySpec interface. */
		KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
		/* Retruns decrypted value. */
		return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
	}

}

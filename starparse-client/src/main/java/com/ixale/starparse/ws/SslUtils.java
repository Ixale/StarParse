package com.ixale.starparse.ws;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslUtils {

	private static final Logger logger = LoggerFactory.getLogger(SslUtils.class);

	public static void configureSsl() {

		// https://community.letsencrypt.org/t/will-the-cross-root-cover-trust-by-the-default-list-in-the-jdk-jre/134/37
		// TODO: implement once Parsely.io is migrated
//		try {
//			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//			ks.load(null, null);
//			ks.setCertificateEntry(Integer.toString(1), CertificateFactory.getInstance("X.509").generateCertificate(SslUtils.class.getClassLoader().getResourceAsStream("cert/DSTRootCAX3.pem")));
//			ks.setCertificateEntry(Integer.toString(2), CertificateFactory.getInstance("X.509").generateCertificate(SslUtils.class.getClassLoader().getResourceAsStream("cert/LetsEncryptAuthorityX1.pem")));
//			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//			tmf.init(ks);
//			final SSLContext ctx = SSLContext.getInstance("TLS");
//			ctx.init(null, tmf.getTrustManagers(), null);
//			HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
//			SSLContext.setDefault(ctx);
//
//			
//		} catch (Exception e) {
//			logger.error("Unable to register LE root: " + e.getMessage(), e);
//		}

		final TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				}
		};

		try {
			// Install the all-trusting trust manager
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			SSLContext.setDefault(sc);

		} catch (Exception e) {
			logger.error("Unable to register SSL socket factory: " + e.getMessage(), e);
		}

		// Create all-trusting host name verifier
		final HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

}

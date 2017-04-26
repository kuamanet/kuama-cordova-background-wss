package net.kuama.backgroundservice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

class SSLUtils {

    private final static String TAG = SSLUtils.class.getSimpleName();

    private static TrustManager[] createTrustManager() {
        return new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

        }};
    }

    private static KeyManager[] createKeyManager(InputStream certFile, String certFilePassword) throws Exception {

        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(createKeyStore(certFile, certFilePassword), certFilePassword.toCharArray());

        return factory.getKeyManagers();
    }

    private static KeyStore createKeyStore(InputStream certFile, String certFilePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        if (certFile != null) {
            keyStore.load(certFile, certFilePassword.toCharArray());
        }

        return keyStore;
    }

    static JSONObject getRemoteData(CertificateData certificate, String uri) throws JSONException, MalformedURLException {

        try {
            FileInputStream certFile = new FileInputStream(certificate.getPath());
            SSLContext context = SSLContext.getInstance("TLSv1.2");


            context.init(
                    SSLUtils.createKeyManager(certFile, certificate.getPassword()),
                    SSLUtils.createTrustManager(),
                    null
            );

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());

            URL url = new URL(uri);

            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();

            urlConn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String jsonString;

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            jsonString = sb.toString();

            return new JSONObject(jsonString);
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        return null;

    }
}

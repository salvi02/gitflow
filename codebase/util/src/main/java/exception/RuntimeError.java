package exception;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import httpclient.ErrorHttpClient;

public class RuntimeError extends Exception {

   int x = 0;
   int y = 1;
   
   public RuntimeError(ErrorHttpClient httpNoSuchAlgorithmError, NoSuchAlgorithmException e) {
      // TODO Auto-generated constructor stub
   }

   public RuntimeError(ErrorHttpClient httpKeyStoreExceptionError, KeyStoreException e) {
      // TODO Auto-generated constructor stub
   }

   public RuntimeError(ErrorHttpClient httpCertificateError, CertificateException e) {
      // TODO Auto-generated constructor stub
   }

   public RuntimeError(ErrorHttpClient httpIoError, IOException e) {
      // TODO Auto-generated constructor stub
   }

   public RuntimeError(ErrorHttpClient httpKeyManangementError, KeyManagementException e) {
      // TODO Auto-generated constructor stub
   }

   public RuntimeError(ErrorHttpClient httpParseError, Exception e) {
      // TODO Auto-generated constructor stub
   }

}

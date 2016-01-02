import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


object RSA {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(373); 

 
                        
   var keyPairGenerator = KeyPairGenerator.getInstance("RSA");System.out.println("""keyPairGenerator  : java.security.KeyPairGenerator = """ + $show(keyPairGenerator ));$skip(40); 
      keyPairGenerator.initialize(2048);$skip(55); 
      var keyPair = keyPairGenerator.generateKeyPair();System.out.println("""keyPair  : java.security.KeyPair = """ + $show(keyPair ));$skip(41); 
     var publicKey = keyPair.getPublic();System.out.println("""publicKey  : java.security.PublicKey = """ + $show(publicKey ));$skip(44); 
      var privateKey = keyPair.getPrivate();System.out.println("""privateKey  : java.security.PrivateKey = """ + $show(privateKey ));$skip(56); 
      
      
      var x = encryptRSA("xyz",publicKey);System.out.println("""x  : String = """ + $show(x ));$skip(49); 
      
      var y = decryptToken(x, privateKey);System.out.println("""y  : String = """ + $show(y ));$skip(340); 
      
       def encryptRSA(randomToken: String, key: PublicKey): String = {
 
    var data =  randomToken.getBytes
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    var encryptedCipher = cipher.doFinal(data);
    
    return  encryptedCipher.toString()
  };System.out.println("""encryptRSA: (randomToken: String, key: java.security.PublicKey)String""");$skip(350); 
  
  
    def decryptToken(inputString: String, key: PrivateKey): String = {
     
 
    var inputBytes = inputString.getBytes();
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.DECRYPT_MODE, key);
    var decBytes = cipher.doFinal(inputBytes);
    
    return  decBytes.toString()
    
  };System.out.println("""decryptToken: (inputString: String, key: java.security.PrivateKey)String""");$skip(84); 
    
    
    
                        
  println("Welcome to the Scala worksheet")}
}

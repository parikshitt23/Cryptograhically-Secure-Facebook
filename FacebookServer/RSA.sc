import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


object RSA {

 
                        
   var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      var keyPair = keyPairGenerator.generateKeyPair()
     var publicKey = keyPair.getPublic()
      var privateKey = keyPair.getPrivate()
      
      
      var x = encryptRSA("xyz",publicKey)
      
      var y = decryptToken(x, privateKey)
      
       def encryptRSA(randomToken: String, key: PublicKey): String = {
 
    var data =  randomToken.getBytes
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    var encryptedCipher = cipher.doFinal(data);
    
    return  encryptedCipher.toString()
  }
  
  
    def decryptToken(inputString: String, key: PrivateKey): String = {
     
 
    var inputBytes = inputString.getBytes();
    var xform = "RSA/ECB/PKCS1Padding";
    var cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.DECRYPT_MODE, key);
    var decBytes = cipher.doFinal(inputBytes);
    
    return  decBytes.toString()
    
  }
    
    
    
                        
  println("Welcome to the Scala worksheet")
}
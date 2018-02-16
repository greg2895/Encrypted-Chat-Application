import java.io.Serializable;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES implements Serializable{
	private static final long serialVersionUID = 1L;
	private SecretKey secretKey;
	private String encodedKey;

    public void keyGen() throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        secretKey = generator.generateKey();
        encodedKey = encodeKey(secretKey);
    }
    
    public String encrypt(String plainText) throws Exception{
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(byteCipherText);
    }
    
    public String decrypt(String cipherText) throws Exception {
    	byte[] decrypted = Base64.getDecoder().decode(cipherText);
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] bytePlainText = aesCipher.doFinal(decrypted);
        return new String(bytePlainText);
    }
    
    public String encodeKey(SecretKey sk){
    	return Base64.getEncoder().encodeToString(sk.getEncoded());
    }
    
    public void decodeKey(String st){
    	byte[] decodedKey = Base64.getDecoder().decode(st);
    	secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    	encodedKey = encodeKey(secretKey);
    }
    
    public String getPrivateKey(){
    	return encodedKey;
    }

}


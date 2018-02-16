import java.io.Serializable;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Random;

public class RSA implements Serializable{
	private static final long serialVersionUID = 1L;
	private BigInteger N;
	private BigInteger e;
	private BigInteger d;
	
	public void keyGen() {
		
    	final int bitLength = 2048;

        Random r = new Random();

        BigInteger p = BigInteger.probablePrime(bitLength, r);
        BigInteger q = BigInteger.probablePrime(bitLength, r);
        
        N = p.multiply(q); //PUBLIC KEY
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.probablePrime(bitLength / 2, r); //PUBLIC KEY
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0)
        {
            e.add(BigInteger.ONE);
        }
        d = e.modInverse(phi); //PRIVATE KEY
	}

	public void setN(BigInteger n) {
		N = n;
	}

	public void setE(BigInteger e) {
		this.e = e;
	}

	//Generate Public Key
	public RSA getPublicKey(){
		RSA publicKey = new RSA();
		publicKey.setE(this.e);
		publicKey.setN(this.N);
		return publicKey;
	}

	// Encrypt Message
    public String encrypt(String message) {
    	try {
    		return Base64.getEncoder().encodeToString(new BigInteger(message.getBytes()).modPow(e, N).toByteArray());
    	}
    	catch(Exception e1){
    		return "";
    	}
    }
	// Decrypt Message
    public String decrypt(String cipher) {
    	try{
    		byte[] cipherBytes = Base64.getDecoder().decode(cipher);
    		return new String(new BigInteger(cipherBytes).modPow(d, N).toByteArray());
    	}
		catch(Exception e1){
			return "";
		}
    }
}

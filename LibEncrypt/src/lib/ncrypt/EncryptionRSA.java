package lib.ncrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public final class EncryptionRSA extends EncryptionComm 
{
	public final boolean hasError;
	
	public final RSAPrivateKey privateKey;
	public final RSAPublicKey publicKey;
	
	public final String publicFile = "/public.key";
	public final String privateFile = "/private.key";
	
	public byte[] encrypt(byte[] srcBytes)
	{
		if (srcBytes == null || srcBytes.length == 0) {
			return null;
		}
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
        try {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace(System.err);
			return null;
		}
        byte[] resultBytes;
		try {
			resultBytes = cipher.doFinal(srcBytes);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return resultBytes;
	}
	public String encrypt(String srcString)
	{
		if (null == srcString || srcString.length() == 0) {
			return null;
		}
		byte[] bytes;
		try {
			bytes = srcString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
			return null;
		}
		bytes = encrypt(bytes);//加密RSA
		if (bytes == null) {
			return null;
		}
		bytes = EncryptionBase64.encode(bytes);//编码Base64
		if (bytes == null) {
			return null;
		}
		try {
			return new String(bytes, "ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public byte[] decrypt(byte[] srcBytes)
	{
		if (srcBytes == null || srcBytes.length == 0) {
			return null;
		}
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
        try {
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace(System.err);
			return null;
		}
        byte[] resultBytes;
		try {
			resultBytes = cipher.doFinal(srcBytes);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return resultBytes;
	}
	public String decrypt(String srcString)
	{
		if (null == srcString || srcString.length() == 0) {
			return null;
		}
		byte[] bytes = null;
		try {
			bytes = srcString.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
			return null;
		}
		bytes = EncryptionBase64.decode(bytes);//解码Base64
		if (bytes == null) {
			return null;
		}
		bytes = decrypt(bytes);//解密RSA
		if (bytes == null) {
			return null;
		}
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public EncryptionRSA(String path)
	{
		if (path == null || path.length() == 0)
		{
			hasError = true;
			publicKey = null;
			privateKey = null;
			return;
		}
		File dir = new File(path);
		if (dir.exists())
		{
			if (dir.isFile())
			{
				if (!dir.delete())
				{
					hasError = true;
					publicKey = null;
					privateKey = null;
					return;
				}
				if (!dir.mkdirs())
				{
					hasError = true;
					publicKey = null;
					privateKey = null;
					return;
				}
			}
		}
		else
		{
			if (!dir.mkdirs())
			{
				hasError = true;
				publicKey = null;
				privateKey = null;
				return;
			}
		}
        File pubKeyFile = new File(dir, publicFile);
        File priKeyFile = new File(dir, privateFile);
        if (pubKeyFile.exists() && priKeyFile.exists()) // 有key，现在读取
        {
        	publicKey = (RSAPublicKey) loadKey(pubKeyFile);
        	if (publicKey == null)
        	{
        		hasError = true;
        		privateKey = null;
    			return;
			}
        	privateKey = (RSAPrivateKey) loadKey(priKeyFile);
        	if (null == privateKey)
        	{
        		hasError = true;
        		return;
			}
        	hasError = false;
		}
        else // 没有key，现在生成
        {
        	if (pubKeyFile.exists())
            {
        		if (!pubKeyFile.delete())
        		{
        			hasError = true;
        			publicKey = null;
        			privateKey = null;
        			return;
				}
    		}
        	else if (priKeyFile.exists())
        	{
				if (!priKeyFile.delete())
				{
					hasError = true;
        			publicKey = null;
        			privateKey = null;
        			return;
				}
			}
        	KeyPair keyPair = null;
    		try
    		{
    			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    			keyPairGen.initialize(1024);
    	        keyPair = keyPairGen.generateKeyPair();
    		}
    		catch (NoSuchAlgorithmException e)
    		{
    			e.printStackTrace(System.err);
    			hasError = true;
    			publicKey = null;
    			privateKey = null;
    			return;
    		}
        	privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            if (!storageKey(publicKey, pubKeyFile))
            {
            	hasError = true;
        		return;
			}
            if (!storageKey(privateKey, priKeyFile))
            {
            	hasError = true;
        		return;
			}
            hasError = false;
		}
	}
	
	protected Serializable loadKey(File keyFile)
	{
		InputStream in;
		try {
			in = new FileInputStream(keyFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
			return null;
		}
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(in);
		} catch (IOException e) {
			try {
				in.close();
			} catch (IOException e2) {
				e2.printStackTrace(System.err);
			}
			e.printStackTrace(System.err);
			return null;
		}
		Serializable key = null;
		try {
			key = (Serializable) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
		return key;
	}
	
	protected boolean storageKey(Serializable key, File keyFile)
	{   
		try {
			keyFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return false;
		}
	    OutputStream out;
		try {
			out = new FileOutputStream(keyFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
			keyFile.delete();
			return false;
		}
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(out);
		} catch (IOException e) {
			try {
				out.close();
			} catch (Exception e2) {
				e2.printStackTrace(System.err);
			}
			keyFile.delete();
			e.printStackTrace(System.err);
			return false;
		}
		boolean ret = true;
		try {
			oos.writeObject(key);
			oos.flush();
		} catch (IOException e) {
			keyFile.delete();
			e.printStackTrace(System.err);
			ret = false;
		} finally {
			try {
				out.close();
			} catch (IOException e2) {
				ret = false;
				e2.printStackTrace(System.err);
			}
			try {
				oos.close();
			} catch (IOException e2) {
				ret = false;
				e2.printStackTrace(System.err);
			}
		}
		return ret;
	}
}
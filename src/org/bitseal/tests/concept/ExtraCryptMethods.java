package org.bitseal.tests.concept;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.bitseal.crypt.CryptProcessor;
import org.bitseal.data.EncryptedPayload;
import org.bitseal.util.ArrayCopier;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.math.ec.ECPoint;

/**
 * A couple of extra methods needed for the message identification
 * 'concept' tests. <br><br>
 * 
 * <b>NOTE:</b> Tests in the 'concept' package are for experimentation
 * related to Protocol Version 3, not for checking the correctness or
 * performance of Bitseal. 
 * 
 * @author Jonathan Coe
 */
public class ExtraCryptMethods
{
	/**
	 * Parses an encrypted payload and reconstructs the public key
	 * 'R' that is encoded within it. <br><br>
	 * 
	 *  Note: This method uses reflection to access one or more private methods
	 * 
	 * @param encryptedPayload - A byte[] containing the encrypted payload
	 * 
	 * @return An ECPublicKey object containing the public key R
	 */
	public static EncryptedPayload parseEncryptedPayload (byte[] encryptedPayload)
	{
		try
		{
			// Use reflection to access the private method 'parseEncryptedPayload()' in CryptProcessor
			CryptProcessor cryptProc = new CryptProcessor();	
			Method method0 = CryptProcessor.class.getDeclaredMethod("parseEncryptedPayload", byte[].class);
			method0.setAccessible(true);
			
			return (EncryptedPayload) method0.invoke(cryptProc, encryptedPayload);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		} 
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		} 
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		} 
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Parses an encrypted payload and reconstructs the public key
	 * 'R' that is encoded within it. <br><br>
	 * 
	 *  Note: This method uses reflection to access one or more private methods
	 * 
	 * @param encryptedPayload - A byte[] containing the encrypted payload
	 * 
	 * @return An ECPublicKey object containing the public key R
	 */
	public static ECPublicKey extractPublicKeyR (byte[] encryptedPayload)
	{
		try
		{
			// Use reflection to access the private method 'createPublicEncryptionKey()' in CryptProcessor
			CryptProcessor cryptProc = new CryptProcessor();	
			Method method1 = CryptProcessor.class.getDeclaredMethod("createPublicEncryptionKey", BigInteger.class, BigInteger.class);
			method1.setAccessible(true);
			
			EncryptedPayload encPay = parseEncryptedPayload(encryptedPayload);
			
			BigInteger x = encPay.getX();
			BigInteger y = encPay.getY();
			
			return (ECPublicKey) method1.invoke(cryptProc, x, y);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		} 
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		} 
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		} 
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Calculates the key_m value for a given ECPoint <br><br>
	 * 
	 *  Note: This method uses reflection to access one or more private methods
	 * 
	 * @param point - An ECPoint object which the key_m is derived from
	 * 
	 * @return A byte[] containing the key_m value
	 */
	public static byte[] calculateKeyM (ECPoint point)
	{
		try
		{
			// Use reflection to access the private method 'deriveKey()' in CryptProcessor
			CryptProcessor cryptProc = new CryptProcessor();	
			Method method0 = CryptProcessor.class.getDeclaredMethod("deriveKey", ECPoint.class);
			method0.setAccessible(true);
			
			byte[] tmpKey = (byte[]) method0.invoke(cryptProc, point);
			
			return ArrayCopier.copyOfRange(tmpKey, 32, 64);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		} 
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		} 
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		} 
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
}
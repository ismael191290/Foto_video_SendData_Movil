package com.gruposantoro.sedeso.tools;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ulises
 */
public class Encryptation {
    
    String secretKey="ulises";
    String base64String="";
    
    public String encryptWithKey(String datos){
        MessageDigest mdg;
        try {
            mdg = MessageDigest.getInstance("MD5");//se obtiene el tipo de encriptado
            //mdg = MessageDigest.getInstance("SHA-512");//se obtiene el tipo de encriptado
            byte[] digestOfPass = mdg.digest(secretKey.getBytes("utf-8"));//se obtienen los bytes de la contraseña
            byte[] keyBytes = Arrays.copyOf(digestOfPass, 24);//copiamos el arreglo de bytes
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] plainText = datos.getBytes("utf-8");
            byte[] buff = cipher.doFinal(plainText);
            byte[] base64Bytes = Base64.encodeBase64(buff);
            base64String = new String(base64Bytes);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Encryptation.class.getName()).log(Level.SEVERE, null, ex);
        }catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
            return "Metodo de enciptacion no soportado";
        }catch(NoSuchPaddingException ex){
            ex.printStackTrace();
        }catch(InvalidKeyException ex){
            ex.printStackTrace();
            return "Key invalida";
        }catch(IllegalBlockSizeException ex){
            ex.printStackTrace();
        }catch(BadPaddingException ex){
            ex.printStackTrace();
        }
        return base64String;
    }
    
    
    public String desEncryptWithKey(String datos){
        try {
            byte[] message = Base64.decodeBase64(datos.getBytes("utf-8"));
            //MessageDigest md = MessageDigest.getInstance("SHA-512");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
 
            Cipher decipher = Cipher.getInstance("DESede");
            decipher.init(Cipher.DECRYPT_MODE, key);
 
            byte[] plainText = decipher.doFinal(message);
 
            base64String = new String(plainText, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return "Metodo de enciptacion no soportado";
        }catch(NoSuchAlgorithmException ex){
            ex.printStackTrace();
        }catch(NoSuchPaddingException ex){
            ex.printStackTrace();
        }catch(InvalidKeyException ex){
            ex.printStackTrace();
            return "Key invalida";
        }catch (IllegalBlockSizeException ex){
            ex.printStackTrace();
        }catch(BadPaddingException ex){
            ex.printStackTrace();
        }
        return base64String;
    }
    
}

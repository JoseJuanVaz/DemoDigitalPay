package org.josejuanvazquez.codi.cifrados;


import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AES_CBC_128 {

    // Definición del tipo de algoritmo a utilizar (AES, DES, RSA)
    private final static String alg = "AES";
    // Definición del modo de cifrado a utilizar
    private final static String cI = "AES/CBC/PKCS5Padding";

    /**
     * Función de tipo String que recibe una llave (key), un vector de inicialización (iv)
     * y el texto que se desea cifrar
     * @param key la llave en tipo String a utilizar
     * @param iv el vector de inicialización a utilizar
     * @param cleartext el texto sin cifrar a encriptar
     * @return el texto cifrado en modo String
     * @throws Exception puede devolver excepciones de los siguientes tipos: NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException
     */
    public String encrypt(String key, String iv, String cleartext) throws Exception {
        Cipher cipher = Cipher.getInstance(cI);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(cleartext.getBytes());
        return new String(encodeBase64(encrypted));
    }


    /**
     * Función de tipo String que recibe una llave (key), un vector de inicialización (iv)
     * y el texto que se desea descifrar
     * @param key la llave en tipo String a utilizar
     * @param iv el vector de inicialización a utilizar
     * @param encrypted el texto cifrado en modo String
     * @return el texto desencriptado en modo String
     * @throws Exception puede devolver excepciones de los siguientes tipos: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException
     */
    public String decrypt(byte[]  key, byte[]  iv, String encrypted) throws Exception {
        Log.i(this.getClass().getName(), "Desencriptar: "+ encrypted);
        Log.i(this.getClass().getName(), "Desencriptar bytes: "+ encrypted.getBytes());
        Log.i(this.getClass().getName(), "Desencriptar decodeBase64 bytes: "+ decodeBase64(encrypted));

        SecretKeySpec skeySpec = new SecretKeySpec(key, alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(cI);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);

        byte[] enc = encrypted.getBytes();//decodeBase64(encrypted);//
        Log.i(this.getClass().getName(), "enc: "+ enc.length);
        //Notas
        // Usando encrypted.getBytes() se genera cadena de 32 bytes, decir,
        // como estamos usando 16 bytes de Llave y 16 Bytes de Vector de inicializacion
        // cadena a 32 bytes envia error: javax.crypto.BadPaddingException: error:1e000065:Cipher functions:OPENSSL_internal:BAD_DECRYPT
        // Aunque este error indica que esta mal la llave para desencriptar.
        //
        //Usando decodeBase64(encrypted); se genera cadena de 24 bytes
        //Lo cual parece estar mal para este tipo de desencripcion porque envia el error javax.crypto.IllegalBlockSizeException: error:1e00007b:Cipher functions:OPENSSL_internal:WRONG_FINAL_BLOCK_LENGTH


        byte[] decrypted = cipher.doFinal(enc);
        return new String(decrypted);
    }


    // decode data from base 64
    private static byte[] decodeBase64(String dataToDecode)
    {
        byte[] dataDecoded = Base64.decode(dataToDecode, Base64.DEFAULT);
        return dataDecoded;
    }

    //enconde data in base 64
    private static byte[] encodeBase64(byte[] dataToEncode)
    {
        byte[] dataEncoded = Base64.encode(dataToEncode, Base64.DEFAULT);
        return dataEncoded;
    }
}

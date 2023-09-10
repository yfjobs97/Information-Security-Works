
import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * @author <Yu Feng>
 * @netid <yxf160330>
 * @email <yxf160330@utdallas.edu>
 */
public class EFS extends Utility{
    public static int USERNAME_SIZE = 128;
    public static int PASSWORD_SIZE = 128;
    String padUser;
    
    byte[] hashPW;
    
    private byte[] initialVector;
    public EFS(Editor e)
    {
        super(e);
        set_username_password();
        
    }
    public void padMetaUser(){
        if(username.length() >= USERNAME_SIZE){
            padUser = super.username.substring(0,USERNAME_SIZE - 1);
        }
        else{
            padUser = super.username.substring(0);
            for(int i = super.username.length(); i < USERNAME_SIZE ; i++){
                padUser += '\0'; 
            }
        }
    }
    public void padMetaPW(){//ASSUME password is always <= 128 bytes
        byte[] passwordByte = super.password.getBytes();
        byte[] padPW = new byte[passwordByte.length + initialVector.length];
        System.arraycopy(passwordByte, 0, padPW, 0, passwordByte.length);
        System.arraycopy(initialVector, 0, padPW, passwordByte.length, initialVector.length);//[password||salt].
        
            try{
                hashPW = hash_SHA256(padPW) ;//H(pw||salt) using SHA256
            }catch(Exception exc){
                System.out.println("There is an Exception in padding and hashing password." );
                exc.printStackTrace();
                System.out.println(exc);
            }
        
    }
    public String HMAC(String msg){
        byte[] key1024Pad = new byte [1024];//New array is filled with 0 in java
        System.arraycopy(initialVector, 0, key1024Pad, 0, initialVector.length);
        byte[] ipadByte = {0x36};
        byte[] opadByte = {0x5C};
        
        for(int i = 1; i < 1024; i++){
            ipadByte[i] = ipadByte[0];
            opadByte[i] = opadByte[0];
        }
        byte[] keyXORipadByte = new byte[1024];
        byte[] keyXORopadByte = new byte[1024];
        byte[] key1024PadByte = key1024Pad;
        for(int i = 0; i < 1024; i++){
            keyXORipadByte[i] = (byte)(key1024PadByte[i] ^ ipadByte[i]);
            keyXORopadByte[i] = (byte)(key1024PadByte[i] ^ opadByte[i]);
        }
        
        byte[] msgByte = msg.getBytes();
        byte[] KIpadMsgByte = new byte[msgByte.length + keyXORipadByte.length];
        System.arraycopy(keyXORipadByte, 0, KIpadMsgByte, 0, keyXORipadByte.length);
        System.arraycopy(msgByte, 0, KIpadMsgByte, keyXORipadByte.length, msgByte.length);
        
        byte[] hashKIpadMsgByte = {};
        try{
            
            hashKIpadMsgByte = hash_SHA256(KIpadMsgByte);//Concatenate keyXORipad and msg, get bytes and calculate hash
        }catch (Exception exc){
            System.out.println("There is an Exception in HMAC, hashing key, ipad with msg." );
            exc.printStackTrace();
            System.out.println(exc);
        }
        
        byte[] KOpadHashKIpadMsgByte = new byte[keyXORopadByte.length + hashKIpadMsgByte.length];
        System.arraycopy(keyXORopadByte, 0, KOpadHashKIpadMsgByte, 0, keyXORopadByte.length);
        System.arraycopy(hashKIpadMsgByte, 0, KOpadHashKIpadMsgByte, keyXORopadByte.length, hashKIpadMsgByte.length);
        
        byte[] hashKOpadHashKIpadMsgByte = {};
        try{
            hashKOpadHashKIpadMsgByte = hash_SHA256( KOpadHashKIpadMsgByte );
        }catch (Exception exc){
            System.out.println("There is an Exception in HMAC, hashing key, opad with hashedK, ipad and M." );
            exc.printStackTrace();
            System.out.println(exc);
        }
        return byteArray2String(hashKOpadHashKIpadMsgByte);
        
    }
    
    public byte[] incrInitialVector(byte[] initVec, int incrValue){
        byte[] initialVectorMod = byteArray2String(initVec).getBytes();//Create a new instance of initialVector
        initialVectorMod[initialVectorMod.length - 1] += incrValue;
        return initialVectorMod;
    }
    public String encryption(String msg, byte[] IV){
        String ciphertext = byteArray2String(IV);//C0
        if( (msg.length() % 128) == 0){
            int num128bitSlots = msg.length() / 128;
            for(int i = 0; i < num128bitSlots; i++){
                try{
                    ciphertext += byteArray2String( encript_AES( (msg.substring(i*16, (i+1)*16)).getBytes(), incrInitialVector(IV,i+1) ) );

                }catch(Exception exc){
                    System.out.println("There is an Exception in message encryption.(even 128)" );
                    exc.printStackTrace();
                    System.out.println(exc);
                }
            }
        }
        else{
            int num128bitSlots = msg.length() / 128 + 1;
            for(int i = 0; i < num128bitSlots; i++){
                if(i != num128bitSlots - 1){//For size not evenly fit 128 bits, do same until second to the last iteration
                    try{
                        ciphertext += byteArray2String( encript_AES( (msg.substring(i*16, (i+1)*16)).getBytes(), incrInitialVector(IV,i+1) ) );

                    }catch(Exception exc){
                        System.out.println("There is an Exception in message encryption. (uneven 128)" );
                        exc.printStackTrace();
                        System.out.println(exc);
                    }
                }
                else{
                    String lastString = msg.substring(i*16, msg.length());
                    for(int j = msg.length(); j < (i+1)*16; j++ ){//Padding the last section of plaintext to align with 128 bit
                        lastString += '\0';
                    }
                     try{
                        ciphertext += byteArray2String( encript_AES( lastString.getBytes(), incrInitialVector(IV,i+1) ) );

                    }catch(Exception exc){
                        System.out.println("There is an Exception in message encryption.(last uneven 128)" );
                        exc.printStackTrace();
                        System.out.println(exc);
                    }
                }
            }
        }
        return ciphertext;
    }
    
    public byte[] findIVFromCipher(String msg){
        return msg.substring(0, 128/8).getBytes();
    }
    public String decryption(String cipher){
        String plaintext = "";
        byte[] IV = (cipher.substring(0, 128/8)).getBytes();
        String msg = cipher.substring(128/8, cipher.length());
        if( (msg.length() % 128) == 0){//Only consider this situation as plaintext are padded during encryption
            int num128bitSlots = msg.length() / 128;
            for(int i = 0; i < num128bitSlots; i++){
                try{
                    plaintext += byteArray2String( decript_AES( (msg.substring(i*16, (i+1)*16)).getBytes(), incrInitialVector(IV,i+1) ) );

                }catch(Exception exc){
                    System.out.println("There is an Exception in message encryption.(even 128)" );
                    exc.printStackTrace();
                    System.out.println(exc);
                }
            }
        }

        return plaintext;
        
        
    }
    /*public boolean verificationProcess(){
        
    }*/
   
    /**
     * Steps to consider... <p>
     *  - add padded username and password salt to header <p>
     *  - add password hash and file length to secret data <p>
     *  - AES encrypt padded secret data <p>
     *  - add header and encrypted secret data to metadata <p>
     *  - compute HMAC for integrity check of metadata <p>
     *  - add metadata and HMAC to metadata file block <p>
     */
    @Override
    public void create(String file_name, String user_name, String password) throws Exception {        
        this.initialVector = secureRandomNumber(128);
        padMetaUser();//pad username
        padMetaPW();//pad and hash password
        
        dir = new File(file_name);
        dir.mkdirs();
        File meta = new File(dir, "0");
        
        //Ecrypt secretData
        String secretData = "";
        secretData = "0\n"; //length [0]
        secretData += hashPW;//PWhash[1]
        
        String cipher_secretData = encryption(secretData, initialVector);
        
        //Writing into metadata
        String toWrite = "";
        toWrite = padUser + "\n";   //add padded username [0]
        
        toWrite += cipher_secretData + "\n"; //[1]
        
        String hashReturn = HMAC(toWrite);//Compute HMAC for integrity check
        toWrite += hashReturn + "\n";//[2]
        
        //padding
        while (toWrite.length() < Config.BLOCK_SIZE) {
            toWrite += '\0';
        }

        save_to_file(toWrite.getBytes(), meta);
        return;
    }

    /**
     * Steps to consider... <p>
     *  - check if metadata file size is valid <p>
     *  - get username from metadata <p>
     */
    @Override
    public String findUser(String file_name) throws Exception {
        
        File file = new File(file_name);
        File meta = new File(file, "0");
        if(meta.length() == Config.BLOCK_SIZE){
            String s = byteArray2String(read_from_file(meta));
            String[] strs = s.split("\n");
            return strs[0];
        }
        else{
            System.out.println("The metadata size is not valid" );
            throw new Exception();
        }

    	
    }

    /**
     * Steps to consider...:<p>
     *  - get password, salt then AES key <p>     
     *  - decrypt password hash out of encrypted secret data <p>
     *  - check the equality of the two password hash values <p>
     *  - decrypt file length out of encrypted secret data
     */
    @Override
    public int length(String file_name, String password) throws Exception {
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");
        
        this.initialVector = findIVFromCipher(strs[1]);
        String ciphertext = strs[1];
        
        String plaintext = decryption(ciphertext);//Decrypt the secret data
        
        String[] plaintextSplit = plaintext.split("\n");
        String storedPWHash = plaintextSplit[1];
         //Check password
        padMetaPW();//Calculate hash for entered password
        for(int i = 0; i < hashPW.length; i++){
            if(hashPW[i] != storedPWHash.getBytes() [i]){
                throw new PasswordIncorrectException();
            }
        }
        
        return Integer.parseInt(plaintextSplit[0]);//length is at slot 0 of secret data
 
    }

    /**
     * Steps to consider...:<p>
     *  - verify password <p>
     *  - check check if requested starting position and length are valid <p>
     *  - decrypt content data of requested length 
     */
    @Override
    public byte[] read(String file_name, int starting_position, int len, String password) throws Exception {
        File root = new File(file_name);
        int file_length = length(file_name, password);//Calling length function checks password and sets initial vector
        if (starting_position + len > file_length) {
            throw new Exception();
        }

        int start_block = starting_position / Config.BLOCK_SIZE;

        int end_block = (starting_position + len) / Config.BLOCK_SIZE;

        String toReturn = "";

        for (int i = start_block + 1; i <= end_block + 1; i++) {
            String encryptedContent = byteArray2String(read_from_file(new File(root, Integer.toString(i))));
            String temp = decryption(encryptedContent);
            if (i == end_block + 1) {
                temp = temp.substring(0, starting_position + len - end_block * Config.BLOCK_SIZE);
            }
            if (i == start_block + 1) {
                temp = temp.substring(starting_position - start_block * Config.BLOCK_SIZE);
            }
            toReturn += temp;
        }
        
        
        return toReturn.getBytes("UTF-8");

    }
    

    
    /**
     * Steps to consider...:<p>
	 *	- verify password <p>
     *  - check check if requested starting position and length are valid <p>
     *  - ### main procedure for update the encrypted content ### <p>
     *  - compute new HMAC and update metadata 
     */
    @Override
    public void write(String file_name, int starting_position, byte[] content, String password) throws Exception {
        
        padMetaUser();//pad username
        padMetaPW();//pad and hash password
        
        String str_content = byteArray2String(content);//Original content
        File root = new File(file_name);
        int file_length = length(file_name, password);//Calling length checks password, and sets this.initial vector to the one provided in metadata
        
        if (starting_position > file_length) {//Must start inside file
            throw new Exception();
        }
        
        
        int len = str_content.length();
        int start_block = starting_position / Config.BLOCK_SIZE;
        int end_block = (starting_position + len) / Config.BLOCK_SIZE;
        
        
        for (int i = start_block + 1; i <= end_block + 1; i++) {
            int sp = (i - 1) * Config.BLOCK_SIZE - starting_position;
            int ep = (i) * Config.BLOCK_SIZE - starting_position;
            String prefix = "";
            String postfix = "";
            if (i == start_block + 1 && starting_position != start_block * Config.BLOCK_SIZE) {
                String encryptedContent = byteArray2String(read_from_file(new File(root, Integer.toString(i))));
                prefix = decryption(encryptedContent);
                prefix = prefix.substring(0, starting_position - start_block * Config.BLOCK_SIZE);
                sp = Math.max(sp, 0);
            }

            if (i == end_block + 1) {
                File end = new File(root, Integer.toString(i));
                if (end.exists()) {
                    
                    postfix = byteArray2String(read_from_file(new File(root, Integer.toString(i))));

                    if (postfix.length() > starting_position + len - end_block * Config.BLOCK_SIZE) {
                        postfix = postfix.substring(starting_position + len - end_block * Config.BLOCK_SIZE);
                    } else {
                        postfix = "";
                    }
                }
                ep = Math.min(ep, len);
            }

            String toWrite = prefix + str_content.substring(sp, ep) + postfix;

            while (toWrite.length() < Config.BLOCK_SIZE) {
                toWrite += '\0';
            }
            String encryptWrite = encryption(toWrite, initialVector);//Encrypt all content data for  block file
            save_to_file(encryptWrite.getBytes(), new File(root, Integer.toString(i)));
        }


        //update meta data

        if (content.length + starting_position > length(file_name, password)) {
            String s = byteArray2String(read_from_file(new File(root, "0")));
            String[] strs = s.split("\n");//[0]: username, [1] secretdata, [2]HMAC hash
            
            
            String plaintext = decryption(strs[1]);//Decrypt the secret data
            String[] plaintextSplit = plaintext.split("\n");//[0] length [1]password hash
            
            String newLength = Integer.toString(content.length + starting_position);
            
            //Ecrypt secretData
            String secretData = "";
            secretData += newLength + "\n"; //length
            secretData += plaintextSplit[1];//PWHash
            String cipher_secretData = encryption(secretData, initialVector);
            
            //Writing into metadata
            String toWrite = "";
            toWrite = strs[0] + "\n";   //add padded username [0]

            toWrite += cipher_secretData + "\n"; //[1]

            String hashReturn = HMAC(toWrite);//Compute HMAC for integrity check
            toWrite += hashReturn + "\n";//[2] 

            while (toWrite.length() < Config.BLOCK_SIZE) {
                toWrite += '\0';
            }
            save_to_file(toWrite.getBytes(), new File(root, "0"));

        }
    
    
    }

    /**
     * Steps to consider...:<p>
  	 *  - verify password <p>
     *  - check the equality of the computed and stored HMAC values for metadata and physical file blocks<p>
     */
    @Override
    public boolean check_integrity(String file_name, String password) throws Exception {
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");//[0]: username, [1] secretdata, [2]HMAC hash
        
        this.initialVector = findIVFromCipher(strs[1]);
        String ciphertext = strs[1];
        
        String plaintext = decryption(ciphertext);//Decrypt the secret data
        
        String[] plaintextSplit = plaintext.split("\n");
        String storedPWHash = plaintextSplit[1];
         //Check password
        padMetaPW();//Calculate hash for entered password
        for(int i = 0; i < hashPW.length; i++){
            if(hashPW[i] != storedPWHash.getBytes() [i]){
                throw new PasswordIncorrectException();
            }
        }
        String hashReturn = HMAC(strs[0]+strs[1]);//Compute HMAC for integrity check
        for(int i = 0; i < hashReturn.length(); i++){
            if(hashReturn.charAt(i) != strs[2].charAt(i)){
                return true;//Yes, there is modification
            }
        }
        return false;//Finish all char by char check and no modification found.
 
  }

    /**
     * Steps to consider... <p>
     *  - verify password <p>
     *  - truncate the content after the specified length <p>
     *  - re-pad, update metadata and HMAC <p>
     */
    @Override
    public void cut(String file_name, int len, String password) throws Exception {
        File root = new File(file_name);
        int file_length = length(file_name, password);//Calling length function sets IV and checks password

        if (len > file_length) {
            throw new Exception();
        }
        int end_block = (len) / Config.BLOCK_SIZE;

        File file = new File(root, Integer.toString(end_block + 1));
        String encryptedStr = byteArray2String(read_from_file(file));
        
        String str = decryption(encryptedStr);
        
        str = str.substring(0, len - end_block * Config.BLOCK_SIZE);
        while (str.length() < Config.BLOCK_SIZE) {
            str += '\0';
        }
        String encryptData = encryption(str,initialVector);
        save_to_file(encryptData.getBytes(), file);

        int cur = end_block + 2;
        file = new File(root, Integer.toString(cur));
        while (file.exists()) {
            file.delete();
            cur++;
        }

        //update meta data
        String s = byteArray2String(read_from_file(new File(root, "0")));
        String[] strs = s.split("\n");//[0]: username, [1] secretdata, [2]HMAC hash
        String plaintext = decryption(strs[1]);//Decrypt the secret data
        String[] plaintextSplit = plaintext.split("\n");//[0] length [1]password hash
            
        String newLength = Integer.toString(len);

        //Ecrypt secretData
        String secretData = "";
        secretData += newLength + "\n"; //length
        secretData += plaintextSplit[1];//PWHash
        String cipher_secretData = encryption(secretData, initialVector);
        
         //Writing into metadata
        String toWrite = "";
        toWrite = strs[0] + "\n";   //add padded username [0]

        toWrite += cipher_secretData + "\n"; //[1]

        String hashReturn = HMAC(toWrite);//Compute HMAC for integrity check
        toWrite += hashReturn + "\n";//[2] 

        while (toWrite.length() < Config.BLOCK_SIZE) {
            toWrite += '\0';
        }
        save_to_file(toWrite.getBytes(), new File(root, "0"));
    }
  
}

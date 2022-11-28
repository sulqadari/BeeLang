package ru.beelang.utils;

public class Hlp
{
    public static byte[] toByteArray(String input) throws StringIndexOutOfBoundsException
	{
		input = input.toUpperCase().replaceAll("\\s+", "");
		int len = input.length();					
		
		if (len % 2 != 0)
        {
		    input = "0" + input;
            //throw new StringIndexOutOfBoundsException("ERROR: The length of the input string is odd.");
            len++;
        }
		byte[] buff = new byte[len/2];
		int bufIdx = 0, i = 0;							
		char msn = '0', lsn = '0';							
		byte compiledByte = (byte)0x00;					

		while(i < len)
		{
			msn = input.charAt(i); i++;
			lsn = input.charAt(i); i++;
			compiledByte = (byte)((Character.digit(msn, 16) << 4) | (Character.digit(lsn, 16)));
			buff[bufIdx]	= compiledByte;				
			bufIdx++;
		}
		
		return buff;
	}
}

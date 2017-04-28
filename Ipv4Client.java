import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.ByteBuffer;


public class Ipv4Client{
	public static void main(String[] args)throws Exception{
		
		try(Socket socket = new Socket("codebank.xyz",38003)){
			byte version = 4;//IP version 4
			byte headerLength = 5; //5 line of 32 bits header
			byte tos = 0; //do not implement
			short length; //total length
			short ident = 0; //do not implement
			short flag = 2; //010 for no fragmentation
			short offset = 0; //do not implement
			byte ttl = 50; // assuming every packet has a TTL of 50
			byte protocal = 6; //TCP
			short checksum ; //0 initially
			int sourceAddress= 1234; //random source address
			int destinationAddress = socket.getInetAddress().hashCode();	//address of server
			byte[] data; //use zero
			InputStream fromServer = socket.getInputStream(); //receive stream from server
			InputStreamReader ibs = new InputStreamReader(fromServer);
			BufferedReader br = new BufferedReader(ibs); //read stream
			OutputStream toServer = socket.getOutputStream(); //send stream to server
			short dataLength = 2;//initialize data length with 2
			int counter = 1; // counter count up to 12
			//Send packet to server, double length of data each time for a total of 12 time
			while(counter <= 12){
				checksum = 0;  //set checksum to 0 at the beginning
				// initialize data array to all zeros
				data = new byte[dataLength];
				for(int i = 0; i < dataLength; i ++){
					data[i] = 1;
				}
				//total length = 20 bytes from header and the length of the data.
				length = (short)(headerLength * 4 + dataLength);
				//packet array is the packet that will send to server. This will have the correct checksum
				byte[] packet = new byte[length]; 
				//The array is only the bits in header, with checksum equals zero. This is use to calculate the correct checksum.
				byte[] header = new byte[headerLength*4];
				//wrap both array to byteBuffer.
				ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
				ByteBuffer forCheckSum = ByteBuffer.wrap(header);
				//shift version 4 bit left, or it with headerlength to form  the first eight bits and store in packet
				byteBuffer.put((byte)((byte)(version & 0xf) << 4 | (byte)headerLength & 0xf));
				forCheckSum.put((byte)((byte)(version & 0xf) << 4 | (byte)headerLength & 0xf));
				//put TOS to packet
				byteBuffer.put(tos);
				forCheckSum.put(tos);
				//put Total Length to packet
				byteBuffer.putShort(length);
				forCheckSum.putShort(length);
				//put Ident to packet
				byteBuffer.putShort(ident);
				forCheckSum.putShort(ident);
				// concatenate flag and offset to packet
				byteBuffer.putShort((short)((flag & 0x7) << 13 | offset & 0x1fff));
				forCheckSum.putShort((short)((flag & 0x7) << 13 | offset & 0x1fff));
				//put TTL to packet
				byteBuffer.put(ttl);
				forCheckSum.put(ttl);
				//put protocal to packet
				byteBuffer.put(protocal);
				forCheckSum.put(protocal);
				//put check sum(0) only to heaader.
				forCheckSum.putShort(checksum);
				//put source address to header.
				forCheckSum.putInt(sourceAddress);
				//put destination address to header.
				forCheckSum.putInt(destinationAddress);
				//get checksum of the header.
				checksum = checkSum(forCheckSum.array(), forCheckSum.array().length);
				//put this checksum to packet.
				byteBuffer.putShort(checksum);
				//put source address to packet
				byteBuffer.putInt(sourceAddress);
				//put destination address to
				byteBuffer.putInt(destinationAddress);
				//put data to packet
				byteBuffer.put(data);
				//send packet to server
				toServer.write(byteBuffer.array());
				System.out.println("data length: " + dataLength);
				//Print out response from server
				System.out.println(br.readLine());
				//double data length
				dataLength = (short)(dataLength*2);
				//increment data by 1
				counter += 1;
			}
		}
	}
	//Return checksum in short
	public static short checkSum(byte[] message, int length) {
	    int i = 0;
	    long sum = 0;
	    while (length > 0) {
	        sum += (message[i]&0xff) << 8;
	        i++;
	        length--;
	        if ((length)==0) break;
	        sum += (message[i++]&0xff);
	        length--;
	    }
	    sum = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF;
	    short cs = (short)(sum & 0xffff);
		return cs;
	}
}

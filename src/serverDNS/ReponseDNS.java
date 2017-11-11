package serverDNS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class ReponseDNS {

	public static final int tailleNom = 2;//octets
	public static final int tailleType= 2;//octets
	public static final int tailleClasse=2;//octets
	public static final int tailleTTL=4;//octets
	public static final int tailleRDLength=2;//octets
	public static int tailleRDData=0;//RDLength octets
	
	public static byte[] Nom = new byte[ReponseDNS.tailleNom];
	public static byte[] Type = new byte[ReponseDNS.tailleType];
	public static byte[] Classe = new byte[ReponseDNS.tailleClasse];
	public static byte[] TTL = new byte[ReponseDNS.tailleTTL];
	public static byte[] RDLength = new byte[ReponseDNS.tailleRDLength];
    public static byte[] RDData = new byte[ReponseDNS.tailleRDData];
    
    public static List<String> IP_addr = new ArrayList<String>();
    
	public static List<String> getIP_addr() {
		return IP_addr;
	}


	private DatagramSocket ds;
	private int tailleMessage;

	public DatagramSocket getDs() {
		return ds;
	}

	public int getTailleMessage() {
		return tailleMessage;
	}

	public ReponseDNS(DatagramSocket pds,int ptailleMessage){
		this.ds=pds;
		this.tailleMessage=ptailleMessage;
	}

	public String decoderLabel(byte[] mess){
		String res ="";
		char c;
		int prochainPoint = mess[0] & 0xff;
		for(int i=0;i<mess.length;i++){
			if(prochainPoint!=0){
				String temp = Integer.toString((mess[i]&0xff)+0x100,16).substring(1);
				int temp2 = Integer.parseInt(temp,16);
				c = (char) temp2;
				res += c;
				prochainPoint--;
			}else{
				prochainPoint = mess[i+1] & 0xff;
				res += (char) 46;
				i++;
			}
		}
		return res;
	}
	
	public String decoderIP(byte[] IP){
		String ip="";
		ip += IP[0] & 0xff;
		for(int i=1;i<IP.length;i++){
			ip += "."+ (IP[i] & 0xff);
		}
		return ip;
	}
	
	
	public void ecoute(){
		byte[] buffer = new byte[1024];
		DatagramPacket dp = new DatagramPacket(buffer,buffer.length);
		dp.setPort(RequeteDNS.portUDP);
		try {
			this.ds.receive(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* ANALYSE DE LA TRAME reponse du serveur DNS pour la Question 4 */
		/*
		for(byte b: dp.getData()){
			int res = b & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}*/
		System.out.println("\nMessage reçu avec Analyse de la trame complete : \n");
		// En tete de la trame de retour sur 12 octets
		System.out.print("En-tête : ");
		for (int i=0;i<RequeteDNS.size_enTete;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		// Question retourner dans la reponse du serveur DNS
		System.out.print("Question : ");

		for(int i=RequeteDNS.size_enTete;i<RequeteDNS.size_enTete+RequeteDNS.size_question;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		// Type et Classe de la question renvoyée dans la reponse du serveur DNS
		System.out.print(" - Type : ");
		for(int i=RequeteDNS.size_enTete+RequeteDNS.size_question;i<RequeteDNS.size_enTete+RequeteDNS.size_question+2;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		System.out.print(" - Class : ");
		for(int i=RequeteDNS.size_enTete+RequeteDNS.size_question+2;i<RequeteDNS.size_enTete+RequeteDNS.size_question+4;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		System.out.println("Réponse : ");

		// Nom codé sur 16 bits
		System.out.print(" - Nom : ");
		for(int i=RequeteDNS.size_message;i<RequeteDNS.size_message+ReponseDNS.tailleNom;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Nom[(RequeteDNS.size_message+ReponseDNS.tailleNom - i)% ReponseDNS.tailleNom] = buffer[i];
		}
		System.out.println();

		// Type et Class de la reponse
		System.out.print(" - Type : ");
		for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Type[(RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType - i)% ReponseDNS.tailleType] = buffer[i];
		}
		System.out.println();
		
		System.out.print(" - Class : ");
		for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//Time to Live
		System.out.print(" - TTL (durée de vie de l'entrée) : ");
		for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDLength codé sur 16 bits
		System.out.print(" - RDLength : ");
		for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i++){
			int res = buffer[i] & 0xff;
			ReponseDNS.tailleRDData += res;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDData codé sur RDLength octets
		System.out.print(" - RDData : ");
		for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//CNAME si le type vaut 00 05 (Canonical name for an alias)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("05")){
			// On recupere la position de l'offset dans le nom apres le CO
			//String temp = Integer.toString((ReponseDNS.Nom[1]&0xff)+0x100,16).substring(1);
			//int temp2 = Integer.parseInt(temp,16);
			// On creer un buffer provisoire pour stocker le codage de l'alias canonical name
			byte[] bufftemp = new byte[RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength - 2 )];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2)] = buffer[i];
			}
			// On decode le CNAME avec le contenu du buffer temporaire
			String CNAME = this.decoderLabel(bufftemp);
			// Ensuite on l'affiche :
			System.out.println(" - Primary name: "+CNAME);
		}
		//A si le type vaut 00 01 (Adresse de l'hote)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("01")){
			// On creer un buffer provisoire pour stocker l'adresse de l'hote
			byte[] bufftemp = new byte[RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength )];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i=RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength)] = buffer[i];
			}
			// On decode l'adresse de l'hote avec le contenu du buffer temporaire
			String IP = this.decoderIP(bufftemp);
			ReponseDNS.IP_addr.add(IP);
			// Ensuite on l'affiche :
			System.out.println(" - Addr: "+IP);
		}
		
		
		/* AUTORITE */
		int autority = RequeteDNS.size_message+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;

		System.out.println("Autorité : ");
		//Initialisation
		ReponseDNS.tailleRDData = 0;
		ReponseDNS.Nom[0]=ReponseDNS.Nom[1]= (byte) 0x00;
		ReponseDNS.Type[0]=ReponseDNS.Type[1]= (byte) 0x00;
		
		// Nom codé sur 16 bits
		System.out.print(" - Nom : ");
		for(int i=autority;i<autority+ReponseDNS.tailleNom;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Nom[(autority+ReponseDNS.tailleNom - i)% ReponseDNS.tailleNom] = buffer[i];
		}
		System.out.println();

		// Type et Class de la reponse
		System.out.print(" - Type : ");
		for(int i=autority+ReponseDNS.tailleNom;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Type[(autority+ReponseDNS.tailleNom+ReponseDNS.tailleType - i)% ReponseDNS.tailleType] = buffer[i];
		}
		System.out.println();
		System.out.print(" - Class : ");
		for(int i=autority+ReponseDNS.tailleNom+ReponseDNS.tailleType;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//Time to Live
		System.out.print(" - TTL (durée de vie de l'entrée) : ");
		for(int i=autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDLength codé sur 16 bits
		System.out.print(" - RDLength : ");
		
		for(int i=autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i++){
			int res = buffer[i] & 0xff;
			ReponseDNS.tailleRDData += res;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDData codé sur RDLength octets
		System.out.print(" - RDData : ");
		for(int i=autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();


		//CNAME si le type vaut 00 05 (Canonical name for an alias)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("05")){
			// On creer un buffer provisoire pour stocker le codage de l'alias canonical name
			byte[] bufftemp = new byte[autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2)];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i= autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2)] = buffer[i];
			}
			// On decode le CNAME avec le contenu du buffer temporaire
			String CNAME = this.decoderLabel(bufftemp);
			// Ensuite on l'affiche :
			System.out.println(" - Primary name: "+CNAME);
		}
		//A si le type vaut 00 01 (Adresse de l'hote)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("01")){
			// On creer un buffer provisoire pour stocker l'adresse de l'hote
			byte[] bufftemp = new byte[autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength )];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i=autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength)] = buffer[i];
			}
			// On decode l'adresse de l'hote avec le contenu du buffer temporaire
			String IP = this.decoderIP(bufftemp);
			ReponseDNS.IP_addr.add(IP);
			// Ensuite on l'affiche :
			System.out.println(" - Addr: "+IP);
		}



		/* ADDITIONNEL */
		int additionnel = autority+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;

		System.out.println("Additionnel : ");
		//Initialisation
		ReponseDNS.tailleRDData = 0;
		ReponseDNS.Nom[0]=ReponseDNS.Nom[1]= (byte) 0x00;
		ReponseDNS.Type[0]=ReponseDNS.Type[1]= (byte) 0x00;
		
		// Nom codé sur 16 bits
		System.out.print(" - Nom : ");
		for(int i=additionnel;i<additionnel+ReponseDNS.tailleNom;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Nom[(additionnel+ReponseDNS.tailleNom - i)% ReponseDNS.tailleNom] = buffer[i];
		}
		System.out.println();

		// Type et Class de la reponse
		System.out.print(" - Type : ");
		for(int i=additionnel+ReponseDNS.tailleNom;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
			ReponseDNS.Type[(additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType - i)% ReponseDNS.tailleType] = buffer[i];
		}
		System.out.println();
		System.out.print(" - Class : ");
		for(int i=additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//Time to Live
		System.out.print(" - TTL (durée de vie de l'entrée) : ");
		for(int i=additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDLength codé sur 16 bits
		System.out.print(" - RDLength : ");
		
		for(int i=additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i++){
			int res = buffer[i] & 0xff;
			ReponseDNS.tailleRDData += res;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		//RDData codé sur RDLength octets
		System.out.print(" - RDData : ");
		for(int i=additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
			int res = buffer[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();	
		
		//CNAME si le type vaut 00 05 (Canonical name for an alias)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("05")){
			// On creer un buffer provisoire pour stocker le codage de l'alias canonical name
			byte[] bufftemp = new byte[additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2)];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i= additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength  - 2)] = buffer[i];
			}
			// On decode le CNAME avec le contenu du buffer temporaire
			String CNAME = this.decoderLabel(bufftemp);
			// Ensuite on l'affiche :
			System.out.println(" - Primary name: "+CNAME);
		}
		//A si le type vaut 00 01 (Adresse de l'hote)
		if(Integer.toString(ReponseDNS.Type[1]+0x100,16).substring(1).equals("01")){
			// On creer un buffer provisoire pour stocker l'adresse de l'hote
			byte[] bufftemp = new byte[additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData - (additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength )];
			// On initialise le buffer temporaire avec les octets RDData à la position de l'offset
			for(int i=additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength;i<additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength+ReponseDNS.tailleRDData;i++){
				bufftemp[i-(additionnel+ReponseDNS.tailleNom+ReponseDNS.tailleType+ReponseDNS.tailleClasse+ReponseDNS.tailleTTL+ReponseDNS.tailleRDLength)] = buffer[i];
			}
			// On decode l'adresse de l'hote avec le contenu du buffer temporaire
			String IP = this.decoderIP(bufftemp);
			ReponseDNS.IP_addr.add(IP);
			// Ensuite on l'affiche :
			System.out.println(" - Addr hote : "+IP);
		}

		/*
		System.out.println();
		//Extraction des autres adresses IP :
		System.out.println("Autres adresse IP presente dans le paquet : ");
		for(int i=0;i<dp.getData().length;i++){
			int v = dp.getData()[i] & 0xff;
			String val = Integer.toString(v+0x100, 16).substring(1);
			if(val.equals("04")){
				System.out.println(" - Addr : "+(dp.getData()[i+1] & 0xff)+"."+(dp.getData()[i+2] & 0xff)+"."+(dp.getData()[i+3] & 0xff)+"."+(dp.getData()[i+4] & 0xff));
			}
		}*/

	}

	
}

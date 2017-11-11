package serverDNS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class RequeteDNS {

	/* CONSTANTES et VARIABLES D'INSTANCES */
	//public static final String IP_DNS = "192.168.1.1"; // a modifier selon l'adresse de votre serveur DNS
	public static final int portUDP = 53; // Le port UDP

	/* ATTRIBUTS DE NOTRE REQUETE DNS */

	private DatagramSocket ds; // Socket pour la connexion reseau
	private DatagramPacket dp; // Le datagramme que l'on va envoyer

	public static byte[] message; // Le message contenu dans le datagramme
	public static int size_message; // la taille du message contenu dans le datagramme
	public static int size_enTete; // la taille de l'en-tete du message
	public static int size_question; // la taille de la question envoyée au serveur DNS
	public static int size_typeEtClass; // la taille du suffixe de la question

	private String label; // L'adresse symbolique que l'on cherche à résoudre
	private InetAddress ip_dns; // Adresse IP du serveur DNS que l'on va questionner

	/* GETTERS & SETTERS */
	

	public DatagramSocket getDs() {return ds;}
	public DatagramPacket getDp() {return dp;}

	public String getLabel() {return label;}
	public InetAddress getIp_dns() {return ip_dns;}
	public void setIp_dns(InetAddress ip_dns) {this.ip_dns = ip_dns;}

	/**
	 * Constructeur d'initialisation de notre classe
	 * @param label l'adresse symbolique que l'on souhaite envoyer dans la requette DNS avec le protocole UDP
	 */
	/* CONSTRUCTEUR D'INITIALISATION */
	public RequeteDNS(String label,String ipDNS){
		this.label = label;
		try {
			this.ds = new DatagramSocket();
			this.setIp_dns(InetAddress.getByName(ipDNS));
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("Impossible de creer le DatagramSocket");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Cette IP : "+ipDNS+" est inconnue.");
			e.printStackTrace();
		}
		// On construit le message
		this.creerMessage(this.getLabel());
		// et on le place dans le datagramme avec le protocole UDP et l'adresse du LIFL
		this.dp = new DatagramPacket(RequeteDNS.message,RequeteDNS.message.length,this.getIp_dns(),portUDP);
	}

	/* METHODE DE NOTRE CLASSE RequeteDNS POUR NOS TRAITEMENTS */

	/**
	 * Methode qui permet d'envoyer un datagramme par l'intermediaire d'une socket datagramme
	 */
	//Methode d'envoie d'une requete
	public void envoyerRequete(){
		try {
			this.getDs().send(this.getDp());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Methode de creation du message de requete DNS que l'on va encapsuler dans notre datagramme
	 * @param label l'adresse symbolique que l'on souhaite introduire dans la requete DNS
	 */
	//Methode qui creer le message pour la requete DNS
	public void creerMessage(String label){

		// Une liste de Byte car on ne connait pas la taille du message à l'avance
		List<Byte> message = new ArrayList<Byte>();

		//On creer d'abord l'en tete du message
		byte[] enTete = this.creerEnTete();
		RequeteDNS.size_enTete = enTete.length;
		//On la concatene au message
		for(byte b: enTete) message.add(new Byte(b));

		//On creer le message
		byte[] labelEncode = this.encoderLabel(label);
		RequeteDNS.size_question = labelEncode.length;
		//On la concatene au message
		for(byte b: labelEncode) message.add(new Byte(b));

		//Finalement on ajoute le type et la class
		byte[] typeEtClasse = this.creerTypeClasse();
		RequeteDNS.size_typeEtClass = typeEtClasse.length;
		//On la concatene au message
		for(byte b: typeEtClasse) message.add(new Byte(b));

		//On transforme la liste en tableau et on l'affecte à notre variable d'instance message
		//de notre classe RequeteDNS
		byte[] res = new byte[message.size()]; 
		for(int i=0;i<message.size();i++) 
			res[i]= message.get(i);
		RequeteDNS.message = res;
		RequeteDNS.size_message = res.length;
	}

	/**
	 * Methode de creation de l'en-tete de notre requete DNS
	 * @return un tableau d'octets representant l'en-tete de notre requete DNS
	 */
	//methode de création de l'entete de notre requete DNS
	private byte[] creerEnTete() {
		// L'enTete est composé de 6 champs composé chacun de 16bits
		// donc on a 16*6 = 96bits = 12 octets au total
		byte[] monEnTete = new byte[12];

		// Le champ IDENTIFIANT est un entier permettant d'identifier la requete
		// codé sur 16 bits = 2 octets
		monEnTete[0]= (byte) 0x08; //08 => 0x08 en héxadecimal en java
		monEnTete[1]= (byte) 0xbb;

		// Le champ PARAMETRES codé sur 16 bits contient les champs suivants :
		/*
QR(1bit)  | OPCODE(4bits) | AA(1bit)  | TC(1bit) | RD(1bit)  | RA(1bit)   | UNUSED,AD,CD(3bits) | RCODE(4bits)
0:question   0000:requete   1:serveur  1:message  1:demande    1:indique                          code de retour      
                  simple    de réponse   tronqué    requete    que le                             0: OK
1:reponse    0001:requete   avec                    recursive  serveur                            1: erreur format de la requete
                  inverse   autorité   0:sinon    0:sinon      peut faire       non utilisés      2: problème du serveur
               		        0:sinon                            une demande      (placé à 0 dans   3: nom de domaine non trouvé
               		                                           recursive.        tout les cas)      (valide seulement si AA=1)
               		                                           0:sinon                            4: requete non supportée
               		                                                                              5: le serveur refuse de répondre
               		                                                                              (raisons de sécurité ou autres)
		 */
		// Nous voulons avoir comme parametres ceci:
		// QR:0 OPCODE:0000 AA:0 TC:0 RD:1 RA:0 UNUSED,AD,CD:000 RCODE:0000
		// ce qui nous donne en binaire : 0000 0001 0000 0000
		// ce qui nous donne en Héxadec :   0    1    0    0
		monEnTete[2]= (byte) 0x01;
		monEnTete[3]= (byte) 0x00;

		// Le champ QDCOUNT qui nous indique le nombre de questions posées codé sur 16 bits
		// Ici on pose une seule question donc 0000 0000 0000 0001 (base2) => 00 01 (base16)
		monEnTete[4]= (byte) 0x00;
		monEnTete[5]= (byte) 0x01;

		// Les champs :
		// ANCOUNT, nombre d'entrées dans les champs "Réponse"
		// NSCOUNT, nombre d'entrées dans les champs "Autorité"
		// ARCOUNT, nombre d'entrées dans les champs "Additionnel"

		// Ici on requete le serveur DNS du LIFL donc ces 3 champs sont vides, il se completeront dans la reponse
		for(int i=6;i<monEnTete.length;i++) monEnTete[i] = (byte) 0x00;

		// Finalement on retourne l'en Tete de notre requete DNS ainsi crée
		return monEnTete;
	}


	//methode de création des derniers champs de notre requete DNS
	private byte[] creerTypeClasse() {
		// TYPE + CLASS = 32bits = 4 octets
		byte[] dernier = new byte[4];
		// Le champ TYPE code sur 16 bits pour indiquer le type de la requete :
		/* Entrée   Valeur                                 Désignation 
   A          01              Adresse de l'hote
   NS         02              Nom du serveur de noms pour ce domaine
   MD         03              Messagerie (obsolete par l'entrée MX)
   MF         04              Messagerie (obsolete par l'entrée MX)
   CNAME      05              Nom canonique (Nom pointant sur un autre nom)
   SOA        06              Début d'une zone d'autorité (informations générales sur la zone)
   MB         07              Une boite a lettre du nom de domaine (experimentale)
   MG         08              Membre d'un groupe de mail (experimentale)
   MR         09              Alias pour un site (experimentale)
   NULL       10              Enregistrement à 0 (experimentale)
   WKS        11              Services Internet connus sur la machine
   PTR        12              Pointeur vers un autre espace du domaine (resolution inverse)
   HINFO      13              Description de la machine
   MINFO      14              Groupe de boite a lettres
   MX         15              Mail exchange (Indique le serveur de messagerie, voir [RFC-974]) 
   TXT		  16              Chaines de caractere
		 */
		// Ici on a host adress donc Entrée : A Valeur : 01
		// En binaire on a : 0000 0000 0000 0001
		// En Héxadecimal  :   0    0    0    1
		dernier[0]= (byte) 0x00;
		dernier[1]= (byte) 0x01;
		// Le champ CLASS code sur 16 bits pour indiquer le type de protocole
		/* Entrée   Valeur                                 Désignation
   In         01               Internet
   Cs         02               Class Csnet (obsolete)
   Ch         03               Chaos (chaosnet est un ancien réseau qui historiquement a eu une grosse influence sur le developpement de l'internet, il n'est plus utilise).
   Hs         04               Hesiod
		 */
		// Ici on utilise le protocole Internet donc Entrée: In Valeur 01
		// En binaire on a : 0000 0000 0000 0001
		// En Héxadecimal  :   0    0    0   1
		dernier[2]= (byte) 0x00;
		dernier[3]= (byte) 0x01;
		// Finalement on retourne le suffixe de notre requete DNS ainsi crée
		return dernier;
	}

	/**
	 * Encodage d'un chaine de caractere designant une adresse symbolique en un tableau d'octets
	 * @param label la chaine representant l'adresse symbolique que l'on souhaite encoder
	 * @return le tableau d'octet representant le label encode
	 */
	//methode d'encodage de l'adresse symbolique en héxadecimal
	private byte[] encoderLabel(String label) {
		/* Ici le but de notre methode est de pouvoir encoder pour toute adresse symbolique donnée en parametre
   un codage héxadecimal definit comme dans le sujet du TP qui peut se resumer de la maniere suivante:

   Exemple:

   adresse symbolique sous le label :  

        www.lifl.fr +'\0'   3caracteres . 4caracteres . 2caracteres + caractere de fin de chaine

   avec :
   - ASCII('w') = 119 (base 10) = 77 (base 16)
   - ASCII('l') = 108 (base 10) = 6C (base 16)
   - ASCII('i') = 105 (base 10) = 69 (base 16)
   - ASCII('f') = 102 (base 10) = 66 (base 16)
   - ASCII('r') = 114 (base 10) = 72 (base 16)
   - ASCII('\0') = 0  (base 10) = 00 (base 16)

   sera alors encodé :
   -  www     =>  03 77 77 77
   - .lifl    =>  04 6C 69 66 6C
   - .fr+'\0' =>  02 66 72 00

   On remarque que pour un label de 11 caractere on a besoin d'un tableau de 13 octets,
   en generalisant on en deduit qu'il nous faut un tableau de label.length + 2 .

   Nous avons donc besoin de connaitre à quel position se situe le prochain '.' afin de connaitre le nombre 
   de caracteres suivant à encoder et egalement de savoir coder un caractere en sa valeur héxadecimale.
		 */
		//le point suivant
		int prochainPoint;
		//Le tableau d'octet a renvoyer une fois le label encode
		byte[] res = new byte[label.length()+2];
		/*
		 ALGORITHME DE CODAGE :
		 DEBUT
		  ALGO <-- chaine de caractere representant le label en parametre
		  ALGO --> tableau d'octets representant cette chaine apres son encodage

		  INITIALISATION:
		   prochainPoint <-- nextDot(label,0)
		   res[0] <-- valeur de prochainPoint en hexadecimale

		  POUR (i = 0 , i < taille(label) , i <- i + 1) FAIRE
		    SI (prochainPoint est different de 0) ALORS
		      debut
		        res[i+1] <-- valeur de label[i] en hexadecimale
		        prochainPoint <-- prochainPoint - 1
		      fin
		    SINON (on relance la recherche du prochain point et on encode la valeur)
		      debut
		        prochainPoint <-- nextDot(label, i+1)
		        res[i+1] <-- valeur de prochainPoint en hexadecimale 
		      fin
		    FINSI
		  FINPOUR
		  res[taille(label)+1]= valeur de fin de chaine en hexadecimale
		  ALGO --> res
		 FIN
		 */
		// ALGORITHME DE CODAGE EN LANGAGE JAVA
		prochainPoint = RequeteDNS.nextDot(label, 0);
		res[0] = (byte) prochainPoint;
		for(int i=0;i<label.length();i++){
			if(prochainPoint != 0){
				res[i+1]=(byte)label.charAt(i);
				prochainPoint--;
			}
			else{
				prochainPoint=RequeteDNS.nextDot(label, i+1);
				res[i+1]=(byte)prochainPoint;
			}
		}
		res[res.length-1]=(byte)0x00;
		return res;
	}

	/**
	 * Determine le nombre de caractere à encoder avant de rencontrer le prochain point
	 * @param label notre adresse symbolique 
	 * @param pos Position initiale du curseur dans la recherche de la chaine de caractere
	 * @return nombre de caractere disponible avant de rencontrer un point ou avant de rencontrer la fin de la chaine
	 */
	public static int nextDot(String label,int pos){
		for(int i = pos;i<label.length();i++){
			// Si on rencontre un point
			if (label.charAt(i)=='.') 
				// On retourne la position du curseur moins la position initiale :
				// Exemple : nextDot(www.google.fr,0) = 3 - 0 = 3 = nbre de caractere avant le prochain point = "www"
				return i - pos;
		}
		//Dans le cas ou il n'y a plus de separateur '.' a rencontrer dans la chaine
		//on se trouve forcement en fin de chaine et on renvoi donc sa taille moins la position initiale.
		// Exemple : nextDot(www.google.fr,11) = 13 - 11 = 2 = nbre de caractere avant la fin de la chaine = "fr"
		return label.length() - pos;
	}
	/**
	 * Methode d'affichage
	 */
	public void afficherMessage(){
		System.out.println("\nMessage envoyé : \n");
		/* ANALYSE DE LA TRAME requete envoyer au serveur DNS pour la Question 3 */

		// En tete de la trame de requete sur 12 octets
		System.out.print("En-tête : ");
		for (int i=0;i<RequeteDNS.size_enTete;i++){
			int res = RequeteDNS.message[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		// Question envoyée dans la requete au serveur DNS
		System.out.print("Question : ");
		for(int i=RequeteDNS.size_enTete;i<RequeteDNS.size_enTete+RequeteDNS.size_question;i++){
			int res = RequeteDNS.message[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();

		// Type et Classe envoyée dans la requete au serveur DNS
		System.out.print("Type : ");
		for(int i=RequeteDNS.size_enTete+RequeteDNS.size_question;i<RequeteDNS.size_enTete+RequeteDNS.size_question+2;i++){
			int res = RequeteDNS.message[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();
		System.out.print("Class : ");
		for(int i=RequeteDNS.size_enTete+RequeteDNS.size_question+2;i<RequeteDNS.size_enTete+RequeteDNS.size_question+4;i++){
			int res = RequeteDNS.message[i] & 0xff;
			System.out.print(Integer.toString(res+0x100,16).substring(1)+" ");
		}
		System.out.println();
	}

	/* PROGRAMME PRINCIPAL */

	public static void main(String[] args){
		if (args.length == 2){

			// Question 3

			// On recupere les arguments de l'utilisateur
			String addr_symbolique = args[0];
			String addr_ip_serveur_DNS = args[1];
			// On creer notre requete DNS
			RequeteDNS ma_requete_DNS = new RequeteDNS(addr_symbolique,addr_ip_serveur_DNS);
			// et on l'envoie
			ma_requete_DNS.envoyerRequete();
			// On affiche ce que l'on vient d'envoyer au serveur DNS
			System.out.println("Trame emise sur le serveur DNS d'adresse IP : "+addr_ip_serveur_DNS);
			System.out.println("avec le label "+addr_symbolique+" selon le protocole UDP (port "+RequeteDNS.portUDP+").");
			ma_requete_DNS.afficherMessage();

			// Question 4
			ReponseDNS ma_reponse_DNS = new ReponseDNS(ma_requete_DNS.getDs(),RequeteDNS.size_message);
			ma_reponse_DNS.ecoute();
			

			//Question 5
			System.out.println();
			System.out.print(" - Addr symbolique : " +addr_symbolique+"\n");
			for(String ip_addr : ReponseDNS.getIP_addr()){
				System.out.println(
						           " - IP hote : " +ip_addr+"\n"+
						           " - en entier 32 bits : "+RequeteDNS.ipToInt(ip_addr)
						           );
			}
		}
		else 
			System.out.println("Usage : $ java exercice2.RequeteDNS <adresse symbolique> <IP DNS>");
	}
	// Question 5
	
	public static String intToIp(int i) {
        return ((i >> 24 ) & 0xFF) + "." +
               ((i >> 16 ) & 0xFF) + "." +
               ((i >>  8 ) & 0xFF) + "." +
               ( i        & 0xFF);
    }
	
	public static Long ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");

        long num = 0;
        for (int i=0;i<addrArray.length;i++) {
            int power = 3-i;

            num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));
        }
        return num;
    }
}
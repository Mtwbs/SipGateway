package Target;


import javax.sdp.*;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;

import gov.nist.javax.sip.header.HeaderFactoryImpl;

import gov.nist.javax.sip.header.ims.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import Target.PCS_RTP_Caller;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.test.BindingLifetimeTest;
import de.javawi.jstun.util.UtilityException;

/**
 * <p>This class is a UAC template.</p>
 * <p>Exemplifies the creation and parsing of the SIP P-Headers for IMS</p>
 *
 * <p>based on examples.simplecallsetup, by M. Ranganathan</p>
 * <p>issued by Miguel Freitas (IT) PT-Inovacao</p>
 */


public class local implements SipListener {
	
	private  ClientTransaction inviteTidClientCall;

    private static SipProvider sipProvider;
    
    private static SipProvider udpProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private ListeningPoint udpListeningPoint;

    private ClientTransaction inviteTid;
    
    private static Dialog dialog;
    
    private Dialog dialog2; //註冊

    private boolean byeTaskRunning;
    
    String transport = "udp";
    
    String peerHostPort = "192.168.0.180:5060";
    
	private Request request;
    
	long invco = 1;
	
	static String sdpData;
    
	private static long cseq = 0L;
    
    private static Header contactH;
    
    private static Dialog dialogCall;
    
    private static Dialog dialogCall2;// 重定向
    
    public Timer timer = new Timer();
    
    public static String localIP;
    private static String IPcallee;
    public static String RTPportcallee;
    public static String RTCPportcallee;
    public static int localRtpPort;
    public static int localRtcpPort;
    public static boolean inPhoneCallProcess = false;

    
    private static  String MyAddress;

    private static  int MyPort ;
    
    public static String Callee="Gateway";
    
    public static String Referer="sis";
    
    public static String Referee="Gateway";
    
    public static boolean inReferProcess = false;
    
  //  private static Referee notifier = new Referee();
    private int port = 6020;
    
    private EventHeader referEvent;
    
 //   protected Dialog dialog;
    
   // protected SipProvider udpProvider;
    
    public static String tempTargetDialogID;
    
    public static Dialog tempTargetDialog;
    
    public static String referto;
    
    private static ClientTransaction inviteTidClient;

	private int set;
	
	static boolean flag = true;
	
	static boolean flag2 = true;
	
	 private ClientTransaction tid2;
	 
	 private static BindingLifetimeTest getstun = new BindingLifetimeTest("192.168.0.181", 3478);
	 
	 private static Shootisttest shoot = new Shootisttest();

	 private static Referee Refer = new Referee();
	 ResponseEvent responseFirstEvent;
	 
	  private void STUNPut()
			    throws SocketException, UnknownHostException, MessageAttributeParsingException, MessageHeaderParsingException, UtilityException, IOException, MessageAttributeException
			  {
			    getstun.test();
			    MyPort = getstun.ma.getPort();
			    MyAddress = getstun.ma.getAddress().toString();
			    
			    System.out.println("MyPort"+MyPort);
			    System.out.println("MyAddress"+MyAddress);
			  }
    
    public void getlocalIP()
    	    throws UnknownHostException
    	  {
    	    localIP = InetAddress.getLocalHost().getHostAddress();
    	    System.out.println("localIP"+localIP);
    	  }
    
    
    private  void recordingSocket(String IPcallee, String RTPportcallee, String RTCPportcallee)
    {
      local.IPcallee = IPcallee;    
      
      local.RTPportcallee = RTPportcallee;
      local.RTCPportcallee = RTCPportcallee;
      
    }
    
    public String getCalleeIP()
    {
    	
      return IPcallee.replaceAll("\\s+", "");
    }
    
    public String getCalleeRTPport()
    {
      return RTPportcallee.replaceAll(" ", "");
    }
    
    public String getCalleeRTCPport()
    {
      return RTCPportcallee.replaceAll(" ", "");
    }
    
  
    class ByeTask  extends TimerTask {
        Dialog dialog;
        public ByeTask(Dialog dialog)  {
            this.dialog = dialog;
        }
        public void run () {
            try {
               Request byeRequest = this.dialog.createRequest(Request.BYE);
               ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
               dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
                junit.framework.TestCase.fail("Exit JVM");
            }

        }

    }

    private static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

	

    private static void usage() {
        System.out.println(usageString);
        junit.framework.TestCase.fail("Exit JVM");

    }


    public void processRequest(RequestEvent requestReceivedEvent)
    {
      Request request = requestReceivedEvent.getRequest();
      ServerTransaction serverTransactionId = requestReceivedEvent.getServerTransaction();
      try
      {
        if (serverTransactionId == null) {
          serverTransactionId = sipProvider.getNewServerTransaction(request);
        }
      }
      catch (Exception localException) {}
      System.out.println("\n\nRequest " + request.getMethod() + 
        " received at " + sipStack.getStackName() + 
        " with server transaction id " + serverTransactionId);
      
      if (processRequestForReferee(requestReceivedEvent, serverTransactionId) == 1)
      {
    	  /*  
        String dialogCallID = dialogCall.getCallId().toString().split("Call-ID: ")[0].replaceAll("\\s+", "");
        if (dialogCallID.equals(tempTargetDialogID))
        {
          System.out.println("TargetDialog setting!!!!!");
          tempTargetDialog = dialogCall;
        }
        else
        {
          System.out.println("TargetDialog setting false!!!!!  dialogCallId():" + dialogCall.getCallId() + "  TargetDialogID: " + tempTargetDialogID);
        }
        
        sendingRequest("INVITE", referto);
        */
    //	  SendInvite(referto);
      }
      if (request.getMethod().equals("BYE")) {
        processBye(request, serverTransactionId);
      }
    }
    
    public int processRequestForReferee(RequestEvent requestEvent, ServerTransaction serverTransactionId)
    {
      Request request = requestEvent.getRequest();
      if (request.getMethod().equals("REFER"))
      {
        System.out.println("YES! I GOT A REFER!!!");
        try
        {
          return processRefer(requestEvent, serverTransactionId);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      return 0;
    }

   
    public void processBye(Request request, ServerTransaction serverTransactionId)
    {
      System.out.println("shootist:  got a bye .");
      new PCS_RTP_Caller().EndSession();
      try
      {
        if (serverTransactionId == null)
        {
          System.out.println("shootist:  null TID.");
          return;
        }
        Dialog dialog = serverTransactionId.getDialog();
        System.out.println("Dialog State = " + dialog.getState());
        Response response = messageFactory.createResponse(200, request);
        serverTransactionId.sendResponse(response);
        System.out.println("shootist:  Sending OK.");
        System.out.println("Dialog State = " + dialog.getState());
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        System.exit(0);
      }
    }

    /**
     * Process the REFER request.
     * @throws ParseException
     * @throws SipException
     * @throws InvalidArgumentException
     */
    
   
    /**
     * Process the REFER request.
     * @throws ParseException
     * @throws SipException
     * @throws InvalidArgumentException
     */
    public int processRefer(RequestEvent requestEvent, ServerTransaction serverTransaction)
    	    throws ParseException, SipException, InvalidArgumentException
    	  {
    	    SipProvider sipProvider = (SipProvider)requestEvent.getSource();
    	    Request refer = requestEvent.getRequest();
    	    
    	    System.out.println("referee: got an REFER sending Accepted");
    	    System.out.println("referee:  " + refer.getMethod());
    	    System.out.println("referee : dialog = " + requestEvent.getDialog());
    	    

    	    ReferToHeader refTo = (ReferToHeader)refer.getHeader("Refer-To");
    	    if (refTo == null)
    	    {
    	      
    	      Response bad = messageFactory.createResponse(Response.BAD_REQUEST, refer);
    	      bad.setReasonPhrase("Missing Refer-To");
    	      sipProvider.sendResponse(bad);
    	      return 0;
    	    }
    	    Response response = null;
    	    
    	    ServerTransaction st = serverTransaction;
    	    if (st == null) {
    	      st = sipProvider.getNewServerTransaction(refer);
    	    }
    	    String toTag = Integer.toHexString((int)(Math.random() * 2147483647.0D));
    	    response = messageFactory.createResponse(202, refer);
    	    ToHeader toHeader = (ToHeader)response.getHeader("To");
    	    if (toHeader.getTag() != null) {
    	      System.err.println("####ERROR: To-tag!=null but no dialog match! My dialog=" + dialog.getState());
    	    }
    	    toHeader.setTag(toTag);
    	    
    	    this.dialog = st.getDialog();
    	    
    	    this.dialog.terminateOnBye(false);
    	    if (dialog != null)
    	    {
    	      System.out.println("Dialog " + dialog);
    	      System.out.println("Dialog state " + dialog.getState());
    	      System.out.println("local tag=" + dialog.getLocalTag());
    	      System.out.println("remote tag=" + dialog.getRemoteTag());
    	    }
    	    tempTargetDialogID = refer.getHeader("Target-Dialog").toString().split("Target-Dialog: ")[0].replaceAll("\\s+", "");
    	    





    	    Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + MyAddress + ":" + MyPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
    	    response.addHeader(contactH);
    	    

    	    ExpiresHeader expires = (ExpiresHeader)refer.getHeader("Expires");
    	    if (expires == null) {
    	      expires = headerFactory.createExpiresHeader(30);
    	    }
    	    response.addHeader(expires);
    	    



    	    System.out.println("Getting refTo: " + refTo.toString().split("sip:|@")[1] + "!!!!!!!!!!!!!!!!!!!!");
    	    System.out.println("ReferTo: " + refTo.toString().split("sip:|@")[1] + "   Referer: " + Referer + "!!!!!!!!!!!!!!!!!!!!");
    	  
    	    referto = refTo.toString().split("sip:|@")[1];
    	    
    	      Object caller="mom";
			if (refTo.toString().split("sip:|@")[1].equals(caller))
    	    {
    	      st.sendResponse(response);
    	      

    	      referEvent = headerFactory.createEventHeader("refer");
    	      

    	      long id = ((CSeqHeader)refer.getHeader("CSeq")).getSeqNumber();
    	      referEvent.setEventId(Long.toString(id));
    	    //  tempTargetDialog = dialog;
    	    //  sendNotify(100, "Trying");
    	     // SendInvite(referto);
    	      /*
    	      
              sendingRequest("INVITE", referto);*/
    	      SendRedirect();
    	      return 1;
    	    }
    	    return 0;
    	  }
    	  
     void SendRedirect() {
		// TODO Auto-generated method stub
    	
        try
        {
          String transport = "udp";
          
          ArrayList viaHeaders = new ArrayList();
          
          ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress, 
        		  MyPort, 
            transport, null);
          

          viaHeaders.add(viaHeader);        

          MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

          CallIdHeader callIdHeader = sipProvider.getNewCallId();

          cseq += 1L;
          CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.INVITE);
          
          javax.sip.address.Address fromAddress = addressFactory.createAddress("sip:Gateway@192.168.0.180");
          
          FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");
          
          javax.sip.address.Address toAddress = addressFactory.createAddress("sip:PN1@192.168.0.180");

          ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
          
          MaxForwardsHeader maxForwards = headerFactory
            .createMaxForwardsHeader(70);
          URI requestURI = addressFactory.createURI("sip:PN1@192.168.0.180");
          
          Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
          
       //   javax.sip.address.Address routeaddress = addressFactory.createAddress("sip:orig@163.17.21.88:5060;lr");
      // routeHeader = headerFactory.createRouteHeader(routeaddress);
          
          ContentTypeHeader ContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
        
      /*    sdpData = "v=0\r\n"
                  + "o=4855 13760799956958020 13760799956958020"
                  + " IN IP4  163.17.21.73\r\n" + "s=mysession session\r\n"
                  + "p=+46 8 52018010\r\n" + "c=IN IP4  163.17.21.73\r\n"
                  + "t=0 0\r\n" ;
           //       + "m=audio 6022 RTP/AVP 0 4 18\r\n"

                  // bandwith
          //        + "b=AS:25.4\r\n";
                  
                  // precondition mechanism
                  + "a=curr:qos local none\r\n"
                  + "a=curr:qos remote none\r\n"
                  + "a=des:qos mandatory local sendrec\r\n"
                  + "a=des:qos none remote sendrec\r\n"

                  + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                  + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
        
          byte[] contents = sdpData.getBytes();
          */
          byte[] contents = SDPsetting2().getBytes();
          
          request.setContent(contents, ContentTypeHeader);
      /*    
          if (Request.INVITE.equals("INVITE"))
          {
            Header AcceptContactH = headerFactory.createAcceptHeader("Accept-Contact", "*; mobility=\"fixed\"");
            Header RejectContactH = headerFactory.createHeader("Reject-Contact", "*; mobility=\"mobile\"");
            Header RequestDispositionH = headerFactory.createHeader("Request-Disposition", "no-fork");
            request.addHeader(AcceptContactH);
            request.addHeader(RejectContactH);
            request.addHeader(RequestDispositionH);
          }
     */     
          request.addHeader(contactH);
          
       //   request.addHeader(routeHeader);
          
          request.addHeader(callIdHeader);
          
          request.addHeader(cSeqHeader);
          
          request.addHeader(fromHeader);
          
          request.addHeader(toHeader);
          
          request.addHeader(maxForwardsHeader);
          
          request.addHeader(viaHeader);
          
          request.addHeader(ContentTypeHeader);
          






          javax.sip.address.Address fromNameAddress = addressFactory.createAddress(fromAddress.toString());
          

          HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
          
          AllowHeader allow1 = headerFactory.createAllowHeader(Request.INVITE);
          request.addHeader(allow1);
          AllowHeader allow2 = 
            headerFactory.createAllowHeader("PRACK");
          request.addHeader(allow2);
          AllowHeader allow3 = 
            headerFactory.createAllowHeader("UPDATE");
          request.addHeader(allow3);
          AllowHeader allow4 = 
            headerFactory.createAllowHeader("ACK");
          request.addHeader(allow4);
          AllowHeader allow5 = 
            headerFactory.createAllowHeader("CANCEL");
          request.addHeader(allow5);
          AllowHeader allow6 = 
            headerFactory.createAllowHeader("BYE");
          request.addHeader(allow6);
          AllowHeader allow7 = 
            headerFactory.createAllowHeader("REFER");
          request.addHeader(allow7);
          AllowHeader allow8 = 
            headerFactory.createAllowHeader("NOTIFY");
          request.addHeader(allow8);
          
          SupportedHeader supported1 = 
            headerFactory.createSupportedHeader("100rel");
          request.addHeader(supported1);
          SupportedHeader supported2 = 
            headerFactory.createSupportedHeader("preconditions");
          request.addHeader(supported2);
          SupportedHeader supported3 = 
            headerFactory.createSupportedHeader("path");
          request.addHeader(supported3);
          
          RequireHeader require1 = 
            headerFactory.createRequireHeader("sec-agree");
          request.addHeader(require1);
          RequireHeader require2 = 
            headerFactory.createRequireHeader("preconditions");
          request.addHeader(require2);
          
          SecurityClientHeader secClient = 
            headerFactoryImpl.createSecurityClientHeader();
          secClient.setSecurityMechanism("ipsec-3gpp");
          secClient.setAlgorithm("hmac-md5-96");
          secClient.setEncryptionAlgorithm("des-cbc");
          secClient.setSPIClient(10000);
          secClient.setSPIServer(10001);
          secClient.setPortClient(5063);
          secClient.setPortServer(4166);
          request.addHeader(secClient);
          
          PAccessNetworkInfoHeader accessInfo = 
            headerFactoryImpl.createPAccessNetworkInfoHeader();
          accessInfo.setAccessType("3GPP-UTRAN-TDD");
          accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
          request.addHeader(accessInfo);
          
          PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
          request.addHeader(privacy);
          PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
          request.addHeader(privacy2);
          

          PPreferredIdentityHeader preferredID = 
            headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
          request.addHeader(preferredID);
          

          PPreferredServiceHeader preferredService = 
            headerFactoryImpl.createPPreferredServiceHeader();
          preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(preferredService);
          

          PAssertedServiceHeader assertedService = 
            headerFactoryImpl.createPAssertedServiceHeader();
          assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(assertedService);
          










          String fromSipAddress = "192.168.0.180";
          String toSipAddress = "192.168.0.180";
          


          PCalledPartyIDHeader calledPartyID = 
            headerFactoryImpl.createPCalledPartyIDHeader(toAddress);
          request.addHeader(calledPartyID);
          

          PVisitedNetworkIDHeader visitedNetworkID1 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID1.setVisitedNetworkID(fromSipAddress
            .substring(fromSipAddress.indexOf("@") + 1));
          PVisitedNetworkIDHeader visitedNetworkID2 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID2.setVisitedNetworkID(toSipAddress
            .substring(toSipAddress.indexOf("@") + 1));
          request.addHeader(visitedNetworkID1);
          request.addHeader(visitedNetworkID2);
          


          PAssociatedURIHeader associatedURI1 = 
            headerFactoryImpl.createPAssociatedURIHeader(toAddress);
          PAssociatedURIHeader associatedURI2 = 
            headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
          request.addHeader(associatedURI1);
          request.addHeader(associatedURI2);
          


          PAssertedIdentityHeader assertedID = 
            headerFactoryImpl.createPAssertedIdentityHeader(
            addressFactory.createAddress(requestURI));
          request.addHeader(assertedID);
          
          TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
          javax.sip.address.Address telAddress = addressFactory.createAddress(tel);
          toAddress.setDisplayName("PN1");
          PAssertedIdentityHeader assertedID2 = 
            headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
          request.addHeader(assertedID2);
          


          PChargingFunctionAddressesHeader chargAddr = 
            headerFactoryImpl.createPChargingFunctionAddressesHeader();
          chargAddr.addChargingCollectionFunctionAddress("PN1.ims.test");
          chargAddr.addEventChargingFunctionAddress("testevent");
          request.addHeader(chargAddr);
          

          PChargingVectorHeader chargVect = 
            headerFactoryImpl.createChargingVectorHeader("icid");
          chargVect.setICIDGeneratedAt("icidhost");
          chargVect.setOriginatingIOI("origIOI");
          chargVect.setTerminatingIOI("termIOI");
          request.addHeader(chargVect);
          

          PMediaAuthorizationHeader mediaAuth1 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
          PMediaAuthorizationHeader mediaAuth2 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
          request.addHeader(mediaAuth1);
          request.addHeader(mediaAuth2);
          


          PathHeader path1 = 
            headerFactoryImpl.createPathHeader(fromNameAddress);
          PathHeader path2 = 
            headerFactoryImpl.createPathHeader(toAddress);
          request.addHeader(path1);
          request.addHeader(path2);
          

          inviteTidClientCall = sipProvider.getNewClientTransaction(request);
          


          inviteTidClientCall.sendRequest();
          if (Request.INVITE.equals("REGISTER")) {
            dialog = inviteTidClientCall.getDialog();
          } else if (Request.INVITE.equals("INVITE")) {
            dialogCall2 = inviteTidClientCall.getDialog();
            System.out.println("inviteTidClientCall2.getDialog()"+inviteTidClientCall.getDialog());
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
	}


	private void sendNotify(int code, String reason)
    	    throws SipException, ParseException
    	  {
    	    Request notifyRequest = dialog.createRequest("NOTIFY");
    	    

    	    String state = "pending";
    	    if ((code > 100) && (code < 200)) {
    	      state = "active";
    	    } else if (code >= 200) {
    	      state = "terminated";
    	    }
    	    SubscriptionStateHeader sstate = headerFactory.createSubscriptionStateHeader(state);
    	    if (state == "terminated") {
    	      sstate.setReasonCode("noresource");
    	    }
    	    notifyRequest.addHeader(sstate);
    	    notifyRequest.setHeader(referEvent);
    	    

    	    Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + MyAddress + ":" + MyPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
    	    notifyRequest.setHeader(contactH);
    	    
    	  //  Address routeaddress = addressFactory.createAddress("sip:orig@163.17.21.88:5060;lr");
    	//    RouteHeader routeHeader = headerFactory.createRouteHeader(routeaddress);
    	//    routeaddress = addressFactory.createAddress("sip:term@163.17.21.88:5060;lr");
    	//    RouteHeader routeHeader2 = headerFactory.createRouteHeader(routeaddress);
    	//    notifyRequest.setHeader(routeHeader2);

    	    HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
    	    

    	    AllowHeader allow1 = 
    	      headerFactory.createAllowHeader("INVITE");
    	    notifyRequest.addHeader(allow1);
    	    AllowHeader allow2 = 
    	      headerFactory.createAllowHeader("ACK");
    	    notifyRequest.addHeader(allow2);
    	    AllowHeader allow3 = 
    	      headerFactory.createAllowHeader("CANCEL");
    	    notifyRequest.addHeader(allow3);
    	    AllowHeader allow4 = 
    	      headerFactory.createAllowHeader("OPTIONS");
    	    notifyRequest.addHeader(allow4);
    	    AllowHeader allow5 = 
    	      headerFactory.createAllowHeader("BYE");
    	    notifyRequest.addHeader(allow5);
    	    AllowHeader allow6 = 
    	      headerFactory.createAllowHeader("REFER");
    	    notifyRequest.addHeader(allow6);
    	    AllowHeader allow7 = 
    	      headerFactory.createAllowHeader("NOTIFY");
    	    notifyRequest.addHeader(allow7);
    	    
    	    SupportedHeader supported1 = 
    	      headerFactory.createSupportedHeader("100rel");
    	    notifyRequest.addHeader(supported1);
    	    SupportedHeader supported2 = 
    	      headerFactory.createSupportedHeader("preconditions");
    	    notifyRequest.addHeader(supported2);
    	    SupportedHeader supported3 = 
    	      headerFactory.createSupportedHeader("path");
    	    notifyRequest.addHeader(supported3);
    	    
    	    RequireHeader require1 = 
    	      headerFactory.createRequireHeader("sec-agree");
    	    notifyRequest.addHeader(require1);
    	    RequireHeader require2 = 
    	      headerFactory.createRequireHeader("preconditions");
    	    notifyRequest.addHeader(require2);
    	    RequireHeader require3 = 
    	      headerFactory.createRequireHeader("tdialog");
    	    notifyRequest.addHeader(require3);
    	    
    	    SecurityClientHeader secClient = 
    	      headerFactoryImpl.createSecurityClientHeader();
    	    secClient.setSecurityMechanism("ipsec-3gpp");
    	    secClient.setAlgorithm("hmac-md5-96");
    	    secClient.setEncryptionAlgorithm("des-cbc");
    	    try
    	    {
    	      secClient.setSPIClient(10000);
    	      secClient.setSPIServer(10001);
    	      secClient.setPortClient(5063);
    	      secClient.setPortServer(4166);
    	    }
    	    catch (InvalidArgumentException e)
    	    {
    	      e.printStackTrace();
    	    }
    	    notifyRequest.addHeader(secClient);
    	    
    	    PAccessNetworkInfoHeader accessInfo = 
    	      headerFactoryImpl.createPAccessNetworkInfoHeader();
    	    accessInfo.setAccessType("3GPP-UTRAN-TDD");
    	    accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
    	    notifyRequest.addHeader(accessInfo);
    	    
    	    PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
    	    notifyRequest.addHeader(privacy);
    	    PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
    	    notifyRequest.addHeader(privacy2);
    	    






    	    PPreferredServiceHeader preferredService = 
    	      headerFactoryImpl.createPPreferredServiceHeader();
    	    preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
    	    notifyRequest.addHeader(preferredService);
    	    

    	    PAssertedServiceHeader assertedService = 
    	      headerFactoryImpl.createPAssertedServiceHeader();
    	    assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
    	    notifyRequest.addHeader(assertedService);
    	    





    	    ClientTransaction ct2 = sipProvider.getNewClientTransaction(notifyRequest);
    	    
    	    ContentTypeHeader ct = headerFactory.createContentTypeHeader("message", "sipfrag");
    	    ct.setParameter("version", "2.0");
    	    
    	    notifyRequest.setContent("SIP/2.0 " + code + ' ' + reason, ct);
    	    
    	    System.out.println("tempTargetDialog"+tempTargetDialog);
    	    System.out.println("Dialog"+dialog);
    	    System.out.println("Dialog2"+dialog2);
    	    System.out.println(" dialogCall"+ dialogCall);
    	   
    	    dialog.sendRequest(ct2);
    	  }
    
    
    public static void SendInvite() {
    	
        try
        {
        	
        	
          String transport = "udp";
          
          ArrayList viaHeaders = new ArrayList();
          
          ViaHeader viaHeader = headerFactory.createViaHeader(shoot.localIP, 
        		  6020, 
            transport, null);
         

          viaHeaders.add(viaHeader);        

          MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

          CallIdHeader callIdHeader = sipProvider.getNewCallId();

          cseq += 1L;
          CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.INVITE);
          
          javax.sip.address.Address fromAddress = addressFactory.createAddress("sip:Gateway@192.168.0.180");
          
          FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");
          
          javax.sip.address.Address toAddress = addressFactory.createAddress("sip:PN1@192.168.0.180");

          ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
          
          MaxForwardsHeader maxForwards = headerFactory
            .createMaxForwardsHeader(70);
          URI requestURI = addressFactory.createURI("sip:PN1@192.168.0.180");
          
          Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
          
       //   javax.sip.address.Address routeaddress = addressFactory.createAddress("sip:orig@163.17.21.88:5060;lr");
      //    RouteHeader routeHeader = headerFactory.createRouteHeader(routeaddress);
          
          ContentTypeHeader ContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
         
          sdpData = "v=0\r\n"
                  + "o=4855 13760799956958020 13760799956958020"
                  + " IN IP4  "+shoot.localIP+"\r\n" + "s=mysession session\r\n"
                  + "p=+46 8 52018010\r\n" + "c=IN IP4  "+shoot.localIP+"\r\n"
                  + "t=0 0\r\n" /*+ "m=audio 6022 RTP/AVP 0 4 18\r\n"*/

                  // bandwith
                //  + "b=AS:25.4\r\n"
                  // precondition mechanism
                  + "a=curr:qos local none\r\n"
                  + "a=curr:qos remote none\r\n"
                  + "a=des:qos mandatory local sendrec\r\n"
                  + "a=des:qos none remote sendrec\r\n"


                  + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                  + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
          byte[] contents = sdpData.getBytes();
        /*  byte[] contents = SDPsetting().getBytes();
*/
          request.setContent(contents, ContentTypeHeader);
          
      /*    
          if (Request.INVITE.equals("INVITE"))
          {
            Header AcceptContactH = headerFactory.createAcceptHeader("Accept-Contact", "*; mobility=\"fixed\"");
            Header RejectContactH = headerFactory.createHeader("Reject-Contact", "*; mobility=\"mobile\"");
            Header RequestDispositionH = headerFactory.createHeader("Request-Disposition", "no-fork");
            request.addHeader(AcceptContactH);
            request.addHeader(RejectContactH);
            request.addHeader(RequestDispositionH);
          }
     */     
          request.addHeader(contactH);
          


       //   request.addHeader(routeHeader);
          
          request.addHeader(callIdHeader);
          
          request.addHeader(cSeqHeader);
          
          request.addHeader(fromHeader);
          
          request.addHeader(toHeader);
          
          request.addHeader(maxForwardsHeader);
          
          request.addHeader(viaHeader);
          



          request.addHeader(ContentTypeHeader);
          






          javax.sip.address.Address fromNameAddress = addressFactory.createAddress(fromAddress.toString());
          /* ++++++++++++++++++++++++++++++++++++++++++++
           *                IMS headers
           * ++++++++++++++++++++++++++++++++++++++++++++
           */
/*
          HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
          
          AllowHeader allow1 = headerFactory.createAllowHeader(Request.INVITE);
          request.addHeader(allow1);
          AllowHeader allow2 = 
            headerFactory.createAllowHeader("PRACK");
          request.addHeader(allow2);
          AllowHeader allow3 = 
            headerFactory.createAllowHeader("UPDATE");
          request.addHeader(allow3);
          AllowHeader allow4 = 
            headerFactory.createAllowHeader("ACK");
          request.addHeader(allow4);
          AllowHeader allow5 = 
            headerFactory.createAllowHeader("CANCEL");
          request.addHeader(allow5);
          AllowHeader allow6 = 
            headerFactory.createAllowHeader("BYE");
          request.addHeader(allow6);
          AllowHeader allow7 = 
            headerFactory.createAllowHeader("REFER");
          request.addHeader(allow7);
          AllowHeader allow8 = 
            headerFactory.createAllowHeader("NOTIFY");
          request.addHeader(allow8);
          
          SupportedHeader supported1 = 
            headerFactory.createSupportedHeader("100rel");
          request.addHeader(supported1);
          SupportedHeader supported2 = 
            headerFactory.createSupportedHeader("preconditions");
          request.addHeader(supported2);
          SupportedHeader supported3 = 
            headerFactory.createSupportedHeader("path");
          request.addHeader(supported3);
          
          RequireHeader require1 = 
            headerFactory.createRequireHeader("sec-agree");
          request.addHeader(require1);
          RequireHeader require2 = 
            headerFactory.createRequireHeader("preconditions");
          request.addHeader(require2);
          
          SecurityClientHeader secClient = 
            headerFactoryImpl.createSecurityClientHeader();
          secClient.setSecurityMechanism("ipsec-3gpp");
          secClient.setAlgorithm("hmac-md5-96");
          secClient.setEncryptionAlgorithm("des-cbc");
          secClient.setSPIClient(10000);
          secClient.setSPIServer(10001);
          secClient.setPortClient(5063);
          secClient.setPortServer(4166);
          request.addHeader(secClient);
          
          PAccessNetworkInfoHeader accessInfo = 
            headerFactoryImpl.createPAccessNetworkInfoHeader();
          accessInfo.setAccessType("3GPP-UTRAN-TDD");
          accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
          request.addHeader(accessInfo);
          
          PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
          request.addHeader(privacy);
          PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
          request.addHeader(privacy2);
          

          PPreferredIdentityHeader preferredID = 
            headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
          request.addHeader(preferredID);
          

          PPreferredServiceHeader preferredService = 
            headerFactoryImpl.createPPreferredServiceHeader();
          preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(preferredService);
          

          PAssertedServiceHeader assertedService = 
            headerFactoryImpl.createPAssertedServiceHeader();
          assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(assertedService);
          










          String fromSipAddress = "192.168.0.180";
          String toSipAddress = "192.168.0.180";
          


          PCalledPartyIDHeader calledPartyID = 
            headerFactoryImpl.createPCalledPartyIDHeader(toAddress);
          request.addHeader(calledPartyID);
          

          PVisitedNetworkIDHeader visitedNetworkID1 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID1.setVisitedNetworkID(fromSipAddress
            .substring(fromSipAddress.indexOf("@") + 1));
          PVisitedNetworkIDHeader visitedNetworkID2 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID2.setVisitedNetworkID(toSipAddress
            .substring(toSipAddress.indexOf("@") + 1));
          request.addHeader(visitedNetworkID1);
          request.addHeader(visitedNetworkID2);
          


          PAssociatedURIHeader associatedURI1 = 
            headerFactoryImpl.createPAssociatedURIHeader(toAddress);
          PAssociatedURIHeader associatedURI2 = 
            headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
          request.addHeader(associatedURI1);
          request.addHeader(associatedURI2);
          


          PAssertedIdentityHeader assertedID = 
            headerFactoryImpl.createPAssertedIdentityHeader(
            addressFactory.createAddress(requestURI));
          request.addHeader(assertedID);
          
          TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
          javax.sip.address.Address telAddress = addressFactory.createAddress(tel);
          toAddress.setDisplayName(referto);
          PAssertedIdentityHeader assertedID2 = 
            headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
          request.addHeader(assertedID2);
          


          PChargingFunctionAddressesHeader chargAddr = 
            headerFactoryImpl.createPChargingFunctionAddressesHeader();
          chargAddr.addChargingCollectionFunctionAddress("PN1.ims.test");
          chargAddr.addEventChargingFunctionAddress("testevent");
          request.addHeader(chargAddr);
          

          PChargingVectorHeader chargVect = 
            headerFactoryImpl.createChargingVectorHeader("icid");
          chargVect.setICIDGeneratedAt("icidhost");
          chargVect.setOriginatingIOI("origIOI");
          chargVect.setTerminatingIOI("termIOI");
          request.addHeader(chargVect);
          

          PMediaAuthorizationHeader mediaAuth1 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
          PMediaAuthorizationHeader mediaAuth2 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
          request.addHeader(mediaAuth1);
          request.addHeader(mediaAuth2);
          


          PathHeader path1 = 
            headerFactoryImpl.createPathHeader(fromNameAddress);
          PathHeader path2 = 
            headerFactoryImpl.createPathHeader(toAddress);
          request.addHeader(path1);
          request.addHeader(path2);
          
*/






          inviteTidClient = sipProvider.getNewClientTransaction(request);
          


          inviteTidClient.sendRequest();
          if (Request.INVITE.equals("REGISTER")) {
            dialog = inviteTidClient.getDialog();
          } else if (Request.INVITE.equals("INVITE")) {
            dialogCall = inviteTidClient.getDialog();
            System.out.println("DDDdialogCallinviteTidClient.getDialog()"+dialogCall);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
public static void SendInvitetest() {
    	
        try
        {
        	
        	
          String transport = "udp";
          
          ArrayList viaHeaders = new ArrayList();
          
          ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress, 
        		  MyPort, 
            transport, null);
         

          viaHeaders.add(viaHeader);        

          MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

          CallIdHeader callIdHeader = sipProvider.getNewCallId();

          cseq += 1L;
          CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.INVITE);
          
          javax.sip.address.Address fromAddress = addressFactory.createAddress("sip:Gateway@192.168.0.180");
          
          FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");
          
          javax.sip.address.Address toAddress = addressFactory.createAddress("sip:PN1@192.168.0.180");

          ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
          
          MaxForwardsHeader maxForwards = headerFactory
            .createMaxForwardsHeader(70);
          URI requestURI = addressFactory.createURI("sip:PN1@192.168.0.180");
          
          Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
          
       //   javax.sip.address.Address routeaddress = addressFactory.createAddress("sip:orig@163.17.21.88:5060;lr");
      //    RouteHeader routeHeader = headerFactory.createRouteHeader(routeaddress);
          
          ContentTypeHeader ContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
      
        //  byte[] contents = sdpData.getBytes();
          byte[] contents = SDPsetting().getBytes();

          request.setContent(contents, ContentTypeHeader);
          
      /*    
          if (Request.INVITE.equals("INVITE"))
          {
            Header AcceptContactH = headerFactory.createAcceptHeader("Accept-Contact", "*; mobility=\"fixed\"");
            Header RejectContactH = headerFactory.createHeader("Reject-Contact", "*; mobility=\"mobile\"");
            Header RequestDispositionH = headerFactory.createHeader("Request-Disposition", "no-fork");
            request.addHeader(AcceptContactH);
            request.addHeader(RejectContactH);
            request.addHeader(RequestDispositionH);
          }
     */     
          request.addHeader(contactH);
          


       //   request.addHeader(routeHeader);
          
          request.addHeader(callIdHeader);
          
          request.addHeader(cSeqHeader);
          
          request.addHeader(fromHeader);
          
          request.addHeader(toHeader);
          
          request.addHeader(maxForwardsHeader);
          
          request.addHeader(viaHeader);
          



          request.addHeader(ContentTypeHeader);
          






          javax.sip.address.Address fromNameAddress = addressFactory.createAddress(fromAddress.toString());
          

          HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
          
          AllowHeader allow1 = headerFactory.createAllowHeader(Request.INVITE);
          request.addHeader(allow1);
          AllowHeader allow2 = 
            headerFactory.createAllowHeader("PRACK");
          request.addHeader(allow2);
          AllowHeader allow3 = 
            headerFactory.createAllowHeader("UPDATE");
          request.addHeader(allow3);
          AllowHeader allow4 = 
            headerFactory.createAllowHeader("ACK");
          request.addHeader(allow4);
          AllowHeader allow5 = 
            headerFactory.createAllowHeader("CANCEL");
          request.addHeader(allow5);
          AllowHeader allow6 = 
            headerFactory.createAllowHeader("BYE");
          request.addHeader(allow6);
          AllowHeader allow7 = 
            headerFactory.createAllowHeader("REFER");
          request.addHeader(allow7);
          AllowHeader allow8 = 
            headerFactory.createAllowHeader("NOTIFY");
          request.addHeader(allow8);
          
          SupportedHeader supported1 = 
            headerFactory.createSupportedHeader("100rel");
          request.addHeader(supported1);
          SupportedHeader supported2 = 
            headerFactory.createSupportedHeader("preconditions");
          request.addHeader(supported2);
          SupportedHeader supported3 = 
            headerFactory.createSupportedHeader("path");
          request.addHeader(supported3);
          
          RequireHeader require1 = 
            headerFactory.createRequireHeader("sec-agree");
          request.addHeader(require1);
          RequireHeader require2 = 
            headerFactory.createRequireHeader("preconditions");
          request.addHeader(require2);
          
          SecurityClientHeader secClient = 
            headerFactoryImpl.createSecurityClientHeader();
          secClient.setSecurityMechanism("ipsec-3gpp");
          secClient.setAlgorithm("hmac-md5-96");
          secClient.setEncryptionAlgorithm("des-cbc");
          secClient.setSPIClient(10000);
          secClient.setSPIServer(10001);
          secClient.setPortClient(5063);
          secClient.setPortServer(4166);
          request.addHeader(secClient);
          
          PAccessNetworkInfoHeader accessInfo = 
            headerFactoryImpl.createPAccessNetworkInfoHeader();
          accessInfo.setAccessType("3GPP-UTRAN-TDD");
          accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
          request.addHeader(accessInfo);
          
          PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
          request.addHeader(privacy);
          PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
          request.addHeader(privacy2);
          

          PPreferredIdentityHeader preferredID = 
            headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
          request.addHeader(preferredID);
          

          PPreferredServiceHeader preferredService = 
            headerFactoryImpl.createPPreferredServiceHeader();
          preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(preferredService);
          

          PAssertedServiceHeader assertedService = 
            headerFactoryImpl.createPAssertedServiceHeader();
          assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
          request.addHeader(assertedService);
          










          String fromSipAddress = "192.168.0.180";
          String toSipAddress = "192.168.0.180";
          


          PCalledPartyIDHeader calledPartyID = 
            headerFactoryImpl.createPCalledPartyIDHeader(toAddress);
          request.addHeader(calledPartyID);
          

          PVisitedNetworkIDHeader visitedNetworkID1 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID1.setVisitedNetworkID(fromSipAddress
            .substring(fromSipAddress.indexOf("@") + 1));
          PVisitedNetworkIDHeader visitedNetworkID2 = 
            headerFactoryImpl.createPVisitedNetworkIDHeader();
          visitedNetworkID2.setVisitedNetworkID(toSipAddress
            .substring(toSipAddress.indexOf("@") + 1));
          request.addHeader(visitedNetworkID1);
          request.addHeader(visitedNetworkID2);
          


          PAssociatedURIHeader associatedURI1 = 
            headerFactoryImpl.createPAssociatedURIHeader(toAddress);
          PAssociatedURIHeader associatedURI2 = 
            headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
          request.addHeader(associatedURI1);
          request.addHeader(associatedURI2);
          


          PAssertedIdentityHeader assertedID = 
            headerFactoryImpl.createPAssertedIdentityHeader(
            addressFactory.createAddress(requestURI));
          request.addHeader(assertedID);
          
          TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
          javax.sip.address.Address telAddress = addressFactory.createAddress(tel);
          toAddress.setDisplayName(referto);
          PAssertedIdentityHeader assertedID2 = 
            headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
          request.addHeader(assertedID2);
          


          PChargingFunctionAddressesHeader chargAddr = 
            headerFactoryImpl.createPChargingFunctionAddressesHeader();
          chargAddr.addChargingCollectionFunctionAddress("PN1.ims.test");
          chargAddr.addEventChargingFunctionAddress("testevent");
          request.addHeader(chargAddr);
          

          PChargingVectorHeader chargVect = 
            headerFactoryImpl.createChargingVectorHeader("icid");
          chargVect.setICIDGeneratedAt("icidhost");
          chargVect.setOriginatingIOI("origIOI");
          chargVect.setTerminatingIOI("termIOI");
          request.addHeader(chargVect);
          

          PMediaAuthorizationHeader mediaAuth1 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
          PMediaAuthorizationHeader mediaAuth2 = 
            headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
          request.addHeader(mediaAuth1);
          request.addHeader(mediaAuth2);
          


          PathHeader path1 = 
            headerFactoryImpl.createPathHeader(fromNameAddress);
          PathHeader path2 = 
            headerFactoryImpl.createPathHeader(toAddress);
          request.addHeader(path1);
          request.addHeader(path2);
          







          inviteTidClient = sipProvider.getNewClientTransaction(request);
          


          inviteTidClient.sendRequest();
          if (Request.INVITE.equals("REGISTER")) {
            dialog = inviteTidClient.getDialog();
          } else if (Request.INVITE.equals("INVITE")) {
            dialogCall = inviteTidClient.getDialog();
            System.out.println("DDDdialogCallinviteTidClient.getDialog()"+dialogCall);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    
    public void sendingRequest(String CseqMethod, String refTo)
    {
      try
      {
        String transport = "udp";
        
        ArrayList viaHeaders = new ArrayList();
        
        ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress, 
          MyPort, 
          transport, null);
        

        viaHeaders.add(viaHeader);
        


        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
        


        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        


        cseq += 1L;
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, CseqMethod);
        
        javax.sip.address.Address fromAddress = addressFactory.createAddress("sip:" + Referee + "@192.168.0.180");
        
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");
        
        javax.sip.address.Address toAddress = addressFactory.createAddress("sip:" + refTo + "@192.168.0.180");
        


        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
        






        MaxForwardsHeader maxForwards = headerFactory
          .createMaxForwardsHeader(70);
        URI requestURI = addressFactory.createURI("sip:" + refTo + "@192.168.0.180");
        
        Request request = messageFactory.createRequest(requestURI, CseqMethod, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
        
   //     javax.sip.address.Address routeaddress = addressFactory.createAddress("sip:orig@163.17.21.88:5060;lr");
    //    RouteHeader routeHeader = headerFactory.createRouteHeader(routeaddress);
        
        ContentTypeHeader ContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
       /* byte[] contents = SDPsetting().getBytes();
        request.setContent(contents, ContentTypeHeader);*/
        if (CseqMethod.equals("INVITE"))
        {
          Header AcceptContactH = headerFactory.createAcceptHeader("Accept-Contact", "*; mobility=\"fixed\"");
          Header RejectContactH = headerFactory.createHeader("Reject-Contact", "*; mobility=\"mobile\"");
          Header RequestDispositionH = headerFactory.createHeader("Request-Disposition", "no-fork");
          request.addHeader(AcceptContactH);
          request.addHeader(RejectContactH);
          request.addHeader(RequestDispositionH);
        }
        request.addHeader(contactH);
        


     //   request.addHeader(routeHeader);
        
        request.addHeader(callIdHeader);
        
        request.addHeader(cSeqHeader);
        
        request.addHeader(fromHeader);
        
        request.addHeader(toHeader);
        
        request.addHeader(maxForwardsHeader);
        
        request.addHeader(viaHeader);
        



        request.addHeader(ContentTypeHeader);
        






        javax.sip.address.Address fromNameAddress = addressFactory.createAddress(fromAddress.toString());
        

        HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
        
        AllowHeader allow1 = headerFactory.createAllowHeader(CseqMethod);
        request.addHeader(allow1);
        AllowHeader allow2 = 
          headerFactory.createAllowHeader("PRACK");
        request.addHeader(allow2);
        AllowHeader allow3 = 
          headerFactory.createAllowHeader("UPDATE");
        request.addHeader(allow3);
        AllowHeader allow4 = 
          headerFactory.createAllowHeader("ACK");
        request.addHeader(allow4);
        AllowHeader allow5 = 
          headerFactory.createAllowHeader("CANCEL");
        request.addHeader(allow5);
        AllowHeader allow6 = 
          headerFactory.createAllowHeader("BYE");
        request.addHeader(allow6);
        AllowHeader allow7 = 
          headerFactory.createAllowHeader("REFER");
        request.addHeader(allow7);
        AllowHeader allow8 = 
          headerFactory.createAllowHeader("NOTIFY");
        request.addHeader(allow8);
        
        SupportedHeader supported1 = 
          headerFactory.createSupportedHeader("100rel");
        request.addHeader(supported1);
        SupportedHeader supported2 = 
          headerFactory.createSupportedHeader("preconditions");
        request.addHeader(supported2);
        SupportedHeader supported3 = 
          headerFactory.createSupportedHeader("path");
        request.addHeader(supported3);
        
        RequireHeader require1 = 
          headerFactory.createRequireHeader("sec-agree");
        request.addHeader(require1);
        RequireHeader require2 = 
          headerFactory.createRequireHeader("preconditions");
        request.addHeader(require2);
        
        SecurityClientHeader secClient = 
          headerFactoryImpl.createSecurityClientHeader();
        secClient.setSecurityMechanism("ipsec-3gpp");
        secClient.setAlgorithm("hmac-md5-96");
        secClient.setEncryptionAlgorithm("des-cbc");
        secClient.setSPIClient(10000);
        secClient.setSPIServer(10001);
        secClient.setPortClient(5063);
        secClient.setPortServer(4166);
        request.addHeader(secClient);
        
        PAccessNetworkInfoHeader accessInfo = 
          headerFactoryImpl.createPAccessNetworkInfoHeader();
        accessInfo.setAccessType("3GPP-UTRAN-TDD");
        accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
        request.addHeader(accessInfo);
        
        PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
        request.addHeader(privacy);
        PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
        request.addHeader(privacy2);
        

        PPreferredIdentityHeader preferredID = 
          headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
        request.addHeader(preferredID);
        

        PPreferredServiceHeader preferredService = 
          headerFactoryImpl.createPPreferredServiceHeader();
        preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
        request.addHeader(preferredService);
        

        PAssertedServiceHeader assertedService = 
          headerFactoryImpl.createPAssertedServiceHeader();
        assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
        request.addHeader(assertedService);
        










        String fromSipAddress = "192.168.0.180";
        String toSipAddress = "192.168.0.180";
        


        PCalledPartyIDHeader calledPartyID = 
          headerFactoryImpl.createPCalledPartyIDHeader(toAddress);
        request.addHeader(calledPartyID);
        

        PVisitedNetworkIDHeader visitedNetworkID1 = 
          headerFactoryImpl.createPVisitedNetworkIDHeader();
        visitedNetworkID1.setVisitedNetworkID(fromSipAddress
          .substring(fromSipAddress.indexOf("@") + 1));
        PVisitedNetworkIDHeader visitedNetworkID2 = 
          headerFactoryImpl.createPVisitedNetworkIDHeader();
        visitedNetworkID2.setVisitedNetworkID(toSipAddress
          .substring(toSipAddress.indexOf("@") + 1));
        request.addHeader(visitedNetworkID1);
        request.addHeader(visitedNetworkID2);
        


        PAssociatedURIHeader associatedURI1 = 
          headerFactoryImpl.createPAssociatedURIHeader(toAddress);
        PAssociatedURIHeader associatedURI2 = 
          headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
        request.addHeader(associatedURI1);
        request.addHeader(associatedURI2);
        


        PAssertedIdentityHeader assertedID = 
          headerFactoryImpl.createPAssertedIdentityHeader(
          addressFactory.createAddress(requestURI));
        request.addHeader(assertedID);
        
        TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
        javax.sip.address.Address telAddress = addressFactory.createAddress(tel);
        toAddress.setDisplayName(refTo);
        PAssertedIdentityHeader assertedID2 = 
          headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
        request.addHeader(assertedID2);
        


        PChargingFunctionAddressesHeader chargAddr = 
          headerFactoryImpl.createPChargingFunctionAddressesHeader();
        chargAddr.addChargingCollectionFunctionAddress("PN1.ims.test");
        chargAddr.addEventChargingFunctionAddress("testevent");
        request.addHeader(chargAddr);
        

        PChargingVectorHeader chargVect = 
          headerFactoryImpl.createChargingVectorHeader("icid");
        chargVect.setICIDGeneratedAt("icidhost");
        chargVect.setOriginatingIOI("origIOI");
        chargVect.setTerminatingIOI("termIOI");
        request.addHeader(chargVect);
        

        PMediaAuthorizationHeader mediaAuth1 = 
          headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
        PMediaAuthorizationHeader mediaAuth2 = 
          headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
        request.addHeader(mediaAuth1);
        request.addHeader(mediaAuth2);
        


        PathHeader path1 = 
          headerFactoryImpl.createPathHeader(fromNameAddress);
        PathHeader path2 = 
          headerFactoryImpl.createPathHeader(toAddress);
        request.addHeader(path1);
        request.addHeader(path2);
        







        inviteTid = sipProvider.getNewClientTransaction(request);
        


        inviteTid.sendRequest();
        if (CseqMethod.equals("REGISTER")) {
          dialog2 = inviteTid.getDialog();
        } else if (CseqMethod.equals("INVITE")) {
          dialogCall = inviteTid.getDialog();
          System.out.println("inviteTid.getDialog();"+inviteTid.getDialog());
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    
    public void sendInvite2(ReferToHeader to)
    {
      try
      {
        String fromName = Referee;
        String fromSipAddress = "192.168.0.180";
        String fromDisplayName = Referee;
        

        SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);
        
        Address fromNameAddress = addressFactory.createAddress(fromAddress);
        fromNameAddress.setDisplayName(fromDisplayName);
        FromHeader fromHeader = headerFactory.createFromHeader(
          fromNameAddress, "12345");
        

        ToHeader toHeader = headerFactory.createToHeader(to.getAddress(), 
          null);
        

        SipURI requestURI = (SipURI)to.getAddress().getURI();
        

        String transport = requestURI.getTransportParam();
        if (transport == null) {
          transport = "udp";
        }
        ListeningPoint lp = udpProvider.getListeningPoint(transport);
        

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress, 
          MyPort, 
          transport, null);
        

        viaHeaders.add(viaHeader);
        

        CallIdHeader callIdHeader = udpProvider.getNewCallId();
        

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, 
          "REFER");
        

        MaxForwardsHeader maxForwards = headerFactory
          .createMaxForwardsHeader(70);
        

        Request request = messageFactory.createRequest(requestURI, 
          "INVITE", callIdHeader, cSeqHeader, fromHeader, 
          toHeader, viaHeaders, maxForwards);
        

        Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + MyAddress+ ":" + MyPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
        request.addHeader(contactH);
        










        HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
        

        AllowHeader allow1 = 
          headerFactory.createAllowHeader("INVITE");
        request.addHeader(allow1);
        AllowHeader allow2 = 
          headerFactory.createAllowHeader("ACK");
        request.addHeader(allow2);
        AllowHeader allow3 = 
          headerFactory.createAllowHeader("CANCEL");
        request.addHeader(allow3);
        AllowHeader allow4 = 
          headerFactory.createAllowHeader("OPTIONS");
        request.addHeader(allow4);
        AllowHeader allow5 = 
          headerFactory.createAllowHeader("BYE");
        request.addHeader(allow5);
        AllowHeader allow6 = 
          headerFactory.createAllowHeader("REFER");
        request.addHeader(allow6);
        AllowHeader allow7 = 
          headerFactory.createAllowHeader("NOTIFY");
        request.addHeader(allow7);
        
        SupportedHeader supported1 = 
          headerFactory.createSupportedHeader("100rel");
        request.addHeader(supported1);
        SupportedHeader supported2 = 
          headerFactory.createSupportedHeader("preconditions");
        request.addHeader(supported2);
        SupportedHeader supported3 = 
          headerFactory.createSupportedHeader("path");
        request.addHeader(supported3);
        
        RequireHeader require1 = 
          headerFactory.createRequireHeader("sec-agree");
        request.addHeader(require1);
        RequireHeader require2 = 
          headerFactory.createRequireHeader("preconditions");
        request.addHeader(require2);
        
        SecurityClientHeader secClient = 
          headerFactoryImpl.createSecurityClientHeader();
        secClient.setSecurityMechanism("ipsec-3gpp");
        secClient.setAlgorithm("hmac-md5-96");
        secClient.setEncryptionAlgorithm("des-cbc");
        secClient.setSPIClient(10000);
        secClient.setSPIServer(10001);
        secClient.setPortClient(5063);
        secClient.setPortServer(4166);
        request.addHeader(secClient);
        
        PAccessNetworkInfoHeader accessInfo = 
          headerFactoryImpl.createPAccessNetworkInfoHeader();
        accessInfo.setAccessType("3GPP-UTRAN-TDD");
        accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
        request.addHeader(accessInfo);
        
        PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
        request.addHeader(privacy);
        PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
        request.addHeader(privacy2);
        

        PPreferredIdentityHeader preferredID = 
          headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
        request.addHeader(preferredID);
        

        PPreferredServiceHeader preferredService = 
          headerFactoryImpl.createPPreferredServiceHeader();
        preferredService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
        request.addHeader(preferredService);
        

        PAssertedServiceHeader assertedService = 
          headerFactoryImpl.createPAssertedServiceHeader();
        assertedService.setApplicationIdentifiers("3gpp-service-ims.icis.mmtel");
        request.addHeader(assertedService);
        






        ClientTransaction inviteTid = udpProvider.getNewClientTransaction(request);
        
        System.out.println("Invite Dialog = " + inviteTid.getDialog());
        

        inviteTid.sendRequest();
      }
      catch (Throwable ex)
      {
        ex.printStackTrace();
        usage();
      }
    }
    
  
    
    
    public void processInviteOK(Response ok, ClientTransaction ct)
    {
    	 
		
    	
        HeaderFactoryImpl headerFactoryImpl =
            (HeaderFactoryImpl) headerFactory;

        try
        {

            RequireHeader require = null;
            String requireOptionTags = new String();
            ListIterator li = ok.getHeaders(RequireHeader.NAME);
            if (li != null) {
                try {
                    while(li.hasNext())
                    {
                        require = (RequireHeader) li.next();
                        requireOptionTags = requireOptionTags
                            .concat( require.getOptionTag())
                            .concat(" ");
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("\n(!) Exception getting Require header! - " + ex);
                }
            }


            // this is only to illustrate the usage of this headers
            // send Security-Verify (based on Security-Server) if Require: sec-agree
            SecurityVerifyList secVerifyList = null;
            if (requireOptionTags.indexOf("sec-agree") != -1)
            {
                ListIterator secServerReceived =
                    ok.getHeaders(SecurityServerHeader.NAME);
                if (secServerReceived != null && secServerReceived.hasNext())
                {
                    System.out.println(".: Security-Server received: ");

                     while (secServerReceived.hasNext())
                    {
                        SecurityServerHeader security = null;
                        try {
                            security = (SecurityServerHeader) secServerReceived.next();
                        }
                        catch (Exception ex)
                        {
                            System.out.println("(!) Exception getting Security-Server header : " + ex);
                        }

                        try {
                            Iterator parameters = security.getParameterNames();
                            SecurityVerifyHeader newSecVerify = headerFactoryImpl.createSecurityVerifyHeader();
                            newSecVerify.setSecurityMechanism(security.getSecurityMechanism());
                            while (parameters.hasNext())
                            {
                                String paramName = (String)parameters.next();
                                newSecVerify.setParameter(paramName,security.getParameter(paramName));
                            }

                            System.out.println("   - " + security.toString());

                        }
                        catch (Exception ex)
                        {
                            System.out.println("(!) Exception setting the security agreement!" + ex);
                            ex.getStackTrace();
                        }

                    }
                }
                System.out.println(".: Security-Verify built and added to response...");
            }
            
            

            CSeqHeader cseq = (CSeqHeader) ok.getHeader(CSeqHeader.NAME);
            ackRequest = dialogCall.createAck( cseq.getSeqNumber() );
            
          //ack SDP
            ContentTypeHeader ContentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
            
            byte[] contents = SDPsetting().getBytes();

            ackRequest.setContent(contents, ContentTypeHeader);
            
           
            
            if (secVerifyList != null && !secVerifyList.isEmpty())
            {
                RequireHeader requireSecAgree = headerFactory.createRequireHeader("sec-agree");
                ackRequest.setHeader(requireSecAgree);

                ackRequest.setHeader(secVerifyList);
            }

            System.out.println("Sending ACK2");
            dialogCall.sendAck(ackRequest);
            
            
         //   new PCS_RTP_Caller().Port();
         //   new PCS_RTP_Caller().Media();
          //  new PCS_RTP_Caller().addNewParticipant(remoteIP, remoteRtpPort, remoteRtcpPort, localRtpPort, localRtcpPort);
            //new PCS_RTP_Caller().startTalking();
            
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception sending ACK to 200 OK " +
                    "response to INVITE : " + ex);
        }
    }

       // Save the created ACK request, to respond to retransmitted 2xx
       private Request ackRequest;
       private Request ackRequest2;

	

	
       public void processResponse(ResponseEvent responseReceivedEvent) {
       	System.out.println("Got a response");
           Response response = (Response) responseReceivedEvent.getResponse();
           ClientTransaction tid = responseReceivedEvent.getClientTransaction();
           CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

           System.out.println("Response received : Status Code = "
                   + response.getStatusCode() + " " + cseq);
           
       
           if (tid == null) {

               // RFC3261: MUST respond to every 2xx
               if (ackRequest!=null && dialogCall!=null) {
                 
                  try {
                	  System.out.println("re-sending ACK");
                	  dialogCall.sendAck(ackRequest);
                    
                  } catch (SipException se) {
                     se.printStackTrace();
                  }
                  
               }
               if (ackRequest2!=null && dialogCall2!=null) {
                  
                   try {
                	   System.out.println("dialogCall22222222"+dialogCall2);
                	  System.out.println("re-sending ACK2");
                      dialogCall2.sendAck(ackRequest2);
                   } catch (SipException se) {
                      se.printStackTrace();
                   }
                   
                }
               return;
           }
           /*
           // If the caller is supposed to send the bye
           if ( Shootme.callerSendsBye && !byeTaskRunning) {
               byeTaskRunning = true;
               new Timer().schedule(new ByeTask(dialog), 2000) ;
           }
           
           System.out.println("transaction state is " + tid.getState());
           System.out.println("Dialog = " + tid.getDialog());
           System.out.println("Dialog State is " + tid.getDialog().getState());
   */
           
           try {

               if (response.getStatusCode() == Response.OK && 
               	cseq.getMethod().equals(Request.REGISTER)) {
               	System.out.println("Sending ACK Gateway!!!!!! REGISTER OK");
               }else if (response.getStatusCode() == Response.OK &&
                  		cseq.getMethod().equals(Request.INVITE)) {
            	   System.out.println("tttt1");
              
            		   System.out.println("processResponse tran");

                    //   new Shootisttest().tran();
                       System.out.println("NNNNNNNNNNNNNNNNNNNNN"+shoot.set);
                       
                       String str = new String(response.getRawContent(), StandardCharsets.UTF_8);
            		   new Shootisttest().SDPParser(str);
            		   
            		   
                       //reinv
                       System.out.println("NNNNNNNNNNDDDDDDDDNNNN"+shoot.referto);
                     //  Shootisttest.sendingRequest("INVITE", shoot.referto);
                    
                       System.out.println("NNNNNNNNNNNNNNNNNNNNN"+shoot.set);
                       
                       while (true){
                    	   System.out.print("");
                    	 
                    	   if(shoot.set == 4){
                    		   System.out.println("NNYYYYYYYYYNNNN"+shoot.set);
                    		   processInviteOK(response, tid);
                    		   
                    		   shoot.set = 0;
                    		   break;
                    		   }
                    	   		
                       		}
    
               } else if (response.getStatusCode() == Response.UNAUTHORIZED) {  
       
            	   register(response);         
             
                        
               }
               
               else if (cseq.getMethod().equals(Request.CANCEL) &&
                   		dialog2.getState() == DialogState.CONFIRMED) {
                      
                           // oops cancel went in too late. Need to hang up the
                           // dialog.
                           System.out
                                   .println("Sending BYE -- cancel went in too late !!");
                           Request byeRequest = dialog2.createRequest(Request.BYE);
                           ClientTransaction ct = sipProvider
                                   .getNewClientTransaction(byeRequest);
                           dialog2.sendRequest(ct);
                       }
               
           } catch (Exception ex) {
               ex.printStackTrace();
               junit.framework.TestCase.fail("Exit JVM");
          
           }
       }
       
       
       
       public void processResponseForReferee(ResponseEvent responseReceivedEvent)
       {
         Response response = responseReceivedEvent.getResponse();
         Transaction tid = responseReceivedEvent.getClientTransaction();
         
         System.out.println("ProcessResponseForReferee income!!!!!!!!!"
         		+ "");
         
         CSeqHeader cseq = (CSeqHeader)response.getHeader("CSeq");
        
         if (cseq.getMethod().equals("INVITE"))
         {
        	 /* 
           System.out.println("ProcessResponseForReferee got a INVITE request");
           try
           {
             sendNotify(response.getStatusCode(), response.getReasonPhrase());
           }
           catch (Exception e1)
           {
             e1.printStackTrace();
           }
           */
        	 /*
           if ((response.getStatusCode() >= 200) && (response.getStatusCode() < 300)) {
             try
             {
               Request ack = tid.getDialog().createAck(cseq.getSeqNumber());
               tid.getDialog().sendAck(ack);
               

               Request bye = tempTargetDialog.createRequest("BYE");
               tempTargetDialog.sendRequest(udpProvider.getNewClientTransaction(bye));
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }
           }*/
         }
       }
       
       
      
       
       public void register(Response response)
       {
         try
         {
           String transport = "udp";
           
           ArrayList viaHeaders = new ArrayList();
           
           ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress, 
             MyPort, 
             transport, null);

           viaHeaders.add(viaHeader);
           


           MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
           


           CallIdHeader callIdHeader = (CallIdHeader)response.getHeader("call-id");
           


           cseq += 1L;
           CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, "REGISTER");
           
           javax.sip.address.Address fromAddress = addressFactory.createAddress("sip:"+Callee+"@192.168.0.180");
           
           FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");
           


           ToHeader toHeader = headerFactory.createToHeader(fromAddress, null);
           






           MaxForwardsHeader maxForwards = headerFactory
             .createMaxForwardsHeader(70);
           URI requestURI = addressFactory.createURI("sip:192.168.0.180");
           Request request = messageFactory.createRequest(requestURI, 
             "REGISTER", callIdHeader, cSeqHeader, fromHeader, 
             toHeader, viaHeaders, maxForwards);
           
           request.addHeader(callIdHeader);
           
           request.addHeader(cSeqHeader);
           
           request.addHeader(fromHeader);
           
           request.addHeader(toHeader);
           
           request.addHeader(maxForwardsHeader);
           
           request.addHeader(viaHeader);
           


           request.addHeader(contactH);
           
           
           if (response != null)
           {
             AuthorizationHeader authHeader = makeAuthHeader(response, request);
             
             request.addHeader(authHeader);
           }
           javax.sip.address.Address fromNameAddress = addressFactory.createAddress(fromAddress.toString());
           

           HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
           

           AllowHeader allow1 = 
             headerFactory.createAllowHeader("REGISTER");
           request.addHeader(allow1);
           AllowHeader allow2 = 
             headerFactory.createAllowHeader("PRACK");
           request.addHeader(allow2);
           AllowHeader allow3 = 
             headerFactory.createAllowHeader("UPDATE");
           request.addHeader(allow3);
           
           SupportedHeader supported1 = 
             headerFactory.createSupportedHeader("100rel");
           request.addHeader(supported1);
           SupportedHeader supported2 = 
             headerFactory.createSupportedHeader("preconditions");
           request.addHeader(supported2);
           SupportedHeader supported3 = 
             headerFactory.createSupportedHeader("path");
           request.addHeader(supported3);
           
           RequireHeader require1 = 
             headerFactory.createRequireHeader("sec-agree");
           request.addHeader(require1);
           RequireHeader require2 = 
             headerFactory.createRequireHeader("preconditions");
           request.addHeader(require2);
           
           SecurityClientHeader secClient = 
             headerFactoryImpl.createSecurityClientHeader();
           secClient.setSecurityMechanism("ipsec-3gpp");
           secClient.setAlgorithm("hmac-md5-96");
           secClient.setEncryptionAlgorithm("des-cbc");
           secClient.setSPIClient(10000);
           secClient.setSPIServer(10001);
           secClient.setPortClient(5063);
           secClient.setPortServer(4166);
           request.addHeader(secClient);
           
           PAccessNetworkInfoHeader accessInfo = 
             headerFactoryImpl.createPAccessNetworkInfoHeader();
           accessInfo.setAccessType("3GPP-UTRAN-TDD");
           accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
           request.addHeader(accessInfo);
           
           PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
           request.addHeader(privacy);
           PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
           request.addHeader(privacy2);
           

           PPreferredIdentityHeader preferredID = 
             headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
           request.addHeader(preferredID);
           





           inviteTid = sipProvider.getNewClientTransaction(request);
           


           inviteTid.sendRequest();
           
           dialog2 = inviteTid.getDialog();
          /* 
           notifier.RefereeEngineStart(MyAddress, MyPort, sipProvider);
           
           System.out.println("notifierMyAddress!!!"+MyAddress);
           System.out.println("notifierMyPort!!!"+MyPort);
           System.out.println("notifiersipProvider!!!"+sipProvider);
           notifier.setReferee("Gateway");
           notifier.setReferer("sis");
           */
         }
         catch (Exception e)
         {
           e.printStackTrace();
         }
       }  
       
   


    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        System.out.println("Transaction Time out");
    }

    public void sendCancel() {
        try {
            System.out.println("Sending cancel");
            Request cancelRequest = inviteTid.createCancel();
            ClientTransaction cancelTid = sipProvider
                    .getNewClientTransaction(cancelRequest);
            cancelTid.sendRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
  
    static String SDPsetting()
    {
    	System.out.println("IPcalleevxcvxcvxcv"+IPcallee);
      SdpFactory sdpFactory = SdpFactory.getInstance();
      String sdpData = null;
      try
      {
        SessionDescription sd = sdpFactory.createSessionDescription();
        

        Version version = sdpFactory.createVersion(0);
        



        Origin origin = sdpFactory.createOrigin("UA", 123456L, 1234567L, "IN", "IP4", IPcallee);
        




        Connection connection = sdpFactory.createConnection("IN", "IP4", IPcallee);
        



        SessionName sessionname = sdpFactory.createSessionName("Session from UA to Rsc");
        
        sd.setVersion(version);
        sd.setOrigin(origin);
        sd.setConnection(connection);
        sd.setSessionName(sessionname);
        


        String[] format = new String[1];
        format[0] = Integer.toString(0);
        
        Vector mds = new Vector();
        //  localRtpPort = Integer.valueOf(RTPportcallee); 
        //(int)(Math.random() * 32767.0D + 24576.0D);
        int rtp = Integer.valueOf(RTPportcallee).intValue(); 
        MediaDescription md1 = sdpFactory.createMediaDescription("audio", rtp, 1, "RTP/AVP", format);
        
        Vector attrs1 = new Vector();
      //  localRtcpPort = Integer.valueOf(RTCPportcallee);
        		//(int)(Math.random() * 32767.0D + 24576.0D);
        int rtcp = Integer.valueOf(RTCPportcallee).intValue();
        Attribute attr1 = sdpFactory.createAttribute("rtcp", Integer.toString(rtcp));
        
        Attribute attr2 = sdpFactory.createAttribute("rtpmap", "0 pcmu/8000");
        Attribute attr3 = sdpFactory.createAttribute("recvonly", null);
        attrs1.addElement(attr1);
        attrs1.addElement(attr2);
        attrs1.addElement(attr3);
        
        md1.setAttributes(attrs1);
        mds.addElement(md1);
        sd.setMediaDescriptions(mds);
        
        sdpData = sd.toString();
      }
      catch (SdpException ex)
      {
        System.err.print(ex.toString());
      }
      return sdpData;
    }
    
    
    static String SDPsetting2()
    {
      SdpFactory sdpFactory = SdpFactory.getInstance();
      String sdpData = null;
      try
      {
        SessionDescription sd = sdpFactory.createSessionDescription();
        

        Version version = sdpFactory.createVersion(0);
        



        Origin origin = sdpFactory.createOrigin("UA", 123456L, 1234567L, "IN", "IP4", shoot.MyAddress);
        




        Connection connection = sdpFactory.createConnection("IN", "IP4", shoot.MyAddress);
        



        SessionName sessionname = sdpFactory.createSessionName("Session from UA to Rsc");
        
        sd.setVersion(version);
        sd.setOrigin(origin);
        sd.setConnection(connection);
        sd.setSessionName(sessionname);
        

        String[] format = new String[1];
        format[0] = Integer.toString(0);
        
        Vector mds = new Vector();
        localRtpPort = (int)(Math.random() * 32767.0D + 24576.0D);
        MediaDescription md1 = sdpFactory.createMediaDescription("audio", localRtpPort, 1, "RTP/AVP", format);
        
        Vector attrs1 = new Vector();
        localRtcpPort = (int)(Math.random() * 32767.0D + 24576.0D);
        Attribute attr1 = sdpFactory.createAttribute("rtcp", Integer.toString(localRtcpPort));
        
        Attribute attr2 = sdpFactory.createAttribute("rtpmap", "0 pcmu/8000");
        Attribute attr3 = sdpFactory.createAttribute("recvonly", null);
        attrs1.addElement(attr1);
        attrs1.addElement(attr2);
        attrs1.addElement(attr3);
        
        md1.setAttributes(attrs1);
        mds.addElement(md1);
  
    
        sd.setMediaDescriptions(mds);
              
        sdpData = sd.toString();
    
      
      }
    
      catch (SdpException ex)
      {
        System.err.print(ex.toString());
      }
      return sdpData;
    }

    public static void SDPParser(String sdpData)
    	    throws ParseException, SdpException
    	  {
    	    SDPAnnounceParser parser = new SDPAnnounceParser(sdpData);
    	    SessionDescriptionImpl sessiondescription = parser.parse();
    	    
    	    Vector attrs = sessiondescription.getAttributes(false);
    	    if (attrs != null)
    	    {
    	      Attribute attrib = (Attribute)attrs.get(0);
    	      System.out.println("attrs = " + attrib.getName());
    	    }
    	    MediaDescription md = 
    	      (MediaDescription)sessiondescription.getMediaDescriptions(false).get(0);
    	    
    	    System.out.println("md attributes " + md.getAttributes(false));
    	    
    	    SessionDescriptionImpl sessiondescription1 = new SDPAnnounceParser(sessiondescription
    	      .toString()).parse();
    	    

    	    new local().recordingSocket(sessiondescription1.getConnection().toString().split("IN IP4 ")[1], md.getMedia().toString().split("m=audio | RTP")[1], md.getAttribute("rtcp"));
    	    System.out.println("Callee IP222222222222: " + IPcallee/*sessiondescription1.getConnection().toString().split("IN IP4 ")[1]*/);
    	    System.out.println("Callee RTP port: " + RTPportcallee/*md.getMedia().toString().split("m=audio | RTP")[1]*/);
    	    System.out.println("Callee RTCP port: " + RTCPportcallee/*md.getAttribute("rtcp")*/);
    	  }
   
    
    public int getLocalRTPport()
    {
      return localRtpPort;
    }
    
    public int getLocalRTCPport()
    {
      return localRtcpPort;
    }
    public  void init() {
    	/*
        try
        {
          new local().STUNPut();
          new local().getlocalIP();
        }
        catch (MessageHeaderParsingException|UtilityException|IOException|MessageAttributeException e)
        {
          ((Throwable) e).printStackTrace();
        }
    */   
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to 
     
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);
        // If you want to use UDP then uncomment this.
        properties.setProperty("javax.sip.STACK_NAME", "local");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.履�??
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootistdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootistlog.txt");

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        // Set to 0 (or NONE) in your production code for max speed.
        // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "TRACE");

        try {  
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        try {
        	
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint(shoot.localIP,
            		6020, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            local listener = this;
            sipProvider.addSipListener(listener);
           
            String fromName = Callee;
            String fromSipAddress = "192.168.0.180";  
            String toUser = Callee;
            String toSipAddress = "192.168.0.180";
            
                   
         // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(shoot.localIP,
                    sipProvider.getListeningPoint(transport).getPort(),
                    transport, null);
       
          System.out.println("sipProvider.getListeningPoint(transport).getPort()"+
          sipProvider.getListeningPoint(transport).getPort());
            // add via headers
            viaHeaders.add(viaHeader);
            
            
            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
                       ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
            		toSipAddress);

            

           // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(invco,
                    Request.REGISTER);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);
        
			// Create the request.
            
            Request request = messageFactory.createRequest(requestURI,
                    Request.REGISTER, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            this.request = request;
            
        /*    
            //Create router headers
            Address routeaddress = addressFactory.createAddress("sip:orig@scscf.192.168.0.180:5060;lr");
            RouteHeader routeHeader = this.headerFactory.createRouteHeader(routeaddress);
			request.addHeader(routeHeader);
			*/
         /*   // Create contact headers
            String host = address.toString();
            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(udpListeningPoint.getPort());
            contactUrl.setLrParam();
         */
            // Create contact headers
			String host = shoot.localIP;
            	//Header contactH;
            			contactH = headerFactory.createHeader("Contact","<sip:"+Callee+"@"+shoot.localIP+":"+MyPort+";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
                        //contactH = headerFactory.createHeader("Contact","<sip:sis@163.17.21.86:5020;transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message");
                        request.addHeader(contactH);
                        
            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);
                  
            
            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);
        
           
            // You can add extension headers of your own making��
            // to the outgoing SIP request.
            // Add the extension header.
            Header extensionHeader = headerFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);

            
            
            /* ++++++++++++++++++++++++++++++++++++++++++++
             *                IMS headers
             * ++++++++++++++++++++++++++++++++++++++++++++
             */

            
            // work-around for IMS headers
            
            HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();

            // Allow header
            /*
            AllowHeader allowHeader =
                headerFactory.createAllowHeader(Request.INVITE + "," +
                        Request.PRACK + "," +
                        Request.UPDATE);
            request.addHeader(allowHeader);
            */
                AllowHeader allow1 =
                headerFactory.createAllowHeader(Request.REGISTER);
            request.addHeader(allow1);
            AllowHeader allow2 =
                headerFactory.createAllowHeader(Request.PRACK);
            request.addHeader(allow2);
            AllowHeader allow3 =
                headerFactory.createAllowHeader(Request.UPDATE);
            request.addHeader(allow3);
            AllowHeader allow4 = 
                    headerFactory.createAllowHeader(Request.REGISTER);
                  request.addHeader(allow4);
           AllowHeader allow5 = 
                    headerFactory.createAllowHeader(Request.INVITE);
                  request.addHeader(allow5);

            // Supported?��???
            /*
            SupportedHeader supportedHeader =
                headerFactory.createSupportedHeader("100rel" + "," +
                        "precondition");
            request.addHeader(supportedHeader);
            */
            SupportedHeader supported1 =
            	headerFactory.createSupportedHeader("100rel");
            request.addHeader(supported1);
            SupportedHeader supported2 =
                headerFactory.createSupportedHeader("preconditions");
            request.addHeader(supported2);
            SupportedHeader supported3 =
                headerFactory.createSupportedHeader("path");
            request.addHeader(supported3);



            // Require
            /*
            RequireHeader requireHeader =
                headerFactory.createRequireHeader("sec-agree"+ "," +
                "precondition");
            request.addHeader(requireHeader);
            */
            RequireHeader require1 =
                headerFactory.createRequireHeader("sec-agree");
            request.addHeader(require1);
            RequireHeader require2 =
                headerFactory.createRequireHeader("preconditions");
            request.addHeader(require2);


            // Security-Client
            SecurityClientHeader secClient =
                headerFactoryImpl.createSecurityClientHeader();
            secClient.setSecurityMechanism("ipsec-3gpp");
            secClient.setAlgorithm("hmac-md5-96");
            secClient.setEncryptionAlgorithm("des-cbc");
            secClient.setSPIClient(10000);
            secClient.setSPIServer(10001);
            secClient.setPortClient(5063);
            secClient.setPortServer(4166);
            request.addHeader(secClient);

            
            // P-Access-Network-Info P
            PAccessNetworkInfoHeader accessInfo =
                headerFactoryImpl.createPAccessNetworkInfoHeader();
            accessInfo.setAccessType("3GPP-UTRAN-TDD");
            accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
            request.addHeader(accessInfo);

            // Privacy
            PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
            request.addHeader(privacy);
            PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
            request.addHeader(privacy2);
/*
            // P-Preferred-Identity
            PPreferredIdentityHeader preferredID =
                headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
            request.addHeader(preferredID);
*/


            /*
             * TEST
             */
            // this is only to illustrate the usage of this headers


            // P-Called-Party-ID
            // only to test
            
  /*          
            
            PCalledPartyIDHeader calledPartyID =
                headerFactoryImpl.createPCalledPartyIDHeader(toNameAddress);
            request.addHeader(calledPartyID);
            
     */       
/*
            // P-Visited-Network-ID
            PVisitedNetworkIDHeader visitedNetworkID1 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID1.setVisitedNetworkID(fromSipAddress
                    .substring(fromSipAddress.indexOf("@")+1));
            PVisitedNetworkIDHeader visitedNetworkID2 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID2.setVisitedNetworkID(toSipAddress
                    .substring(toSipAddress.indexOf("@")+1));
            request.addHeader(visitedNetworkID1);
            request.addHeader(visitedNetworkID2);
*/
/*
            // P-Associated-URI
            PAssociatedURIHeader associatedURI1 =
                headerFactoryImpl.createPAssociatedURIHeader(toNameAddress);
            PAssociatedURIHeader associatedURI2 =
                headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
            request.addHeader(associatedURI1);
            request.addHeader(associatedURI2);
*/

            // P-Asserted-Identity
            PAssertedIdentityHeader assertedID =
                headerFactoryImpl.createPAssertedIdentityHeader(
                        addressFactory.createAddress(toAddress));
            request.addHeader(assertedID);

            TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
            Address telAddress = addressFactory.createAddress(tel);
            
            PAssertedIdentityHeader assertedID2 =
                headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
            request.addHeader(assertedID2);

/*
            // P-Charging-Function-Addresses
            PChargingFunctionAddressesHeader chargAddr =
                headerFactoryImpl.createPChargingFunctionAddressesHeader();
            chargAddr.addChargingCollectionFunctionAddress("test1.ims.test");
            chargAddr.addEventChargingFunctionAddress("testevent");
            request.addHeader(chargAddr);

            // P-Charging-Vector
            PChargingVectorHeader chargVect =
                headerFactoryImpl.createChargingVectorHeader("icid");
            chargVect.setICIDGeneratedAt("icidhost");
            chargVect.setOriginatingIOI("origIOI");
            chargVect.setTerminatingIOI("termIOI");
            request.addHeader(chargVect);

            // P-Media-Authorization
            PMediaAuthorizationHeader mediaAuth1 =
                headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
            PMediaAuthorizationHeader mediaAuth2 =
                headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
            
            request.addHeader(mediaAuth1);
            request.addHeader(mediaAuth2);
            

            // Path header
            PathHeader path1 =
                headerFactoryImpl.createPathHeader(fromNameAddress);
            PathHeader path2 =
                headerFactoryImpl.createPathHeader(toNameAddress);
            request.addHeader(path1);
            request.addHeader(path2);
 */           
/*
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"

                    // bandwith
                    + "b=AS:25.4\r\n"
                    // precondition mechanism
                    + "a=curr:qos local none\r\n"
                    + "a=curr:qos remote none\r\n"
                    + "a=des:qos mandatory local sendrec\r\n"
                    + "a=des:qos none remote sendrec\r\n"


                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            request.setContent(contents, contentTypeHeader);
            // You can add as many extension headers as you
            // want.
   */         
            extensionHeader = headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);       
            // send the request out.
           
            inviteTid.sendRequest();
          
            dialogCall = inviteTid.getDialog();
            
            
            
            

        } catch (Exception ex) {	
            ex.printStackTrace();
            usage();
        }
    }
    
    public  void init2() {
    	
        try
        {
          new local().STUNPut();
          new local().getlocalIP();
        }
        catch (MessageHeaderParsingException|UtilityException|IOException|MessageAttributeException e)
        {
          ((Throwable) e).printStackTrace();
        }
       
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to 
     
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);
        // If you want to use UDP then uncomment this.
        properties.setProperty("javax.sip.STACK_NAME", "shootmetest");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.履�??
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootistdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootistlog.txt");

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        // Set to 0 (or NONE) in your production code for max speed.
        // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "TRACE");

        try {  
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint(localIP,
            		MyPort, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            local listener = this;
            sipProvider.addSipListener(listener);
           
            String fromName = Callee;
            String fromSipAddress = "192.168.0.180";  
            String toUser = Callee;
            String toSipAddress = "192.168.0.180";
            
                   
         // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(MyAddress,
                    sipProvider.getListeningPoint(transport).getPort(),
                    transport, null);
       
          System.out.println("sipProvider.getListeningPoint(transport).getPort()"+
          sipProvider.getListeningPoint(transport).getPort());
            // add via headers
            viaHeaders.add(viaHeader);
            
            
            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
                       ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
            		toSipAddress);

            

           // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(invco,
                    Request.REGISTER);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);
        
			// Create the request.
            
            Request request = messageFactory.createRequest(requestURI,
                    Request.REGISTER, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            this.request = request;
            
        /*    
            //Create router headers
            Address routeaddress = addressFactory.createAddress("sip:orig@scscf.192.168.0.180:5060;lr");
            RouteHeader routeHeader = this.headerFactory.createRouteHeader(routeaddress);
			request.addHeader(routeHeader);
			*/
         /*   // Create contact headers
            String host = address.toString();
            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(udpListeningPoint.getPort());
            contactUrl.setLrParam();
         */
            // Create contact headers
			String host = MyAddress;
            	//Header contactH;
            			contactH = headerFactory.createHeader("Contact","<sip:"+Callee+"@"+MyAddress+":"+MyPort+";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
                        //contactH = headerFactory.createHeader("Contact","<sip:sis@163.17.21.86:5020;transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message");
                        request.addHeader(contactH);
                        
            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);
                  
            
            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);
        
           
            // You can add extension headers of your own making��
            // to the outgoing SIP request.
            // Add the extension header.
            Header extensionHeader = headerFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);

            
            
            /* ++++++++++++++++++++++++++++++++++++++++++++
             *                IMS headers
             * ++++++++++++++++++++++++++++++++++++++++++++
             */

            
            // work-around for IMS headers
            
            HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();

            // Allow header
            /*
            AllowHeader allowHeader =
                headerFactory.createAllowHeader(Request.INVITE + "," +
                        Request.PRACK + "," +
                        Request.UPDATE);
            request.addHeader(allowHeader);
            */
                AllowHeader allow1 =
                headerFactory.createAllowHeader(Request.REGISTER);
            request.addHeader(allow1);
            AllowHeader allow2 =
                headerFactory.createAllowHeader(Request.PRACK);
            request.addHeader(allow2);
            AllowHeader allow3 =
                headerFactory.createAllowHeader(Request.UPDATE);
            request.addHeader(allow3);
            AllowHeader allow4 = 
                    headerFactory.createAllowHeader(Request.REGISTER);
                  request.addHeader(allow4);
           AllowHeader allow5 = 
                    headerFactory.createAllowHeader(Request.INVITE);
                  request.addHeader(allow5);

            // Supported?��???
            /*
            SupportedHeader supportedHeader =
                headerFactory.createSupportedHeader("100rel" + "," +
                        "precondition");
            request.addHeader(supportedHeader);
            */
            SupportedHeader supported1 =
            	headerFactory.createSupportedHeader("100rel");
            request.addHeader(supported1);
            SupportedHeader supported2 =
                headerFactory.createSupportedHeader("preconditions");
            request.addHeader(supported2);
            SupportedHeader supported3 =
                headerFactory.createSupportedHeader("path");
            request.addHeader(supported3);



            // Require
            /*
            RequireHeader requireHeader =
                headerFactory.createRequireHeader("sec-agree"+ "," +
                "precondition");
            request.addHeader(requireHeader);
            */
            RequireHeader require1 =
                headerFactory.createRequireHeader("sec-agree");
            request.addHeader(require1);
            RequireHeader require2 =
                headerFactory.createRequireHeader("preconditions");
            request.addHeader(require2);


            // Security-Client
            SecurityClientHeader secClient =
                headerFactoryImpl.createSecurityClientHeader();
            secClient.setSecurityMechanism("ipsec-3gpp");
            secClient.setAlgorithm("hmac-md5-96");
            secClient.setEncryptionAlgorithm("des-cbc");
            secClient.setSPIClient(10000);
            secClient.setSPIServer(10001);
            secClient.setPortClient(5063);
            secClient.setPortServer(4166);
            request.addHeader(secClient);

            
            // P-Access-Network-Info P
            PAccessNetworkInfoHeader accessInfo =
                headerFactoryImpl.createPAccessNetworkInfoHeader();
            accessInfo.setAccessType("3GPP-UTRAN-TDD");
            accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
            request.addHeader(accessInfo);

            // Privacy
            PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
            request.addHeader(privacy);
            PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
            request.addHeader(privacy2);
/*
            // P-Preferred-Identity
            PPreferredIdentityHeader preferredID =
                headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
            request.addHeader(preferredID);
*/


            /*
             * TEST
             */
            // this is only to illustrate the usage of this headers


            // P-Called-Party-ID
            // only to test
            
  /*          
            
            PCalledPartyIDHeader calledPartyID =
                headerFactoryImpl.createPCalledPartyIDHeader(toNameAddress);
            request.addHeader(calledPartyID);
            
     */       
/*
            // P-Visited-Network-ID
            PVisitedNetworkIDHeader visitedNetworkID1 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID1.setVisitedNetworkID(fromSipAddress
                    .substring(fromSipAddress.indexOf("@")+1));
            PVisitedNetworkIDHeader visitedNetworkID2 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID2.setVisitedNetworkID(toSipAddress
                    .substring(toSipAddress.indexOf("@")+1));
            request.addHeader(visitedNetworkID1);
            request.addHeader(visitedNetworkID2);
*/
/*
            // P-Associated-URI
            PAssociatedURIHeader associatedURI1 =
                headerFactoryImpl.createPAssociatedURIHeader(toNameAddress);
            PAssociatedURIHeader associatedURI2 =
                headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
            request.addHeader(associatedURI1);
            request.addHeader(associatedURI2);
*/

            // P-Asserted-Identity
            PAssertedIdentityHeader assertedID =
                headerFactoryImpl.createPAssertedIdentityHeader(
                        addressFactory.createAddress(toAddress));
            request.addHeader(assertedID);

            TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
            Address telAddress = addressFactory.createAddress(tel);
            
            PAssertedIdentityHeader assertedID2 =
                headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
            request.addHeader(assertedID2);

/*
            // P-Charging-Function-Addresses
            PChargingFunctionAddressesHeader chargAddr =
                headerFactoryImpl.createPChargingFunctionAddressesHeader();
            chargAddr.addChargingCollectionFunctionAddress("test1.ims.test");
            chargAddr.addEventChargingFunctionAddress("testevent");
            request.addHeader(chargAddr);

            // P-Charging-Vector
            PChargingVectorHeader chargVect =
                headerFactoryImpl.createChargingVectorHeader("icid");
            chargVect.setICIDGeneratedAt("icidhost");
            chargVect.setOriginatingIOI("origIOI");
            chargVect.setTerminatingIOI("termIOI");
            request.addHeader(chargVect);

            // P-Media-Authorization
            PMediaAuthorizationHeader mediaAuth1 =
                headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
            PMediaAuthorizationHeader mediaAuth2 =
                headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
            
            request.addHeader(mediaAuth1);
            request.addHeader(mediaAuth2);
            

            // Path header
            PathHeader path1 =
                headerFactoryImpl.createPathHeader(fromNameAddress);
            PathHeader path2 =
                headerFactoryImpl.createPathHeader(toNameAddress);
            request.addHeader(path1);
            request.addHeader(path2);
 */           
/*
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"

                    // bandwith
                    + "b=AS:25.4\r\n"
                    // precondition mechanism
                    + "a=curr:qos local none\r\n"
                    + "a=curr:qos remote none\r\n"
                    + "a=des:qos mandatory local sendrec\r\n"
                    + "a=des:qos none remote sendrec\r\n"


                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            request.setContent(contents, contentTypeHeader);
            // You can add as many extension headers as you
            // want.
   */         
            extensionHeader = headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);       
            // send the request out.
            inviteTid.sendRequest();
          
            dialogCall = inviteTid.getDialog();
            
            
            

        } catch (Exception ex) {	
            ex.printStackTrace();
            usage();
        }
    }
    
    
    
    AuthorizationHeader makeAuthHeader(Response resp, Request req)
	  {
	    AuthorizationHeader nothing = null;
	    try
	    {
	      WWWAuthenticateHeader ah_c = 
	        (WWWAuthenticateHeader)resp.getHeader("WWW-Authenticate");
	      

	      AuthorizationHeader ah_r = 
	        headerFactory.createAuthorizationHeader(ah_c.getScheme());
	     
/*�`�N*/	  URI request_uri = req.getRequestURI();
	      String request_method = req.getMethod();
	      String nonce = ah_c.getNonce();
	      String algrm = ah_c.getAlgorithm();
	      String realm = ah_c.getRealm();
	      String username = Callee+"@192.168.0.180";
	      String password = Callee;
	      
	      MessageDigest mdigest = MessageDigest.getInstance(algrm);
	      
	      DigestServerAuthenticationHelper Str = null;
	      
	      String A1 = username + ":" + realm + ":" + password;
	      String HA1 = DigestServerAuthenticationHelper.toHexString(mdigest.digest(A1.getBytes()));
	      

	      String A2 = request_method.toUpperCase() + ":" + request_uri;
	      String HA2 = DigestServerAuthenticationHelper.toHexString(mdigest.digest(A2.getBytes()));
	      

	      String KD = HA1 + ":" + nonce + ":" + HA2;
	      String response = DigestServerAuthenticationHelper.toHexString(mdigest.digest(KD.getBytes()));
	      
	      ah_r.setRealm(realm);
	      ah_r.setNonce(nonce);
	      ah_r.setUsername(username);
	      ah_r.setURI(request_uri);
	      ah_r.setAlgorithm(algrm);
	      ah_r.setResponse(response);
	      
	      return ah_r;
	    }
	    catch (Exception e)
	    {
	      System.out.println("oh hell");
	    }
	    return nothing;
}
    


    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException happened for "
                + exceptionEvent.getHost() + " port = "
                + exceptionEvent.getPort());

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("dialogTerminatedEvent");

    }
   

	public void SendBye() {
		// TODO Auto-generated method stub
		try
	    {
			 Request byeRequest = dialogCall.createRequest("BYE");
		      ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
		      dialogCall.sendRequest(ct);
	    }
	    catch (SipException e)
	    {
	      e.printStackTrace();
	    }
	}
	
	 private static void initFactories () throws Exception {

	        SipFactory sipFactory = SipFactory.getInstance();
	        sipFactory.setPathName("gov.nist");
	        Properties properties = new Properties();
/*
	        logger.addAppender(new FileAppender
	            ( new SimpleLayout(),"refereeoutputlog.txt" ));
*/
	        properties.setProperty("javax.sip.STACK_NAME", "referee" );
	        // You need 16 for logging traces. 32 for debug + traces.
	        // Your code will limp at 32 but it is best for debugging.
	        // JvB note: debug level may impact order of messages!
	        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
	        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
	                "refereedebug.txt");
	        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
	                "refereelog.txt");


	        try {
	            // Create SipStack object
	            sipStack = sipFactory.createSipStack(properties);
	         //   logger.info("sipStack = " + sipStack);
	        } catch (PeerUnavailableException e) {
	            // could not find
	            // gov.nist.jain.protocol.ip.sip.SipStackImpl
	            // in the classpath
	            e.printStackTrace();
	            System.err.println(e.getMessage());
	            if (e.getCause() != null)
	                e.getCause().printStackTrace();
	            junit.framework.TestCase.fail("Exit JVM");
	        }

	        try {
	            headerFactory = sipFactory.createHeaderFactory();
	            addressFactory = sipFactory.createAddressFactory();
	            messageFactory = sipFactory.createMessageFactory();
	        } catch  (Exception ex) {
	            ex.printStackTrace();
	            junit.framework.TestCase.fail("Exit JVM");
	        }
	    }
	 public local( int port ) {
	        this.port = port;
	    }
	 public local() {
		// TODO Auto-generated constructor stub
	}


	public void createProvider() {

	        try {

	            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
	                    this.port, "udp");

	            this.sipProvider = sipStack.createSipProvider(lp);
	           // logger.info("udp provider " + udpProvider);

	        } catch (Exception ex) {
	           // logger.info(ex.getMessage());
	            ex.printStackTrace();
	            usage();
	        }

	    }
	public void referstart(){
		try {
			initFactories();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*local notifier = new local( 5065 );
     /* notifier.createProvider( );
        try {
			notifier.sipProvider.addSipListener(notifier);
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public void tran() {
		// TODO Auto-generated method stub
		
	}


	
	
   
}


package Target;

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfoHeader;
import gov.nist.javax.sip.header.ims.PAssertedServiceHeader;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.SecurityClientHeader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AllowHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class Referee
{
  private static String pubHostAddre="163.17.21.85";
  private static int pubHostPort=5123;
  private static String Referer="sis";
  private static String Referee="calleelv";
  private static AddressFactory addressFactory;
  private static MessageFactory messageFactory;
  private static HeaderFactory headerFactory;
  private static SipProvider udpProvider;
  private static Dialog dialog;
  public static String tempTargetDialogID;
  public static Dialog tempTargetDialog;
  private static EventHeader referEvent;
  public String refto;
  
  public void setReferer(String Referer)
  {
    Referer = Referer;
    System.out.println("Referer!!!"+Referer);
  }
  
  public void setReferee(String Referee)
  {
    Referee = Referee;
    System.out.println("Referee!!!"+Referee);
  }
  
  private static void usage()
  {
    System.exit(0);
  }
  
  public int processRequestrefer(RequestEvent requestEvent, ServerTransaction serverTransactionId)
  {
    Request request = requestEvent.getRequest();
    if (request.getMethod().equals("REFER"))
    {
      System.out.println(" got REFER!!!!!!!!!!!!!!");
      System.out.println(" REFER!!!!!!aaaaaaaaaa!!!!!!!!"+requestEvent);
      System.out.println(" REFER!!!!!!aBBBBBBBBBBaaaa!!!!!!!!"+serverTransactionId);
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
      System.out.println("Missing Refer-To");
      Response bad = messageFactory.createResponse(400, refer);
      bad.setReasonPhrase("Missing Refer-To");
      sipProvider.sendResponse(bad);
      return 0;
    }
    Response response = null;
    
    ServerTransaction st = serverTransaction;
    if (st == null) {
      st = sipProvider.getNewServerTransaction(refer);
    }
  //  String toTag = Integer.toHexString( (int) (Math.random() * Integer.MAX_VALUE) );
    String toTag = Integer.toHexString((int)(Math.random() * 2147483647.0D));
    response = messageFactory.createResponse(202, refer);
    ToHeader toHeader = (ToHeader)response.getHeader("To");
    if (toHeader.getTag() != null) {
      System.err.println("####ERROR: To-tag!=null but no dialog match! My dialog=" + dialog.getState());
    }
    toHeader.setTag(toTag);
    
    dialog = st.getDialog();
    
    dialog.terminateOnBye(false);
    if (dialog != null)
    {
      System.out.println("Dialog " + dialog);
      System.out.println("Dialog state " + dialog.getState());
      System.out.println("local tag=" + dialog.getLocalTag());
      System.out.println("remote tag=" + dialog.getRemoteTag());
    }
    tempTargetDialogID = refer.getHeader("Target-Dialog").toString().split("Target-Dialog: ")[0].replaceAll("\\s+", "");
    





    Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + pubHostAddre + ":" + pubHostPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
    response.addHeader(contactH);
    

    ExpiresHeader expires = (ExpiresHeader)refer.getHeader("Expires");
    if (expires == null) {
      expires = headerFactory.createExpiresHeader(30);
    }
    response.addHeader(expires);
    



    System.out.println("Getting refTo: " + refTo.toString().split("sip:|@")[1] + "!!!!!!!!!!!!!!!!!!!!");
    System.out.println("ReferTo: " + refTo.toString().split("sip:|@")[1] + "   Referer: " + Referer + "!!!!!!!!!!!!!!!!!!!!");
    if (refTo.toString().split("sip:|@")[1].equals("CN"))
    {
      st.sendResponse(response);
      

      referEvent = headerFactory.createEventHeader("refer");
      

      long id = ((CSeqHeader)refer.getHeader("CSeq")).getSeqNumber();
      referEvent.setEventId(Long.toString(id));
      
    //  sendNotify(100, "Trying");
       refto = refTo.toString().split("sip:|@")[1];
       System.out.println("asdasdasdas"+refto);


      return 1;
    }
    return 0;
  }
  
  private void sendNotify(int code, String reason)
    throws SipException, ParseException
  {
	  System.out.println("sendNotifyXXXXXXXXXXXXXXXX");
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
    

    Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + pubHostAddre + ":" + pubHostPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
    notifyRequest.setHeader(contactH);
    




    Address routeaddress = addressFactory.createAddress("sip:orig@88.17.21.88:5060;lr");
    RouteHeader routeHeader = headerFactory.createRouteHeader(routeaddress);
    routeaddress = addressFactory.createAddress("sip:term@163.17.21.88:5060;lr");
    RouteHeader routeHeader2 = headerFactory.createRouteHeader(routeaddress);
    notifyRequest.setHeader(routeHeader2);
    






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
    





    ClientTransaction ct2 = udpProvider.getNewClientTransaction(notifyRequest);
    
    ContentTypeHeader ct = headerFactory.createContentTypeHeader("message", "sipfrag");
    ct.setParameter("version", "2.0");
    
    notifyRequest.setContent("SIP/2.0 " + code + ' ' + reason, ct);
    


    dialog.sendRequest(ct2);
  }
  
  public void processResponseForReferee(ResponseEvent responseReceivedEvent)
  {
    Response response = responseReceivedEvent.getResponse();
    Transaction tid = responseReceivedEvent.getClientTransaction();
    
    System.out.println("ProcessResponseForReferee income!!!!!!!!!");
    
    CSeqHeader cseq = (CSeqHeader)response.getHeader("CSeq");
    if (cseq.getMethod().equals("INVITE"))
    {
      System.out.println("ProcessResponseForReferee got a INVITE request");
      try
      {
        sendNotify(response.getStatusCode(), response.getReasonPhrase());
      }
      catch (Exception e1)
      {
        e1.printStackTrace();
      }
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
      }
    }
  }
  
  public void sendInvite(ReferToHeader to)
  {
    try
    {
      String fromName = Referee;
      String fromSipAddress = "open-ims.test";
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
      ViaHeader viaHeader = headerFactory.createViaHeader(pubHostAddre, 
        pubHostPort, 
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
      

      Header contactH = headerFactory.createHeader("Contact", "<sip:" + Referee + "@" + pubHostAddre + ":" + pubHostPort + ";transport=udp>;expires=60;+g.oma.sip-im;language=\"en,fr\";+g.3gpp.smsip;+g.oma.sip-im.large-message;audio;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\";+g.3gpp.cs-voice");
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
  
  private static void initFactories()
    throws Exception
  {
    SipFactory sipFactory = SipFactory.getInstance();
    sipFactory.setPathName("gov.nist");
    Properties properties = new Properties();
    
    properties.setProperty("javax.sip.STACK_NAME", "referee");
    


    properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    try
    {
      headerFactory = sipFactory.createHeaderFactory();
      addressFactory = sipFactory.createAddressFactory();
      messageFactory = sipFactory.createMessageFactory();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(0);
    }
  }
  
  public void RefereeEngineStart(String pubHostAddre, int pubHostPort, SipProvider udpProvider)
  {
    try
    {
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    udpProvider = udpProvider;
    pubHostAddre = pubHostAddre;
    pubHostPort = pubHostPort;
    
    System.out.println("udpProvider!!!"+udpProvider);
    System.out.println("pubHostAddre!!!"+pubHostAddre);
    System.out.println("pubHostPort!!!"+pubHostPort);
  }
}


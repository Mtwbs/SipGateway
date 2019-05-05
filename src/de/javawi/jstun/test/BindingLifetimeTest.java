package de.javawi.jstun.test;
import com.owera.common.log.*;
import de.javawi.jstun.attribute.*;
import de.javawi.jstun.header.*;
import de.javawi.jstun.util.*;


import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeInterface.MessageAttributeType;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.attribute.ResponseAddress;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderInterface.MessageHeaderType;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class BindingLifetimeTest
{
  private static final Logger LOGGER = new Logger();
  String stunServer;
  int port;
  int timeout = 300;
  public MappedAddress ma;
  Timer timer;
  private DatagramSocket initialSocket;
  int upperBinarySearchLifetime = 345000;
  int lowerBinarySearchLifetime = 0;
  int binarySearchLifetime = (this.upperBinarySearchLifetime + this.lowerBinarySearchLifetime) / 2;
  int lifetime = -1;
  boolean completed = false;
  
  public BindingLifetimeTest(String stunServer, int port)
  {
    this.stunServer = stunServer;
    this.port = port;
    this.timer = new Timer(true);
  }
  
  public void test()
    throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageAttributeException, MessageHeaderParsingException
  {
    this.initialSocket = new DatagramSocket();
    this.initialSocket.connect(InetAddress.getByName(this.stunServer), this.port);
    this.initialSocket.setSoTimeout(this.timeout);
    if (bindingCommunicationInitialSocket()) {
      return;
    }
    BindingLifetimeTask task = new BindingLifetimeTask();
    this.timer.schedule(task, this.binarySearchLifetime);
    LOGGER.debug("Timer scheduled initially: " + this.binarySearchLifetime + ".");
  }
  
  public void test2(int defPORT)
    throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageAttributeException, MessageHeaderParsingException
  {
    this.initialSocket = new DatagramSocket(new InetSocketAddress(defPORT));
    this.initialSocket.connect(InetAddress.getByName(this.stunServer), this.port);
    this.initialSocket.setSoTimeout(this.timeout);
    if (bindingCommunicationInitialSocket()) {}
  }
  
  private boolean bindingCommunicationInitialSocket()
    throws UtilityException, IOException, MessageHeaderParsingException, MessageAttributeParsingException
  {
    MessageHeader sendMH = new MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest);
    sendMH.generateTransactionID();
    ChangeRequest changeRequest = new ChangeRequest();
    sendMH.addMessageAttribute(changeRequest);
    byte[] data = sendMH.getBytes();
    
    DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(this.stunServer), this.port);
    this.initialSocket.send(send);
    LOGGER.debug("Binding Request sent.");
    System.out.println("Binding Request sent.");
    
    MessageHeader receiveMH = new MessageHeader();
    while (!receiveMH.equalTransactionID(sendMH))
    {
      DatagramPacket receive = new DatagramPacket(new byte['È'], 200);
      this.initialSocket.receive(receive);
      receiveMH = MessageHeader.parseHeader(receive.getData());
      receiveMH.parseAttributes(receive.getData());
    }
    this.ma = ((MappedAddress)receiveMH.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress));
    ErrorCode ec = (ErrorCode)receiveMH.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode);
    if (ec != null)
    {
      LOGGER.debug("Message header contains an Errorcode message attribute.");
      return true;
    }
    if (this.ma == null)
    {
      LOGGER.debug("Response does not contain a Mapped Address message attribute.");
      return true;
    }
    this.initialSocket.close();
    return false;
  }
  
  public int getLifetime()
  {
    return this.lifetime;
  }
  
  public boolean isCompleted()
  {
    return this.completed;
  }
  
  public void setUpperBinarySearchLifetime(int upperBinarySearchLifetime)
  {
    this.upperBinarySearchLifetime = upperBinarySearchLifetime;
    this.binarySearchLifetime = ((upperBinarySearchLifetime + this.lowerBinarySearchLifetime) / 2);
  }
  
  class BindingLifetimeTask
    extends TimerTask
  {
    public BindingLifetimeTask() {}
    
    public void run()
    {
      try
      {
        lifetimeQuery();
      }
      catch (Exception e)
      {
        BindingLifetimeTest.LOGGER.debug("Unhandled Exception. BindLifetimeTasks stopped.");
        e.printStackTrace();
      }
    }
    
    public void lifetimeQuery()
      throws UtilityException, MessageAttributeException, MessageHeaderParsingException, MessageAttributeParsingException, IOException
    {
      try
      {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName(BindingLifetimeTest.this.stunServer), BindingLifetimeTest.this.port);
        socket.setSoTimeout(BindingLifetimeTest.this.timeout);
        
        MessageHeader sendMH = new MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();
        ChangeRequest changeRequest = new ChangeRequest();
        ResponseAddress responseAddress = new ResponseAddress();
        responseAddress.setAddress(BindingLifetimeTest.this.ma.getAddress());
        responseAddress.setPort(BindingLifetimeTest.this.ma.getPort());
        sendMH.addMessageAttribute(changeRequest);
        sendMH.addMessageAttribute(responseAddress);
        byte[] data = sendMH.getBytes();
        
        DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(BindingLifetimeTest.this.stunServer), BindingLifetimeTest.this.port);
        socket.send(send);
        BindingLifetimeTest.LOGGER.debug("Binding Request sent.");
        
        MessageHeader receiveMH = new MessageHeader();
        while (!receiveMH.equalTransactionID(sendMH))
        {
          DatagramPacket receive = new DatagramPacket(new byte['È'], 200);
          BindingLifetimeTest.this.initialSocket.receive(receive);
          receiveMH = MessageHeader.parseHeader(receive.getData());
          receiveMH.parseAttributes(receive.getData());
        }
        ErrorCode ec = (ErrorCode)receiveMH.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode);
        if (ec != null)
        {
          BindingLifetimeTest.LOGGER.debug("Message header contains errorcode message attribute.");
          return;
        }
        BindingLifetimeTest.LOGGER.debug("Binding Response received.");
        if (BindingLifetimeTest.this.upperBinarySearchLifetime == BindingLifetimeTest.this.lowerBinarySearchLifetime + 1)
        {
          BindingLifetimeTest.LOGGER.debug("BindingLifetimeTest completed. UDP binding lifetime: " + BindingLifetimeTest.this.binarySearchLifetime + ".");
          BindingLifetimeTest.this.completed = true;
          return;
        }
        BindingLifetimeTest.this.lifetime = BindingLifetimeTest.this.binarySearchLifetime;
        BindingLifetimeTest.LOGGER.debug("Lifetime update: " + BindingLifetimeTest.this.lifetime + ".");
        BindingLifetimeTest.this.lowerBinarySearchLifetime = BindingLifetimeTest.this.binarySearchLifetime;
        BindingLifetimeTest.this.binarySearchLifetime = ((BindingLifetimeTest.this.upperBinarySearchLifetime + BindingLifetimeTest.this.lowerBinarySearchLifetime) / 2);
        if (BindingLifetimeTest.this.binarySearchLifetime > 0)
        {
          BindingLifetimeTask task = new BindingLifetimeTask();
          BindingLifetimeTest.this.timer.schedule(task, BindingLifetimeTest.this.binarySearchLifetime);
          BindingLifetimeTest.LOGGER.debug("Timer scheduled: " + BindingLifetimeTest.this.binarySearchLifetime + ".");
        }
        else
        {
          BindingLifetimeTest.this.completed = true;
        }
      }
      catch (SocketTimeoutException ste)
      {
        BindingLifetimeTest.LOGGER.debug("Read operation at query socket timeout.");
        if (BindingLifetimeTest.this.upperBinarySearchLifetime == BindingLifetimeTest.this.lowerBinarySearchLifetime + 1)
        {
          BindingLifetimeTest.LOGGER.debug("BindingLifetimeTest completed. UDP binding lifetime: " + BindingLifetimeTest.this.binarySearchLifetime + ".");
          BindingLifetimeTest.this.completed = true;
          return;
        }
        BindingLifetimeTest.this.upperBinarySearchLifetime = BindingLifetimeTest.this.binarySearchLifetime;
        BindingLifetimeTest.this.binarySearchLifetime = ((BindingLifetimeTest.this.upperBinarySearchLifetime + BindingLifetimeTest.this.lowerBinarySearchLifetime) / 2);
        if (BindingLifetimeTest.this.binarySearchLifetime > 0)
        {
          if (BindingLifetimeTest.this.bindingCommunicationInitialSocket()) {
            return;
          }
          BindingLifetimeTask task = new BindingLifetimeTask();
          BindingLifetimeTest.this.timer.schedule(task, BindingLifetimeTest.this.binarySearchLifetime);
          BindingLifetimeTest.LOGGER.debug("Timer scheduled: " + BindingLifetimeTest.this.binarySearchLifetime + ".");
        }
        else
        {
          BindingLifetimeTest.this.completed = true;
        }
      }
    }
  }
}

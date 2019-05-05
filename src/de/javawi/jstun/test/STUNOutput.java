package de.javawi.jstun.test;

import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;

public class STUNOutput
{
  private static BindingLifetimeTest getstun = new BindingLifetimeTest("163.17.21.90", 3478);
  
  public static void main(String[] args)
    throws SocketException, UnknownHostException, MessageAttributeParsingException, MessageHeaderParsingException, UtilityException, IOException, MessageAttributeException
  {
    getstun.test();
    System.out.println(getstun.ma.getPort() + " OK");
    System.out.println(getstun.ma.getAddress() + " OK");
  }
}

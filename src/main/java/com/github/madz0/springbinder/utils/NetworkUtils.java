package com.github.madz0.springbinder.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by Hamid Haghshenas.
 */
public class NetworkUtils {
    /**
     * To prevent abuse
     */
    private NetworkUtils() {
        throw new IllegalStateException("This class must be used in a static way");
    }

    private static final Pattern ipV4Pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");

    public enum IpVersion {
        IPv4, IPv6;
    }

    public static IpVersion guessVersion(String ipAddress) {
        if (StringUtils.isBlank(ipAddress)) {
            return null;
        }
        if (ipAddress.contains(".")) {
            return IpVersion.IPv4;
        }
        if (ipAddress.contains(":")) {
            return IpVersion.IPv6;
        }
        return null;
    }

    public static int[] textToNumericFormat(String ip) {
        IpVersion version = guessVersion(ip);
        if (version == null) {
            return null;
        }
        byte[] byteArray;
        //TODO we must replace IPAddressUtils someday when we have suitable replacement
        switch (version) {
            case IPv4:
                if (!ipV4Pattern.matcher(ip).matches()) {
                    return null;
                }
                byteArray = IPAddressUtil.textToNumericFormatV4(ip);
                break;
            case IPv6:
                byteArray = IPAddressUtil.textToNumericFormatV6(ip);
                break;
            default:
                throw new IllegalStateException("Unknown IP version: " + version);
        }
        if (byteArray == null) {
            return null;
        }
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = (byteArray[i] >= 0 ? byteArray[i] : byteArray[i] + 256);
        }
        return intArray;
    }

    public static String numericFormatToText(int[] ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        byte[] byteArray = new byte[ipAddress.length];
        for (int i = 0; i < ipAddress.length; i++) {
            if (ipAddress[i] < 0 || ipAddress[i] > 255) {
                return null;
            }
            byteArray[i] = (byte) ipAddress[i];
        }
        try {
            return InetAddress.getByAddress(byteArray).getHostAddress();
        } catch (UnknownHostException e) {
            LoggerFactory.getLogger(NetworkUtils.class).debug("can not resolve ip: " + Arrays.toString(ipAddress), e);
            return null;
        }
    }

    /**
     * Gets a representation of the given IP-address which
     * <ul>
     * <li>is either valid or null (if <code>ipAddress</code> is invalid, returns null)</li>
     * <li>does not change the IP-address (may only get another representation)</li>
     * <li>is unified (if <code>ip1</code> and <code>ip2</code> are given, the results are equal strings
     * iff <code>ip1</code> and <code>ip2</code> are the same IP-address)</li>
     * </ul>
     * For example, if <code>ipAddress = "192.168.010.002"</code>, the result will be <code>"192.168.10.2"</code>.
     *
     * @param ipAddress a given IP-address, either valid or invalid (or even null)
     * @return <code>null</code> if <code>ipAddress</code> is null or invalid.
     * Otherwise, returns a unified representation of <code>ipAddress</code>.
     */
    public static String getUnifiedIpAddressRepresentation(String ipAddress) {
        int[] intArray = textToNumericFormat(ipAddress);
        return numericFormatToText(intArray);
    }

    /**
     * Compares the given IP-addresses lexicographically.
     *
     * @param address1 an IP-address to be compared
     * @param address2 another IP-address to be compared
     * @return the result of the comparison, regarding to Java comparison rules
     * @throws NullPointerException if either argument is null
     */
    public static int compare(int[] address1, int[] address2) {
        for (int i = 0; i < address1.length; i++) {
            if (address1[i] != address2[i]) {
                return address1[i] - address2[i];
            }
        }
        return 0;
    }

    /**
     * Compares the numeric representation of the given IP-addresses lexicographically
     * (see {@link #compare(int[], int[])}).
     *
     * @param address1 an IP-address to be compared
     * @param address2 another IP-address to be compared
     * @return the result of the comparison
     */
    public static int compare(String address1, String address2) {
        int[] address1Array = textToNumericFormat(address1);
        int[] address2Array = textToNumericFormat(address2);
        return compare(address1Array, address2Array);
    }

    /**
     * Checks whether the given address represents a valid IPv4, IPv6 address or a subnet mask.
     *
     * @param ipAddress an IP-address (or subnet mask)
     * @return whether <code>ipAddress</code> is a valid IP-address or subnet mask
     */
    public static boolean isValid(String ipAddress) {
        return getUnifiedIpAddressRepresentation(ipAddress) != null;
    }

    /**
     * Checks whether the given address represents a valid IPv4 subnet mask
     *
     * @param subnetMask an IPv4 subnet mask
     * @return whether <code>subnetMask</code> is a valid IPv4 subnet mask
     */
    public static boolean isValidMask(String subnetMask) {
        if (!isValid(subnetMask)) {
            return false;
        }
        int[] parts = textToNumericFormat(subnetMask);
        int totalLength = parts.length * 8;
        boolean zeroFound = false;
        for (int i = 0; i < totalLength; i++) {
            if (getBit(parts, i) == 0) {
                zeroFound = true;
            } else {
                if (zeroFound) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the <code>bitIndex</code>-th bit of the IP-address, from the left
     *
     * @param ipAddress
     * @param bitIndex  0-based, from the left
     * @return the <code>bitIndex</code>-th bit of the IP-address, from the left
     */
    public static int getBit(int[] ipAddress, int bitIndex) {
        int partIndex = bitIndex / 8;
        int bitIndexInPart = bitIndex % 8;
        return (ipAddress[partIndex] >> (7 - bitIndexInPart)) & 1;
    }

    /**
     * Sets the <code>bitIndex</code>-th bit of the IP-address, from the left
     *
     * @param ipAddress
     * @param bitIndex  0-based, from the left
     * @param bitValue  a value in {0, 1} to be placed in the specified position of the IP-address
     */
    public static void setBit(int[] ipAddress, int bitIndex, int bitValue) {
        int partIndex = bitIndex / 8;
        int bitIndexInPart = bitIndex % 8;
        if (bitValue == 0) {
            // A number whose 'bitIndexInPart'-th bit is 0 and the rest are 1
            ipAddress[partIndex] &= ~((1 << (7 - bitIndexInPart)));
        } else if (bitValue == 1) {
            // A number whose 'bitIndexInPart'-th bit is 1 and the rest are 0
            ipAddress[partIndex] |= (1 << (7 - bitIndexInPart));
        } else {
            throw new IllegalArgumentException("A value in {0, 1} was expected, given " + bitValue);
        }
    }

    /**
     * Modifies the given IP-address to represent the subnet address with the specified bit-length.
     *
     * @param ipAddress an IP-address
     * @param bitLength the bit-length of the target subnet
     */
    public static void setToSubnetAddress(int[] ipAddress, int bitLength) {
        int totalLength = ipAddress.length * 8;
        for (int i = bitLength; i < totalLength; i++) {
            setBit(ipAddress, i, 0);
        }
    }

    /**
     * Modifies the given IP-address to represent the broadcast address of a subnet with the specified bit-length.
     *
     * @param ipAddress an IP-address belonging to a subnet whose broadcast address is required
     * @param bitLength the bit-length of the target subnet
     */
    public static void setToBroadcastAddress(int[] ipAddress, int bitLength) {
        int totalLength = ipAddress.length * 8;
        for (int i = bitLength; i < totalLength; i++) {
            setBit(ipAddress, i, 1);
        }
    }

    /**
     * Calculates the bit-length of the subnet specified by the given mask.
     * It simply returns the position of the first 0 bit in the mask.
     *
     * @param mask the mask of a subnet
     * @return the bit-length of the given mask
     */
    public static int getBitLength(int[] mask) {
        int totalLength = mask.length * 8;
        // Find the first 0 bit and return its index
        for (int i = 0; i < totalLength; i++) {
            if (getBit(mask, i) == 0) {
                return i;
            }
        }
        return totalLength;
    }

    /**
     * Calculates the bit-length of the subnet specified by the given mask.
     * If mask is a string like "255.255.255.0", calls {@link #getBitLength(int[])}.
     * If mask is a number, simply returns it.
     *
     * @param mask the mask of a subnet
     * @return the bit-length of the given mask
     */
    public static int getBitLength(String mask) {
        if (isValid(mask)) {
            int[] ints = textToNumericFormat(mask);
            return getBitLength(ints);
        }
        try {
            return Integer.parseInt(mask);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid mask: " + mask);
        }
    }
    
    /**
     * Calculates subnet addresses of a subnet,
     * given an IP-address belonging to the subnet and the subnet mask.
     *
     * @param ipAddress an IP-address belonging to the subnet
     * @param mask      the subnet mask
     * @return subnet address
     */
    public static String getSubnetAddress(String ipAddress, String mask) {
        
    	if (StringUtils.isBlank(ipAddress) || StringUtils.isBlank(mask)) {
            
    		return null;
        }
    	
        int bitLength = getBitLength(mask);
        int[] subnetInts = textToNumericFormat(ipAddress);

        setToSubnetAddress(subnetInts, bitLength);

        String subnetStr = numericFormatToText(subnetInts);

        return subnetStr;
    }
    
    /**
     * Calculates broadcast addresses of a subnet,
     * given an IP-address belonging to the subnet and the subnet mask.
     *
     * @param ipAddress an IP-address belonging to the subnet
     * @param mask      the subnet mask
     * @return broadcast address
     */
    public static String getBroadcastAddress(String ipAddress, String mask) {
        
    	if (StringUtils.isBlank(ipAddress) || StringUtils.isBlank(mask)) {
            
    		return null;
        }
        int bitLength = getBitLength(mask);
        int[] subnetInts = textToNumericFormat(ipAddress);
        int[] broadcastInts = Arrays.copyOf(subnetInts, subnetInts.length);

        setToBroadcastAddress(broadcastInts, bitLength);

        String broadcastStr = numericFormatToText(broadcastInts);

        return broadcastStr;
    }
    
    /**
     * Checks whether two given IP-addresses belong to the same subnet.
     * If any of the given parameters is null or empty, returns false.
     *
     * @param ipAddress1 an IP-address
     * @param ipAddress2 another IP-address
     * @param mask       a subnet mask
     * @return {@code true} if all parameters are not null and nonempty and the IP-addresses belong to the same subnet
     * (with respect to the given mask), {@code false} otherwise
     */
    public static boolean belongToSameSubnet(String ipAddress1, String ipAddress2, String mask) {
        if (ipAddress1 == null || ipAddress2 == null || mask == null) {
            return false;
        }
        if (ipAddress1.isEmpty() || ipAddress2.isEmpty() || mask.isEmpty()) {
            return false;
        }

        String subnet1 = getSubnetAddress(ipAddress1, mask);
        String subnet2 = getSubnetAddress(ipAddress2, mask);
        
        if(subnet1 == null) {
        	
        	return false;
        }

        return subnet1.equals(subnet2);
    }

}

package com.jimmiker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.print.attribute.standard.Compression;
import javax.swing.table.TableColumn;

public class SerializationData {
    static final String[] type = new String[] { "Byte[]", "SByte[]", "Short[]", "Integer[]", "Long[]", "UShort[]", "UInteger[]", "ULong[]", "Float[]", "Double[]", "BigInteger[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "SByte", "Short", "Integer", "Long", "UShort", "UInteger", "ULong", "Float", "Double", "BigInteger", "Character", "String", "Boolean", "Object", "null" };
    static final String[] type2 = new String[] { "Byte[]", "Byte[]", "Short[]", "Integer[]", "Long[]", "Short[]", "Integer[]", "Long[]", "Float[]", "Double[]", "BigInteger[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "Byte", "Short", "Integer", "Long", "Short", "Integer", "Long", "Float", "Double", "BigInteger", "Character", "String", "Boolean", "Object", "null" };
    static final String[] typelist = new String[] { "byte[]", "sbyte[]", "short[]", "int[]", "long[]", "ushort[]", "uint[]", "ulong[]", "float[]", "double[]", "decimal[]", "char[]", "string[]", "bool[]", "object[]", "Dictionary", "byte", "sbyte", "short", "int", "long", "ushort", "uint", "ulong", "float", "double", "decimal", "char", "string", "bool", "object" };
    static final String[] typelist2 = new String[] { "Byte[]", "SByte[]", "Short[]", "Integer[]", "Long[]", "UShort[]", "UInteger[]", "ULong[]", "Float[]", "Double[]", "BigInteger[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "SByte", "Short", "Integer", "Long", "UShort", "UInteger", "ULong", "Float", "Double", "BigInteger", "Character", "String", "Boolean", "Object" };
    public static Object PrimitiveAndClassArray(Object inputArray)
    {
    	if(!inputArray.getClass().isArray())
    	{
    		throw new IllegalArgumentException("Argument is not an array");
    	}
    	HashMap<String, Class> ChangeType = new HashMap<String, Class>()
    	{
    		{
    			put("Byte", byte.class);
    			put("Short", short.class);
    			put("Integer", int.class);
    			put("Long", long.class);
    			put("Float", float.class);
    			put("Double", double.class);
    			put("Character", char.class);
    			put("Boolean", boolean.class);
    			put("byte", Byte.class);
    			put("short", Short.class);
    			put("int", Integer.class);
    			put("long", Long.class);
    			put("float", Float.class);
    			put("double", Double.class);
    			put("char", Character.class);
    			put("boolean", Boolean.class);
    		}
    	};
    	String name = inputArray.getClass().getSimpleName().replace("[]", "");
    	int len = Array.getLength(inputArray);
    	Object output = Array.newInstance(ChangeType.get(name), len);
    	for(int i = 0; i < len; i++)
    	{
    		Array.set(output, i, Array.get(inputArray, i));
    	}
    	//System.out.println("ggg " + now.getClass().getSimpleName());
    	return output;
    }
    public static byte[] ArrayReverse(byte[] input)
    {
    	byte[] output = new byte[input.length];
		for(int i = 0; i < input.length; i++)
		{
			output[input.length - i - 1] = input[i];
		}
		return output;
	}
    public static byte[] GetBytesLength(int cont) throws IOException
    {
    	ByteArrayOutputStream data = new ByteArrayOutputStream();
    	DataOutputStream vs = new DataOutputStream(data);
        for (int i = cont / 128; i != 0; i = cont / 128)
        {
            vs.writeByte((byte)(cont % 128 + 128));
            cont = i;
        }
        vs.writeByte((byte)(cont % 128));
        vs.close();
        data.close();
        return data.toByteArray();
    }
    public static int GetIntLength(DataInputStream reader) throws IOException
    {
    	ArrayList<Integer> vs = new ArrayList<Integer>();
    	int a;
        do
        {
        	a = reader.readByte() & 0x0FF;
            vs.add(a % 128);
        } while (a >= 128);
        int x = 0;
        for (int i = 0; i < vs.size(); i++)
        {
            x += (int)(vs.get(i) * Math.pow(128, i));
        }
        return x;
    }
    public static byte[] ToBytes(Object thing) throws IOException
    {
        byte[] output = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            try (DataOutputStream writer = new DataOutputStream(stream))
            {
                output = Typing(stream, thing);
                writer.close();
                stream.close();
            }
        }
        return output;
    }
    public static Object ToObject(byte[] input) throws IOException
    {
    	Object output = null;
    	try (ByteArrayInputStream stream = new ByteArrayInputStream(input))
        {
            try (DataInputStream reader = new DataInputStream(stream))
            {
                output = GetTyp(reader);
                reader.close();
                stream.close();
            }
        }
        return output;
    }
    public static byte[] Typing(ByteArrayOutputStream stream, Object thing) throws IOException
    {
    	DataOutputStream writer = new DataOutputStream(stream);
        if (thing != null)
        {
        	thing = thing.getClass().getName().length() > 2 ? thing : PrimitiveAndClassArray(thing);
        	String typename = Arrays.asList(type).indexOf(thing.getClass().getSimpleName()) >= 0 ? thing.getClass().getSimpleName() : thing.getClass().getInterfaces()[0].getSimpleName();
            
        	switch (typename)
            {
                case "Byte[]":
                    {
                        byte[] c = (byte[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        writer.write(c);
                        break;
                    }
                case "Short[]":
                    {
                        short[] c = (short[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.write(ArrayReverse(ByteBuffer.allocate(Short.BYTES).putShort(c[ii]).array()));
                        }
                        break;
                    }
                case "Integer[]":
                    {
                        int[] c = (int[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.write(ArrayReverse(ByteBuffer.allocate(Integer.BYTES).putInt(c[ii]).array()));
                        }
                        break;
                    }
                case "Long[]":
                    {
                        long[] c = (long[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.write(ArrayReverse(ByteBuffer.allocate(Long.BYTES).putLong(c[ii]).array()));
                        }
                        break;
                    }
                case "Float[]":
                    {
                        float[] c = (float[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.write(ArrayReverse(ByteBuffer.allocate(Float.BYTES).putFloat(c[ii]).array()));
                        }
                        break;
                    }
                case "Double[]":
                    {
                        double[] c = (double[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.write(ArrayReverse(ByteBuffer.allocate(Double.BYTES).putDouble(c[ii]).array()));
                        }
                        break;
                    }
                case "BigInteger[]":
                	{
                		BigInteger[] c = (BigInteger[])thing;
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                    		byte[] nowdata = ArrayReverse(c[ii].toByteArray());
                    		writer.write(nowdata);
                        }
                		break;
                	}
                case "Character[]":
                    {
                        char[] c = (char[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.writeChar(c[ii]);
                        }
                        break;
                    }
                case "String[]":
                    {
                        String[] c = (String[])thing;
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
		               	    byte[] data = c[ii].getBytes(Charset.forName("UTF-8"));
		               	    writer.write(GetBytesLength(data.length));
		               	    writer.write(data);
                        }
                        break;
                    }
                case "Boolean[]":
                    {
                        boolean[] c = (boolean[])PrimitiveAndClassArray(thing);
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                            writer.writeBoolean(c[ii]);
                        }
                        break;
                    }
                case "Object[]":
                    {
                        Object[] c = (Object[])thing;
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(GetBytesLength(c.length));
                        for (int ii = 0; ii < c.length; ii++)
                        {
                        	writer.close();
                            Typing(stream, c[ii]);
                            writer = new DataOutputStream(stream);
                        }
                        break;
                    }
                case "Map":
                    {
                        Map c = (Map)thing;
                        Class datatype = thing.getClass();
                        Object[][] data = new Object[][] {c.keySet().toArray(), c.values().toArray()} ;
                        Class[] Subdatatype = new Class[2];
                        for(int i = 0; i < data.length; i++)
                        {
	                        for(int j = 0; j < data[i].length; j++)
	                        {
	                        	Class nowclass = Arrays.asList(type).indexOf(data[i][j].getClass().getSimpleName()) >= 0 ? data[i][j].getClass() : data[i][j].getClass().getInterfaces()[0];
	                        	if(Subdatatype[i] == null || Subdatatype[i] == nowclass)
	                        	{
	                        		Subdatatype[i] = nowclass;
	                        	}
	                        	else 
	                        	{
									Subdatatype[i] = Object.class;
								}
	                        }
	                        if(Subdatatype[i] == null)
	                        {
								Subdatatype[i] = Object.class;
	                        }
                        }
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(Subdatatype[0].getSimpleName())));
                        writer.writeByte((byte)(Arrays.asList(type).indexOf(Subdatatype[1].getSimpleName())));
                        writer.write(GetBytesLength(c.size()));

                        for (int i = 0; i < c.size(); i++)
                        {
                        	writer.close();
                            Typing(stream, data[0][i]);
                            Typing(stream, data[1][i]);
                            writer = new DataOutputStream(stream);
                        }
                        break;
                    }
                case "Byte":
                    {
                        byte c = (byte)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.writeByte(c);
                        break;
                    }
                case "Short":
                    {
                        short c = (short)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(ArrayReverse(ByteBuffer.allocate(Short.BYTES).putShort(c).array()));
                        break;
                    }
                case "Integer":
                    {
                        int c = (int)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(ArrayReverse(ByteBuffer.allocate(Integer.BYTES).putInt(c).array()));
                        break;
                    }
                case "Long":
                    {
                        long c = (long)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(ArrayReverse(ByteBuffer.allocate(Long.BYTES).putLong(c).array()));
                        break;
                    }
                case "Float":
                    {
                        float c = (float)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(ArrayReverse(ByteBuffer.allocate(Float.BYTES).putFloat(c).array()));
                        break;
                    }
                case "Double":
                    {
                        double c = (double)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.write(ArrayReverse(ByteBuffer.allocate(Double.BYTES).putDouble(c).array()));
                        break;
                    }
                case "BigInteger":
	            	{
	            		BigInteger c = (BigInteger)thing;
	                    writer.writeByte((byte)(Arrays.asList(type).indexOf(typename)));
                		byte[] nowdata = ArrayReverse(c.toByteArray());
                		writer.write(nowdata);
	            		break;
	            	}
                case "Character":
                    {
                        char c = (char)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.writeChar(c);
                        break;
                    }
                case "String":
                    {
                    	String c = (String)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
	               	    byte[] data = c.getBytes(Charset.forName("UTF-8"));
	               	    writer.write(GetBytesLength(data.length));
	               	    writer.write(data);
                        break;
                    }
                case "Boolean":
                    {
                        boolean c = (boolean)thing;
                        writer.write((byte)(Arrays.asList(type).indexOf(typename)));
                        writer.writeBoolean(c);
                        break;
                    }
                default:
                    {
                        writer.write((byte)type.length);
	               	    byte[] data = thing.toString().getBytes(Charset.forName("UTF-8"));
	               	    writer.write(GetBytesLength(data.length));
	               	    writer.write(data);
                        break;
                    }
            }
        }
        else
        {
            writer.write((byte)(Arrays.asList(type).indexOf("null")));
            writer.writeBoolean(false);
        }
        
        writer.close();
        return stream.toByteArray();
    }
    public static Object GetTyp(DataInputStream reader) throws IOException
    {
        byte data = reader.readByte();
        Object get;
        if (data < type2.length)
        {
            String typ = type2[data];
            switch (typ)
            {
                case "Byte[]":
                    {
                        byte[] d = new byte[GetIntLength(reader)];
                        reader.read(d, 0, d.length);
                        get = d;
                        break;
                    }
                case "Short[]":
                    {
                        short[] d = new short[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] readdata = new byte[Short.BYTES];
                        	reader.read(readdata, 0, readdata.length);
                        	readdata = ArrayReverse(readdata);
                            d[ii] = ByteBuffer.wrap(readdata).getShort();
                        }
                        get = d;
                        break;
                    }
                case "Integer[]":
                    {
                        int[] d = new int[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] readdata = new byte[Integer.BYTES];
                        	reader.read(readdata, 0, readdata.length);
                        	readdata = ArrayReverse(readdata);
                            d[ii] = ByteBuffer.wrap(readdata).getInt();
                        }
                        get = d;
                        break;
                    }
                case "Long[]":
                    {
                        long[] d = new long[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] readdata = new byte[Long.BYTES];
                        	reader.read(readdata, 0, readdata.length);
                        	readdata = ArrayReverse(readdata);
                            d[ii] = ByteBuffer.wrap(readdata).getLong();
                        }
                        get = d;
                        break;
                    }
                case "Float[]":
                    {
                        float[] d = new float[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] readdata = new byte[Float.BYTES];
                        	reader.read(readdata, 0, readdata.length);
                        	readdata = ArrayReverse(readdata);
                            d[ii] = ByteBuffer.wrap(readdata).getFloat();
                        }
                        get = d;
                        break;
                    }
                case "Double[]":
                    {
                        double[] d = new double[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] readdata = new byte[Double.BYTES];
                        	reader.read(readdata, 0, readdata.length);
                        	readdata = ArrayReverse(readdata);
                            d[ii] = ByteBuffer.wrap(readdata).getDouble();
                        }
                        get = d;
                        break;
                    }
                case "BigInteger[]":
                    {
                    	BigInteger[] d = new BigInteger[GetIntLength(reader)];
                        //byte[][] d = new byte[GetIntLength(reader)][16];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	byte[] nowdata = new byte[16];
                        	reader.read(nowdata, 0, nowdata.length);
                            d[ii] = new BigInteger(ArrayReverse(nowdata));
                        }
                        get = d;
                        break;
                    }
                case "Character[]":
                    {
                        char[] d = new char[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                            d[ii] = reader.readChar();
                        }
                        get = d;
                        break;
                    }
                case "String[]":
                    {
                        String[] d = new String[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                        	int size = GetIntLength(reader);
                        	byte[] nowdata = new byte[size];
                        	reader.read(nowdata, 0, size);
                        	d[ii] = new String(nowdata, Charset.forName("UTF-8"));
                        }
                        get = d;
                        break;
                    }
                case "Boolean[]":
                    {
                        boolean[] d = new boolean[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                            d[ii] = reader.readBoolean();
                        }
                        get = d;
                        break;
                    }
                case "Object[]":
                    {
                        Object[] d = new Object[GetIntLength(reader)];
                        for (int ii = 0; ii < d.length; ii++)
                        {
                            d[ii] = GetTyp(reader);
                        }
                        get = d;
                        break;
                    }
                case "Map":
                    {
                        String[] typenames = new String[] { type[reader.readByte()], type[reader.readByte()] };
                        Map d = new HashMap();
                        int count = GetIntLength(reader);
                        for (int ii = 0; ii < count; ii++)
                        {
                            Object key = GetTyp(reader);
                            Object value = GetTyp(reader);
                            d.put(key, value);
                        }
                        get = d;
                        break;
                    }
                case "Byte":
                    {
                        get = reader.readByte();
                        break;
                    }
                case "Short":
                    {
                    	byte[] readdata = new byte[Short.BYTES];
                    	reader.read(readdata, 0, readdata.length);
                    	readdata = ArrayReverse(readdata);
                    	get = ByteBuffer.wrap(readdata).getShort();
                        break;
                    }
                case "Integer":
                    {
                    	byte[] readdata = new byte[Integer.BYTES];
                    	reader.read(readdata, 0, readdata.length);
                    	readdata = ArrayReverse(readdata);
                    	get = ByteBuffer.wrap(readdata).getInt();
                        break;
                    }
                case "Long":
                    {
                    	byte[] readdata = new byte[Long.BYTES];
                    	reader.read(readdata, 0, readdata.length);
                    	readdata = ArrayReverse(readdata);
                    	get = ByteBuffer.wrap(readdata).getLong();
                        break;
                    }
                case "Float":
                    {
                    	byte[] readdata = new byte[Float.BYTES];
                    	reader.read(readdata, 0, readdata.length);
                    	readdata = ArrayReverse(readdata);
                    	get = ByteBuffer.wrap(readdata).getFloat();
                        break;
                    }
                case "Double":
                    {
                    	byte[] readdata = new byte[Double.BYTES];
                    	reader.read(readdata, 0, readdata.length);
                    	readdata = ArrayReverse(readdata);
                    	get = ByteBuffer.wrap(readdata).getDouble();
                        break;
                    }
                case "BigInteger":
                    {
                    	byte[] nowdata = new byte[16];
                    	reader.read(nowdata, 0, nowdata.length);
                    	get = new BigInteger(ArrayReverse(nowdata));
                        break;
                    }
                case "Character":
                    {
                        get = reader.readChar();
                        break;
                    }
                case "String":
                    {
                    	int size = GetIntLength(reader);
                    	byte[] nowdata = new byte[size];
                    	reader.read(nowdata, 0, size);
                    	get = new String(nowdata, Charset.forName("UTF-8"));
                        break;
                    }
                case "Boolean":
                    {
                        get = reader.readBoolean();
                        break;
                    }
                case "null":
                    {
                        boolean a = reader.readBoolean();
                        get = null;
                        break;
                    }
                default:
                    {
                        get = typ;
                        break;
                    }
            }
        }
        else
        {
        	int size = GetIntLength(reader);
        	byte[] nowdata = new byte[size];
        	reader.read(nowdata, 0, size);
        	get = new String(nowdata, Charset.forName("UTF-8"));
        }
        return get;
    }
    static int Matches(String input, char a)
    {
    	String[] j = Split(input, a);
        return j.length + 1;
    }
    public static String[] Split(String input, char a)
    {
        ArrayList<String> vs = new ArrayList<String>();
        int now = 0;
        for (int i = 0; i < input.length(); i++)
        {
            if (input.charAt(i) == '\\')
            {
                i++;
            }
            else if (input.charAt(i) == a)
            {
                vs.add(input.substring(now, i));
                now = i + 1;
            }
        }
        vs.add(input.substring(now, input.length()));
        String[] outdata = new String[vs.size()];
        return vs.toArray(outdata);
    }
    public static String FormattingString(String input)
    {
        StringBuilder stringBuilder = new StringBuilder(input);
        for (int i = 0; i < stringBuilder.length(); i++)
        {
            if (stringBuilder.charAt(i) == '\\')
            {
                stringBuilder.delete(i, 1);
            }
        }
        return stringBuilder.toString();
    }
    public static String BeforeFormatString(String input, char[] a)
    {
        StringBuilder stringBuilder = new StringBuilder(input);
        for (int i = 0; i < stringBuilder.length(); i++)
        {
            if (Arrays.asList(a).indexOf(stringBuilder.charAt(i)) >= 0 || stringBuilder.charAt(i) == '\\')
            {
                stringBuilder.insert(i, "\\");
                i++;
            }
        }
        return stringBuilder.toString();
    }
    public static String[] TakeString(String text, Character a, Character b)
    {
        ArrayList<String> q = new ArrayList<String>(Arrays.asList(Split(text, b)));
        if (a == b)
        {
            if (q.size() % 2 == 0)
            {
                q.remove(q.size() - 1);
            }
            for (int i = 0; i < q.size(); i++)
            {
                q.remove(i);
            }
            for (int i = 0; i < q.size(); i++)
            {
                q.set(i, FormattingString(q.get(i)));
            }
            String[] outdata = new String[q.size()];
            return q.toArray(outdata);
        }
        q.remove(q.size() - 1);
        for (int i = 0; i < q.size();)
        {
            if (q.get(i) != "")
            {
                if (Matches(q.get(i), a) != Matches(q.get(i), b) + 1)
                {
                	q.set(i, q.get(i) + b.toString() + q.get(i + 1));
                    q.remove(i + 1);
                }
                else
                {
                    i++;
                }
            }
            else
            {
            	q.set(i - 1, q.get(i - 1) + b.toString());
                q.remove(i);
            }
        }
        ArrayList<String> vs = new ArrayList<String>();
        for (int ii = 0; ii < q.size(); ii++)
        {
        	String s = q.get(ii);
            int found = 0;
            for (int i = 0; i < s.length(); i++)
            {
                if (s.charAt(i) == '\\')
                {
                    i++;
                }
                else if (s.charAt(i) == a)
                {
                    found = i;
                    break;
                }
            }
            if (found != -1)
            {
                if (found + 1 == s.length())
                {
                    vs.add("");
                }
                else
                {
                    vs.add(s.substring(found + 1));
                }
            }
        }
        String[] outdata = new String[vs.size()];
        return vs.toArray(outdata);
    }
    static String RemoveString(String input, String[] arg)
    {
        for(int i = 0; i < arg.length; i++)
        {
            input = input.replaceAll(arg[i], "");
        }
        return input;
    }
    static String printTab(Boolean enable, int cont)
    {
        if(!enable)
        {
            return "";
        }
        String ans = "\r\n";
        for(int i = 0; i < cont; i++)
        {
            ans += "\t";
        }
        return ans;
    }
    static String ObjectToString(int cont, Object thing, boolean enter)
    {
        String a = "";
        if (thing != null)
        {
        	thing = thing.getClass().getName().length() > 2 ? thing : PrimitiveAndClassArray(thing);
        	String typename = Arrays.asList(type).indexOf(thing.getClass().getSimpleName()) >= 0 ? thing.getClass().getSimpleName() : thing.getClass().getInterfaces()[0].getSimpleName();

            String type = typelist[Arrays.asList(typelist2).indexOf(typename)];
            a += type + ":";
            switch (type)
            {
                case "byte[]":
                    {
                        Byte[] c = (Byte[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1) + ReadString((byte[])PrimitiveAndClassArray(c)) + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "short[]":
                    {
                        Short[] c = (Short[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "int[]":
                    {
                        Integer[] c = (Integer[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "long[]":
                    {
                        Long[] c = (Long[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "float[]":
                    {
                        Float[] c = (Float[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "double[]":
                    {
                        Double[] c = (Double[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "char[]":
                    {
                        Character[] c = (Character[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += "\'" + c[i].toString() + "\',";
                            }
                            a += "\'" + BeforeFormatString(c[c.length - 1].toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + "\'" + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "string[]":
                    {
                        String[] c = (String[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += "\"" + c[i].toString() + "\",";
                            }
                            a += "\"" + BeforeFormatString(c[c.length - 1].toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + "\"" + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "bool[]":
                    {
                        Boolean[] c = (Boolean[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += c[i].toString() + ",";
                            }
                            a += c[c.length - 1].toString() + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "object[]":
                    {
                        Object[] c = (Object[])thing;
                        if (c.length > 0)
                        {
                            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                            for (int i = 0; i < c.length - 1; i++)
                            {
                                a += ObjectToString(cont + 1, c[i], enter) + "," + printTab(enter, cont + 1);
                            }
                            a += ObjectToString(cont + 1, c[c.length - 1], enter) + printTab(enter, cont) + "}";
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        break;
                    }
                case "Dictionary":
                    {
                        Map c = (Map)thing;
                        Class datatype = thing.getClass();
                        Object[][] data = new Object[][] {c.keySet().toArray(), c.values().toArray()} ;
                        Class[] Subdatatype = new Class[2];
                        for(int i = 0; i < data.length; i++)
                        {
	                        for(int j = 0; j < data[i].length; j++)
	                        {
	                        	Class nowclass = Arrays.asList(typelist2).indexOf(data[i][j].getClass().getSimpleName()) >= 0 ? data[i][j].getClass() : data[i][j].getClass().getInterfaces()[0];
	                        	if(Subdatatype[i] == null || Subdatatype[i] == nowclass)
	                        	{
	                        		Subdatatype[i] = nowclass;
	                        	}
	                        	else 
	                        	{
									Subdatatype[i] = Object.class;
								}
	                        }
	                        if(Subdatatype[i] == null)
	                        {
								Subdatatype[i] = Object.class;
	                        }
                        }
                    	
                        a += printTab(enter, cont) + "{" + printTab(enter, cont + 1) + typelist[Arrays.asList(typelist2).indexOf(Subdatatype[0].getSimpleName())] + ":" + typelist[Arrays.asList(typelist2).indexOf(Subdatatype[1].getSimpleName())] + ":";

                        if (c.size() > 0)
                        {
                            for (int i = 0; i < c.size(); i++)
                            {
                                a += printTab(enter, cont + 1) + "{" + printTab(enter, cont + 2) + ObjectToString(cont + 2, data[0][i], enter) + "," + printTab(enter, cont + 2) + ObjectToString(cont + 2, data[1][i], enter) + printTab(enter, cont + 1) + "}";
                            }
                        }
                        else
                        {
                            a += "NotThing";
                        }
                        a += printTab(enter, cont) + "}";
                        break;
                    }
                case "byte":
                    {
                        Byte c = (Byte)thing;
                        a += c.toString();
                        break;
                    }
                case "short":
                    {
                        Short c = (Short)thing;
                        a += c.toString();
                        break;
                    }
                case "int":
                    {
                        Integer c = (Integer)thing;
                        a += c.toString();
                        break;
                    }
                case "long":
                    {
                        Long c = (Long)thing;
                        a += c.toString();
                        break;
                    }
                case "float":
                    {
                        Float c = (Float)thing;
                        a += c.toString();
                        break;
                    }
                case "double":
                    {
                        Double c = (Double)thing;
                        a += c.toString();
                        break;
                    }
                case "char":
                    {
                        Character c = (Character)thing;
                        a += "\'" + BeforeFormatString(c.toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + "\'";
                        break;
                    }
                case "string":
                    {
                        String c = (String)thing;
                        a += "\"" + BeforeFormatString(c.toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + "\"";
                        break;
                    }
                case "bool":
                    {
                        Boolean c = (Boolean)thing;
                        a += c.toString();
                        break;
                    }
                default:
                    {
                        a += thing.toString();
                        break;
                    }
            }
        }
        else
        {
            a += "null";
        }
        return a;
    }
    public static String ObjectToString(Object thing)
    {
        return ObjectToString(0, thing, false);
    }
    public static String ObjectToStringWithEnter(Object thing)
    {
        return ObjectToString(0, thing, true);
    }
    public static Object StringToObject(String thing)
    {
        String[] vs = Split(thing, ':');
        String typ = RemoveString(vs[0], new String[] { " ", "\n", "\r", "\t" });
        Object get;
        switch (typ)
        {
            case "byte[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0].replace(" ", "");
                        /*string[] b = Split(a, ',');
                        List<byte> c = new List<byte>();
                        for (int i = 0; i < b.Length; i++)
                        {
                            c.Add((byte)Convert.ToInt32(b[i]));
                        }*/
                        get = PrimitiveAndClassArray(WriteString(a));//c.ToArray();
                    }
                    else
                    {
                        get = new Byte[0];
                    }
                    break;
                }
            case "sbyte[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Byte> c = new ArrayList<Byte>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add((byte)sc.nextInt());
                            sc.close();
                        }
                        Byte[] outdata = new Byte[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Byte[0];
                    }
                    break;
                }
            case "short[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Short> c = new ArrayList<Short>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextShort());
                            sc.close();
                        }
                        Short[] outdata = new Short[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Short[0];
                    }
                    break;
                }
            case "int[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                    	String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                    	String[] b = Split(a, ',');
                        ArrayList<Integer> c = new ArrayList<Integer>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextInt());
                            sc.close();
                        }
                        Integer[] outdata = new Integer[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Integer[0];
                    }
                    break;
                }
            case "long[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Long> c = new ArrayList<Long>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextLong());
                            sc.close();
                        }
                        Long[] outdata = new Long[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Long[0];
                    }
                    break;
                }
            case "ushort[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Short> c = new ArrayList<Short>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add((short)sc.nextInt());
                            sc.close();
                        }
                        Short[] outdata = new Short[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Short[0];
                    }
                    break;
                }
            case "uint[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Integer> c = new ArrayList<Integer>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add((int)sc.nextLong());
                            sc.close();
                        }
                        Integer[] outdata = new Integer[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Integer[0];
                    }
                    break;
                }
            case "ulong[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Long> c = new ArrayList<Long>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextBigInteger().longValue());
                            sc.close();
                        }
                        Long[] outdata = new Long[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Long[0];
                    }
                    break;
                }
            case "float[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Float> c = new ArrayList<Float>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextFloat());
                            sc.close();
                        }
                        Float[] outdata = new Float[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Float[0];
                    }
                    break;
                }
            case "double[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Double> c = new ArrayList<Double>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	Scanner sc = new Scanner(b[i]);
                            c.add(sc.nextDouble());
                            sc.close();
                        }
                        Double[] outdata = new Double[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Double[0];
                    }
                    break;
                }
            case "decimal[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<BigInteger> c = new ArrayList<BigInteger>();
                        for (int i = 0; i < b.length; i++)
                        {
                        	boolean nig = b[i].contains("-");
                        	int cont = b[i].lastIndexOf('.') == -1 ? 0 : b[i].length() - 1 - b[i].lastIndexOf('.');
                        	String stringdata = b[i].replace("-", "");
                        	stringdata = stringdata.replace(".", "");
                        	Scanner sc = new Scanner(stringdata);
                        	BigInteger nowdata = sc.nextBigInteger();
                            sc.close();
                        	byte[] databyte = nowdata.toByteArray();
                        	byte[] databyte2 = new byte[16];
                        	databyte2[0] = (byte) (nig ? -128 : 0);
                        	databyte2[1] = (byte) cont;
                        	System.arraycopy(databyte, 0, databyte2, databyte2.length - databyte.length, databyte.length);
                        	BigInteger end = new BigInteger(databyte2);
                            c.add(end);
                        }
                        BigInteger[] outdata = new BigInteger[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new BigInteger[0];
                    }
                    break;
                }
            case "char[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Character> c = new ArrayList<Character>();
                        for (int i = 0; i < b.length; i++)
                        {
                            String ans = TakeString(b[i], '\'', '\'')[0];
                            c.add(ans.charAt(0));
                        }
                        Character[] outdata = new Character[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Character[0];
                    }
                    break;
                }
            case "string[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<String> c = new ArrayList<String>();
                        for (int i = 0; i < b.length; i++)
                        {
                            String ans = TakeString(b[i], '\"', '\"')[0];
                            c.add(ans);
                        }
                        String[] outdata = new String[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new String[0];
                    }
                    break;
                }
            case "bool[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = Split(a, ',');
                        ArrayList<Boolean> c = new ArrayList<Boolean>();
                        for (int i = 0; i < b.length; i++)
                        {
                            c.add(Boolean.parseBoolean(b[i].replace(" ", "")));
                        }
                        Boolean[] outdata = new Boolean[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Boolean[0];
                    }
                    break;
                }
            case "object[]":
                {
                    int found = thing.indexOf(':');
                    if (thing.substring(found + 1) != "NotThing")
                    {
                        String a = TakeString(thing.substring(found + 1), '{', '}')[0];
                        String[] b = TakeString(a, '{', '}');
                        for (int i = 0; i < b.length; i++)
                        {
                            int index = a.indexOf("{" + b[i] + "}");
                            a = a.substring(0, index) + "[" + i + "]" + a.substring(index + b[i].length() + 2);
                        }
                        String[] bb = Split(a, ',');
                        for (int i = 0; i < bb.length; i++)
                        {
                            for (int ii = 0; ii < b.length; ii++)
                            {
                                bb[i] = bb[i].replace("[" + ii + "]", "{" + b[ii] + "}");
                            }
                        }
                        ArrayList<Object> c = new ArrayList<Object>();
                        for (int i = 0; i < bb.length; i++)
                        {
                            c.add(StringToObject(bb[i]));
                        }
                        Object[] outdata = new Object[c.size()];
                        get = c.toArray(outdata);
                    }
                    else
                    {
                        get = new Object[0];
                    }
                    break;
                }
            case "Dictionary":
                {
                    int found = thing.indexOf(':');
                    String _data = TakeString(thing.substring(found + 1), '{', '}')[0];

                    int data_index = _data.indexOf(':');
                    int data_index2 = _data.indexOf(':', data_index + 1);

                    String[] data = new String[] { _data.substring(0, data_index), _data.substring(data_index + 1, data_index2), _data.substring(data_index2 + 1) };
                    data[0] = RemoveString(data[0], new String[] { " ", "\n", "\r", "\t" });
                    data[1] = RemoveString(data[1], new String[] { " ", "\n", "\r", "\t" });
                    String[] typenames = new String[] { typelist2[Arrays.asList(typelist).indexOf(data[0])], typelist2[Arrays.asList(typelist).indexOf(data[1])] };
                    
                    get = new HashMap();

                    if (data[2] != "NotThing")
                    {
                        String[] a = TakeString(data[2], '{', '}');

                        for (int i = 0; i < a.length; i++)
                        {
                            String[] b = TakeString(a[i], '{', '}');
                            for (int ii = 0; ii < b.length; ii++)
                            {
                                int index = a[i].indexOf("{" + b[ii] + "}");
                                a[i] = a[i].substring(0, index) + "[" + ii + "]" + a[i].substring(index + b[ii].length() + 2);
                            }
                            String[] nowdata = Split(a[i], ',');
                            for (int ii = 0; ii < nowdata.length; ii++)
                            {
                                for (int iii = 0; iii < b.length; iii++)
                                {
                                    nowdata[ii] = nowdata[ii].replace("[" + iii + "]", "{" + b[iii] + "}");
                                }
                            }
                            Object key = StringToObject(nowdata[0]);
                            Object value = StringToObject(nowdata[1]);
                            ((Map)get).put(key, value);
                        }
                    }
                    break;
                }
            case "byte":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = (byte)sc.nextInt();
                    sc.close();
                    break;
                }
            case "sbyte":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextByte();
                    sc.close();
                    break;
                }
            case "short":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextShort();
                    sc.close();
                    break;
                }
            case "int":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextInt();
                    sc.close();
                    break;
                }
            case "long":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextLong();
                    sc.close();
                    break;
                }
            case "ushort":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = (short)sc.nextInt();
                    sc.close();
                    break;
                }
            case "uint":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = (int)sc.nextLong();
                    sc.close();
                    break;
                }
            case "ulong":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextBigInteger().longValue();
                    sc.close();
                    break;
                }
            case "float":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextFloat();
                    sc.close();
                    break;
                }
            case "double":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);
                	Scanner sc = new Scanner(a);
                	get = sc.nextDouble();
                    sc.close();
                    break;
                }
            case "decimal":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1);

                	boolean nig = a.contains("-");
                	int cont = a.lastIndexOf('.') == -1 ? 0 : a.length() - 1 - a.lastIndexOf('.');
                	String stringdata = a.replace("-", "");
                	stringdata = stringdata.replace(".", "");
                	Scanner sc = new Scanner(stringdata);
                	BigInteger nowdata = sc.nextBigInteger();
                    sc.close();
                	byte[] databyte = nowdata.toByteArray();
                	byte[] databyte2 = new byte[16];
                	databyte2[0] = (byte) (nig ? -128 : 0);
                	databyte2[1] = (byte) cont;
                	System.arraycopy(databyte, 0, databyte2, databyte2.length - databyte.length, databyte.length);
                	BigInteger end = new BigInteger(databyte2);
                	
                    get = end;
                    break;
                }
            case "char":
                {
                    int found = thing.indexOf(':');
                    String a = TakeString(thing.substring(found + 1), '\'', '\'')[0];
                    get = a.charAt(0);
                    break;
                }
            case "string":
                {
                    int found = thing.indexOf(':');
                    String a = TakeString(thing.substring(found + 1), '\"', '\"')[0];
                    get = a;
                    break;
                }
            case "bool":
                {
                    int found = thing.indexOf(':');
                    String a = thing.substring(found + 1).replace(" ", "");
                    get = Boolean.parseBoolean(a);
                    break;
                }
            case "null":
                {
                    get = null;
                    break;
                }
            default:
                {
                    get = typ;
                    break;
                }
        }
        return get;
    }
    public static String BytesToString(byte[] input) throws IOException
    {
        Object datas = ToObject(input);
        return ObjectToString(datas);
    }
    public static String BytesToStringWithEnter(byte[] input) throws IOException
    {
        Object datas = ToObject(input);
        return ObjectToStringWithEnter(datas);
    }
    public static byte[] StringToBytes(String input) throws IOException
    {
        Object data = StringToObject(input);
        return ToBytes(data);
    }
    public static byte[] AESEncrypt(byte[] inputByteArray, byte[] _keyData, String strKey) throws Exception
    {
    	byte[] raw = strKey.getBytes();
    	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    	Cipher cipher = Cipher.getInstance("AES");//"算法/模式/补码方式"
    	IvParameterSpec iv = new IvParameterSpec(_keyData);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
    	cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
    	byte[] encrypted = cipher.doFinal(inputByteArray);    	
        return encrypted;
    }
    public static byte[] AESDecrypt(byte[] cipherText, byte[] _keyData, String strKey) throws Exception
    {
    	byte[] raw = strKey.getBytes();
    	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    	Cipher cipher = Cipher.getInstance("AES");
    	IvParameterSpec iv = new IvParameterSpec(_keyData);
    	cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
    	byte[] original = cipher.doFinal(cipherText);
        return original;
    }
    static public byte[] Lock(byte[] bs, String key, Boolean _Lock) throws Exception
    {
        byte[] encryptBytes;
        if (key != "")
        {
            if (_Lock)
            {
                byte[] _key1 = new byte[16];
                for (int i = 0; i < 16; i++)
                {
                    _key1[i] = (byte)(int)(Math.random() * 256);
                }
                encryptBytes = AESEncrypt(bs, _key1, key);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream writer = new DataOutputStream(stream);

                writer.writeBoolean(_Lock);
                writer.write(_key1, 0, 16);
                writer.write(encryptBytes, 0, encryptBytes.length);
                writer.close();
                stream.close();

                encryptBytes = stream.toByteArray();
            }
            else
            {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream writer = new DataOutputStream(stream);

                writer.writeBoolean(_Lock);
                writer.write(bs, 0, bs.length);

                writer.close();
                stream.close();

                encryptBytes = stream.toByteArray();
            }
        }
        else
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream writer = new DataOutputStream(stream);

            writer.writeBoolean(false);
            writer.write(bs, 0, bs.length);

            writer.close();
            stream.close();

            encryptBytes = stream.toByteArray();
        }
        Object[] b = new Object[1];
        Compress(b, encryptBytes);
        return (byte[])b[0];
    }
    static public byte[] UnLock(byte[] bs, String key) throws Exception
    {
        byte[] _out;
        ByteArrayInputStream stream = new ByteArrayInputStream(bs);
        DataInputStream reader = new DataInputStream(stream);
        boolean _Lock = reader.readBoolean();
        if (_Lock)
        {
            byte[] _key1 = new byte[16];
            reader.read(_key1, 0, 16);
            byte[] data = new byte[bs.length - 17]; 
            reader.read(data, 0, bs.length - 17);
            _out = AESDecrypt(data, _key1, key);
        }
        else
        {
            _out = new byte[bs.length - 1]; 
            reader.read(_out, 0, bs.length - 1);
        }
        reader.close();
        stream.close();
        return _out;
    }
    static public void Compress(Object[] _bytes, byte[] bytes) throws IOException
    {
        ByteArrayOutputStream stream;
        DataOutputStream writer;
        stream = new ByteArrayOutputStream();

        int byteLength = bytes.length;
        try (GZIPOutputStream compressionStream = new GZIPOutputStream(stream))
        {
            compressionStream.write(bytes, 0, bytes.length);
            compressionStream.close();
        }
        stream.close();

        byte[] bytes2 = stream.toByteArray();
        stream = null;

        stream = new ByteArrayOutputStream();
        writer = new DataOutputStream(stream);

        writer.writeBoolean(byteLength > bytes2.length);

        writer.writeInt(byteLength);
        if (byteLength > bytes2.length)
        {
            writer.writeInt(bytes2.length);
            writer.write(bytes2);
        }
        else
        {
            writer.write(bytes);
        }
        writer.close();
        stream.close();
        _bytes[0] = stream.toByteArray();
    }
    static public void Decompress(byte[] _bytes, int index, Object[] str, int[] length) throws IOException
    {
        ByteArrayInputStream stream = new ByteArrayInputStream(_bytes);
        DataInputStream reader = new DataInputStream(stream);
        reader.skipBytes(index);
        boolean compress = reader.readBoolean();
        int q = reader.readInt();
        byte[] bs = new byte[compress ? reader.readInt() : q]; 
        reader.read(bs, 0, bs.length);

        reader.close();
        stream.close();
        stream = null;

        if (compress)
        {
            stream = new ByteArrayInputStream(bs);

            str[0] = new byte[q];

            try (GZIPInputStream decompressionStream = new GZIPInputStream(stream))
            {
                decompressionStream.read((byte[])str[0], 0, q);
            }
            length[0] = bs.length + 9;
        }
        else
        {
            str[0] = bs;
            length[0] = bs.length + 5;
        }
    }
    static private byte HexToByte(String hex)
    {
        if (hex.length() > 2 || hex.length() <= 0)
            throw new IllegalArgumentException("hex must be 1 or 2 characters in length");
        byte newByte = (byte) Integer.parseInt(hex,16);
        return newByte;
    }
    static public byte[] WriteString(String str)
    {
        str = RemoveString(str, new String[] { " ", "\n", "\r", "\t" });
        byte[] bytes = new byte[str.length() / 2];
        int j = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = new String(new char[] { str.charAt(j), str.charAt(j + 1) });
            bytes[i] = HexToByte(hex);
            j = j + 2;
        }
        return bytes;
    }
    static public String ReadString(byte[] bytes)
    {
        StringBuilder str2 = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            str2.append(String.format("%02X", bytes[i]));
        }
        return str2.toString();
    }

}

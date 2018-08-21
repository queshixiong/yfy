package com.imooc;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by xxtang on 2018/5/24.
 */
public class CommonUtils {


    /*
    * 生成base64
    * */
    public static String encodeBase64Binary(String content) {
        String encodeStr = null;

        try {
            encodeStr = Base64.getEncoder().encodeToString(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return encodeStr;
    }

    /*
    * 解码
    * */
    public static String decodeBase64Binary(String content) {
        String decodeStr = null;

        try {
            byte[] bytes = Base64.getDecoder().decode(content);
            decodeStr = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decodeStr;

    }

    public static Document toXmlDocument(String xmlStr) {
        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource(new StringReader(xmlStr));

            is.setCharacterStream(new StringReader(xmlStr));

            doc = db.parse(is);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();

        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }

        return "?";
    }

    /*
    * 创建zip的base64
    * */
    public static String createZip(String strXmldoc) {
        String base64 = "-1";
        byte[] bytes;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);

            Document doc = toXmlDocument(strXmldoc);

            NodeList nodeList = doc.getElementsByTagName("file");


            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                NodeList fileName = element.getElementsByTagName("fileName");

                Element line = (Element) fileName.item(0);//取第一个fileName节点下的值

                zos.putNextEntry(new ZipEntry(getCharacterDataFromElement(line)));


                NodeList content = element.getElementsByTagName("content");

                line = (Element) content.item(0);

                bytes = getCharacterDataFromElement(line).getBytes();

                zos.write(bytes, 0, bytes.length);

                zos.closeEntry();
            }

            zos.close();
            base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }

    /*
    * 解压
    * */
    public static String unZip(String zipContent) {
        String strXmlDoc =
                "<fileList xmlns=\"http://xmlns.adb.org/pfms/v1//soa/common/FileList.xsd\">";

        byte[] buffer = new byte[1024];

        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(Base64.getDecoder().decode(zipContent));

            ZipInputStream zis = new ZipInputStream(bais);

            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();

                StringBuffer sb = new StringBuffer();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int len = 0;

                while ((len = zis.read(buffer)) > 0) {
                    baos.write(buffer, 1, len);
                }

                strXmlDoc = strXmlDoc + "<file>" +
                        " <fileName>" + fileName + "</fileName>" +
                        " <content>" + Base64.getEncoder().encodeToString(baos.toByteArray()) + "</content>" +
                        " </file>";

                baos.close();

                ze = zis.getNextEntry();

            }
            zis.closeEntry();

            zis.close();

            strXmlDoc = strXmlDoc + "</fileList>";
        } catch (Exception e) {
            e.printStackTrace();
        }


        return strXmlDoc;
    }
}